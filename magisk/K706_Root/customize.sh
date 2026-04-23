#!/system/bin/sh

# Magisk 27 module install script (customize.sh)
# Se ejecuta durante la instalación del ZIP. (LF normalizado)

# false = montar system/ (APK trampolín QF_FMRadioExt). true = solo scripts (no overlay).
SKIPMOUNT=false
PROPFILE=true
POSTFSDATA=false
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
}

set_permissions() {
  set_perm_recursive "$MODPATH" 0 0 0755 0644
  set_perm "$MODPATH/service.sh" 0 0 0755
  set_perm "$MODPATH/uninstall.sh" 0 0 0755
  # Algunos entornos Magisk/ZIP dejan service.sh como 0644; asegurar ejecutable.
  chmod 0755 "$MODPATH/service.sh" 2>/dev/null
  chmod 0755 "$MODPATH/uninstall.sh" 2>/dev/null
}
