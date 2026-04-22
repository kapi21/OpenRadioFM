#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta en boot (late_start service).

MODDIR="${0%/*}"

PKG_FM_OEM="com.android.fmradio.ext"

log_print() {
  # logcat tag + magisk log (si existe)
  /system/bin/log -t ORF_K706_ROOT "$1" 2>/dev/null
  echo "[ORF_K706_ROOT] $1"
}

log_print "service.sh start: disabling OEM FM package: ${PKG_FM_OEM}"

# Espera a que el package manager esté listo (K706 a veces tarda)
i=0
while [ $i -lt 30 ]; do
  pm path android >/dev/null 2>&1 && break
  i=$((i+1))
  sleep 1
done

# Deshabilitar (reversible) + cortar proceso si estuviera vivo
pm disable-user --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1
am force-stop "${PKG_FM_OEM}" >/dev/null 2>&1

log_print "service.sh done"

