#!/system/bin/sh
# Rollback al desinstalar el módulo. (LF normalizado)

PKG_FM_OEM="com.android.fmradio.ext"
LAUNCHER_CITY_PKG="com.android.launcher.city"
LAUNCHER_CITY_PREF="/data/data/com.android.launcher.city/shared_prefs/sp_app_pkg_file.xml"

/system/bin/log -t ORF_K706_ROOT "uninstall.sh start: enabling OEM FM package: ${PKG_FM_OEM}" 2>/dev/null

pm enable --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1

# Restaurar launcher.city si existe backup del módulo.
if [ -f "${LAUNCHER_CITY_PREF}.bak_orf" ]; then
  cp "${LAUNCHER_CITY_PREF}.bak_orf" "${LAUNCHER_CITY_PREF}" >/dev/null 2>&1
  am force-stop "${LAUNCHER_CITY_PKG}" >/dev/null 2>&1
fi

/system/bin/log -t ORF_K706_ROOT "uninstall.sh done" 2>/dev/null

