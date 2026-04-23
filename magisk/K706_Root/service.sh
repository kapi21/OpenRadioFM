#!/system/bin/sh
# OpenRadioFM — K706 Root Hijack (Magisk)
# Se ejecuta en boot (late_start service). (LF normalizado)

MODDIR="${0%/*}"
# Algunos entornos/llamadas pueden envolver $0 en comillas; saneamos para evitar rutas '"...".
MODDIR="${MODDIR#\"}"
MODDIR="${MODDIR%\"}"

PKG_FM_OEM="com.android.fmradio.ext"
PKG_ORF="com.example.openradiofm"

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
patch_all_radio_shortcut_prefs

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
