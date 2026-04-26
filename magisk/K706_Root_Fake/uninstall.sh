#!/system/bin/sh

ui_print() {
  echo -e "ui_print $1\nui_print" > /proc/self/fd/$OUTFD
}

ui_print "- OpenRadioFM K706 FAKE: desinstalado."

