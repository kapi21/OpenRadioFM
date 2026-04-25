#!/system/bin/sh
# Rollback al desinstalar el módulo (K706):
# - Limpieza profunda de /data de los paquetes tocados por el módulo
# - Restaurar la Radio OEM (com.android.fmradio.ext) desde:
#   1) APK empaquetada en el módulo (oem/*.apk)
#   2) Backup capturado en install (oem_backup/fmradio_ext_base.apk)
#   3) APK real del sistema vía mirror Magisk (/system/priv-app/QF_FMRadioExt/...)

PKG_FM_OEM="com.android.fmradio.ext"
PKG_ORF="com.example.openradiofm"
LOG_TAG="ORF_K706_ROOT"

logi() { /system/bin/log -t "$LOG_TAG" "$1" 2>/dev/null; }
LOGFILE="/data/local/tmp/orf_k706_root.log"
logf() {
  ts="$(date +%F_%T 2>/dev/null)"
  echo "${ts} [uninstall] $1" >> "$LOGFILE" 2>/dev/null
}

first_existing() {
  for p in "$@"; do
    [ -f "$p" ] && { echo "$p"; return 0; }
  done
  return 1
}

# Magisk suele exportar MODPATH. Si no, intentamos inferirlo.
if [ -z "$MODPATH" ]; then
  MODPATH="$(cd "$(dirname "$0")" 2>/dev/null && pwd 2>/dev/null)"
fi

logi "uninstall.sh start: deep cleanup + restore OEM Radio (${PKG_FM_OEM})"
logf "start MODPATH=${MODPATH}"

wait_pm_ready() {
  i=0
  while [ $i -lt 90 ]; do
    out="$(pm path android 2>&1)"
    case "$out" in
      *"Can't find service:"*)
        ;;
      package:*)
        logf "pm ready after ${i}s"
        return 0
        ;;
    esac
    i=$((i + 1))
    sleep 1
  done
  logf "ERROR: pm not ready after 90s (last=$out)"
  return 1
}

wait_pm_ready || exit 20

wait_boot_completed() {
  i=0
  while [ $i -lt 120 ]; do
    bc="$(getprop sys.boot_completed 2>/dev/null)"
    [ "$bc" = "1" ] && { logf "boot_completed after ${i}s"; return 0; }
    i=$((i + 1))
    sleep 1
  done
  logf "WARN: sys.boot_completed not reached after 120s"
  return 1
}

wait_boot_completed || true

TARGET_APK="/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk"
OEM_MIRROR_APK="$(first_existing \
  "/sbin/.magisk/mirror${TARGET_APK}" \
  "/debug_ramdisk/.magisk/mirror${TARGET_APK}" \
  "/data/adb/magisk/mirror${TARGET_APK}" \
)"

# Preferimos: APK OEM empaquetada (si existe) -> backup -> mirror del sistema
OEM_APK="$(first_existing \
  "${MODPATH}/oem/Radio_vnull.apk" \
  "${MODPATH}/oem/QF_FMRadioExt.apk" \
  "${MODPATH}/oem/FMRadio.apk" \
  "${MODPATH}/oem/Radio.apk" \
  "${MODPATH}/oem_backup/fmradio_ext_base.apk" \
  "/data/adb/orf_k706_root/oem/Radio_vnull.apk" \
  "${OEM_MIRROR_APK}" \
)"

logi "MODPATH=${MODPATH}"
logi "OEM_APK_CANDIDATE=${OEM_APK}"
logf "OEM_APK_CANDIDATE=${OEM_APK}"

force_uninstall_and_purge() {
  pkg="$1"
  [ -n "$pkg" ] || return 0

  out="$(am force-stop "$pkg" 2>&1)"; rc=$?; [ $rc -eq 0 ] || true
  [ -n "$out" ] && logf "am force-stop ${pkg}: ${out}"
  out="$(pm disable --user 0 "$pkg" 2>&1)"; rc=$?; [ $rc -eq 0 ] || true
  [ -n "$out" ] && logf "pm disable ${pkg}: ${out}"

  out="$(pm uninstall --user 0 "$pkg" 2>&1)"
  rc_user=$?
  logi "pm uninstall --user 0 ${pkg} rc=${rc_user}"
  logf "pm uninstall --user 0 ${pkg} rc=${rc_user}"
  [ -n "$out" ] && logf "pm uninstall --user 0 ${pkg} out: ${out}"

  out="$(pm uninstall "$pkg" 2>&1)"
  rc_global=$?
  logi "pm uninstall (global) ${pkg} rc=${rc_global}"
  logf "pm uninstall (global) ${pkg} rc=${rc_global}"
  [ -n "$out" ] && logf "pm uninstall (global) ${pkg} out: ${out}"

  out="$(pm clear --user 0 "$pkg" 2>&1)"; rc=$?; [ $rc -eq 0 ] || true
  [ -n "$out" ] && logf "pm clear --user 0 ${pkg}: ${out}"

  rm -rf /data/data/"$pkg" /data/user/0/"$pkg" /data/user_de/0/"$pkg" >/dev/null 2>&1 || true
  rm -rf /data/app/"$pkg"-* /data/app/*"$pkg"* >/dev/null 2>&1 || true
}

# 1) Quitar OpenRadioFM (si quedó como user-app) y limpiar datos
force_uninstall_and_purge "$PKG_ORF"

# 2) Quitar cualquier instalación /data de la Radio OEM (stub o update) y limpiar datos
force_uninstall_and_purge "$PKG_FM_OEM"

# 3) Restaurar preferencias de launchers/temas (y borrar backups para dejar el sistema limpio)
RESTORED_PKGS=""
for bak in /data/data/*/shared_prefs/*.bak_orf; do
  [ -f "$bak" ] || continue
  orig="${bak%.bak_orf}"
  cp "$bak" "$orig" >/dev/null 2>&1 || continue
  rm -f "$bak" >/dev/null 2>&1 || true
  pkg="${orig%/shared_prefs/*}"
  pkg="${pkg#/data/data/}"
  case " $RESTORED_PKGS " in
    *" $pkg "*) ;;
    *) RESTORED_PKGS="$RESTORED_PKGS $pkg" ;;
  esac
done
for pkg in $RESTORED_PKGS; do
  [ -n "$pkg" ] || continue
  am force-stop "$pkg" >/dev/null 2>&1 || true
done

# 4) Restaurar la OEM desde /system: install-existing + enable.
# Nota: en muchas ROMs OEM este paquete usa sharedUserId, por lo que "pm install" desde /data puede fallar
# con INSTALL_FAILED_SHARED_USER_INCOMPATIBLE. Lo correcto es reactivar el paquete preinstalado en /system.
RC_INSTALL=0
out="$(cmd package install-existing --user 0 "${PKG_FM_OEM}" 2>&1)"; rc_ie=$?
logf "cmd install-existing rc=${rc_ie} out: ${out}"
out2="$(pm install-existing --user 0 "${PKG_FM_OEM}" 2>&1)"; rc_ie2=$?
logf "pm install-existing rc=${rc_ie2} out: ${out2}"
out3="$(pm enable --user 0 "${PKG_FM_OEM}" 2>&1)"; rc_en=$?
logf "pm enable rc=${rc_en} out: ${out3}"

pp="$(pm path "${PKG_FM_OEM}" 2>&1)"
logf "pm path OEM: ${pp}"
case "$pp" in
  package:*) RC_INSTALL=0 ;;
  *) RC_INSTALL=1 ;;
esac

logi "uninstall.sh done: rc_install=${RC_INSTALL}"
logf "done rc_install=${RC_INSTALL}"
