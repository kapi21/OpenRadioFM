#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta temprano en boot (post-fs-data).
#
# Implementa "modo OEM" sin desinstalar el módulo:
# - Por defecto: el launcher (com.android.fmradio.ext/FmMainActivity) abre OpenRadioFM vía trampolín.
# - Con flag: restaura el APK OEM real (desde mirror Magisk) para que el launcher abra la OEM.
#
# El overlay de permisos (MODIFY_PHONE_STATE) se mantiene porque el módulo sigue instalado.

MODDIR="${0%/*}"
MODDIR="${MODDIR#\"}"
MODDIR="${MODDIR%\"}"

# Flag (nombre acordado): si existe, habilita modo OEM.
OEM_FLAG="${MODDIR}/orf_oem_mode"

TARGET_APK="/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk"
STUB_APK="${MODDIR}/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk"

log_print() {
  /system/bin/log -t ORF_K706_ROOT "$1" 2>/dev/null
}

first_existing() {
  for p in "$@"; do
    [ -f "$p" ] && { echo "$p"; return 0; }
  done
  return 1
}

ensure_parent_dir() {
  d="${1%/*}"
  [ -d "$d" ] || mkdir -p "$d" >/dev/null 2>&1
}

bind_mount() {
  src="$1"
  dst="$2"
  [ -f "$src" ] || return 1
  ensure_parent_dir "$dst"
  mount -o bind "$src" "$dst" >/dev/null 2>&1
}

# Localizar el APK OEM "real" sin overlays (mirror Magisk)
OEM_REAL_APK="$(first_existing \
  "/sbin/.magisk/mirror${TARGET_APK}" \
  "/debug_ramdisk/.magisk/mirror${TARGET_APK}" \
  "/data/adb/magisk/mirror${TARGET_APK}" \
)"

# --- Privapp permissions patch (ROMs que ignoran XML adicionales) ---
PLATFORM_PERMS="/system/etc/permissions/privapp-permissions-platform.xml"
PLATFORM_MIRROR="$(first_existing \
  "/sbin/.magisk/mirror${PLATFORM_PERMS}" \
  "/debug_ramdisk/.magisk/mirror${PLATFORM_PERMS}" \
  "/data/adb/magisk/mirror${PLATFORM_PERMS}" \
)"

patch_privapp_platform_perms() {
  # En algunos entornos el mirror puede no estar accesible aquí. Fallback: leer el archivo actual de /system.
  SRC_FILE=""
  if [ -n "$PLATFORM_MIRROR" ] && [ -f "$PLATFORM_MIRROR" ]; then
    SRC_FILE="$PLATFORM_MIRROR"
  elif [ -f "$PLATFORM_PERMS" ]; then
    SRC_FILE="$PLATFORM_PERMS"
  else
    log_print "post-fs-data: WARN privapp-permissions-platform.xml no accesible"
    return 1
  fi

  # Preferir toybox para consistencia en boot (awk/grep pueden no existir como binarios separados)
  TB="toybox"

  # Si ya está presente, no tocar.
  # Si ya está presente, no tocar.
  if $TB grep -q 'package="com.android.fmradio.ext"' "$SRC_FILE" 2>/dev/null; then
    START_LINE="$($TB grep -n 'package=\"com.android.fmradio.ext\"' "$SRC_FILE" 2>/dev/null | $TB head -n 1 | $TB cut -d: -f1)"
    if [ -n "$START_LINE" ]; then
      # Buscar el primer cierre </privapp-permissions> a partir del bloque
      REL_END="$($TB sed -n "${START_LINE},99999p" "$SRC_FILE" 2>/dev/null | $TB grep -n '</privapp-permissions>' 2>/dev/null | $TB head -n 1 | $TB cut -d: -f1)"
      if [ -n "$REL_END" ]; then
        END_LINE=$((START_LINE + REL_END - 1))
        if $TB sed -n "${START_LINE},${END_LINE}p" "$SRC_FILE" 2>/dev/null | $TB grep -q 'android.permission.MODIFY_PHONE_STATE' 2>/dev/null; then
          log_print "post-fs-data: privapp-permissions-platform.xml ya contiene MODIFY_PHONE_STATE para fmradio.ext"
          return 0
        fi
      fi
    fi
  fi

  OUT_DIR="${MODDIR}/perm_patch"
  OUT_FILE="${OUT_DIR}/privapp-permissions-platform.xml"
  mkdir -p "$OUT_DIR" >/dev/null 2>&1

  # Inserta MODIFY_PHONE_STATE dentro del bloque de com.android.fmradio.ext justo antes del cierre.
  # Inserta MODIFY_PHONE_STATE dentro del bloque de com.android.fmradio.ext justo antes del cierre.
  START_LINE="$($TB grep -n 'package=\"com.android.fmradio.ext\"' "$SRC_FILE" 2>/dev/null | $TB head -n 1 | $TB cut -d: -f1)"
  if [ -z "$START_LINE" ]; then
    log_print "post-fs-data: ERROR no se encontró bloque com.android.fmradio.ext en privapp-permissions-platform.xml"
    return 1
  fi

  REL_END="$($TB sed -n "${START_LINE},99999p" "$SRC_FILE" 2>/dev/null | $TB grep -n '</privapp-permissions>' 2>/dev/null | $TB head -n 1 | $TB cut -d: -f1)"
  if [ -z "$REL_END" ]; then
    log_print "post-fs-data: ERROR no se encontró cierre </privapp-permissions> para bloque fmradio.ext"
    return 1
  fi
  END_LINE=$((START_LINE + REL_END - 1))

  # Insertar en la línea del cierre, antes de imprimirla.
  # toybox sed soporta 'i\' con número de línea.
  $TB sed "${END_LINE}i\\
        <permission name=\"android.permission.MODIFY_PHONE_STATE\"/>\\
" "$SRC_FILE" > "$OUT_FILE"

  if [ -s "$OUT_FILE" ]; then
    bind_mount "$OUT_FILE" "$PLATFORM_PERMS" && log_print "post-fs-data: bind privapp-permissions-platform.xml parcheado" \
      || log_print "post-fs-data: ERROR bind privapp-permissions-platform.xml"
  else
    log_print "post-fs-data: ERROR generando privapp-permissions-platform.xml parcheado (OUT vacio)"
  fi
}

patch_privapp_platform_perms

if [ -f "$OEM_FLAG" ]; then
  log_print "post-fs-data: OEM mode ON (flag=$(basename "$OEM_FLAG"))"
  if [ -n "$OEM_REAL_APK" ] && [ -f "$OEM_REAL_APK" ]; then
    bind_mount "$OEM_REAL_APK" "$TARGET_APK" && log_print "post-fs-data: bound OEM real APK -> ${TARGET_APK}" \
      || log_print "post-fs-data: ERROR binding OEM real APK"
  else
    log_print "post-fs-data: WARN no OEM mirror APK found; keeping current ${TARGET_APK}"
  fi
else
  log_print "post-fs-data: OEM mode OFF (default OpenRadioFM trampoline)"
  if [ -f "$STUB_APK" ]; then
    bind_mount "$STUB_APK" "$TARGET_APK" && log_print "post-fs-data: bound STUB APK -> ${TARGET_APK}" \
      || log_print "post-fs-data: ERROR binding STUB APK"
  else
    log_print "post-fs-data: WARN STUB APK missing at ${STUB_APK}"
  fi
fi

