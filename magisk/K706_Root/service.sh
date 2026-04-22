#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta en boot (late_start service). (LF normalizado)

MODDIR="${0%/*}"

PKG_FM_OEM="com.android.fmradio.ext"
LAUNCHER_CITY_PKG="com.android.launcher.city"
LAUNCHER_CITY_PREF="/data/data/com.android.launcher.city/shared_prefs/sp_app_pkg_file.xml"

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

# Launcher.city: mantener icono/botón RADIO pero redirigir el click a OpenRadioFM.
# No cambia UI: solo sustituye el componente destino en su lista interna.
if [ -f "${LAUNCHER_CITY_PREF}" ]; then
  log_print "Patching launcher.city RADIO target -> OpenRadioFM"
  # Backup único (si no existe)
  if [ ! -f "${LAUNCHER_CITY_PREF}.bak_orf" ]; then
    cp "${LAUNCHER_CITY_PREF}" "${LAUNCHER_CITY_PREF}.bak_orf" >/dev/null 2>&1
  fi
  sed -i 's/com\\.android\\.fmradio\\.ext-com\\.android\\.fmradio\\.FmMainActivity/com\\.example\\.openradiofm-com\\.example\\.openradiofm\\.ui\\.main\\.MainActivity/g' "${LAUNCHER_CITY_PREF}" >/dev/null 2>&1
  am force-stop "${LAUNCHER_CITY_PKG}" >/dev/null 2>&1
fi

log_print "service.sh done"

