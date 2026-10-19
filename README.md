# TV Home — Google TV limpa, rápida e com jogos

Transforma uma TV com **Google TV / Android TV** (testado numa **TCL de 2 GB**) em algo bem mais leve:

- **Tela inicial própria**: papel de parede 4K que troca sozinho, relógio grande, clima, seus apps com o ícone oficial, botão de liberar memória.
- **Sem propaganda**: desativa a tela do Google TV e os serviços de propaganda/telemetria da fabricante (nada é apagado, tudo volta com um comando).
- **Jogos**: prateleiras com capas (Super Nintendo, Fliperama, PlayStation 1). Aperta OK na capa e o jogo abre direto no RetroArch, com save automático e controle Bluetooth.
- **TV ao vivo**: canais gratuitos da lista pública do iptv-org, testados e separados por categoria.
- **PC na TV**: guia para jogar PS2/jogos do PC na TV com Sunshine + Moonlight.

Feito conversando com o Claude Code ligado na TV pela rede (ADB). O jeito mais fácil de repetir é o mesmo: abra o Claude Code, cole o [PROMPT.md](PROMPT.md) e siga.

## O que tem aqui

| Pasta | O quê |
|---|---|
| [`PROMPT.md`](PROMPT.md) | Prompt para colar no Claude Code (ou outro assistente com terminal). |
| [`docs/GUIA.md`](docs/GUIA.md) | Passo a passo completo, com os tropeços que já aconteceram e como evitar. |
| `launcher/` | O app da tela inicial (Java puro + Media3 para a TV ao vivo). |
| `debloat/` | Lista de pacotes testada na TCL BeyondTV4 + scripts para aplicar/desfazer. |
| `retroarch/` | Ajustes do RetroArch: PS1 mais leve, botões de fliperama. |
| `tools/` | Scripts: baixar papéis de parede e capas, gerar lista de canais, trapaças de personagens, descompactar `.ecm`. |

## O que NÃO tem aqui (de propósito)

- **Nenhum jogo (ROM) e nenhum BIOS.** Use só cópias de jogos que você tem. O guia explica onde colocar os arquivos e como nomear.
- **Nenhuma capa, logo ou foto de terceiros.** O `tools/baixar_recursos.py` baixa na sua máquina (libretro-thumbnails e Unsplash).
- **Nenhum canal pago nem retransmissão de emissora.** O `tools/gerar_canais.py` filtra.

## Créditos

Inspirado em [tv-vibehack](https://github.com/Gathin23/tv-vibehack) (método de limpeza por ADB) e [Sva Launcher](https://github.com/clay-36/Sva-launcher) (ideia de juntar jogos, TV ao vivo e tela inicial). Usa [RetroArch](https://github.com/libretro/RetroArch), [iptv-org](https://github.com/iptv-org/iptv), [Open-Meteo](https://open-meteo.com), [libretro-thumbnails](https://github.com/libretro-thumbnails), [FBNeo-cheats](https://github.com/finalburnneo/FBNeo-cheats), [Sunshine](https://github.com/LizardByte/Sunshine)/[Moonlight](https://moonlight-stream.org) e a fonte [Outfit](https://fonts.google.com/specimen/Outfit) (SIL OFL).

Licença: MIT (código). A fonte Outfit segue a SIL Open Font License (`launcher/app/src/main/assets/fonts/OFL.txt`).
