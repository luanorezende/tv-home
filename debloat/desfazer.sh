#!/usr/bin/env bash
# Reativa tudo da lista e volta animações/processos ao padrão. Uso: TV_HOST=192.168.x.x bash debloat/desfazer.sh [lista.txt]
set -u
: "${TV_HOST:?defina TV_HOST com o IP da TV}"
export MSYS_NO_PATHCONV=1
ADB="${ADB:-adb}"
LISTA="${1:-$(dirname "$0")/tcl-beyondtv4-android11.txt}"
"$ADB" connect "$TV_HOST:5555" >/dev/null
grep -v '^\s*#' "$LISTA" | awk '{print $1}' | grep . | tr -d '\r' | while read -r p; do
  echo "$p: $("$ADB" -s "$TV_HOST:5555" shell pm enable "$p" </dev/null | tr -d '\r')"
done
for k in window_animation_scale transition_animation_scale animator_duration_scale; do
  "$ADB" -s "$TV_HOST:5555" shell settings put global $k 1.0
done
"$ADB" -s "$TV_HOST:5555" shell settings delete global activity_manager_constants
echo "Pronto. Reinicie a TV."
