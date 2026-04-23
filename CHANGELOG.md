## [Unreleased] - MCU2

### K706 Root Edition (rama `K706_Root`, abril 2026)
- **Magisk (`magisk/K706_Root/`)**: **overlay** `system/priv-app/QF_FMRadioExt/QF_FMRadioExt.apk` con APK trampolín (`:stub-fmradio`: `FmMainActivity` → OpenRadioFM, `FmService` stub para `IFmRadioService`); `service.sh` hace **`pm enable`** del paquete (necesario para el `ComponentName` explícito del launcher). Sigue el **parche** de XML en `shared_prefs`; `customize.sh` usa `SKIPMOUNT=false`; `build_k706_root_zip.bat` compila el stub y empaqueta el ZIP.
- **Magisk — build Windows**: `magisk/build_k706_root_zip.bat` fuerza **LF** en scripts antes del ZIP; `.gitattributes` fuerza `eol=lf` bajo `magisk/`.
- **K706 / widget OEM**: `com.qf.radio.update_action` también se envía al **paquete HOME** resuelto (p. ej. `com.android.launcher.gradient.black`), además de `movablecell` y `com.android.auto.autohome` (`K706Engine`, `WidgetBroadcastManager`). **Fix:** si un destino lanza `SecurityException`, ya no se cancela el resto del bucle (antes el widget podía quedarse en `radioStartup=false`).
- **`LauncherIntentUtils`**: movido a `com.example.openradiofm.util` para uso desde el motor.
- **UI logos**: evitar crash Glide en fallo de carga (p. ej. sintonía problemática): fallback de logo en `Handler.post` fuera del callback de `RequestListener`.
- **Documentación**: `HANDOFF_K706_ROOT.md` (handoff, roadmap corto, ADB). Ver también `K706_ROOT_CHECKLIST.md` y `magisk/K706_Root/README_K706_ROOT_MAGISK.md`.
- **Pendiente (abr. 2026):** error al **instalar** el módulo en unidad de prueba (Magisk App y/o flasher); seguir con ZIP de `build_k706_root_zip.bat`, vía ADB `magisk --install-module` y diagnóstico en `HANDOFF_K706_ROOT.md` (*Para mañana*). Completar **Nivel A** (asistente root en app) según checklist.

### FYT / Teyes — motor OEM por intents (abril 2026)
- **`FYTOemEngine`**: nuevo motor **FYT/OEM** (paquete `com.syu.radio`) sin root ni AIDL: `tune` por deep‑link `radio://tune?freq=…` y **Prev/Next** vía `startService` a `com.syu.broadcast.MyService` (`com.syu.radio.prevservice/nextservice`). Incluye autodetección en `RadioServiceController` (usa `sys.fyt.platform` cuando está disponible).

### QS6 / NWD — arranque, rebind y estabilidad (abril 2026)
- **`QS6Engine`**: *warm-up rebind* tras reboot: si en ~2,2 s no llega RX (callbacks/settings) se reintenta `connect()` y se fuerza un `pollNwdSettingsAndFire()` para enganchar estado sin abrir la radio OEM.
- **`NWDTunerAdapter`**: reconexión AIDL con **backoff**, *warm-up* `startService()` (sin UI), y `linkToDeath` para re-bind automático si muere el binder o falla `registCallback`.

### Launchers / MediaSession — metadata inicial (abril 2026)
- **`RadioMediaService`**: publicación de **metadata inicial** diferida al arrancar (evita sesión visible pero sin datos en launchers tipo Agama) y ampliación de paquetes con permiso de lectura de artwork para launchers de coche comunes.

### UI — seek/scan “fluido” y edición de nombre (abril 2026)
- **`MainActivity` / `ScanManager`**: *ticker* de frecuencia “optimista” durante `seekUp/seekDown` y mientras el escaneo está activo; se corta automáticamente al llegar la frecuencia real o al finalizar el scan.
- **`DialogManager`**: nuevo diálogo **Editar nombre de emisora** (guardar/restaurar original) con refresco inmediato de UI/presets y limpieza de caché de logos.

### Rendimiento de arranque (abril 2026)
- **`MainActivityBootstrap`**: diferir operaciones costosas (fuentes, icon pack, `MediaSessionManager.connect`, registro de media receiver y fase final de bootstrap) al siguiente tick de UI para reducir “Skipped frames” en head units.

## [5.2.0] - 2026-04-19

### Estabilización Crítica y Restauración de Motores Legados
- **QS6 (Nowada)**: Implementación de **Modo Master** (sincronización directa con `Settings.System`), cierre limpio del canal de audio y **fix de muteo estabilizado** mediante conmutación de fuentes.
- **MT8163 (HCN)**: Revertido a la implementación original estable (HCN native AIDL). Solucionados problemas de regresión en sintonía, RDS y mutes espurios.
- **MTK8259/8667 (Topway)**: Restaurado sistema de doble vínculo (`ITsCommon` + `ITsSpeechRadio`) para total compatibilidad con MainUI.apk.
- **Jancar (8227L)**: Revertido al motor original basado en el servicio nativo de Jancar.
- **Mute Legado (MT8163)**: Restaurada lógica de audio v18.6 (mantenimiento de `fm_radio_on=1`) para evitar ruidos de conmutación.
- **Paridad RadioEngine**: Implementado `setBand(int)` en todos los motores restaurados para compatibilidad con la interfaz unificada v5.x.
- **Correcciones de Compilación**: Resueltos 5 fallos críticos de símbolos y métodos abstractos derivados de la restauración de código antiguo.

### QS6 / NWD — RDS, espejo System y bucles OEM (abril 2026)
- **Operativa**: si RDS o el estado del sintonizador queda desalineado respecto al firmware NWD, **workaround verificado en unidad**: usar los **ajustes de la radio del dispositivo** (OEM) para activar o corregir RDS u opciones equivalentes; la app y `KernelService` siguen siendo la vía preferida en desarrollo. Documentado en `README.md` (*Problemas conocidos*) y `QS6_MCU_KERNELSERVICE_INFORME.md`.
- **`QS6Engine`**: en **AutoScan lento** se prioriza **AIDL** para `tune`/`seek` (`setAutoScanOemPreferred`, enganchado desde `ScanManager`); fuera de AutoScan se mantiene **MCU primero** donde aplica. Limpieza de **RT** al cambiar frecuencia o banda por callback AIDL. Espejo opcional a `Settings.System` con `canWrite` (M+) y **un solo log** si no hay permiso de escritura.
- **`MainActivity`**: **coalescencia ~280 ms** del trabajo pesado de `handleFrequencyChange` para amortiguar ráfagas de callbacks/broadcasts (freq inestable, PS pegado).
- **`WidgetBroadcastManager`**: **coalescencia ~320 ms** en `sendUpdate` ante reinyecciones rápidas del launcher/HAL.
- **`StatusRefreshCoordinator`**: **reset RDS**, logo y **MHz en UI** antes de encolar `getStationInfo` en el executor (evita carrera que repintaba el PS previo al zapping).
- **`RadioRepository`**: **`pickBestSupabaseRow`** cuando la API devuelve varias filas (`ps_name` ilike); **fallback** por frecuencia+país si no hubo datos tras custom/PI/ps_name; parseo de frecuencia en filas Supabase (MHz vs kHz).
- **`MT8163Engine`**: reconexión HCN tras streaming vía **`ACTION_MT8163_FM_HANDOFF`** y bind retardado (**~550 ms**), con espera si la ventana OEM bloquea el bind; evita doble ruta con `MainActivity` y `forceStop` en algunas ROM.
- **`MainActivity` / MT8163**: eliminado `mHcnPostStreamReconnectRunnable`; la ventana post-streaming la coordina el motor.
- **`AndroidManifest`**: `WRITE_SETTINGS` (opcional, con `tools:ignore`) para el espejo QS6 en `Settings.System`.

### AutoScan por sobrescritura (abril 2026)
- **Siempre FM1**: antes de borrar memorias 1–18 se alinea hardware y `MainActivity.mCurrentBand` a FM1 (QS6 `tuneWithBand(87,5 MHz, 0)`; K706/MT8163/MTK8259 `bandCycle` hasta FM1 + `tune`). Textos `autoscan_confirm_message` y `toast_autoscan_slow` actualizados en todos los idiomas.
- **Ritmo**: tiempos de validación RDS, intervalo entre seeks, hold de guardado en preset y paso tras emisora aceptada **más pausados** (sin volver al barrido extremadamente lento inicial de 11 s / 8,5 s).
- **Botón AutoScan**: sin animación de rotación; solo **tinte verde** en activo. Sincronización de `MainActivity.mIsScanning` con el estado efectivo en `applyEngineScanState`; durante el barrido lento el flag OEM incoherente no corta el estado “escaneando”; tras el fin, **latch** que ignora `onScanStatusChanged(true)` espurio (hasta nuevo autoscan o **~45 s**).
- **Guardado**: tras aceptar emisora se puede reprogramar el siguiente seek antes del intervalo largo.

### MT8163 / HCN — mute e init (abril 2026)
- **`mRadioMuteDesired`**: evita forzar ruta FM / `setMute(false)` en recuperación AIDL y en `deferredBinderRecovery` cuando el usuario mantiene la radio silenciada (p. ej. tras inyección OEM).
- **`mInitCompleted`**: evita doble `init` cuando `MainActivity` reentra con freq 0 tras `onServiceConnected`.
- **Motor / `HiddenRadioPlayer`**: ampliaciones de control y coherencia con preferencia modo directo RadioPlayer; documentación mute HAL vs mux OEM.
- **Menú ingeniería**: ajustes asociados (layout/strings) al modo experimental MT8163.

### MT8163 / HCN — Agama launcher y MediaSession (abril 2026)
- **Motor compartido con `RadioMediaService`**: tras el bind HCN en `MainActivity`, el `MT8163Engine` se registra en `RadioServiceController`; el servicio llama a `start()` solo para reinyectar `onEngineReady` **sin** un segundo `bindService` a `com.hcn.autoradio` (evita force-stop en algunas ROM).
- **Controles externos**: seek / preset desde launchers tipo **Agama** (y sesión de medios) reciben `mEngine` y dejan de quedar en silencio funcional.
- **`MT8163Engine.getCallback()`**: composición de callback con la UI (mismo patrón que K706/QS6).
- **`release(false)`**: `clearSharedLocalEngineIfSame` al soltar el motor.
- **Limitación**: sin haber abierto la app al menos una vez en la sesión no hay motor compartido; el widget/sesión no opera hasta ese bind inicial (por diseño).

### UI / ST, layouts y playback (abril 2026)
- **`SignalMeterCoordinator`**: eliminado el color dinámico del icono **ST** por SNR (sombras/tinte); el ST sigue la lógica de skin / `MainActivity.refreshStereoIndicatorUi` y layouts estándar/simple/V3.
- **`PlaybackManager`** y **`StatusRefreshCoordinator`**: refinamiento de mute/recuperación y refresco de estado acorde a MT8163 y uso en cabecera.
- Coordinadores de tema/layout (**`DayModeManager`**, **`NightModeManager`**, **`StandardLayoutManager`**, etc.): alineación con el indicador ST y botones de control.

### K706 / Android Auto + Spotify + diagnóstico (Z-Link; seguimiento pendiente)
- **`RadioActivityFileLogger`**: log a fichero estable (`commit` del nombre y del flag; sin carrera `apply`); **heartbeat `TICK`** periódico con estado FM/foco/UI; opción **volcar buffer `logcat`** desde ingeniería K706 (`logcat_dump_*.txt` en `RadioLogos/`). `LifecycleCoordinator` informa `uiResumed` al logger.
- **Menús de ingeniería** (K706 / genérico / QS6): el toggle de log no duplica `putBoolean`+`apply`; solo `onToggleChanged` persiste. **K706**: botón volcado logcat + string i18n `eng_dev_logcat_dump_button`.
- **`K706RadioManager`**: rama **`LOSS_TRANSIENT`** sin tratar voz AA como llamada GSM (`VOICE_SESSION_AA_OR_TTS`); reflexión playback / logs `APLAY_*` / `AUDIO_FOCUS`; **weak ref** para línea `TICK` en fichero.
- **Carreras foco vs `getCurrentFreq`**: en **`AUDIOFOCUS_LOSS`** real, **`mIsAudioFocusHeld`** y **`mAllowImplicitFmRecoverFromPoll`** se ponen a **false al inicio** del `try` (antes de MCU), para que hilos Binder no ejecuten “secuestro de canal” con foco aún “held”.
- **Spotify / AA**: con canal **4** y **`isMusicActive()`** o competencia de mux, no forzar FM; sincronizar foco lógico si hace falta. **`getCurrentFreq`** vuelve a llamar **`checkAndRecoverAudio`** con **`mUserWantsFmAudio`** para poder retomar FM cuando cesa la media externa (sin depender solo de `AUDIOFOCUS_GAIN`). Heartbeat con **`enforceAudioChannelRecovery()`** en lugar de solo `SetChannel(2)`.
- **`K706Engine.switchToFmAudio`**: delega en **`requestPlayAudio()`** para limpiar flags y secuencia completa al volver desde la UI.
- **Pendiente (anotado para más adelante)**: acercar el comportamiento al **ducking tipo QS6** (notificaciones AA sin `LOSS` permanente brusco); validar en unidad K706+Z-Link con logs `RadioLogos` (`implRec`, `TICK`, `AUDIO_FOCUS`). **MT8163/QS6** siguen con arquitectura distinta (HAL/AIDL vs mux MCU); no unificar motores.

### K706 / Android Auto y voz de navegación (Zlink)
- **`K706RadioManager`**: el “Glitch Protect” que relanzaba FM tras `AUDIOFOCUS_LOSS` / `LOSS_TRANSIENT` **no** se aplica si `AudioManager.isMusicActive()` (voz de Maps u otra app); imita la radio OEM (ceder mux). Los reintentos de **`mAutoRecoveryRunnable`** llaman `requestAudioFocus(false)` para **no** alargar la ventana anti-LOSS de 2,5 s (dejaba la app peleando con la guía).
- **`RadioMediaService`**: ante **`EVENT_LOSS_TRANSIENT`** del broadcast OEM ya **no** se llama `refreshSteeringMediaSessionAndForeground()` (evitaba volver a PLAYING+FGS y a pedir foco mientras suena la navegación).

### K706 / motor compartido y escaneo (UI)
- **`CompositeRadioEngineCallback`**: reenvío de **`onHwAutomationEvent`** a ambos receptores (eventos de hardware 122–125 ya no se pierden con motor compartido).
- **Escaneo selectivo (K706)**: al cerrar el diálogo se restaura el **callback previo** (p. ej. `Composite` UI + `RadioMediaService`), no solo `EngineCallbackCoordinator`.
- **AutoScan**: ver bloque *AutoScan por sobrescritura* arriba (FM1, latch OEM, botón sin rotación).

### K706 / Media (independencia y auditoría)
- **Recuperación FM sin micro-corte (K706)**: si `RPC_GetChannel` ya devuelve **FM (2)**, `enforceAudioChannelRecovery()` evita el ritual completo que empezaba con `setMute(true)` (colisión con `PlaybackManager.setMute(false)` → `enforceAudioRecovery`). En `startFmAudioSequence(fast)` se omite el pre-mute inicial cuando el canal ya es 2. El cambio real **4→2** sigue usando la secuencia completa cuando hace falta.
- **Pendiente / futura revisión (audio OEM)**: si tras BT/stack QF aún se nota un clic al **mux 4→2** con el heartbeat, valorar **debounce** o **ventana de gracia** tras `abandonCustomAudioFocus` antes de forzar `SetChannel(2)` (riesgo: retrasar recuperación; medir en carretera).
- **RDS RT end-to-end (K706)**: compatibilidad con RadioText desde **`0xB7`** (RT) y heurística para firmwares donde RT llega como **`0xB3`**; limpieza de texto más robusta para evitar RT vacío por filtrado.
- **ACC (K706)**: `0x24` tratado como **ACC** y expuesto como evento `125` (pipeline Engine → UI/Servicio). Se incluye en el estado compartido (`RadioSessionState.accOn`).
- **Estado único (UI + Service)**: `RadioMediaService` y `MainActivity` comparten el mismo `RadioSessionController` para evitar estados divergentes.
- **MediaSession**: el servicio es la **fuente de verdad** de metadata/Now Playing; se ignoran updates externos vía `ACTION_UPDATE_METADATA`.
- **Widget OEM QuickFish (K706)**:
  - Recepción de acciones del widget del launcher (`/customize/radio/*`) mediante `OemRadioWidgetReceiver` (prev/next/seek/mute), reenrutadas a `RadioMediaService`.
  - Endurecimiento frente a ROMs que bloquean `com.qf.radio.update_action`: si el broadcast falla por `SecurityException`, se **deshabilitan reintentos** para evitar spam/binder flood.
 - **Widget genérico de música (K706 / Now Playing)**:
   - **PowerOff / Atrás = minimizar** (sin `finish()`), evitando que el launcher ejecute `forceStopPackage()` sobre OpenRadioFM.
   - Al minimizar: **mute + abandono de AudioFocus** (sin cerrar HAL) para “cerrar” el sonido de FM en background.
   - Se mantiene **`MediaSession`/FGS** activo en `RadioMediaService` para que el widget de música siga controlando la app.

### QS6 / NWD — KernelService y menú de ingeniería (sustituir radio OEM)
- **`Qs6KernelMcuClient`**: cliente experimental hacia `com.nwd.kernel.service.KernelService` / `IKernelFeature.request([B)` (Binder `transact`), para enviar tramas FM al MCU sin abrir la app nativa.
- **Menú QS6 (Technical Matrix)**: botonera ampliada — tune, paso fino vs salto de emisora (mapeo OEM `seek`/`search`), banda, AMS, intro, near DX/LOC, RDS/PTY de laboratorio, **SAVE_P1..P16** vía `dataType=0x0C`, y pruebas AIDL (`RadioFeature`: stereo, prefeb, intro, refresh de estado, `STOP_SEARCH` por broadcast).
- **Sesión Kernel**: el bind al `KernelService` se mantiene entre cierres del diálogo de desarrollo; se libera al destruir `MainActivity` en modo QS6 (`QS6EngineeringDialog.releaseSharedKernelClient()`).
- **Documentación**: `QS6_MCU_KERNELSERVICE_INFORME.md` (protocolo, validación en hardware,riesgos RX).
- **`NWDTunerAdapter`**: métodos auxiliares para ingeniería (`setStereoOn`, `prefeb`, `saveCurrentFrequency`, `getDebugStatus`).
- **QS6Engine “MCU-first” (híbrido)**: preferencia por órdenes vía `KernelService` (tune/seek/band/dx-local/stereo) con fallback a AIDL; RX complementario por `Settings.System` (freq/band/PS y claves opcionales RT/PTY si existen) para reducir dependencia de callbacks OEM.

### MT8163 / HCN — MCU-first (sin com.hcn.autoradio)
- **HiddenRadioPlayer**: ampliado con control directo (tune/seek/step/band/local/estado) vía reflexión sobre `android.radio.RadioPlayer`.
- **Preferencia `pref_mt8163_mcu_direct`**: modo experimental para **omitir** el bind a `com.hcn.autoradio` y operar por RadioPlayer; toggle en menú de ingeniería (requiere reinicio de la app).
- **Correcciones AIDL**: fix de inversión de `seekUp/seekDown` en `MT8163Engine` y ajuste para que `ScanManager` (software) sea la única fuente de AutoScan/presets.

### Accesibilidad (volante / teclas de medios)
- **FactoryRadioHijackerService**: solicitud reforzada de `FLAG_REQUEST_FILTER_KEY_EVENTS` al conectar y log a nivel INFO para diagnósticos en ROMs que filtran DEBUG.
- **Config**: `accessibility_service_config.xml` pasa a `typeAllMask` + `canRequestFilterKeyEvents=true` para mejorar compatibilidad con unidades OEM.
- **UI / fondo dinámico**: `ivDynamicBackground` pasa a **fitCenter** (layouts default y `sw720dp`); `LogoManager` decodifica el bitmap al tamaño de pantalla (tope **1600 px** en el lado largo) y usa **fitCenter** en Glide para contener el arte sin recorte tipo centerCrop.
- **Versión app (MCU2)**: `versionCode` **40**, `versionName` **5.2.0**.

## [5.1.8] - 2026-04-12
### Solucionado
- **QS6 (NWD)**: Sincronización total con Ingeniería Inversa G5 (Fase B).
- **QS6 (NWD)**: Corregidos IDs de fuente: Radio=4 (0x04), Android=0 (0x00).
- **QS6 (NWD)**: Implementado broadcast `ACTION_APP_IN_OUT` con **AppID 8**, obligatorio para activar el audio FM en hardware Nowada.
- **QS6 (NWD)**: Limpieza de constantes conflictivas y mejora de compatibilidad con `InitFM()`.

## [5.1.7] - 2026-04-12

## [5.1.6] - 2026-04-12

---

## [5.1.5] - 2026-04-12
### Added
- **Arquitectura Modular Total**: Finalización de la migración de todos los motores de radio (`QS6`, `MT8163`, `MTK8259`, `K706`, `Jancar`) a una arquitectura totalmente desacoplada basada en **`TunerAdapter`**.
- **Motores Refactorizados**:
    - **QS6 (Nowada)**: Sincronización completa con el SDK de Nowada y soporte de redundancia por Shadow Intents.
    - **MT8163 (HCN)**: Adaptador modular dedicado con corrección de sintonía fina.
    - **MTK8259/8667 (TopWay)**: Soporte para doble vínculo (`ITsCommon` + `ITsSpeechRadio`) sincronizado con la API real de TS.
- **Evolución de Interfaz**: Añadido `setBand(int)` a `RadioEngine` para control unificado de bandas en todos los chips.

### Fixed
- **Estabilidad de Compilación**: Resolución de 51 errores de símbolos y métodos abstractos derivados del desacoplamiento.
- **RadioServiceController**: No-args initialization para todos los motores modulares.

---

## [5.1.2] - 2026-04-11
*Parche sobre 5.1.2 (`versionCode` **35**, `versionName` **5.1.3**).*

### Added
- **Indicador de señal en barras** (`pref_signal_meter_bars`): opción en Ajustes premium → **Audio y pantalla**; en Layout **V2 y V3** sustituye el icono clásico por **5 segmentos** en la caja de frecuencia. Colores según skin (classic: blanco + alfas; noche: `night_blue_primary`; día/CLEAR: negro + alfas). Componentes `SignalBarsView` y `SignalMeterCoordinator`; i18n en todas las locales.

### Fixed / Improved
- **Medidor de barras**: el path de polling ya no aplicaba el color legado amarillo (se mapeaba mal a “rojo” → siempre 1 barra); `applyModeVisibility` solo hace *seed* desde estéreo/RDS cuando aún no hay nivel (`mLastLit` inicial), para no pisar RSSI al cambiar skin; soporte **dBm** cuando RSSI/SNR son negativos; escala **0–5** de **QS6/NWD** tratada de forma directa (antes los valores 1–3 en escala OEM caían casi siempre en 1 segmento con la fórmula 0–15); `legacyColorToLit` distingue ámbar/amarillo de rojo.

### Docs
- Imagen conceptual del medidor en `docs/img/concept_signal_bars_main_ui.png`.

---

## [5.1.2] - 2026-04-11
*Parche sobre 5.1.1 (`versionCode` **34**, `versionName` **5.1.2**).*

### Fixed / Improved
- **Autoscan lento (sobrescritura / QS6)**: el bucle de seek vuelve a programarse al cambiar de subbanda (FM2/FM3); si la frecuencia no avanza entre ticks se usa `stepUp()` para desbloquear búsquedas que se quedaban clavadas.
- **Tras autoscan lento**: al finalizar, sintonía al **primer preset** disponible (lista capturada o primer slot con frecuencia), con retardo breve para no perderse frente a callbacks OEM; ventana corta para **no forzar** `handleFrequencyChange` desde la UI cuando aún se reporta el tope de banda (p. ej. 108 MHz).
- **Layout 2**: contenedor **`boxAutoScan`** con el cristal; el **`ImageButton`** solo lleva el icono y el ripple — la animación de autoscan **gira solo el icono**, no el recuadro. `ThemeManager` aplica el skin al contenedor cuando existe.

---

## [5.1.1] - 2026-04-11
*Parche sobre 5.1.0 (`versionCode` **33**, `versionName` **5.1.1**).*

### Added
- **Eslovenia (SI)**: país en el selector de la app (i18n), carpeta `eslovenia/` para logos en Storage y función Supabase `country_folder` alineada.
- **Diagnóstico widget**: logs en `RadioWidgetActionReceiver` (tag **`ORF_WidgetRx`**) y trazas en `RadioMediaService.handleWidgetSeek`.
- **Presets en bucle (opcional)**: ajustes de pantalla — desplazamiento infinito de la tira en **Layout V2** y **V3** (`pref_preset_scroll_loop`, recrea la actividad al cambiar); layouts `preset_loop_slot_v2/v3` y `InfinitePresetScrollHelper`.

### Changed
- **Versionado app**: `versionCode 33`, `versionName 5.1.1`; `app_name_internal` **v5.1.1**.
- **BuildConfig**: URL base pública de logos en la raíz del bucket `station-logos` (subcarpetas por país).
- **Backup Studio (web)**: código **SI** en el selector de `pref_country_code` del `.ors`.
- **Modo Día**: fondo **beige** unificado (`@color/day_mode_background` **#EDE4D3**) en ventana y `LogoManager`; color de acento del skin `DAY_MODE` alineado.
- **Ajustes premium**: secciones con títulos y **resumen** Encendido/Apagado en interruptores (mejor lectura del estado).
- **Presets / volante**: navegación **siguiente–anterior** en modo favoritos por **orden de slots** (1…N) con bucle, alineada con la pantalla activa.

### Fixed / Improved
- **Widget (launcher, p. ej. QS6)**: `RadioMediaService` actualiza frecuencia/banda/PS del **widget propio** al variar la sintonía **sin depender de `MainActivity`** (callbacks del motor + refrescos diferidos si el OEM tarda).
- **Widget — logo**: carga con **Glide en hilo principal** y refresco desde el servicio que evita perder el bitmap al actualizar el `RemoteViews`.
- **Compilación release**: `SupabaseLogoSource` declara `applicationContext` al inicio del bloque async antes de subir a Storage (corrige `appContext` no resuelto).
- **Presets en bucle**: clones con el **mismo skin**, color de texto y filtro de logo que los slots principales; **`mLogoUiGeneration`** solo se incrementa si el skin **activo** cambia de verdad (evita vaciar el **fondo dinámico** con comprobaciones periódicas de modo noche).
- **Modo noche automático**: no invoca `applySkin` si el skin activo ya es el objetivo (menos trabajo y menos invalidaciones).

### Known / Pendiente
- **Widget**: el logo de emisora puede mostrarse y luego volver al icono de la app; revisión pendiente (carga Glide / re-render del `RemoteViews`).
- **Layout 2 / estrés**: bajo zapping rápido y cambios de layout, **`ivMainLogo`** puede mostrar el logo de emisora unos segundos y pasar a fallback o vacío; **pendiente** análisis (carreras `clearLogo` / `getStationInfo` / orden de callbacks).

---

## [5.1.0] - 2026-04-09 — “Backup Studio + Web Tools”
*Release centrada en herramientas de backup/importación y entorno web instalable (`versionCode` **31**, `versionName` **5.1.0**).*

### Added
- **Acceso al editor web**: en el diálogo **Guardar/Cargar Favoritos** se añade un botón **✏️** que abre `https://kapi21.github.io/OpenRadioFM/editor/`.
- **Backup Studio (web/PWA)**: web instalable en Android con soporte para:
  - **Favoritos `.fav`**: crear/editar presets por banda y exportar.
  - **Opciones `.ors`**: formulario guiado + carga/descarga.
  - **Backup completo `.orzip`**: generar/cargar ZIP con `state.json` + `RadioLogos/` (logos por preset, `car_logo.png`, `background.*`), con reescalado automático y previsualización.
  - **Manual ES/EN** integrado en la propia web y botones de navegación (volver a web principal + manual).
  - Tooltips **(i)** por campo para documentación de opciones.
- **Backup Studio — más campos `.ors`**: proveedor de logos, cabecera coche/reloj, volante NEXT/PREV, modo noche con horario (casilla que habilita edición de inicio/fin), tintado de logos en noche, barra de estado, relieve HD, guardar historial, presets a la derecha (layout 2); textos más claros del asistente inicial (idioma/país).
- **Supabase (comunidad)**: si no hay logo pero el **RDS PS es estable y pasa el quality gate**, se puede hacer **upsert “solo metadatos”** (PI/PS + frecuencia) para poblar la base y completar logo/stream más adelante; **solo FM** (≥ 30 MHz), respeta `pref_cloud_contrib` y `CloudContributionGuard`, con **cooldown** en memoria para no spamear upserts.

### Changed
- **Versionado app**: `versionCode 31`, `versionName 5.1.0`; `app_name_internal` **v5.1.0**.

### Fixed / Improved
- **Skins**: nuevo **Modo Día** (fondo tono hueso + tintes negros) y refinamiento de **Modo Noche** (consistencia de tintes en Layout 2/3).
- **Fondos (Layout 2/3)**: mitigado flicker/flash del `background.jpg/png` y aplicado refresco inmediato del fondo al entrar/salir de Modo Día.
- **Logos**: endurecimiento de carga/limpieza para evitar “arrastre” visual al cambiar de frecuencia/presets (especialmente en QS6 y Layout 3).
- **Fondo dinámico (bgMode=2)**: reintento de carga cuando el `ivDynamicBackground` queda vacío tras cancelaciones por zapping (evita “a veces no aparece”).
- **QS6 (NWD)**: al pulsar **PowerOff** se fuerza parada/silencio más robustos de la radio OEM en segundo plano.
- **QS6 (NWD)**: ajuste del guard anti-bootstrap durante SEEK/AutoScan para evitar “clavarse” en 87.5/87.6 durante el escaneo.
- **Mandos volante (QS6)**: puente “silencioso” para que NEXT/PREV/SEEK enruten a OpenRadioFM en segundo plano sin forzar audio.
- **Supabase**: chequeo de conectividad usando `auth/v1/health` para evitar falsos “offline”.
- **UI**: indicador de nube con parpadeo suave durante actividad online y transición de 200 ms al atenuar por falta de internet.
- **UI (RDS)**: “tick” visual al enganchar **RDS lock** (flash breve en PS/PTY).
- **UI**: actualización de iconos (toast + acceso editor web) y ajustes de tintes por skin en layouts.

---

## [5.0.16] - 2026-04-03 — “Setup & Stability Hotfix”
*Hotfix sobre la línea 5.0.15 (`versionCode` 29). **Pendiente verificación en hardware real K706 y MT8163.***

### Added
- **HiHack / reinicio del sistema**: `HihackBootReminderReceiver` + `HiHackBootReminder`, preferencias, recordatorio al abrir la app e interruptor en ajustes premium; cadenas en todas las locales.
- **Backups (estado de app)**: pulsación larga en **Guardar/Cargar Favoritos** abre un menú de **Copia de seguridad** con export/import de:
  - **Opciones (solo menú)** a `.ors` (ajustes `pref_*` + `ThemePrefs`)
  - **Copia completa** a `.orzip` (ZIP con `state.json` + imágenes de `RadioLogos/`)
  Incluye progreso y botón **Cancelar**, y sugiere **reinicio** tras restaurar para aplicar layouts/tema.
- **Widget escritorio**: 2 layouts (compacto/expandido), indicador de banda, acciones extra (seek/mute) y pulsación en PS para info rápida; en launchers sin resize el widget simple vuelve a **SEEK** (regresión evitada).
- **Icono launcher (QS6/NWD)**: `android:icon` y `roundIcon` apuntan a `@drawable` con fallback `drawable-nodpi-v4` (alta resolución) al estilo de la radio OEM.

### Fixed
- **Streaming / MT8163 (HCN)**: en `OnlineStreamManager`, `stopStream()` / `release()` coordinados con el *main looper*; si aplica handoff MT8163 diferido, se retrasa `ExoPlayer.release` para evitar solapes de dos streams.
- **Logo (Layout V3 / QS6)**: caché de logo por banda, reaplicar carga cuando la URL no cambia tras `applyFallbackLogo`; `clearLogo()` al cambiar frecuencia para evitar logo “pegado” o artefactos tras el RDS PS.
- **Volante en segundo plano (QS6)**: misma ruta de puente que K706 (`sWheelMediaBridgeActive`, `onStop`, `ACTION_FORCE_PLAY`); `RadioMediaService` / FGS y `FactoryRadioHijackerService` contemplan QS6; `RadioServiceController.isSteeringWheelMediaBridgeMode()`.
- **RDS / MediaSession / notificaciones**: normalización de subtítulo (MHz) para deduplicar metadatos en `MediaSessionManager`; frecuencia en `onRdsName` con formato `%.1f MHz` al refrescar estado; `RadioMediaService` evita `notify()` si no hay permiso de notificaciones o están desactivadas (el **widget** sigue usando `AppWidgetManager` + preferencias).
- **Hilo UI / logos**: si `RadioRepository.getStationInfo(..., callback != null)` se invoca desde el hilo de UI, el trabajo va a `logoExecutor` (`getStationInfoImpl`).
- **Calidad logos**: evitar guardado de logos descargados a 512×512 (causaba pixelado al ampliar); cargar/decodificar con ARGB8888 y tamaño original donde aplica.

### Versión
- `versionCode 29`, `versionName 5.0.16`; `app_name_internal` **v5.0.16**.

---

## [5.0.15] - 2026-04-03
*Release de cierre de la línea 5.0.15 (`versionCode` 28).*

### Added
- **Widget escritorio** (`AppWidget`): frecuencia, RDS PS, icono de app y memorias anterior/siguiente; actualización al ritmo de `MainActivity`; mandos vía `RadioMediaService` + `RadioWidgetActionReceiver` (útil en launchers tipo `com.android.launcher`; muchos launchers de automoción no exponen widgets de terceros).
- **Cadenas** del widget y del diálogo AutoScan en todas las locales.
- **Internacionalización**: ampliación de `strings.xml` (toasts, diálogos, AutoScan, escaneo, historial) con paridad de claves entre `values` y `values-*`; scripts auxiliares en `tools/` para sincronizar/validar claves (`diff_strings.py`, `sync_toast_strings_locales.py`, inserciones de bloques, etc.).
- **Diálogos alineados con menú premium / AutoScan**: selector en cuadrícula (`dialog_language_selector`), listado de archivos `.fav` al cargar favoritos (`dialog_favorites_file_picker`), historial de emisoras (`dialog_station_history`); marco oscuro, tarjeta con **skin** activo, tipografía del usuario, botón **Cancelar** rojo en listas; celdas `item_fav_file_row` / `item_language`.
- **Layouts traducibles**: textos de `dialog_save_load`, `dialog_credits`, `dialog_selective_scan` y filas de escaneo enlazados a `@string/`.
- **Build / Supabase**: credenciales vía `SUPABASE_URL` y `SUPABASE_ANON_KEY` en `local.properties` (raíz), variables de entorno o `-P` Gradle; sin valores por defecto en el repositorio. `BuildConfig` genera URL, clave y base pública de Storage. Plantilla `local.properties.example`; documentación [`docs/CI_SUPABASE.md`](docs/CI_SUPABASE.md).

### Fixed
- **Build / Supabase**: normalización de `SUPABASE_URL` (corrige valores copiados/escapados tipo `https\://...` que rompían Retrofit/OkHttp).
- **Comunidad Supabase (calidad de datos)**: puerta de calidad centralizada (`isAcceptableForCloudUpsert`, `sanitizePsForCloudUpsert`, reglas de PS); `CloudContributionGuard` — no contribuir en escaneo FM ni durante ~1,75 s tras cambiar de frecuencia; estabilidad del PS (~4 s) antes de contribuir. `CloudContributionGuard.java`, cambios en `RadioRepository`, `MainActivity`, `SupabaseLogoSource`.

### Changed
- **Toasts y mensajes**: literales sustituidos por `getString(R.string.*)` en `MainActivity`, `DialogManager`, `ScanManager`, `ControlPanelManager`, `HardwareManager`, `MinimalLayoutManager`, `StationAdapter`, etc.
- **RDS en listas de escaneo**: placeholders `scan_rds_searching` / `selective_scan_waiting_rds` coherentes con el idioma (sin mezclar español fijo en la UI).
- **AutoScan (lento)**: diálogo de confirmación con el mismo lenguaje visual que menús premium (cristal/tema activo, tipografía, botones).
- **AutoScan (FM)**: arranque siempre en **87,5 MHz** para recorrer la banda completa; cierre automático al alcanzar **108 MHz** o si el tuner hace *wrap* al inicio de FM.
- **Jancar IVI / 8227L**: refuerzo con `com.jancar.radio.FmService` (`fmradio.freq.valid`, seek next/prev, apagado al cerrar) cuando el tuner real no sigue solo a `IRadio`.
- **Layout V3**: el logo pequeño se oculta al pintar el logo de emisora como fondo, para no solaparse con el PS.
- **Diálogo de historial**: eliminado `applyPremiumListStyle` sobre lista del sistema; sustituido por layout propio unificado.
- **Menús ingeniería** (layouts): ajustes de scroll / secciones en diálogos K706, QS6 y modo estándar.
- **Layout V2**: mismo espacio horizontal (`layout_v2_column_spacing`) entre borde de pantalla, columna de presets y guía central.
- **Versión**: `versionCode 28`, `versionName 5.0.15`; `app_name_internal` **v5.0.15**.

---

## [5.0.14 (Beta)] - 2026-03-30
*Tipografía aplicada a indicadores (banda + unidad), y números de presets en assets.*

### Added
- **Números presets**: nueva opción en personalización **🔢 Números presets** con estilo **Por defecto** o **Tabler** (assets `icons_numbers/number-1-small.svg` … `number-18-small.svg`).
- **HiHack (Accesibilidad)**: indicador de estado (Activado/Desactivado) dentro de ajustes premium, con acceso directo a los ajustes de accesibilidad del sistema.

### Changed
- **Indicador de banda**: `FM1/FM2/FM3/AM1/AM2` pasa de iconos a **texto autosize** para respetar la tipografía seleccionada, manteniendo el mismo hueco en layouts V2/V3 (incl. `sw720dp`).
- **Indicador ST**: el indicador de estéreo pasa de icono a **texto “ST”** manteniendo el hueco reservado y el tinte en modo noche.
- **Ajuste de tamaños**: se reduce ligeramente el autosize del texto de banda para mejorar proporción visual.
- **Unidad**: `MHz/kHz` pasa de icono a **texto autosize** (cambia según banda), manteniendo el mismo hueco en layouts.
- **Assets**: se empaqueta `icons_numbers` como `assets` vía `sourceSets` en `app/build.gradle.kts`.
- **Versión**: `versionCode 26`, `versionName 5.0.14 (Beta)`.

---

## [5.0.13 (Beta)] - 2026-03-29
*Packs de iconos SVG (Material, Lucide, Remix, Font Awesome, Tabler), tintes modo noche / CLEAR / nube, y ajustes UX V3 / MT8163.*

### Added
- **Icon packs** (assets + selector en ajustes): `icons_google` (*_p3), `Icons_lucide` (*_p4), `icons_remix` (*_p5, incl. `power_off_p5.svg`), `icons_awesome` (*_p6), `icons_tabler` (*_p7; excepciones `ic_android_settings_p2.svg` / `power_off_p2.svg` en carpeta). `IconPackManager`: raster SVG → bitmap + silueta blanca para tintes; `isSvgTemplatePack()` para packs 2–6.
- **Strings**: `icon_pack_lucide`, `icon_pack_remix`, `icon_pack_awesome`, `icon_pack_tabler` (todas las locales).

### Fixed
- **MT8163 / pack iconos**: sincronía del botón LOC/DX con `isDxLocal()` cuando el callback AIDL no llega (incl. post-click); iconos Google/SVG no quedaban “pegados” al alternar.
- **Icono nube**: colores rojo / amarillo / azul noche / negro (CLEAR) sin que `applyClearButtonIconTint` pise el estado de streaming; `updateDataActivityUI` gestiona idle CLEAR con negro explícito.

### Changed
- **Layout V3**: logo coche (`ivCarLogo`) — mismo comportamiento que el reloj (toque = ciclar skin, largo = modo noche); `clickable`/`focusable` en XML.
- **Versión**: `versionCode 25`, `versionName 5.0.13 (Beta)`; `app_name_internal` **v5.0.13 Beta**.

---

## [5.0.12 (Beta)] - 2026-03-27
*Versionado: la etiqueta «Stable» se sustituye por «Beta» en nombre visible y documentación activa hasta consolidar la calidad de release.*

### Changed
- **Marca / versionado**: `versionCode 24`, `versionName 5.0.12 (Beta)`; `app_name_internal` **v5.0.12 Beta** (sin cambios funcionales respecto a 5.0.11).

---

## [5.0.11 (Stable)] - 2026-03-28
*K706: streaming online coherente con el canal MCU; layout simple (pantallas no sw720dp); menús ingeniería con toggle AutoScan experimental.*

### Fixed
- **K706 — Radio online se oía FM**: al iniciar stream, `stopStreamInternal(false)` programaba igual un `postDelayed` que llamaba a `switchToFmAudio()` y forzaba `SetChannel(2)` ~150 ms después de `SetChannel(4)`. La recuperación FM al parar stream solo se programa cuando el usuario detiene el stream (`stopStream()`), no en la limpieza previa a un nuevo arranque (`OnlineStreamManager`).

### Added
- **Menús ingeniería** (MT8163/MTK8259, K706, QS6): interruptor *Modo AutoScan* (`pref_dev_autoscan_enabled`, por defecto desactivado); con él el botón de escaneo en la UI principal llama a `ScanManager.toggleAutoScan` en lugar del toast «en estudio». `DevAutoscanToggleHelper`, `MainActivity.applyDevAutoScanButtonState()`.

### Changed
- **Layout simple** (`layout/activity_simple_radio.xml`, no `sw720dp`): iconos nube / seek / mute algo más pequeños y fila de botones `match_parent` para evitar desbordes con packs de iconos recientes.
- **Versión**: `versionCode 23`, `versionName 5.0.11 (Stable)`; `app_name_internal` **v5.0.11 Stable**.

---

## [5.0.10 (Stable)] - 2026-03-27
*K706: mandos de volante en segundo plano vía servicio de accesibilidad; sesión de medios y FGS más coherentes cuando la app no está al frente.*

### Added
- **K706 / accesibilidad**: `FactoryRadioHijackerService` intercepta `KeyEvent` del volante cuando el launcher u otra app está al frente (la radio OEM recibe mandos por MCU/QuickFish; OpenRadioFM no) y reenvía `ACTION_MEDIA_BUTTON` a `RadioMediaService`; `accessibility_service_config.xml` con `requestFilterKeyEvents` y sin filtro por paquete para capturar teclas globales.
- **Preferencias**: `pref_a11y_forward_media_keys` (por defecto activo) para desactivar el reenvío si no se desea.

### Changed
- **MainActivity**: banderas `sMainActivityStarted` / `sK706WheelBridgeActive` para reenviar solo en K706 con la app en segundo plano.
- **RadioMediaService**: en pérdida de audio OEM en K706, mantener sesión en reproducción y FGS cuando aplica; `onStartCommand` evita quitar FGS en ese escenario; `ACTION_FORCE_PLAY` desde `MainActivity.onStop` si no hay mute ni streaming.
- **RadioServiceController**: `isK706Mode()` para la lógica anterior.
- **Strings**: descripción del servicio de accesibilidad (ES/EN) alineada con captura de teclas de medios.
- **Versión**: `versionCode 22`, `versionName 5.0.10 (Stable)`; `app_name_internal` **v5.0.10 Stable**.

---

## [5.0.9 (Stable)] - 2026-03-27
*MT8163/HCN: handoff de MediaSession al cerrar streaming; mandos de volante y sesión de medios más robustos en segundo plano.*

### Added
- **MT8163**: acciones `ACTION_MT8163_FM_HANDOFF` / `ACTION_MT8163_FM_HANDOFF_COMPLETE` en `RadioMediaService` para bajar la sesión antes de reconectar FM y reducir force-stop OEM; `OnlineStreamManager` dispara el handoff y muestra toast localizado al parar el stream (aviso de reinicio de app si la FM queda rara).
- **Media / volante**: `setMediaButtonReceiver` explícito hacia `MediaButtonBootstrapReceiver`; `ACTION_FAST_FORWARD` / `ACTION_REWIND` mapeados como NEXT/PREV OEM; `handleSteeringSkip` unificado con cola en arranque en frío.

### Changed
- **MT8163Engine / RadioServiceController / MainActivity**: ventana que bloquea bind a HCN tras streaming, `requestPlayAudio` diferido y reconexión coordinada con el handoff de sesión.
- **Strings**: cadena `mt8163_stream_stopped_restart_hint` en todas las locales; eliminado diálogo/cadenas QS6 de aviso de firmware; `app_name_internal` **v5.0.9 Stable**; `versionCode 21`, `versionName 5.0.9 (Stable)`.

---

## [5.0.8 (Stable)] - 2026-03-25
*Versión estable: calidad de icono y logos, menú ingeniería (acceso y MTK8259), UX nube sin red, y ajustes de layout.*

### Added
- **MTK8259/8667**: interruptor en menú ingeniería *Compatibilidad mixer v5.0* (`pref_mtk8259_v5_stream_mixer_compat`): ruta legacy solo `CloseRadioCh` / `OpenRadioCh` para pruebas de mezcla FM vs streaming (vs. ruta actual con `EnterMode` / mute explícito).

### Changed
- **Menú ingeniería**: acceso por **pulsación larga en GPS** en todos los motores (sustituye 5 pulsaciones en ≤3 s). Diálogo: pista de scroll, barra vertical visible, sección `[ DEV_TOGGLES_HW ]`, log `MODE` con `FmMode` y motor reales (antes texto fijo `MT8163_DIAGNOSTIC_CLONE`).
- **Icono nube** (logos online activos): sin conectividad el contenedor se **atenua** (opacidad ~0,38) en lugar de ignorar el estado de red; con internet, comportamiento anterior (parpadeo si hay operaciones Supabase).
- **Launcher / UI**: icono adaptativo con capas `foreground` dedicadas (evita robot por referencia circular); `ic_app_logo` en UI; mipmaps unificados a calidad alta.
- **Layout 3 / presets**: menos solape nube–reloj; refresco de logos en presets al retunar.
- **Marca y versionado**: `app_name` fijado en **OpenRadioFM** (launcher, todas las locales); `app_name_internal` mantiene la versión visible interna; `versionCode 20`, `versionName 5.0.8 (Stable)`.

---

## [5.0.7 (Stable)] - 2026-03-24
*Versión estable publicada: nombre de app, Acerca de y documentación alineados con `5.0.7 (Stable)`.*

### Fixed
- **K706 — Radio Online sin audio / sigue sonando FM**: durante la carga del stream (y en otros `setMute(false)`), `PlaybackManager` llamaba a `enforceAudioRecovery()` → `SetChannel(2)` y anulaba el paso a canal Android (`SetChannel(4)`) que usa ExoPlayer. Se omite el recovery forzado a FM si el motor indica streaming activo; `refreshRadioStatus` trata como streaming también el estado **loading** (no solo `isPlaying`); `K706RadioManager` no ejecuta `enforceAudioChannelRecovery` ni el heartbeat de canal mientras `mIsOnlineStreamingActive`.

### Changed
- **RadioEngine**: método por defecto `isOnlineStreamingActive()`; implementado en K706, MT8163 y QS6.
- **UI layout 2**: icono nube más compacto; preset + nube alineados a la derecha del `boxIcons` con huecos fijos (`Space` + `INVISIBLE` en preset).
- **Marca en launcher**: `app_name` / `app_name_internal` actualizados a **v5.0.7 Stable** (todas las locales).
- **Versionado app**: `versionCode 19`, `versionName 5.0.7 (Stable)`.

---

## [5.0.6 (K706 cold start + QS6 parity)] - 2026-03-24

### Fixed
- **K706 — primera apertura tras reinicio de unidad**: el arranque ya no depende solo de `getCurrentFreq() <= 0` (el motor reportaba frecuencia distinta de la guardada sin forzar `tune`). Se alinea la lógica con QS6: comparar con `pref_last_freq`, reforzar sintonía ~1,4 s después del init, guardas de persistencia 87.5/87.6 y saneo opcional de prefs bootstrap (`pref_k706_bootstrap_sanitized`).
- **K706RadioManager**: valor inicial de `mCurrentFreq` corregido de **8750** a **87500** (misma escala OpenRadioFM ×1000 que el resto del pipeline MCU → UI).

### Changed
- **Versionado app**: `versionCode 17`, `versionName 5.0.6 (K706 cold start + QS6 parity)`.

---

## [5.0.5 (Hardening Phase 7 + QS6 startup)] - 2026-03-24
*Merge de `hardening/phase-7-stability` en `main`: estabilidad QS6 al arranque y ajuste de reproducción.*

### Fixed
- **QS6 / NWD — arranque en última emisora**: refuerzo frente a callbacks del stack OEM que reinyectan 87.5/87.6 MHz o el primer preset nativo tras auto-scan; `tuneWithBand` con banda explícita, re-afirmación tras cortar scan, protección de sintonía solicitada y restauración desde frecuencia estable reciente; logs `SRC=` para trazar origen.
- **QS6 / NWD — persistencia**: no guardar 87.x como última emisora salvo sintonía explícita del usuario; guardas en arranque y al apagado (`prepareForPowerOff`) para que callbacks tardíos no pisen `pref_last_freq` / `pref_last_band`; saneo único de preferencias bootstrap contaminadas.
- **Reproducción**: registro del receptor de medios **idempotente** para evitar duplicados al reanudar la actividad.

### Changed
- **Versionado app**: `versionCode 16`, `versionName 5.0.5 (Hardening Phase 7 + QS6 startup)`.

---

## [5.0.4 (QS NWD Advance + K706 Fixed)] - 2026-03-23
*Build de avance y corrección (fixed build, no release mayor).*

### Fixed
- **QS6 / NWD — transición de emisora**: mitigado arrastre temporal de PS/logo de la emisora anterior al cambiar rápido de frecuencia o preset.
- **QS6 / NWD — presets**: reforzada la coherencia de logos y textos por slot para descartar callbacks asíncronos obsoletos.
- **QS6 / NWD — PTY**: persistencia y fallback de PTY por frecuencia para evitar mostrar "Sin PTY" cuando el evento sí llegó desde el HAL.
- **QS6 / NWD — arranque**: bloqueo de auto-scan espontáneo al iniciar (manteniendo autoscan manual por botón).
- **QS6 / NWD — última frecuencia**: al abrir la app en frío se sintoniza de nuevo `pref_last_freq` si difiere del estado local del motor (antes `getCurrentFreq()` nunca era ≤0 y no se restauraba la emisora guardada).
- **K706 / controles**: se mantienen y consolidan los fixes de mandos volante/media para comportamiento estable en foreground/background.

### Added
- **Ajustes de Hardware**: nuevo selector para mandos de volante `NEXT/PREV`:
  - `Seek (emisoras)`
  - `Preset (memorias)`
- **UI**:
  - sección **👏 Agradecimientos** en Personalización Premium.
  - texto pulsable `Radio Android España` con diálogo QR.
  - acceso a `dialog_credits` con 1 toque sobre el logo en **Acerca de OpenRadioFM**.

### Changed
- **Versionado app**: `versionCode 15`, `versionName 5.0.4 (QS NWD Advance + K706 Fixed)`.
- **README**: actualizado historial y resumen bilingüe de la build `v5.0.4`.
- **Hardening / estabilidad**: limpieza más segura de recursos/ciclo de vida en `RadioMediaService` y `RadioServiceController`; indicador de actividad Supabase no bloqueante en `RadioRepository`; cachés y tracking pendiente reforzados para concurrencia; menor verbosidad de logs de red (`DEBUG=BASIC`, `RELEASE=NONE`).

---

## [5.0.2 fixed (QS6 / NWD)] - 2026-03-22
*Build de corrección (no release completo).*

### Fixed
- **QS6 / NWD**: al pasar la app a segundo plano se libera la competencia por audio con el reproductor del sistema (`releaseAudioFocusOnlyForBackground`); el reproductor nativo deja de cortarse.
- **AutoScan**: `ScanManager` + `MainActivity` — botón alineado con el estado real del escaneo.

### Added
- **QS6 Engineering**: diálogo *Technical Matrix* (easter egg GPS ×5).
- **Docs / assets**: `INTELIGENCIA_QS_NWD.md`, imágenes V5 en `docs/img/`.

### Changed
- **QS6Engine**, **RDSManager**, logos (`RadioRepository`, `SupabaseLogoSource`, `LogoManager`).

---

## [5.0.1 (K706 hotfix)] - 2026-03-22
### Fixed
- **K706 — Llamadas**: `READ_PHONE_STATE` en runtime; `PhoneStateListener` registrado solo con permiso; al colgar se llama a `requestPlayAudio()` para recuperar FM.
- **K706 — Volante / MediaSession**: `MEDIA_NEXT` / `PREVIOUS` y `RadioMediaService` (`skip` / cola) usan **seek** (frecuencia), no siguiente/anterior preset.

### Added
- **Docs**: `COMPARATIVA_K706_OPENRADIO_VS_OEM.md`, ampliaciones al estudio OEM; script opcional `scripts/k706_jadx_decompile.bat`.

---

## [5.0.0 (Stability Beta)] - 2026-03-21
### Added
- **Estabilidad MTK8259 (Crítico)**:
    - Nuevo sistema de paso de frecuencia (manual) para evitar errores de sintonía en Topway.
    - Eliminada la opción "Desactivar AM" en Ajustes Premium para prevenir bucles infinitos en el hardware.
    - Sincronización de Mute delegada exclusivamente a `TsCommon` para mayor fiabilidad.
- **Modo Claro (White Skin)**: Implementación completa del skin "White" con visibilidad garantizada en todos los elementos de UI y fuentes.
- **Transparencia Uniforme**: Aplicada opacidad consistente en todas las tarjetas de Layout 2 y Classic Skin.
- **Engineering Mode v2**: Mejoras en el terminal de log "Matrix Style" y comprobación de permisos Root.

### Fixed
- **MT8163 Initialization**: Corregido bug de instancia duplicada en `onServiceConnected` mediante el método `updateService`.
- **Iconos Recursores**: Solucionado el crash de "Recursive reference" en el icono del lanzador.
- **RDS Flickering**: Optimizado el buffer de texto RDS para evitar parpadeos en unidades MTK.
- **Supabase Stability**: Reemplazado `catch (Exception)` por `catch (Throwable)` en procesos de red para evitar cierres por `NoClassDefFoundError` (ClassValue) en Android 9.
- **Service Recovery**: Mejorada la recuperación del servicio nativo tras un cierre inesperado del proceso remoto.

## [4.9.5] - 2026-03-19
### Added
- **Engineering Mode Dashboard**: Hidden diagnostic menu (GPS button x5) features:
    - **RF Telemetry**: Signal Quality (SQI), Stereo/Mono, LOC/DX, Estimated RSSI.
    - **RDS Inspector**: PI, PTY, PS, AF List, Sync Status.
    - **Asset Diagnostics**: Real-time checks for `RadioLogos` folder, custom backgrounds, and car logos.
    - **System Info**: Device Model, Board ID, and **Root Access** verification (SU Check).
    - **Interactive Tuner**: Manual stepping controls (`<` / `>`) with real-time signal feedback.
    - **Live Terminal Log**: "Matrix Style" scrolling console showing kernel events (TUNED, RDS_UPDATE, STEREO_LOCK).
    - **Data Management**: Tools to Factory Reset favorites and clear station history.
- **Categorical PTY Icons**: Visual indicators for station categories (Music, News, Sport, Talk, Jazz, Classical, etc.) based on RDS PTY codes.

## [4.0.0] - 2026-02-09
### Added
- **Dynamic Backgrounds (Global Edition)**: Entirely refactored the dynamic blur engine for Layout 3. Background now updates even when central logo is hidden, providing a premium translucent glass effect.
- **Signal Level Quality Tinting**: Implemented dynamic coloring for the signal icon (`ivSignalLevel`) based on reception quality:
    - **Green**: Excellent (Stereo + RDS Lock).
    - **Yellow**: Medium (Stereo or RDS).
    - **Red**: Poor (No Lock).
- **Expanded Layout 3 (Horizontal)**: Control bar expanded to 8 buttons, integrated Android settings, and improved sidebar for panoriamic displays.
- **Save/Load Favorites**: Advanced backup system using `.fav` files in `RadioLogos` folder.

### Fixed
- **Layout 2 Stability**: Prevented layout shifting by ensuring RDS boxes (`tvRdsName`, `tvRdsInfo`) stay visible as transparent placeholders when empty.
- **Night Mode Consistency**: Consolidated all color-tinting logic. Now Frequency, Band, Signal, and RDS consistently use **Night Blue** in Night Mode.
- **Activity State Persistence**: Implemented `onSaveInstanceState` to preserve favorites and signal status across layout switches.
- **Icon Alignment**: Standardized `fitStart` and `fitEnd` across all layouts to eliminate internal drawable padding issues.
- **Language Selector**: Fixed automatic restart when switching between Spanish, English, and Russian.

### Changed
- **Icon Sizing**: Increased padding for secondary control buttons (GPS, Settings) to 28dp for better visual weight in Layout 2.
- **PTY Refinement**: Temporarily simplified PTY display logic to prioritize stability in low-signal environments.

---

## [3.0] - 2026-02-08
### Added
- Initial implementation of Layout 3 (Premium Horizontal).
- Support for Car Brand Logo customization.

Actualización centrada en la internacionalización, gestión de contenidos y refinamiento visual.

### 🌍 Internacionalización
*   **Soporte Multiidioma:** Traducción completa de la interfaz a Español, Inglés y Ruso.
*   **Selector Manual:** Nuevo selector de idioma en el menú Premium que permite forzar el idioma independientemente del sistema.
*   **Reinicio Dinámico:** La aplicación aplica el nuevo idioma instantáneamente reiniciando la actividad.

### 💾 Gestión de Favoritos
*   **Save/Load System:** Nueva funcionalidad para guardar y cargar tus listas de emisoras favoritas.
*   **Formato .fav (JSON):** Los archivos se guardan en `/sdcard/RadioLogos` y son fáciles de compartir o respaldar.
*   **Contenido Guardado:** Frecuencia, número de preset, nombre personalizado y timestamp.

### 🎨 Mejoras Visuales (V2 & V3)
*   **Layout V2 Refinado:** Corrección total de la alineación en la columna derecha. Nuevos botones de acceso rápido.
*   **Modo Nocturno Premium:** Los iconos de banda (FM) y etiquetas (MHz) ahora reciben un tinte azul nocturno ("Night Blue") en modo noche, junto con los bordes de los botones.
*   **Iconos Optimizados:** Reducción de tamaño (padding 18dp) para una estética más limpia.

### 🛠️ Otras Mejoras
*   **Botones Extra (V2):**
    *   **Settings:** Acceso directo a los ajustes de Android.
    *   **Favoritos:** Acceso directo al diálogo de Guardar/Cargar.
*   **Gestos (Beta):** Marcados como característica experimental en el menú.
*   **Correcciones:** 
    *   Solucionado error de compilación con referencias a variables antiguas.
    *   Corregidos formats strings con múltiples argumentos.

---


## v3.0 "The Car Experience" (Febrero 2026)

Esta es la actualización más ambiciosa hasta la fecha, transformando OpenRadioFM en una experiencia premium diseñada específicamente para el entorno del automóvil.

### 🚀 Novedades Principales
*   **Nuevo Layout Horizontal (V3):** Diseñado para pantallas de 1024x600. Iconos maximizados, visualización de frecuencia optimizada y logos tipo galería.
*   **Logo Marca Coche (V3):** Soporte para logo de marca personalizado en `/sdcard/RadioLogos/car_logo.png`.
*   **Indicadores de Banda Gráficos (V3):** Sustitución del texto FM1/FM2 por iconos dinámicos (`radio_fm1.png`, etc.) y nuevo icono de botón `BAND`.
*   **Menú de Personalización Premium:** Todo un centro de ajustes estéticos al alcance de una pulsación larga en el botón EQ/Settings.
*   **Capa de Fondo Dinámico (Universal):** El fondo de la aplicación (tanto en V2 como en V3) ahora puede cambiar dinámicamente al logo de la emisora sintonizada (con efecto Blur/Desenfoque).
*   **Gestor de Tipografías:** Soporte para 5 fuentes distintas (System, Bebas, Digital, Inter y la nueva **Orbitron**) integradas directamente en la app.
*   **Acceso Rápido GPS:** Nuevo botón dedicado para lanzar tu navegador GPS favorito (Maps, Waze, etc.) directamente desde la radio.

### 🛠️ Mejoras Técnicas & UI
*   **Visibilidad de Diálogos:** Nuevo fondo difuminado (70% dim) en todos los ajustes para mejorar el contraste sobre la interfaz.
*   **Coloreado de Bordes:** Optimizado según el layout (completo en V2, solo memorias en V3).
*   **Legibilidad de Memorias:** Aumento de tamaño de fuente a **19sp** en el layout horizontal.
*   **Separador Decimal:** Forzado a punto (**108.0**) para una visualización uniforme.
*   **Maximización de Controles:** Eliminación de márgenes (padding) en la fila inferior para facilitar la pulsación táctil conduciendo.
*   **Optimización 1024x600:** Reajuste de pesos en el diseño horizontal para evitar recortes en pantallas de baja resolución vertical.
*   **Refresco en Tiempo Real:** Los cambios de fondo y tipografía se aplican instantáneamente sin necesidad de reiniciar.
*   **Internacionalización:** Soporte completo de idiomas para Español, Inglés y Ruso en todos los menús.

### 🐛 Bugs Corregidos
*   **Persistencia de Logos:** Solucionado el problema que hacía desaparecer los logos tras un reinicio mediante integración forzada con **MediaScanner**.
*   **Lógica SEEK:** Corregida la dirección de los botones de búsqueda (Izquierda=Bajar, Derecha=Subir) para ser intuitiva y compatible con mandos al volante.
*   **Persistencia de Fondo:** Corregido el error que impedía aplicar el modo de fondo seleccionado desde el menú.
*   **Pantalla Completa:** Ajuste en el manejo de la barra de estado de Android en modo horizontal.
*   **Menú TEST:** El menú técnico de fábrica ahora está oculto tras 5 clics en el icono de GPS.

---

## v2.0b (Enero 2026)
*   **Expansión de Memorias:** De 6 a 12 presets por banda.
*   **Personalización de Fondos:** Soporte para `background.jpg/png`.
*   **Interfaz Glassmorphism:** Nuevos botones semitransparentes.
