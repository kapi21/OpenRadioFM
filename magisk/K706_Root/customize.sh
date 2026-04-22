#!/system/bin/sh

# Magisk 27 module install script (customize.sh)
# Se ejecuta durante la instalación del ZIP. (LF normalizado)

SKIPMOUNT=true
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
  unzip -o "$ZIPFILE" 'module.prop' -d "$MODPATH" >/dev/null 2>&1
  unzip -o "$ZIPFILE" 'service.sh' -d "$MODPATH" >/dev/null 2>&1
  unzip -o "$ZIPFILE" 'uninstall.sh' -d "$MODPATH" >/dev/null 2>&1
}

set_permissions() {
  set_perm_recursive "$MODPATH" 0 0 0755 0644
  set_perm "$MODPATH/service.sh" 0 0 0755
  set_perm "$MODPATH/uninstall.sh" 0 0 0755
}

