#!/system/bin/sh

# Magisk module install script (FAKE TEST)
SKIPMOUNT=true
PROPFILE=true
POSTFSDATA=false
LATESTARTSERVICE=true

print_modname() {
  ui_print " "
  ui_print "***************************************"
  ui_print " OpenRadioFM — K706 Root (FAKE TEST)   "
  ui_print "***************************************"
  ui_print " "
}

on_install() {
  ui_print "- Instalando modulo FAKE (solo scripts)..."
  unzip -o "$ZIPFILE" -x 'META-INF/*' -d "$MODPATH" >&2
  sed -i 's/\r$//' "$MODPATH/service.sh" 2>/dev/null
}

set_permissions() {
  set_perm_recursive "$MODPATH" 0 0 0755 0644
  set_perm "$MODPATH/service.sh" 0 0 0755
  chmod 0755 "$MODPATH/service.sh" 2>/dev/null
}

