#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (FAKE TEST)
# late_start service

MODDIR="${0%/*}"
MODDIR="${MODDIR#\"}"
MODDIR="${MODDIR%\"}"

LOGFILE="/data/local/tmp/orf_k706_fake_magisk.log"

log_print() {
  /system/bin/log -t ORF_K706_FAKE "$1" 2>/dev/null
  echo "[ORF_K706_FAKE] $1" >> "$LOGFILE" 2>/dev/null
}

log_print "FAKE module running. MODDIR=$MODDIR"
log_print "If you see this after reboot, Magisk installed and executed the module OK."

exit 0

