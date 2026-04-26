#!/system/bin/sh

# Magisk 27 module install script (customize.sh)
# Se ejecuta durante la instalación del ZIP. (LF normalizado)

# false = montar system/ (APK trampolín QF_FMRadioExt). true = solo scripts (no overlay).
SKIPMOUNT=false
PROPFILE=true
POSTFSDATA=true
LATESTARTSERVICE=true

print_modname() {
  ui_print " "
  ui_print "*******************************"
  ui_print " OpenRadioFM — K706 Root Hijack "
  ui_print "*******************************"
  ui_print " "
}

on_install() {
  ui_print "- Instalando archivos del módulo..."
  unzip -o "$ZIPFILE" -x 'META-INF/*' -d "$MODPATH" >&2
  # Asegurar LF (evita shebang con CRLF => "No such file or directory")
  sed -i 's/\r$//' "$MODPATH/service.sh" 2>/dev/null
  sed -i 's/\r$//' "$MODPATH/uninstall.sh" 2>/dev/null

  # Backup defensivo: algunas ROMs usan una actualización de FM OEM en /data/app.
  # Si al desinstalar volviéramos al APK viejo de /system, puede crashear (provider/permisos).
  # Guardamos el base.apk actual si viene de /data/app, para poder restaurarlo en uninstall.sh.
  PKG_FM_OEM="com.android.fmradio.ext"
  PKG_PATH="$(pm path "$PKG_FM_OEM" 2>/dev/null | head -n 1 | sed 's/^package://')"
  APK_CANDIDATE=""
  case "$PKG_PATH" in
    /data/app/*/base.apk) APK_CANDIDATE="$PKG_PATH" ;;
  esac

  # A veces pm path apunta a /system aunque siga existiendo un /data/app/... (p.ej. estado por-user).
  # Buscamos directamente por ruta conocida.
  if [ -z "$APK_CANDIDATE" ]; then
    for p in /data/app/"${PKG_FM_OEM}"-*/base.apk; do
      [ -f "$p" ] || continue
      APK_CANDIDATE="$p"
      break
    done
  fi

  if [ -n "$APK_CANDIDATE" ] && [ -f "$APK_CANDIDATE" ]; then
    ui_print "- Backup OEM FM (data app) para rollback..."
    mkdir -p "$MODPATH/oem_backup" 2>/dev/null
    cp "$APK_CANDIDATE" "$MODPATH/oem_backup/fmradio_ext_base.apk" 2>/dev/null
  fi

  # --- Rollback robusto aunque Magisk borre el módulo antes del boot ---
  # Guardamos una copia persistente del APK OEM y dejamos un watcher en /data/adb/service.d
  # para que, si el módulo desaparece, ejecute el rollback 1 sola vez.
  PERSIST_DIR="/data/adb/orf_k706_root"
  PERSIST_OEM="$PERSIST_DIR/oem/Radio_vnull.apk"
  WATCHER="/data/adb/service.d/99-orf-k706-root-rollback.sh"

  if [ -f "$MODPATH/oem/Radio_vnull.apk" ]; then
    ui_print "- Preparando rollback persistente (service.d) ..."
    mkdir -p "$PERSIST_DIR/oem" >/dev/null 2>&1
    cp "$MODPATH/oem/Radio_vnull.apk" "$PERSIST_OEM" >/dev/null 2>&1
    chmod 0644 "$PERSIST_OEM" >/dev/null 2>&1
  else
    ui_print "! WARN: No existe $MODPATH/oem/Radio_vnull.apk (rollback OEM puede fallar)"
  fi

  # Escribimos el watcher (si existe, lo reemplazamos para mantenerlo actualizado)
  mkdir -p /data/adb/service.d >/dev/null 2>&1
  cat > "$WATCHER" <<'EOF'
#!/system/bin/sh
# Ejecuta rollback OEM cuando el módulo se elimina (1 sola vez).
LOG="/data/local/tmp/orf_k706_root.log"
PERSIST_DIR="/data/adb/orf_k706_root"
DONE_FLAG="$PERSIST_DIR/rollback_done"
OEM_APK="$PERSIST_DIR/oem/Radio_vnull.apk"
PKG_FM_OEM="com.android.fmradio.ext"
PKG_ORF="com.example.openradiofm"

logf() {
  ts="$(date +%F_%T 2>/dev/null)"
  echo "${ts} [watcher] $1" >> "$LOG" 2>/dev/null
}

if [ -f "$DONE_FLAG" ]; then
  exit 0
fi

# Si el módulo sigue instalado, no hacemos nada.
if [ -d "/data/adb/modules/openradiofm_k706_root" ] || [ -d "/data/adb/modules_update/openradiofm_k706_root" ]; then
  exit 0
fi

logf "Modulo no presente. Ejecutando rollback OEM..."

am force-stop "$PKG_ORF" >/dev/null 2>&1 || true
pm uninstall --user 0 "$PKG_ORF" >/dev/null 2>&1 || true
pm uninstall "$PKG_ORF" >/dev/null 2>&1 || true
rm -rf /data/data/"$PKG_ORF" /data/user/0/"$PKG_ORF" /data/user_de/0/"$PKG_ORF" >/dev/null 2>&1 || true

am force-stop "$PKG_FM_OEM" >/dev/null 2>&1 || true
pm uninstall --user 0 "$PKG_FM_OEM" >/dev/null 2>&1 || true
pm uninstall "$PKG_FM_OEM" >/dev/null 2>&1 || true
rm -rf /data/data/"$PKG_FM_OEM" /data/user/0/"$PKG_FM_OEM" /data/user_de/0/"$PKG_FM_OEM" >/dev/null 2>&1 || true
rm -rf /data/app/"$PKG_FM_OEM"-* /data/app/*"$PKG_FM_OEM"* >/dev/null 2>&1 || true

if [ -f "$OEM_APK" ]; then
  pm install -r -d --user 0 "$OEM_APK" >/dev/null 2>&1
  logf "pm install OEM rc=$?"
else
  logf "OEM_APK no existe en $OEM_APK"
fi

cmd package install-existing --user 0 "$PKG_FM_OEM" >/dev/null 2>&1 || pm install-existing --user 0 "$PKG_FM_OEM" >/dev/null 2>&1 || true
pm enable --user 0 "$PKG_FM_OEM" >/dev/null 2>&1 || true

mkdir -p "$PERSIST_DIR" >/dev/null 2>&1 || true
echo "done $(date +%F_%T 2>/dev/null)" > "$DONE_FLAG" 2>/dev/null
logf "Rollback DONE. Deshabilitando watcher."

# Autolimpieza del watcher (best-effort)
rm -f "$0" >/dev/null 2>&1 || true
EOF
  chmod 0755 "$WATCHER" >/dev/null 2>&1
}

set_permissions() {
  set_perm_recursive "$MODPATH" 0 0 0755 0644
  set_perm "$MODPATH/service.sh" 0 0 0755
  set_perm "$MODPATH/uninstall.sh" 0 0 0755
  # Algunos entornos Magisk/ZIP dejan service.sh como 0644; asegurar ejecutable.
  chmod 0755 "$MODPATH/service.sh" 2>/dev/null
  chmod 0755 "$MODPATH/uninstall.sh" 2>/dev/null
}
