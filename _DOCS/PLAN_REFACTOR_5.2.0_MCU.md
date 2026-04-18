# Plan de refactor — OpenRadioFM 5.2.0.MCU

**Rama de trabajo:** `5.2.0.MCU` (desde `MCU2`).  
**Objetivo:** Reducir el acoplamiento de `MainActivity`, acotar la superficie pública y agrupar responsabilidades sin cambiar el comportamiento observable en hardware.

**Referencias:** `CHANGELOG_REFACTOR.md`, `README.md` (arquitectura), informes en `_DOCS/`.

**Notas para el usuario final:** al cerrar cada fase, este documento incluye un apartado **«Para el usuario»** (lenguaje sencillo: qué notarás o qué mejora a medio plazo). La Fase 0 no cambia la experiencia en pantalla; fases posteriores pueden hacerlo solo si el comportamiento lo merece (siempre probado en radio).

---

## Criterios de éxito por fase

- [x] Compila: `./gradlew :app:assembleDebug` (última verificación en máquina de desarrollo)
- [x] Cadenas: `./gradlew :app:auditStrings`
- [ ] Prueba manual mínima: **K706**, **MT8163**, **QS6** (sintonía, preset, mute, segundo plano si aplica)
- [ ] Sin regresiones conocidas en los flujos tocados (documentar en CHANGELOG *Unreleased* si cambia comportamiento intencionado)

---

## Fase 0 — Inventario y contrato (`RadioUiHost` / scope)

- [x] Grep de usos: `mActivity.` en `ui/main` y accesos a campos públicos de `MainActivity` (inventario amplio; muchos managers siguen en `MainActivity` hasta fases posteriores).
- [x] Contrato **`RadioUiHost`**: `app/.../ui/main/RadioUiHost.java`; **`MainActivity`** lo implementa.
- [x] Coordinadores migrados al contrato (primera tanda): **`LifecycleCoordinator`**, **`HardwareKeyCoordinator`**, **`SignalMeterCoordinator`** (`RadioUiHost` en constructor; sin cambio de comportamiento).
- [x] **`StatusRefreshCoordinator`** y **`EngineCallbackCoordinator`**: constructor `RadioUiHost`; `RadioUiHost` ampliado con getters/setters y operaciones de UI que antes leían campos públicos de `MainActivity`.
- [ ] Resto de `*Coordinator` / diálogos / managers: siguen con **`MainActivity`** donde aún no hay método en el host; migración incremental en siguientes fases.

### API `RadioUiHost` (consumidor → métodos relevantes)

| Consumidor | Métodos usados (resumen) |
|------------|---------------------------|
| `LifecycleCoordinator` | `getHostContext`, `hostSendBroadcast`, `hostStartForegroundService`, `hostStartService`, `getOnlineStreamManager`, `getRadioService`, `getFmMode`, `getServiceController`, `requestHcnBindWithMediaSessionHandoff`, `getPlaybackManager`, `getRadioEngine`, `getScanManager`, `setUiScanningFlag`, `isPowerOffRequested`, `isHostChangingConfigurations`, `getMainHandler`, `getAutoHideHandler`, `getClockHandler`, `isHostFinishing`, `stopStatusPolling`, `getStationInfoExecutor`, `setStationInfoExecutor`, `getMediaSessionManager`, `getDeviceManager`, `getHardwareManager`, `getHiddenPlayer`, `setHiddenPlayer`, `setOnlineStreamManagerRef`, `getPresetManager`, `getLogoManager`, `getRdsManager`, `getUiController`, `getRadioPresets`, `getLastFreqKhz`, `getUiCurrentBand`, `setShutdownPersistGuardUntilMs`, `setPowerOffRequested` |
| `HardwareKeyCoordinator` | `getRadioPresets`, `getRadioEngine`, `getPresetManager`, `setMute`, `isMuteState` |
| `SignalMeterCoordinator` | `getRadioPresets`, `getThemeManager`, `getRadioEngine`, `isRdsLockHeld`, `getUiCurrentBand` |
| `StatusRefreshCoordinator` | Casi todo el bloque ampliado de **«Coordinación UI motor / refresco»** en `RadioUiHost` (motor, prefs, ejecutor de emisora, RDS, logo, widget, modo noche, medidor, `findHostViewById`, etc.). |
| `EngineCallbackCoordinator` | Mismo bloque ampliado + `getRadioSessionController`, diálogos de ingeniería, handlers HW; sustituye acceso directo a campos de `MainActivity`. |

Puente Activity: `getHostContext`, `isHostFinishing`, `isHostDestroyed`, `runOnHostUiThread`, `isHostChangingConfigurations`, envío de intents/broadcasts.

**Nota:** `getUiCurrentBand()` expone el índice de banda **`mCurrentBand`** de la UI; no confundir con `MainActivity.getCurrentBand()` (derivado del motor cuando existe).

### Para el usuario (Fase 0 — cerrada)

No verás cambios nuevos en botones, sonido ni pantallas: la radio se comporta igual que antes. Lo que hemos hecho es **ordenar por dentro** cómo la app habla consigo misma al arrancar, al usar teclas del volante o al mostrar el medidor de señal. Eso **facilita corregir fallos y añadir mejoras** sin tocar cientos de sitios a la vez, y reduce el riesgo de que un arreglo en una radio rompa otra. Es la base de la línea **5.2.0.MCU** hacia un código más fiable a largo plazo.

---

## Fase 1 — Bootstrap de `onCreate`

- [x] Secuencia tras `super.onCreate` movida a **`MainActivityBootstrap.runAfterSuper(MainActivity, Bundle)`** (`app/.../MainActivityBootstrap.java`).
- [x] `onCreate` queda en: `setTheme` → `super.onCreate` → `MainActivityBootstrap.runAfterSuper`.
- [x] Visibilidad **package** en campos que el bootstrap asigna (vistas, flags de arranque, `mServiceListener`, constantes de banda / prefs bootstrap, `TAG`) para no exponer API pública innecesaria.
- [x] Script opcional de mantenimiento: `tools/gen_main_activity_bootstrap.py` (regenerar desde `MainActivity` si se edita el orden; revisar `R.id` y claves de `Bundle` a mano).
- [x] *Opcional:* `runAfterSuper` troceado en métodos `private static` nombrados (`createEarlyCoordinators`, `bootstrapLastFrequencyAndBands`, etc.) sin cambiar orden ni comportamiento.

### Para el usuario (Fase 1 — cerrada)

La pantalla y la radio deben comportarse **igual que antes** al abrir la app, cambiar de layout o recuperar estado tras rotar la pantalla. Lo mejorado es **interno**: el arranque está concentrado en un solo módulo, lo que facilita revisar el orden de inicialización y detectar fallos sin recorrer miles de líneas. No es una función nueva para el conductor; es **calidad y mantenimiento** para que las próximas versiones salgan más estables.

---

## Fase 2 — Presentación de emisora (freq / RDS / logo / coalescing)

- [x] Agrupar **`handleFrequencyChange`**, coalescencia ~280 ms y trabajo pesado asociado en **`FrequencyChangeCoordinator`** (`app/.../FrequencyChangeCoordinator.java`); `MainActivity` delega; creado en **`MainActivityBootstrap.createEarlyCoordinators`**; **`cancelPendingHeavy`** en `onDestroy`.
- [x] **`StatusRefreshCoordinator`**: orden crítico RDS + MHz antes del executor **sin cambio**; sigue siendo el colaborador correcto tras `FrequencyChangeCoordinator` / `finishUserTuneFromUi`.
- [x] Unificar efectos de estado: **`FrequencyChangeCoordinator.finishUserTuneFromUi(freq, isQs6)`** tras **`MainActivity.gotoFreq`** (misma tubería pesada que el motor, sin coalescencia; sin historial; QS6 no pisa PS primado; durante escaneo sigue persistiendo como antes).
- [ ] Validar en **QS6** (ráfagas OEM) y **K706** (zapping rápido)

### Para el usuario (Fase 2 — cierre técnico; HW pendiente)

La intención sigue siendo **nombre, RDS y logo más estables** al zappear o con ráfagas del sistema. El código ya concentra coalescencia, trabajo pesado y la ruta **`gotoFreq`** en **`FrequencyChangeCoordinator`**. Falta **confirmar en radio real (QS6 / K706)** que no hay regresiones; si todo va bien, notarás sobre todo **menos “PS pegado” o logos cruzados** en escenarios que antes eran delicados.

---

## Fase 3 — Streaming y conectividad

- [x] **`setupOnlineStreaming`** delega en **`StreamingUiCoordinator.install(MainActivity)`** (`app/.../StreamingUiCoordinator.java`); **`MainActivity.removeHcnBindAfterHandoffCallbacks()`** expone cancelación del runnable MT8163 al paquete `ui.main`.
- [x] Separar en código: helpers **`isOnlineStreamPlaying` / `isOnlineStreamLoading`** (ExoPlayer/red) frente a FM por **`mEngine`** en el resto de `MainActivity`; comentario de módulo en **`StreamingUiCoordinator`**.
- [x] Indicador nube / offline: **`StreamingUiCoordinator.updateDataActivityUi`**, **`ensureDataActivityIndicatorManager`**, **`isInternetReachable`**; `MainActivity` solo delega (`mActiveDataOps` y caché de red visibles al paquete).

### Para el usuario (Fase 3 — en curso)

Objetivo: **menos choques entre FM y streaming** y un icono de datos **coherente**. Por ahora el cambio es **interno** (mismo icono y mismos toques); la lógica vive en **`StreamingUiCoordinator`** para que futuros ajustes no repartan el pintado entre varias clases. Sigue siendo importante **probar** cambio FM ↔ nube en **MT8163** y al menos un head unit con streaming.

---

## Fase 4 — Apariencia (skins, fuentes, icon packs)

- [x] **`applyFonts`** y **`applyRecursiveFont`** viven en **`SkinCoordinator`**; **`MainActivity`** delega (misma API pública para `DialogManager`, `HistoryManager`, etc.). **`applySkin`** ya delegaba en `SkinCoordinator`.
- [ ] Evitar duplicar lógica entre `NightModeManager` / `DayModeManager` y la activity (p. ej. color `tvDigitalClock` en `applySkin` vs `applyVisualStateForSkin`)

### Para el usuario (Fase 4 — en curso)

La apariencia en pantalla debe seguir **igual**; el cambio es **dónde vive el código** de fuentes (ahora junto a skins en **`SkinCoordinator`**). Próximo paso lógico: reducir duplicados de color reloj / modo día-noche entre el coordinador y los managers.

---

## Fase 5 — Intents, widget, permisos

- [ ] Centralizar **`onNewIntent`**, deep links de widget, reassert K706 hijacker en **`IntentRouter`** (o clase dedicada)
- [ ] Agrupar **`onRequestPermissionsResult`** y helpers de almacenamiento
- [ ] Comprobar **`WidgetBroadcastManager`** sigue recibiendo datos desde un solo sitio

### Para el usuario (Fase 5 — pendiente)

*(Completar al cerrar la fase.)* Objetivo: **abrir la app desde el widget o accesos del sistema** de forma más fiable; permisos y avisos más claros. Menos comportamientos «solo en mi radio» por rutas de entrada distintas.

---

## Fase 6 — Estado y visibilidad de miembros

- [ ] Sustituir campos **`public`** por **package-private** + getters donde sea necesario
- [ ] Agrupar flags relacionados (guards RDS, secuencias logo, bootstrap K706/QS6) en objetos de estado pequeños
- [ ] Pasada final de grep: ningún paquete externo debería requerir campos crudos de `MainActivity`

### Para el usuario (Fase 6 — pendiente)

*(Completar al cerrar la fase.)* Cambio casi todo **interno**. Beneficio: menos regresiones al actualizar; la app **se comporta igual** salvo que se cierre un bug concreto durante la limpieza.

---

## Fase 7 — Cierre de línea 5.2.0 (opcional, cuando el refactor estabilice)

- [ ] Alinear `versionCode` / `versionName` con **5.2.0** en `app/build.gradle.kts`
- [ ] Cerrar entradas **[Unreleased]** en `CHANGELOG.md` / `CHANGELOG_EN.md`
- [ ] Actualizar badges y párrafo de versión en `README.md`
- [ ] Tag `v5.2.0` y merge acordado (`5.2.0.MCU` → `MCU2` o `main`, según política del repo)

### Para el usuario (Fase 7 — pendiente)

*(Completar al cerrar la fase.)* Aquí sí suele haber **versión nueva numerada (5.2.0)** y notas públicas: qué radios probar, novedades visibles y enlaces al changelog. Es el momento de resumir todo el trabajo MCU en un mensaje claro para quien instala el APK.

---

## Notas

- Priorizar **PRs pequeños** por fase o sub-bloque; evitar un único diff masivo.
- Cualquier cambio que altere timing (handlers, coalescing) debe probarse en **hardware real**.
- Si se renombra o mueve API interna, actualizar solo los call sites necesarios (sin refactors colaterales).

---

*Última actualización: documento creado para la rama `5.2.0.MCU`.*
