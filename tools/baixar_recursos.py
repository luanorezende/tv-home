"""Baixa os recursos que NÃO ficam no repositório (direitos de terceiros) para dentro do app:
- papéis de parede (fotos do Unsplash via picsum.photos, licença Unsplash)
- capas dos jogos (libretro-thumbnails)

Uso:  python tools/baixar_recursos.py
Depois compile o app normalmente.
"""
import io
import os
import urllib.parse
import urllib.request

from PIL import Image

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(RAIZ, "launcher", "app", "src", "main", "assets")

# fotos 4K de paisagem (ids do picsum.photos); troque à vontade
PAPEIS = [1015, 1018, 1036, 1039, 1043, 1044, 1050, 1057, 1067, 1002, 1016, 1022]

THUMBS = "https://raw.githubusercontent.com/libretro-thumbnails/{repo}/master/Named_Boxarts/{nome}.png"
SNES = "Nintendo_-_Super_Nintendo_Entertainment_System"
ARCADE = "FBNeo_-_Arcade_Games"
PS1 = "Sony_-_PlayStation"

# id da capa (o mesmo usado em Jogos.java) -> (repositório, nome exato no libretro-thumbnails)
CAPAS = {
    "smw": (SNES, "Super Mario World (USA)"),
    "dkc": (SNES, "Donkey Kong Country (USA)"),
    "topgear": (SNES, "Top Gear (USA)"),
    "topgear3000": (SNES, "Top Gear 3000 (USA)"),
    "contra3": (SNES, "Contra III - The Alien Wars (USA)"),
    "sf2turbo": (SNES, "Street Fighter II Turbo (USA)"),
    "umk3": (SNES, "Ultimate Mortal Kombat 3 (USA)"),
    "chrono": (SNES, "Chrono Trigger (USA)"),
    "bomberman": (SNES, "Super Bomberman (USA)"),
    "kof97": (ARCADE, "The King of Fighters '97 (NGM-2320)"),
    "kof98": (ARCADE, "The King of Fighters '98 - The Slugfest _ King of Fighters '98 - Dream Match Never Ends (NGM-2420)"),
    "kof02": (ARCADE, "The King of Fighters 2002 (NGM-2650 ~ NGH-2650)"),
    "metalslugx": (ARCADE, "Metal Slug - Super Vehicle-001"),
    "metalslug3": (ARCADE, "Metal Slug 3 (NGM-2560)"),
    "cadillacs": (ARCADE, "Cadillacs and Dinosaurs (World 930201)"),
    "yugioh": (PS1, "Yu-Gi-Oh! Forbidden Memories (USA)"),
    "thps3": (PS1, "Tony Hawk's Pro Skater 3 (USA)"),
    "thps2": (PS1, "Tony Hawk's Pro Skater 2 (USA)"),
    "sfa3": (PS1, "Street Fighter Alpha 3 (USA)"),
}


def baixar(url):
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 tv-launcher-setup"})
    return urllib.request.urlopen(req, timeout=30).read()


def main():
    pasta = os.path.join(ASSETS, "wallpapers")
    os.makedirs(pasta, exist_ok=True)
    for i in PAPEIS:
        destino = os.path.join(pasta, f"w{i}.jpg")
        if not os.path.exists(destino):
            open(destino, "wb").write(baixar(f"https://picsum.photos/id/{i}/1920/1080.jpg"))
        print("papel de parede", i)

    pasta = os.path.join(ASSETS, "capas")
    os.makedirs(pasta, exist_ok=True)
    for cid, (repo, nome) in CAPAS.items():
        destino = os.path.join(pasta, cid + ".jpg")
        if os.path.exists(destino):
            continue
        try:
            im = Image.open(io.BytesIO(baixar(THUMBS.format(repo=repo, nome=urllib.parse.quote(nome))))).convert("RGB")
        except Exception as e:
            print("capa não encontrada:", cid, nome, e)
            continue
        im.thumbnail((640, 640))
        im.save(destino, quality=88)
        print("capa", cid)


if __name__ == "__main__":
    main()
