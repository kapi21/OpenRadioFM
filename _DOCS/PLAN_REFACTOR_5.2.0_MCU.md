# Plan de refactor — OpenRadioFM 5.2.0.MCU

**Rama de trabajo:** `5.2.0.MCU` (desde `MCU2`).  
**Objetivo:** Reducir el acoplamiento de `MainActivity`, acotar la superficie pública y agrupar responsabilidades sin cambiar el comportamiento observable en hardware.

**Referencias:** `CHANGELOG_REFACTOR.md`, `README.md` (arquitectura), informes en `_DOCS/`.

---

## Criterios de éxito por fase

- [ ] Compila: `./gradlew :app:assembleDebug`
- [ ] Cadenas: `./gradlew :app:auditStrings`
- [ ] Prueba manual mínima: **K706**, **MT8163**, **QS6** (sintonía, preset, mute, segundo plano si aplica)
- [ ] Sin regresiones conocidas en los flujos tocados (documentar en CHANGELOG *Unreleased* si cambia comportamiento intencionado)

---

## Fase 0 — Inventario y contrato (`RadioUiHost` / scope)

- [x] Grep de usos: `mActivity.` en `ui/main` y accesos a campos públicos de `MainActivity` (inventario amplio; muchos managers siguen en `MainActivity` hasta fases posteriores).
- [x] Contrato **`RadioUiHost`**: `app/.../ui/main/RadioUiHost.java`; **`MainActivity`** lo implementa.
- [x] Coordinadores migrados al contrato (primera tanda): **`LifecycleCoordinator`**, **`HardwareKeyCoordinator`**, **`SignalMeterCoordinator`** (`RadioUiHost` en constructor; sin cambio de comportamiento).
- [ ] Resto de `*Coordinator` / diálogos / managers: siguen con **`MainActivity`**; migración incremental en siguientes fases.

### API `RadioUiHost` (consumidor → métodos relevantes)

| Consumidor | Métodos usados (resumen) |
|------------|---------------------------|
| `LifecycleCoordinator` | `getHostContext`, `hostSendBroadcast`, `hostStartForegroundService`, `hostStartService`, `getOnlineStreamManager`, `getRadioService`, `getFmMode`, `getServiceController`, `requestHcnBindWithMediaSessionHandoff`, `getPlaybackManager`, `getRadioEngine`, `getScanManager`, `setUiScanningFlag`, `isPowerOffRequested`, `isHostChangingConfigurations`, `getMainHandler`, `getAutoHideHandler`, `getClockHandler`, `isHostFinishing`, `stopStatusPolling`, `getStationInfoExecutor`, `setStationInfoExecutor`, `getMediaSessionManager`, `getDeviceManager`, `getHardwareManager`, `getHiddenPlayer`, `setHiddenPlayer`, `setOnlineStreamManagerRef`, `getPresetManager`, `getLogoManager`, `getRdsManager`, `getUiController`, `getRadioPresets`, `getLastFreqKhz`, `getUiCurrentBand`, `setShutdownPersistGuardUntilMs`, `setPowerOffRequested` |
| `HardwareKeyCoordinator` | `getRadioPresets`, `getRadioEngine`, `getPresetManager`, `setMute`, `isMuteState` |
| `SignalMeterCoordinator` | `getRadioPresets`, `getThemeManager`, `getRadioEngine`, `isRdsLockHeld`, `getUiCurrentBand` |

Puente Activity: `getHostContext`, `isHostFinishing`, `isHostDestroyed`, `runOnHostUiThread`, `isHostChangingConfigurations`, envío de intents/broadcasts.

**Nota:** `getUiCurrentBand()` expone el índice de banda **`mCurrentBand`** de la UI; no confundir con `MainActivity.getCurrentBand()` (derivado del motor cuando existe).

---

## Fase 1 — Bootstrap de `onCreate`

- [ ] Extraer secuencia de inicialización a **`MainActivityBootstrap`** (o equivalente) con pasos nombrados, por ejemplo:
  - [ ] Ventana / volumen / tema
  - [ ] `SharedPreferences`, flags de layout, `setContentView`, `UiViewMediator`
  - [ ] Managers agnósticos (`LogoManager`, `RadioServiceController`, …)
  - [ ] `PlaybackManager` / `DeviceManager` / receptores
- [ ] Dejar `onCreate` reducido a delegación + orden explícito
- [ ] Revisar que `onSaveInstanceState` / recreación de layout sigan coherentes

---

## Fase 2 — Presentación de emisora (freq / RDS / logo / coalescing)

- [ ] Agrupar **`handleFrequencyChange`**, **`runPendingFrequencyChangeHeavy`**, estado **`mFreqHeavy*`** en **`StationPresentationController`** o **`FrequencyChangeCoordinator`**
- [ ] Mantener **`StatusRefreshCoordinator`** como colaborador (orden reset RDS + MHz antes del executor)
- [ ] Unificar entrada única para “cambió la frecuencia desde el motor” vs “desde la UI”
- [ ] Validar en **QS6** (ráfagas OEM) y **K706** (zapping rápido)

---

## Fase 3 — Streaming y conectividad

- [ ] Extraer **`setupOnlineStreaming`** y lógica relacionada a **`StreamingUiCoordinator`** (o extensión de `OnlineStreamManager`)
- [ ] Separar claramente: **FM hardware** vs **ExoPlayer / red**
- [ ] Indicador de actividad de datos / offline: API única desde el nuevo módulo

---

## Fase 4 — Apariencia (skins, fuentes, icon packs)

- [ ] Mover restos de **`applySkin`**, **`applyFonts`**, tintes sueltos hacia **`SkinCoordinator`** / **`AppearanceController`**
- [ ] Evitar duplicar lógica entre `NightModeManager` / `DayModeManager` y la activity

---

## Fase 5 — Intents, widget, permisos

- [ ] Centralizar **`onNewIntent`**, deep links de widget, reassert K706 hijacker en **`IntentRouter`** (o clase dedicada)
- [ ] Agrupar **`onRequestPermissionsResult`** y helpers de almacenamiento
- [ ] Comprobar **`WidgetBroadcastManager`** sigue recibiendo datos desde un solo sitio

---

## Fase 6 — Estado y visibilidad de miembros

- [ ] Sustituir campos **`public`** por **package-private** + getters donde sea necesario
- [ ] Agrupar flags relacionados (guards RDS, secuencias logo, bootstrap K706/QS6) en objetos de estado pequeños
- [ ] Pasada final de grep: ningún paquete externo debería requerir campos crudos de `MainActivity`

---

## Fase 7 — Cierre de línea 5.2.0 (opcional, cuando el refactor estabilice)

- [ ] Alinear `versionCode` / `versionName` con **5.2.0** en `app/build.gradle.kts`
- [ ] Cerrar entradas **[Unreleased]** en `CHANGELOG.md` / `CHANGELOG_EN.md`
- [ ] Actualizar badges y párrafo de versión en `README.md`
- [ ] Tag `v5.2.0` y merge acordado (`5.2.0.MCU` → `MCU2` o `main`, según política del repo)

---

## Notas

- Priorizar **PRs pequeños** por fase o sub-bloque; evitar un único diff masivo.
- Cualquier cambio que altere timing (handlers, coalescing) debe probarse en **hardware real**.
- Si se renombra o mueve API interna, actualizar solo los call sites necesarios (sin refactors colaterales).

---

*Última actualización: documento creado para la rama `5.2.0.MCU`.*
