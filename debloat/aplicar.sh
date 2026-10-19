#!/usr/bin/env bash
# Desativa (não apaga) os pacotes da lista. Uso: TV_HOST=192.168.x.x bash debloat/aplicar.sh [lista.txt]
set -u
: "${TV_HOST:?defina TV_HOST com o IP da TV}"
export MSYS_NO_PATHCONV=1
ADB="${ADB:-adb}"
LISTA="${1:-$(dirname "$0")/tcl-beyondtv4-android11.txt}"
"$ADB" connect "$TV_HOST:5555" >/dev/null
grep -v '^\s*#' "$LISTA" | awk '{print $1}' | grep . | tr -d '\r' | while read -r p; do
  echo "$p -> $("$ADB" -s "$TV_HOST:5555" shell pm disable-user --user 0 "$p" </dev/null 2>&1 | tr -d '\r')"
done
"$ADB" -s "$TV_HOST:5555" shell settings put global window_animation_scale 0.5
"$ADB" -s "$TV_HOST:5555" shell settings put global transition_animation_scale 0.5
"$ADB" -s "$TV_HOST:5555" shell settings put global animator_duration_scale 0.5
"$ADB" -s "$TV_HOST:5555" shell settings put global activity_manager_constants max_cached_processes=16
"$ADB" -s "$TV_HOST:5555" shell pm trim-caches 999G
echo "Pronto. Reinicie a TV."
