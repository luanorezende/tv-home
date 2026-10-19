"""Baixa as trapaças oficiais do FBNeo (github.com/finalburnneo/FBNeo-cheats) e deixa ligadas por padrão
as que liberam personagens escondidos, sem precisar fazer o código na tela de seleção.

Uso:  python tools/preparar_trapacas.py
Saída: retroarch/cheats/*.ini  ->  copie para  <pasta do RetroArch>/system/fbneo/cheats/
"""
import os
import urllib.request

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SAIDA = os.path.join(RAIZ, "retroarch", "cheats")
BASE = "https://raw.githubusercontent.com/finalburnneo/FBNeo-cheats/master/cheats/{}.ini"

LIGAR = {
    "kof97": ["Enable 5 hidden characters", "Enable Orochi(Between Iori and Shingo)"],
    "kof98": ["Boss selectabled with START button in MVS"],
}


def main():
    os.makedirs(SAIDA, exist_ok=True)
    for jogo, nomes in LIGAR.items():
        s = urllib.request.urlopen(BASE.format(jogo), timeout=30).read().decode("utf-8")
        for n in nomes:
            antes = f'cheat "{n}"\ndefault 0'
            if antes not in s:
                print("não achei", jogo, n)
                continue
            s = s.replace(antes, f'cheat "{n}"\ndefault 1')
        open(os.path.join(SAIDA, jogo + ".ini"), "w", encoding="utf-8", newline="\n").write(s)
        print("ok", jogo)


if __name__ == "__main__":
    main()
