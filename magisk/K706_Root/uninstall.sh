#!/system/bin/sh
# Rollback al desinstalar el módulo. (LF normalizado)

PKG_FM_OEM="com.android.fmradio.ext"

/system/bin/log -t ORF_K706_ROOT "uninstall.sh start: enabling OEM FM package: ${PKG_FM_OEM}" 2>/dev/null

pm enable --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1

# Restaurar cualquier shared_prefs respaldado por el módulo (*.bak_orf).
RESTORED_PKGS=""
for bak in /data/data/*/shared_prefs/*.bak_orf; do
  [ -f "$bak" ] || continue
  orig="${bak%.bak_orf}"
  cp "$bak" "$orig" >/dev/null 2>&1 || continue
  pkg="${orig%/shared_prefs/*}"
  pkg="${pkg#/data/data/}"
  case " $RESTORED_PKGS " in
    *" $pkg "*) ;;
    *) RESTORED_PKGS="$RESTORED_PKGS $pkg" ;;
  esac
done
for pkg in $RESTORED_PKGS; do
  [ -n "$pkg" ] || continue
  am force-stop "$pkg" >/dev/null 2>&1
done

/system/bin/log -t ORF_K706_ROOT "uninstall.sh done" 2>/dev/null
