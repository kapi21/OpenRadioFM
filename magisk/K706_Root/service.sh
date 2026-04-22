#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta en boot (late_start service). (LF normalizado)

MODDIR="${0%/*}"

PKG_FM_OEM="com.android.fmradio.ext"

log_print() {
  /system/bin/log -t ORF_K706_ROOT "$1" 2>/dev/null
  echo "[ORF_K706_ROOT] $1"
}

# Parchea prefs de launchers / temas que guardan el destino del icono RADIO como texto.
# Cubre varios formatos (slash, guion+punto escapado como en launcher.city) sin depender
# del launcher por defecto: cualquier paquete con XML en shared_prefs se revisa.
patch_all_radio_shortcut_prefs() {
  PATCHED_PKGS=""
  for dir in /data/data/*/shared_prefs; do
    [ -d "$dir" ] || continue
    for f in "$dir"/*.xml; do
      [ -f "$f" ] || continue
      if ! grep -q 'com\.android\.fmradio' "$f" 2>/dev/null; then
        continue
      fi
      if ! grep -q 'fmradio.ext' "$f" 2>/dev/null && ! grep -q 'FmMainActivity' "$f" 2>/dev/null; then
        continue
      fi
      bak="${f}.bak_orf"
      if [ ! -f "$bak" ]; then
        cp "$f" "$bak" >/dev/null 2>&1 || continue
      fi
      sed -i \
        -e 's/com\\.android\\.fmradio\\.ext-com\\.android\\.fmradio\\.FmMainActivity/com\\.example\\.openradiofm-com\\.example\\.openradiofm\\.ui\\.main\\.MainActivity/g' \
        -e 's/com\.android\.fmradio\.ext\/com\.android\.fmradio\.FmMainActivity/com\.example\.openradiofm\/com\.example\.openradiofm\.ui\.main\.MainActivity/g' \
        -e 's/com\.android\.fmradio\.ext-com\.android\.fmradio\.FmMainActivity/com\.example\.openradiofm-com\.example\.openradiofm\.ui\.main\.MainActivity/g' \
        -e 's/com\.android\.fmradio\.FmMainActivity/com.example.openradiofm.ui.main.MainActivity/g' \
        -e 's/com\.android\.fmradio\.ext/com.example.openradiofm/g' \
        "$f" >/dev/null 2>&1
      pkg="${dir%/shared_prefs}"
      pkg="${pkg#/data/data/}"
      case " $PATCHED_PKGS " in
        *" $pkg "*) ;;
        *) PATCHED_PKGS="$PATCHED_PKGS $pkg" ;;
      esac
    done
  done
  for pkg in $PATCHED_PKGS; do
    [ -n "$pkg" ] || continue
    log_print "Restarting launcher/theme after RADIO remap: $pkg"
    am force-stop "$pkg" >/dev/null 2>&1
  done
}

log_print "service.sh start: stub QF_FMRadioExt + launcher prefs (${PKG_FM_OEM})"

i=0
while [ $i -lt 30 ]; do
  pm path android >/dev/null 2>&1 && break
  i=$((i + 1))
  sleep 1
done

# El módulo monta un APK trampolín en system/priv-app/QF_FMRadioExt/ (mismo package
# com.android.fmradio.ext). Si el paquete sigue disable-user, el launcher lanza
# ActivityNotFoundException al pulsar el widget (Function.onRadio -> FmMainActivity).
pm enable --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1

log_print "Scanning shared_prefs for OEM RADIO shortcut targets -> OpenRadioFM"
patch_all_radio_shortcut_prefs

log_print "service.sh done"
