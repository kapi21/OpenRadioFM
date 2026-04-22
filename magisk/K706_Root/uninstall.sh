#!/system/bin/sh
# Rollback al desinstalar el módulo.

PKG_FM_OEM="com.android.fmradio.ext"

/system/bin/log -t ORF_K706_ROOT "uninstall.sh start: enabling OEM FM package: ${PKG_FM_OEM}" 2>/dev/null

pm enable --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1

/system/bin/log -t ORF_K706_ROOT "uninstall.sh done" 2>/dev/null

