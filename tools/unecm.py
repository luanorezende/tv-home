"""Descompacta .ecm (formato de Neill Corlett) para a imagem de CD original.
Lê o fluxo inteiro, monta a saída e recalcula EDC/ECC em lote com numpy."""
import sys
import numpy as np

src_path, dst_path = sys.argv[1], sys.argv[2]
data = open(src_path, 'rb').read()
assert data[:4] == b'ECM\x00', 'não é ECM'

# ---- tabelas ----
edc_lut = np.zeros(256, dtype=np.uint32)
for i in range(256):
    e = i
    for _ in range(8):
        e = (e >> 1) ^ (0xD8018001 if e & 1 else 0)
    edc_lut[i] = e
ecc_f = np.zeros(256, dtype=np.uint8)
ecc_b = np.zeros(256, dtype=np.uint8)
for i in range(256):
    j = (i << 1) ^ (0x11D if i & 0x80 else 0)
    ecc_f[i] = j & 0xFF
    ecc_b[i ^ (j & 0xFF)] = i

# ---- 1ª passada: lê os registros ----
pos = 4
saida = bytearray()
setores = {1: [], 2: [], 3: []}  # deslocamento na saída de cada setor de 2352 (montado)
blocos = []  # (tipo, offset_saida_do_setor_montado) para recortar depois
SYNC = b'\x00' + b'\xff' * 10 + b'\x00'
montados = []  # bytearray de setores 2352 por tipo, na ordem

out_parts = []  # lista de ('raw', bytes) ou ('sec', tipo, idx)
sec_buf = {1: [], 2: [], 3: []}
while True:
    c = data[pos]; pos += 1
    tipo = c & 3
    num = (c >> 2) & 0x1F
    bits = 5
    while c & 0x80:
        c = data[pos]; pos += 1
        num |= (c & 0x7F) << bits
        bits += 7
    if num == 0xFFFFFFFF:
        break
    num += 1
    if tipo == 0:
        out_parts.append(('raw', data[pos:pos + num]))
        pos += num
        continue
    for _ in range(num):
        s = bytearray(2352)
        if tipo == 1:
            s[0:12] = SYNC
            s[12:15] = data[pos:pos + 3]; pos += 3
            s[15] = 1
            s[16:16 + 2048] = data[pos:pos + 2048]; pos += 2048
        elif tipo == 2:
            s[15] = 2
            s[0x14:0x14 + 0x804] = data[pos:pos + 0x804]; pos += 0x804
            s[0x10:0x14] = s[0x14:0x18]
        else:
            s[15] = 2
            s[0x14:0x14 + 0x918] = data[pos:pos + 0x918]; pos += 0x918
            s[0x10:0x14] = s[0x14:0x18]
        sec_buf[tipo].append(s)
        out_parts.append(('sec', tipo, len(sec_buf[tipo]) - 1))

print('setores tipo1/2/3:', {k: len(v) for k, v in sec_buf.items()})


def edc_lote(arr, ini, fim):
    edc = np.zeros(arr.shape[0], dtype=np.uint32)
    for k in range(ini, fim):
        edc = (edc >> 8) ^ edc_lut[(edc ^ arr[:, k]) & 0xFF]
    return edc


def ecc_bloco(arr, major_count, minor_count, major_mult, minor_inc, dest):
    src = arr[:, 0xC:]
    size = major_count * minor_count
    idx = np.zeros((major_count, minor_count), dtype=np.int64)
    for major in range(major_count):
        index = (major >> 1) * major_mult + (major & 1)
        for minor in range(minor_count):
            idx[major, minor] = index
            index += minor_inc
            if index >= size:
                index -= size
    a = np.zeros((arr.shape[0], major_count), dtype=np.uint8)
    b = np.zeros_like(a)
    for minor in range(minor_count):
        t = src[:, idx[:, minor]]
        a ^= t
        b ^= t
        a = ecc_f[a]
    a = ecc_b[ecc_f[a] ^ b]
    arr[:, dest:dest + major_count] = a
    arr[:, dest + major_count:dest + 2 * major_count] = a ^ b


def ecc(arr, zerar_endereco):
    if zerar_endereco:
        guarda = arr[:, 12:16].copy()
        arr[:, 12:16] = 0
    ecc_bloco(arr, 86, 24, 2, 86, 0x81C)
    ecc_bloco(arr, 52, 43, 86, 88, 0x8C8)
    if zerar_endereco:
        arr[:, 12:16] = guarda


prontos = {}
for tipo, lista in sec_buf.items():
    if not lista:
        continue
    arr = np.frombuffer(b''.join(lista), dtype=np.uint8).reshape(-1, 2352).copy()
    if tipo == 1:
        e = edc_lote(arr, 0, 0x810)
        arr[:, 0x810:0x814] = e.view(np.uint8).reshape(-1, 4)
        arr[:, 0x814:0x81C] = 0
        ecc(arr, False)
    elif tipo == 2:
        e = edc_lote(arr, 0x10, 0x10 + 0x808)
        arr[:, 0x818:0x81C] = e.view(np.uint8).reshape(-1, 4)
        ecc(arr, True)
    else:
        e = edc_lote(arr, 0x10, 0x10 + 0x91C)
        arr[:, 0x92C:0x930] = e.view(np.uint8).reshape(-1, 4)
    prontos[tipo] = arr
    print('tipo', tipo, 'ok')

with open(dst_path, 'wb') as f:
    total = 0
    for p in out_parts:
        if p[0] == 'raw':
            f.write(p[1]); total += len(p[1])
        else:
            s = prontos[p[1]][p[2]]
            pedaco = s.tobytes() if p[1] == 1 else s[0x10:].tobytes()
            f.write(pedaco); total += len(pedaco)
print('sobra no fim:', len(data) - pos, 'bytes (esperado 4)');
print('gravado', total, 'bytes; setores de 2352:', total / 2352)

esperado = int.from_bytes(data[pos:pos + 4], 'little')
tab = [int(x) for x in edc_lut]
e = 0
with open(dst_path, 'rb') as f:
    while True:
        blk = f.read(1 << 20)
        if not blk: break
        arr = np.frombuffer(blk, dtype=np.uint8)
        for b in blk:
            e = (e >> 8) ^ tab[(e ^ b) & 0xFF]
print('EDC total confere:', e == esperado)
