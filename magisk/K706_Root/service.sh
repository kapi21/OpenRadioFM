#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta en boot (late_start service). (LF normalizado)

MODDIR="${0%/*}"
# Algunos entornos/llamadas pueden envolver $0 en comillas; saneamos para evitar rutas '"...".
MODDIR="${MODDIR#\"}"
MODDIR="${MODDIR%\"}"

PKG_FM_OEM="com.android.fmradio.ext"
PKG_ORF="com.example.openradiofm"
OEM_FLAG="${MODDIR}/orf_oem_mode"

log_print() {
  /system/bin/log -t ORF_K706_ROOT "$1" 2>/dev/null
  echo "[ORF_K706_ROOT] $1"
}

# Log adicional a archivo para depurar flows de uninstall/remove (logcat a veces no es suficiente)
LOGFILE="/data/local/tmp/orf_k706_root.log"
log_file() {
  ts="$(date +%F_%T 2>/dev/null)"
  echo "${ts} $1" >> "$LOGFILE" 2>/dev/null
}

ensure_rollback_watcher() {
  # Asegura que exista:
  # - Copia persistente del APK OEM
  # - watcher /data/adb/service.d que corre incluso si el módulo se borra
  PERSIST_DIR="/data/adb/orf_k706_root"
  PERSIST_OEM="$PERSIST_DIR/oem/Radio_vnull.apk"
  WATCHER="/data/adb/service.d/99-orf-k706-root-rollback.sh"

  mkdir -p "$PERSIST_DIR/oem" >/dev/null 2>&1 || true
  mkdir -p /data/adb/service.d >/dev/null 2>&1 || true

  if [ -f "${MODDIR}/oem/Radio_vnull.apk" ]; then
    if [ ! -f "$PERSIST_OEM" ]; then
      cp "${MODDIR}/oem/Radio_vnull.apk" "$PERSIST_OEM" >/dev/null 2>&1 || true
      chmod 0644 "$PERSIST_OEM" >/dev/null 2>&1 || true
      log_file "Persist OEM APK copied to $PERSIST_OEM"
    fi
  else
    log_file "WARN: MODDIR/oem/Radio_vnull.apk missing; cannot seed persistent OEM"
  fi

  # Re-escribir SIEMPRE el watcher para que se actualice la lógica.
  cat > "$WATCHER" <<'EOF'
#!/system/bin/sh
LOG="/data/local/tmp/orf_k706_root.log"
PERSIST_DIR="/data/adb/orf_k706_root"
DONE_FLAG="$PERSIST_DIR/rollback_done"
OEM_APK="$PERSIST_DIR/oem/Radio_vnull.apk"
PKG_FM_OEM="com.android.fmradio.ext"
PKG_ORF="com.example.openradiofm"
MODID="openradiofm_k706_root"

MOD_DIR="/data/adb/modules/$MODID"
MOD_UPD="/data/adb/modules_update/$MODID"
REMOVE_FLAG1="$MOD_DIR/remove"
REMOVE_FLAG2="$MOD_UPD/remove"

logf() {
  ts="$(date +%F_%T 2>/dev/null)"
  echo "${ts} [watcher] $1" >> "$LOG" 2>/dev/null
}

wait_pm_ready() {
  i=0
  while [ $i -lt 120 ]; do
    out="$(pm path android 2>&1)"
    case "$out" in
      *"Can't find service:"*)
        ;;
      package:*)
        logf "pm ready after ${i}s"
        return 0
        ;;
    esac
    i=$((i + 1))
    sleep 1
  done
  logf "ERROR: pm not ready after 120s (last=$out)"
  return 1
}

wait_boot_completed() {
  i=0
  while [ $i -lt 180 ]; do
    bc="$(getprop sys.boot_completed 2>/dev/null)"
    [ "$bc" = "1" ] && { logf "boot_completed after ${i}s"; return 0; }
    i=$((i + 1))
    sleep 1
  done
  logf "ERROR: sys.boot_completed not reached after 180s"
  return 1
}

logf "boot: watcher running"

if [ -f "$DONE_FLAG" ]; then
  # No confiar ciegamente en un DONE viejo: si la Radio OEM no está instalada, reintentar.
  out="$(pm path "$PKG_FM_OEM" 2>&1)"
  case "$out" in
    package:*)
      logf "DONE flag present + OEM present; exiting"
      exit 0
      ;;
    *"Can't find service:"*)
      # esperamos servicios y luego re-evaluamos
      ;;
    *)
      logf "DONE flag present but OEM missing; removing DONE and retrying"
      rm -f "$DONE_FLAG" >/dev/null 2>&1 || true
      ;;
  esac
fi

# Disparar rollback cuando Magisk marca "Remove" (flag remove) aunque la carpeta siga existiendo.
if [ -f "$REMOVE_FLAG1" ] || [ -f "$REMOVE_FLAG2" ]; then
  logf "remove flag detected; running rollback"
elif [ -d "$MOD_DIR" ] || [ -d "$MOD_UPD" ]; then
  logf "module present; no action"
  exit 0
else
  logf "module absent; running rollback"
fi

wait_pm_ready || exit 20
wait_boot_completed || exit 21

out="$(am force-stop "$PKG_ORF" 2>&1)" || true
[ -n "$out" ] && logf "am force-stop ORF: $out"
out="$(pm uninstall --user 0 "$PKG_ORF" 2>&1)" ; rc=$?
logf "pm uninstall --user 0 ORF rc=$rc out=$out"
out="$(pm uninstall "$PKG_ORF" 2>&1)" ; rc=$?
logf "pm uninstall (global) ORF rc=$rc out=$out"
rm -rf /data/data/"$PKG_ORF" /data/user/0/"$PKG_ORF" /data/user_de/0/"$PKG_ORF" >/dev/null 2>&1 || true

out="$(am force-stop "$PKG_FM_OEM" 2>&1)" || true
[ -n "$out" ] && logf "am force-stop OEM: $out"
out="$(pm uninstall --user 0 "$PKG_FM_OEM" 2>&1)" ; rc=$?
logf "pm uninstall --user 0 OEM rc=$rc out=$out"
out="$(pm uninstall "$PKG_FM_OEM" 2>&1)" ; rc=$?
logf "pm uninstall (global) OEM rc=$rc out=$out"
rm -rf /data/data/"$PKG_FM_OEM" /data/user/0/"$PKG_FM_OEM" /data/user_de/0/"$PKG_FM_OEM" >/dev/null 2>&1 || true
rm -rf /data/app/"$PKG_FM_OEM"-* /data/app/*"$PKG_FM_OEM"* >/dev/null 2>&1 || true

out="$(cmd package install-existing --user 0 "$PKG_FM_OEM" 2>&1)" ; rc=$?
logf "cmd install-existing rc=$rc out=$out"
EXISTING_OUT="$out"
out="$(pm install-existing --user 0 "$PKG_FM_OEM" 2>&1)" ; rc2=$?
logf "pm install-existing rc=$rc2 out=$out"
out="$(pm enable --user 0 "$PKG_FM_OEM" 2>&1)" ; rc3=$?
logf "pm enable rc=$rc3 out=$out"

mkdir -p "$PERSIST_DIR" >/dev/null 2>&1 || true
# Solo marcar DONE si vemos Success en install o si pm path ya devuelve package:...
OK=0
echo "$EXISTING_OUT" | grep -qi "Success" && OK=1
pp="$(pm path "$PKG_FM_OEM" 2>&1)"
case "$pp" in package:*) OK=1 ;; esac

if [ $OK -eq 1 ]; then
  echo "done $(date +%F_%T 2>/dev/null)" > "$DONE_FLAG" 2>/dev/null
  logf "rollback DONE (verified)"
  rm -f "$0" >/dev/null 2>&1 || true
else
  logf "rollback NOT done (pm still failing). Will retry next boot."
fi
EOF
  chmod 0755 "$WATCHER" >/dev/null 2>&1 || true
  log_file "Watcher ensured at $WATCHER"
}

# Si el usuario pulsó "Remove" en Magisk, se crea el flag "remove" dentro del módulo.
# En algunas ROMs/builds, el uninstall.sh no se ejecuta de forma fiable; lo forzamos aquí.
REMOVE_FLAG="${MODDIR}/remove"
if [ -f "${REMOVE_FLAG}" ]; then
  log_print "Remove flag detectado (${REMOVE_FLAG}). Ejecutando uninstall.sh en boot..."
  log_file "REMOVE flag detectado en ${REMOVE_FLAG}. MODDIR=${MODDIR}"
  if [ -f "${MODDIR}/uninstall.sh" ]; then
    sh "${MODDIR}/uninstall.sh" >>"$LOGFILE" 2>&1
    RC=$?
    log_file "uninstall.sh ejecutado rc=${RC}"
    [ $RC -eq 0 ] || log_print "WARN: uninstall.sh devolvió non-zero"
  else
    log_print "ERROR: uninstall.sh no existe en MODDIR"
    log_file "ERROR: uninstall.sh no existe en MODDIR"
  fi
  log_print "Remove flow: fin (no aplico parches normales)."
  log_file "Remove flow FIN"
  exit 0
fi

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

restore_all_radio_shortcut_prefs() {
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
    log_print "Restoring launcher/theme after OEM mode: $pkg"
    am force-stop "$pkg" >/dev/null 2>&1
  done
}

log_print "service.sh start: stub QF_FMRadioExt + launcher prefs (${PKG_FM_OEM})"
log_file "service.sh start MODDIR=${MODDIR}"
ensure_rollback_watcher

i=0
while [ $i -lt 30 ]; do
  pm path android >/dev/null 2>&1 && break
  i=$((i + 1))
  sleep 1
done

# Asegurar que el bind-mount de modo OEM/OpenRadio se aplique incluso si post-fs-data
# no se ejecuta en este entorno (algunas ROMs/Magisk builds).
if [ -f "${MODDIR}/post-fs-data.sh" ]; then
  log_print "Running post-fs-data.sh from service.sh (mode/perm patch)"
  sh "${MODDIR}/post-fs-data.sh" >/dev/null 2>&1 || log_print "WARN: post-fs-data.sh returned non-zero"
else
  log_print "WARN: post-fs-data.sh no existe en MODDIR"
fi

APK_SYS="/system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk"

# A prueba de reinicios / limpiezas:
# - Si el sistema no tiene instalado el paquete (pm path vacío), lo reinstalamos desde el APK
#   montado por Magisk en /system/priv-app/...
# - Luego lo habilitamos (si está disabled, el launcher no resuelve el ComponentName explícito).
if pm path "${PKG_FM_OEM}" >/dev/null 2>&1; then
  log_print "Package ${PKG_FM_OEM} presente."
else
  log_print "Package ${PKG_FM_OEM} NO presente. Instalando desde ${APK_SYS} ..."
  if [ -f "${APK_SYS}" ]; then
    pm install -r "${APK_SYS}" >/dev/null 2>&1 || pm install "${APK_SYS}" >/dev/null 2>&1
  else
    log_print "ERROR: No existe ${APK_SYS} (overlay no aplicado)."
  fi
fi

pm enable --user 0 "${PKG_FM_OEM}" >/dev/null 2>&1 || true

# OpenRadioFM como priv-app (systemless):
# Si existe instalado como app de usuario (/data/app), el launcher lo tratará como third-party.
# Para forzar flags de sistema, quitamos la instalación de usuario y dejamos que el APK montado
# en /system/priv-app/OpenRadioFM/ sea el que registre el PackageManager tras boot.
ORF_SYS_APK="/system/priv-app/OpenRadioFM/OpenRadioFM.apk"
ORF_PATH="$(pm path "${PKG_ORF}" 2>/dev/null | head -n 1)"
case "$ORF_PATH" in
  package:/data/app/*)
    log_print "OpenRadioFM detectado como user-app ($ORF_PATH). Desinstalando para usar priv-app..."
    pm uninstall --user 0 "${PKG_ORF}" >/dev/null 2>&1 || true
    pm uninstall "${PKG_ORF}" >/dev/null 2>&1 || true
    ;;
  package:/system/*|package:/product/*|package:/system_ext/*)
    log_print "OpenRadioFM ya es system app ($ORF_PATH)."
    ;;
  *)
    if [ -f "${ORF_SYS_APK}" ]; then
      log_print "OpenRadioFM no encontrado como paquete. Esperando a que PM lo registre (priv-app montada)."
    else
      log_print "WARN: No existe ${ORF_SYS_APK} (no se montó OpenRadioFM como priv-app)."
    fi
    ;;
esac

# Asegurar que el paquete está "instalado para el usuario 0".
# (Tras desinstalar el update de /data/app, puede quedar como installed=false para el usuario,
# aunque exista como priv-app en /system/priv-app).
if [ -f "${ORF_SYS_APK}" ]; then
  cmd package install-existing --user 0 "${PKG_ORF}" >/dev/null 2>&1 || pm install-existing --user 0 "${PKG_ORF}" >/dev/null 2>&1 || true
fi

log_print "Scanning shared_prefs for OEM RADIO shortcut targets -> OpenRadioFM"
if [ -f "$OEM_FLAG" ]; then
  log_print "OEM mode flag present: restoring RADIO shortcuts to OEM"
  restore_all_radio_shortcut_prefs
else
  patch_all_radio_shortcut_prefs
fi

# QuickFish multimedia widget: algunos launchers solo actualizan carátula/metadata con broadcasts OEM
# protegidos (ej. com.qf.musicplayer.action.UPDATE_ACTION). Este bucle se ejecuta como root (Magisk),
# así que evita "Permission Denial" que tendría la app.
start_qf_media_widget_bridge() {
  PIDFILE="${MODDIR}/qf_media_widget_bridge.pid"
  LOGFILE="${MODDIR}/qf_media_widget_bridge.log"
  LASTFREQFILE="${MODDIR}/qf_media_last_freq.txt"

  if [ -f "$PIDFILE" ]; then
    oldpid="$(cat "$PIDFILE" 2>/dev/null)"
    if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
      log_print "QF media widget bridge ya en marcha (pid=$oldpid)"
      return
    fi
  fi

  (
    set +e
    # Asegurar que el directorio existe y es escribible
    mkdir -p "$MODDIR" >/dev/null 2>&1 || true
    echo $$ > "$PIDFILE" 2>/dev/null
    log_print "QF media widget bridge start (pid=$$)"
    # Capturar stdout/stderr para depuración (si muere, veremos el motivo).
    exec >> "$LOGFILE" 2>&1
    echo "[bridge] start pid=$$"
    set -x

    LAST=""
    # Algunos launchers solo aplican cover/metadata si está corriendo el MediaService OEM (com.qf.musicplayer).
    # Lo levantamos aquí para que CustomMusicWidget pase musicServiceRunning=true.
    am startservice -n com.qf.musicplayer/com.carsyso.mediasdk.core.MediaService >/dev/null 2>&1 || true

    while true; do
      # Última usada: preferimos leer pref_last_freq desde shared_prefs.
      PREFS=""
      NAMES=""
      for base in /data/user_de/0/com.example.openradiofm /data/user/0/com.example.openradiofm /data/data/com.example.openradiofm; do
        [ -z "$PREFS" ] && [ -f "$base/shared_prefs/RadioPresets.xml" ] && PREFS="$base/shared_prefs/RadioPresets.xml"
        [ -z "$NAMES" ] && [ -f "$base/shared_prefs/RadioStationNames.xml" ] && NAMES="$base/shared_prefs/RadioStationNames.xml"
      done

      # Saneamos rutas por si llegan con comillas
      PREFS="${PREFS#\"}"; PREFS="${PREFS%\"}"
      NAMES="${NAMES#\"}"; NAMES="${NAMES%\"}"

      # Última usada: la app mantiene una lista recent_freqs "87600,89100,..." (kHz).
      RECENTS="$(toybox sed -n '/<string name=\"recent_freqs\">/ { s/.*<string name=\"recent_freqs\">//; s#</string>.*##; p; q }' "$PREFS" 2>/dev/null)"
      FREQ="$(echo "$RECENTS" | toybox cut -d',' -f1 | toybox tr -cd '0-9' | toybox head -c 10)"
      if [ -n "$FREQ" ]; then
        echo "$FREQ" > "$LASTFREQFILE" 2>/dev/null
      else
        # Fallback persistente si todavía no hay prefs en boot
        FREQ="$(cat "$LASTFREQFILE" 2>/dev/null | toybox tr -cd '0-9' | toybox head -c 10)"
      fi
      if [ -z "$FREQ" ]; then
        sleep 2
        continue
      fi

      TITLE_LINE="$(toybox grep -o "<string name=\\\"RDS_${FREQ}\\\">[^<]*" "$NAMES" 2>/dev/null | toybox head -n 1)"
      TITLE="$(echo "$TITLE_LINE" | toybox sed 's/.*>//')"
      [ -n "$TITLE" ] || TITLE="OpenRadioFM"

      # carátula por archivo (si existe)
      # Usar path canónico (algunos launchers no resuelven /sdcard). Si no existe, fallback a /sdcard.
      COVER="/storage/emulated/0/RadioLogos/${FREQ}.png"
      [ -f "$COVER" ] || COVER="/sdcard/RadioLogos/${FREQ}.png"
      if [ ! -f "$COVER" ]; then
        # opcional: buscar por nombre saneado si existe
        SAFE="$(echo "$TITLE" | toybox tr -cd '[:alnum:]' | toybox tr '[:lower:]' '[:upper:]')"
        if [ -n "$SAFE" ] && [ -f "/storage/emulated/0/RadioLogos/${FREQ}_${SAFE}.png" ]; then
          COVER="/storage/emulated/0/RadioLogos/${FREQ}_${SAFE}.png"
        elif [ -n "$SAFE" ] && [ -f "/sdcard/RadioLogos/${FREQ}_${SAFE}.png" ]; then
          COVER="/sdcard/RadioLogos/${FREQ}_${SAFE}.png"
        else
          # fallback: cualquier logo que empiece por la freq (primero que exista)
          ANY="$(ls "/storage/emulated/0/RadioLogos/${FREQ}_"*.png 2>/dev/null | toybox head -n 1)"
          [ -n "$ANY" ] || ANY="$(ls "/sdcard/RadioLogos/${FREQ}_"*.png 2>/dev/null | toybox head -n 1)"
          [ -n "$ANY" ] && COVER="$ANY"
        fi
      fi

      # evitar spam si no cambia nada
      CUR="${FREQ}|${TITLE}|${COVER}"
      if [ "$CUR" != "$LAST" ]; then
        LAST="$CUR"

        # am broadcast: no usar espacios en trackName para evitar que am interprete tokens como componente
        SAFE_TITLE="$(echo "$TITLE" | toybox tr '\n\r\t' '   ' | toybox tr ' ' '_' | toybox sed 's/__*/_/g')"
        # Subtítulo: frecuencia formateada tipo 89.1 FM (sin depender de awk)
        MHZ_I=$((FREQ / 1000))
        MHZ_D=$(((FREQ % 1000) / 100))
        SAFE_SUBTITLE="${MHZ_I}.${MHZ_D}_FM"

        # Extra names que suelen escuchar widgets OEM (igual que en la app)
        COMMON_ARGS="\
 --es packageName com.example.openradiofm --es pkg com.example.openradiofm --es app com.example.openradiofm \
 --es currentTrack com.example.openradiofm \
 --es trackName $SAFE_TITLE --es title $SAFE_TITLE --es name $SAFE_TITLE --es song $SAFE_TITLE \
 --es artistName OpenRadioFM --es artist OpenRadioFM \
 --es subTitle $SAFE_SUBTITLE --es subtitle $SAFE_SUBTITLE \
 --ez isPlaying true --ez playing true --ei playState 1 --ei state 1"

        ART_ARGS=""
        if [ -f "$COVER" ]; then
          ART_URI="file://${COVER}"
          ART_ARGS=" --es coverPath $COVER --es artPath $COVER --es picPath $COVER --es imagePath $COVER --es coverUri $ART_URI --es artUri $ART_URI --es picUri $ART_URI --es imageUri $ART_URI"
        fi

        # Hack K706: el widget parece reconstruir artwork desde el cache del Music SDK (com.qf.musicplayer),
        # ignorando coverPath en el broadcast. Reescribimos el "MUSIC_PLAY_MEDIA_OBJECT" para apuntar al logo.
        QF_PREF="/data/data/com.qf.musicplayer/shared_prefs/car_music_sdk_cache_file.xml"
        if [ -d /data/data/com.qf.musicplayer/shared_prefs ]; then
          # Mantener un backup una sola vez
          [ -f "${QF_PREF}.bak_orf" ] || cp "$QF_PREF" "${QF_PREF}.bak_orf" >/dev/null 2>&1 || true
          NOW_MS="$(date +%s)000"
          # mID=1 es el mismo id que usa el OEM sample_media; el widget/SDK lo tolera.
          OBJ="{&quot;collected&quot;:false,&quot;dataType&quot;:0,&quot;folder&quot;:false,&quot;folderName&quot;:&quot;OpenRadioFM&quot;,&quot;genre&quot;:&quot;Radio&quot;,&quot;isSameFile&quot;:false,&quot;mAlbum&quot;:&quot;OpenRadioFM&quot;,&quot;mAlbumID&quot;:-1,&quot;mArtist&quot;:&quot;OpenRadioFM&quot;,&quot;mDuration&quot;:-1,&quot;mFilePath&quot;:&quot;${COVER}&quot;,&quot;mFolderPath&quot;:&quot;/sdcard/RadioLogos&quot;,&quot;mID&quot;:1,&quot;mIsDelete&quot;:false,&quot;mIsPlayItem&quot;:false,&quot;mLastModified&quot;:${NOW_MS},&quot;mMediaID&quot;:-1,&quot;mMediaType&quot;:&quot;AUDIO&quot;,&quot;mName&quot;:&quot;${SAFE_TITLE}.png&quot;,&quot;mParseStatus&quot;:&quot;FINISHED&quot;,&quot;mPlaylistIndex&quot;:0,&quot;mPosition&quot;:0,&quot;mScanIndex&quot;:0,&quot;mSize&quot;:0,&quot;mTitle&quot;:&quot;${SAFE_TITLE}&quot;,&quot;mUpdateTime&quot;:${NOW_MS}}"
          cat > "$QF_PREF" <<EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="MUSIC_PLAY_MEDIA_PATH">${COVER}</string>
    <string name="current_function_tag">flash_list</string>
    <string name="MUSIC_PLAY_MEDIA_OBJECT">${OBJ}</string>
</map>
EOF
          chown system:system "$QF_PREF" >/dev/null 2>&1 || true
          chmod 0660 "$QF_PREF" >/dev/null 2>&1 || true
        fi

        # 3 acciones: algunos launchers solo reaccionan a una parte
        am startservice -n com.qf.musicplayer/com.carsyso.mediasdk.core.MediaService >/dev/null 2>&1 || true
        am broadcast -a com.qf.musicplayer.action.UPDATE_ACTION $COMMON_ARGS $ART_ARGS >/dev/null 2>&1
        am broadcast -a com.qf.action.UPDATE_MEDIA_INFO $COMMON_ARGS $ART_ARGS >/dev/null 2>&1
        am broadcast -a com.qf.action.UPDATE_MEDIA_STATE $COMMON_ARGS $ART_ARGS >/dev/null 2>&1

        echo "[bridge] sent freq=$FREQ title=$SAFE_TITLE cover=$( [ -f "$COVER" ] && echo 1 || echo 0 ) prefs=$( [ -n \"$PREFS\" ] && echo 1 || echo 0 )" >> "$LOGFILE" 2>/dev/null
      fi

      sleep 2
    done
  ) &
}

start_qf_media_widget_bridge

log_print "service.sh done"
