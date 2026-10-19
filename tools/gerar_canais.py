"""Gera launcher/app/src/main/assets/canais.json a partir da lista pública do iptv-org.

- Testa cada canal até chegar no vídeo (descarta os que não respondem).
- Tira canais pagos/premium e retransmissões não oficiais de emissoras (o app não deve
  distribuir sinal de terceiros sem autorização).
- Agrupa por categoria em português.
- No Brasil, inclui o atalho da CazéTV (abre o canal oficial no YouTube).

Uso:  python tools/gerar_canais.py            (Brasil)
      python tools/gerar_canais.py pt         (outro país, código ISO de 2 letras)
"""
import concurrent.futures as cf
import json
import os
import re
import ssl
import sys
import urllib.parse
import urllib.request

PAIS = (sys.argv[1] if len(sys.argv) > 1 else "br").lower()
LISTA = f"https://iptv-org.github.io/iptv/countries/{PAIS}.m3u"
RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SAIDA = os.path.join(RAIZ, "launcher", "app", "src", "main", "assets", "canais.json")

CATEGORIAS = [
    ("Notícias", "News"), ("Esportes", "Sports"), ("Filmes", "Movies"), ("Séries", "Series"),
    ("Infantil", "Kids|Animation"), ("Documentários", "Documentary|Outdoor|Culture"),
    ("Música", "Music"), ("Comédia", "Comedy"), ("Variedades", "Entertainment|Lifestyle|General"),
]
# grupos que não entram
FORA_GRUPO = r"Shop|Religious|Legislative|Undefined"
# marcas de TV paga e emissoras cujo sinal na lista costuma ser retransmissão não oficial
FORA_NOME = (r"^(ESPN|AMC|AXN|Sony|A&E|Lifetime|History|Cartoon Network|Comedy Central Brasil|Arte 1|HBO|"
             r"Telecine|Warner|TNT(?! Novelas)|Discovery|Globo|Rede Globo|TV Globo|SporTV|Premiere|Combate)|\[Geo-blocked\]")

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE


def get(u, n=None, cab=None):
    h = {"User-Agent": "Mozilla/5.0"}
    h.update(cab or {})
    r = urllib.request.urlopen(urllib.request.Request(u, headers=h), timeout=8, context=ctx)
    return r.read(n) if n else r.read()


def funciona(c):
    """Segue o HLS até baixar um pedaço de vídeo."""
    try:
        url = c["url"]
        texto = get(url, cab=c["h"]).decode("utf-8", "ignore")
        for _ in range(3):
            linhas = [l for l in texto.splitlines() if l and not l.startswith("#")]
            if not linhas:
                return False
            prox = urllib.parse.urljoin(url, linhas[0])
            if ".m3u8" in prox.split("?")[0]:
                url, texto = prox, get(prox, cab=c["h"]).decode("utf-8", "ignore")
                continue
            return len(get(prox, 4096, c["h"])) > 0
    except Exception:
        return False
    return False


def main():
    linhas = get(LISTA).decode("utf-8").splitlines()
    canais = []
    for i, l in enumerate(linhas):
        if not l.startswith("#EXTINF"):
            continue
        url = next((x for x in linhas[i + 1:i + 4] if x and not x.startswith("#")), None)
        cab = {}
        for o in linhas[i + 1:i + 4]:
            m = re.match(r"#EXTVLCOPT:http-(user-agent|referrer)=(.*)", o)
            if m:
                cab["User-Agent" if m[1] == "user-agent" else "Referer"] = m[2].strip()
        attr = lambda k: (re.search(k + r'="([^"]*)"', l) or [None, ""])[1]
        nome = l.split(",", 1)[1].strip()
        grupo = attr("group-title")
        if not url or re.search(FORA_GRUPO, grupo) or re.search(FORA_NOME, nome):
            continue
        cat = next((pt for pt, g in CATEGORIAS if re.search(g, grupo)), None)
        if not cat:
            continue
        nome = re.sub(r"\s*\((\d+p|SD|HD)\)|\s*\[.*?\]", "", nome).strip()
        canais.append({"cat": cat, "nome": nome, "logo": attr("tvg-logo"), "id": attr("tvg-id"), "url": url, "h": cab})

    print(len(canais), "candidatos; testando…")
    with cf.ThreadPoolExecutor(32) as ex:
        ok = [c for c, v in zip(canais, ex.map(funciona, canais)) if v]
    # atalhos oficiais que abrem outro app em vez do player (canal "app" + "link")
    if PAIS == "br":
        ok.append({"cat": "Esportes", "nome": "CazéTV", "logo": "", "app": "com.google.android.youtube.tv",
                   "link": "https://www.youtube.com/@CazeTV/streams"})
    ordem = [pt for pt, _ in CATEGORIAS]
    ok.sort(key=lambda c: (ordem.index(c["cat"]), c["nome"].lower()))
    json.dump(ok, open(SAIDA, "w", encoding="utf-8"), ensure_ascii=False)
    print(len(ok), "canais funcionando ->", SAIDA)


if __name__ == "__main__":
    main()
