# Guia passo a passo

Testado numa **TCL Smart TV Pro (BeyondTV4), Google TV, Android 11, 2 GB de RAM, processador 4× Cortex-A55 em 32 bits, GPU Mali-G31**. Outras TVs Android/Google TV devem funcionar; a lista de pacotes da etapa 2 muda de marca para marca.

Ferramentas no computador: `adb` (Android SDK platform-tools), JDK 17 ou 21, Android SDK com platform 35, Python 3 com Pillow (`pip install pillow`).

---

## Etapa 0 — Conectar na TV

1. Na TV: **Configurações → Sistema → Sobre → Build do Android TV OS**, aperte OK 7 vezes.
2. **Sistema → Opções do desenvolvedor → Depuração USB: ligada.** (Na TCL não existe "depuração pela rede"; a USB já abre a porta 5555.)
3. Ache a TV na rede: ela responde nas portas 5555 (ADB), 6466 (controle remoto do Google TV) e 8008/8009 (Chromecast).
4. `adb connect <ip>:5555` → a TV pergunta "Permitir depuração?" → **Sempre permitir**.
5. Backup: `pm list packages -f`, `settings list global|secure|system`, um `screencap`.

> O IP muda quando a TV religa ou troca de Wi-Fi para cabo. Se a conexão cair, procure de novo. Uma reserva de DHCP no roteador resolve.

## Etapa 1 — Diagnóstico

- RAM livre: `grep MemAvailable /proc/meminfo` (na TCL de 2 GB: ~400 MB com a tela do Google TV).
- Quem come memória: `dumpsys meminfo` (seção "Total PSS by process").
- Espaço: `df -h /data`. `pm trim-caches 999G` liberou 2,7 GB de cache na TCL.

## Etapa 2 — Limpeza

`TV_HOST=<ip> bash debloat/aplicar.sh` aplica `debloat/tcl-beyondtv4-android11.txt` (TCL + Google sem uso) e:
animações 0.5, `activity_manager_constants max_cached_processes=16`, limpeza de cache.

Para outra marca: liste com `pm list packages -s -e` e `dumpsys meminfo`, monte a lista, **mostre ao dono antes**. Nunca desative: o controle remoto e a entrada de TV/HDMI da fabricante (na TCL: `com.tcl.tv`, `com.tcl.tvinput`, `com.tcl.tcl_bt_rcu_service`), Play Store, GMS, WebView, teclado, Bluetooth, `com.google.android.tv.remote.service`.

Desfazer: `TV_HOST=<ip> bash debloat/desfazer.sh`.

## Etapa 3 — Tela inicial (`launcher/`)

1. Edite `launcher/app/src/main/java/com/tvhome/launcher/Config.java` (cidade, latitude/longitude, apps da barra).
2. `python tools/baixar_recursos.py` (papéis de parede + capas).
3. Compile: crie `launcher/local.properties` com `sdk.dir=<caminho do SDK>` e rode `./gradlew assembleDebug` (com `JAVA_HOME` apontando para o JDK 17/21).
4. `adb install -r launcher/app/build/outputs/apk/debug/app-debug.apk`
5. `adb shell cmd package set-home-activity com.tvhome.launcher/.MainActivity`
6. `adb shell appops set com.tvhome.launcher MANAGE_EXTERNAL_STORAGE allow` (para ver quais jogos existem).

Como funciona: Java puro, tudo desenhado numa grade 1920×1080 e escalado. Navegação feita à mão em `dispatchKeyEvent` (mais previsível na TV do que o foco do Android). Os ícones dos apps são o **banner oficial** de cada app (`PackageManager.getApplicationBanner`). Papel de parede com fade e movimento lento; troca a cada 1/5/15/60 min (Ajustes ⚙). Clima pelo Open-Meteo, guardado para funcionar sem internet. Bloco **Liberar memória**: `killBackgroundProcesses` em tudo menos o RetroArch.

### Etapa 3b — Tirar a tela do Google TV

Só depois de testar o launcher: `TV_HOST=<ip> bash debloat/aplicar.sh debloat/tela-inicial-google.txt`

- `set-home-activity` sozinho **não basta** na TCL: o Home continua abrindo o Google TV.
- Ao desativar `com.google.android.apps.tv.launcherx`, o Home passa a abrir uma tela invisível de "recuperação" do assistente de configuração (`com.google.android.tungsten.setupwraith/.RecoveryActivity`). O shell não consegue desativar só essa activity, então desativa-se o pacote inteiro (ele volta sozinho num reset de fábrica).
- Confira: `dumpsys activity activities | grep mResumedActivity` depois de apertar Home e depois de reiniciar.

## Etapa 4 — Jogos

1. Instale o **RetroArch da Play Store**. Se o botão "Instalar" parecer não fazer nada, é porque a Play Store abre um aviso "Conecte um gamepad" → **Continuar**.
2. Abra o RetroArch uma vez e espere a barra "Extraindo base.apk" terminar.
3. A versão da Play Store **não tem permissão de armazenamento**: ela só lê a pasta dela. Os jogos ficam em
   `/sdcard/Android/media/com.retroarch/Jogos/{SNES,Fliperama,PS1}`.
4. Os núcleos já vêm dentro do app (32 bits na TCL): `<nativeLibraryDir>/libsnes9x.so`, `libfbneo.so`, `libpcsx_rearmed.so`. O launcher abre o jogo direto com um intent para `com.retroarch/com.retroarch.browser.retroactivity.RetroActivityFuture` com os extras `ROM`, `LIBRETRO`, `CONFIGFILE`, `DATADIR`, `APK`, `SDCARD`, `EXTERNAL` (ver `Jogos.java`).
5. Edite `Android/data/com.retroarch/files/retroarch.cfg` **com o RetroArch fechado**:
   `savestate_auto_save/auto_load = "true"`, `config_save_on_exit = "false"`, `input_quit_gamepad_combo = "4"` (Start+Select sai salvando), `input_menu_toggle_gamepad_combo = "2"` (L3+R3), `video_threaded = "true"`, `remap_save_on_exit = "false"`.
6. PS1 mais leve: copie `retroarch/PCSX-ReARMed.opt` para `Android/data/com.retroarch/files/config/PCSX-ReARMed/`.
7. Fliperama: copie `retroarch/FinalBurn Neo.rmp` para `Android/media/com.retroarch/RetroArch/config/remaps/FinalBurn Neo/` (R2 = A+B esquiva, R1 = A+B+C, L1 = C+D, L2 = B+C+D). O FBNeo já cria esses botões combinados; especial de movimento num botão só não existe.
8. Personagens escondidos (KOF 97/98): `python tools/preparar_trapacas.py` e copie `retroarch/cheats/*.ini` para `Android/media/com.retroarch/RetroArch/system/fbneo/cheats/`. O FBNeo usa o `default` do .ini como valor da opção.

### Nomes dos arquivos

- **SNES**: qualquer nome; renomeie para o id do `Jogos.java` (`smw.sfc`, `dkc.sfc`…).
- **Fliperama**: o `.zip` precisa ter o **nome oficial do MAME/FBNeo** (`kof97.zip`, `mslug3.zip`, `dino.zip`) e o BIOS `neogeo.zip` na mesma pasta. Confira o conteúdo: "versões" de 1 a 10 MB costumam ser clones/hacks que dependem do jogo original.
- **PS1**: `.chd`, `.cue`+`.bin`, `.ccd`+`.img`+`.sub`. Um ".iso" de PS1 com tamanho múltiplo de 2352 é BIN bruto: renomeie para `.bin` e crie um `.cue` (`MODE2/2352`). `.img.ecm` precisa ser descompactado: `python tools/unecm.py jogo.img.ecm jogo.img` (confere o EDC no final).
- O PS2 não roda nessa TCL (32 bits, A55 + Mali-G31). Use a etapa 6.

### Cuidados

- **Trocar de jogo por intent não grava o save automático**; só sair com Start+Select grava. Antes de testar, veja se o RetroArch está aberto e pergunte ao dono.
- `force-stop` no RetroArch perde a partida. `input keyevent ESCAPE` pelo adb não fecha.
- Pelo adb dá para "jogar": `KEYCODE_BUTTON_SELECT` (ficha), `KEYCODE_BUTTON_START`, `KEYCODE_BUTTON_A`.
- O log do RetroArch não vai para o logcat: ligue `log_to_file = "true"` temporariamente e leia `Android/media/com.retroarch/RetroArch/logs/retroarch.log`.

## Etapa 5 — TV ao vivo

1. `python tools/gerar_canais.py` (testa cada canal até o vídeo; tira canais pagos e retransmissões de emissoras).
2. Recompile o launcher. O app atualiza os endereços 1×/dia pelo `tvg-id`.
3. Player Media3 em tela cheia: ↑↓ troca de canal, OK mostra o nome, Voltar sai.
- O `screencap` sai **preto** quando tem vídeo na tela (camada de vídeo da TCL). Confira pelo logcat (tag `TvHome`).
- Alguns canais dão 403 de vez em quando na origem; o app mostra "Fora do ar".

## Etapa 6 — PC na TV (Sunshine + Moonlight)

1. No PC: instale o **Sunshine** do GitHub oficial (LizardByte). Confira hash/assinatura. O dono cria o usuário e senha no painel `https://localhost:47990` (não preencha senha por ele).
2. Na TV: **Moonlight** (Play Store) → escolha o PC → aparece um PIN → no painel do Sunshine, aba **PIN**.
3. Prefira **cabo de rede** na TV. Se a TV tem cabo mas continua no Wi-Fi, confira `ifconfig eth0`: sem `RUNNING` e 0 pacotes = problema físico (cabo, conector, porta do roteador).

## Problemas conhecidos

- Screenshot de vídeo sai preto (etapa 5).
- O Home da TCL ignora o launcher padrão enquanto o Google TV estiver ativo (etapa 3b).
- Desativar `com.google.android.apps.tv.dreamx` deixa o descanso de tela apontando para ele: `settings put secure screensaver_components com.android.dreams.basic/.Colors`.
- Fire TV: o Fire OS é Android e roda o launcher e o RetroArch, mas não deixa trocar a tela inicial; os modelos com **Vega OS** (Fire TV Stick 4K Select, 2025) não rodam apps Android.
