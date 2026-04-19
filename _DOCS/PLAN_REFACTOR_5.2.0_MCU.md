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
- [x] **`FrequencyChangeCoordinator`**: constructor `RadioUiHost` (2026-04-19); métodos nuevos en el host para guardas de arranque, ventana nube post-sintonía, transición RDS/logo e historial.
- [ ] Resto de `*Coordinator` / diálogos / managers: siguen con **`MainActivity`** donde aún no hay método en el host; migración incremental en siguientes fases.

### API `RadioUiHost` (consumidor → métodos relevantes)

| Consumidor | Métodos usados (resumen) |
|------------|---------------------------|
| `LifecycleCoordinator` | `getHostContext`, `hostSendBroadcast`, `hostStartForegroundService`, `hostStartService`, `getOnlineStreamManager`, `getRadioService`, `getFmMode`, `getServiceController`, `requestHcnBindWithMediaSessionHandoff`, `getPlaybackManager`, `getRadioEngine`, `getScanManager`, `setUiScanningFlag`, `isPowerOffRequested`, `isHostChangingConfigurations`, `getMainHandler`, `getAutoHideHandler`, `getClockHandler`, `isHostFinishing`, `stopStatusPolling`, `getStationInfoExecutor`, `setStationInfoExecutor`, `getMediaSessionManager`, `getDeviceManager`, `getHardwareManager`, `getHiddenPlayer`, `setHiddenPlayer`, `setOnlineStreamManagerRef`, `getPresetManager`, `getLogoManager`, `getRdsManager`, `getUiController`, `getRadioPresets`, `getLastFreqKhz`, `getUiCurrentBand`, `setShutdownPersistGuardUntilMs`, `setPowerOffRequested` |
| `HardwareKeyCoordinator` | `getRadioPresets`, `getRadioEngine`, `getPresetManager`, `setMute`, `isMuteState` |
| `SignalMeterCoordinator` | `getRadioPresets`, `getThemeManager`, `getRadioEngine`, `isRdsLockHeld`, `getUiCurrentBand` |
| `StatusRefreshCoordinator` | Casi todo el bloque ampliado de **«Coordinación UI motor / refresco»** en `RadioUiHost` (motor, prefs, ejecutor de emisora, RDS, logo, widget, modo noche, medidor, `findHostViewById`, etc.). |
| `EngineCallbackCoordinator` | Mismo bloque ampliado + `getRadioSessionController`, diálogos de ingeniería, handlers HW; sustituye acceso directo a campos de `MainActivity`. |
| `FrequencyChangeCoordinator` | `RadioUiHost` (2026-04-19): handlers, motor, prefs, RDS/logo reset, streaming, widget, guardas de arranque (`getStartupFreqPersistGuards`), `beginRdsLogoTransitionAfterTune`, `armCloudContribFreqSettleWindow`, etc. |

Puente Activity: `getHostContext`, `isHostFinishing`, `isHostDestroyed`, `runOnHostUiThread`, `isHostChangingConfigurations`, envío de intents/broadcasts.

**Nota:** `getUiCurrentBand()` expone el índice de banda **`mCurrentBand`** de la UI; no confundir con `MainActivity.getCurrentBand()` (derivado del motor cuando existe).

### Inventario código — `MainActivity` vs `RadioUiHost` (2026-04-19)

Ámbito: `app/src/main/java/com/example/openradiofm/ui/main/`. Objetivo del inventario: saber qué falta para el ítem *«Resto de *Coordinator / diálogos / managers»* de la Fase 0.

**Ya usan `RadioUiHost` en el constructor**

| Clase |
|--------|
| `LifecycleCoordinator` |
| `HardwareKeyCoordinator` |
| `SignalMeterCoordinator` |
| `StatusRefreshCoordinator` |
| `EngineCallbackCoordinator` |
| `FrequencyChangeCoordinator` |

**Siguen con `MainActivity` como referencia principal** (`private final MainActivity` o constructor equivalente)

| Clase | Comentario breve |
|--------|------------------|
| `LogoManager` | Logos, fondo dinámico, visibilidad |
| `DialogManager` | Diálogos (prefs, about, save/load, hijacker, etc.) |
| `HardwareManager` | Bridge / hardware |
| `MediaSessionManager` | Sesión de medios ligada a la activity |
| `ScanManager` | Escaneo y flujo OEM |
| `PresetManager` | Presets y repositorio |
| `ControlPanelManager` | Panel de controles |
| `SkinCoordinator` | Skins y fuentes (ver también Fase 4) |
| `UiViewMediator` | Mediación de vistas |
| `HistoryManager` | Historial |
| `StandardLayoutManager` | Layout estándar |
| `SimpleLayoutManager` | Layout simple |
| `MinimalLayoutManager` | Layout mínimo |
| `StationAdapter` | Lista tras escaneo |
| `EngineeringModeDialog` | Menú ingeniería genérico |
| `K706EngineeringDialog` | Ingeniería K706 |
| `QS6EngineeringDialog` | Ingeniería QS6 |

**Layouts (`BaseLayoutController` → `protected final MainActivity mActivity`)**

| Clase |
|--------|
| `MainLayoutController` |
| `SimpleLayoutController` |
| `V3LayoutController` |

**Helpers estáticos con parámetro `MainActivity`**

| Clase | Métodos / entrada |
|--------|-------------------|
| `IntentRouter` | `dispatchNewIntent`, `dispatchPermissionsResult`, `scheduleK706McuListenerReassertAfterOem`, handlers internos |
| `StreamingUiCoordinator` | `install`, `updateDataActivityUi`, `isInternetReachable`, helpers de streaming / indicador de datos |
| `InfinitePresetScrollHelper` | `attachIfNeeded(MainActivity)` |

**Por diseño ligados a la activity (no “coordinador” de UI)**

| Clase | Nota |
|--------|------|
| `MainActivityBootstrap` | Secuencia `runAfterSuper(MainActivity, Bundle)`; mover al host implicaría repartir responsabilidades, no solo renombrar. |

**Acoplamiento de tipo o casts (sin campo `MainActivity` obligatorio)**

| Clase | Detalle |
|--------|---------|
| `RadioServiceController` | Usa `MainActivity.FmMode` en detección y ramas de motor. |
| `RDSManager` | Constructor `Context`; usa métodos estáticos de `MainActivity` para actualizar `TextView` (`setTextIfChanged`, etc.). |
| `DayModeManager` / `NightModeManager` | Reciben `Activity`; casts a `MainActivity` para `setColorFilterIfChanged`, `refreshStereoIndicatorUi` y coherencia con datos/nube. |

**Sugerencia de orden al ampliar `RadioUiHost`** (incremental): (1) managers que más cruzan motor y UI (`ScanManager`, `PresetManager`, `DialogManager`, `MediaSessionManager`); (2) layouts y `LogoManager` / `ControlPanelManager` / `UiViewMediator` / `HistoryManager`; (3) diálogos de ingeniería; (4) sustituir parámetro `MainActivity` por `RadioUiHost` en `IntentRouter` / `StreamingUiCoordinator` cuando el API del host cubra las llamadas; (5) reducir casts en `DayModeManager` / `NightModeManager` exponiendo en el host lo que hoy solo vive en `MainActivity`. **`FrequencyChangeCoordinator`** ya usa `RadioUiHost` (2026-04-19).

*Última revisión del inventario: 2026-04-19 — actualizado cierre `FrequencyChangeCoordinator` + API host.*

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
- [x] Color **`tvDigitalClock`**: una sola implementación **`SkinCoordinator.applyDigitalClockTextColor`** (antes se pintaba dos veces con reglas ligeramente distintas). Otros tintes / managers siguen en **NightModeManager** / **DayModeManager** hasta un repaso mayor.

### Para el usuario (Fase 4 — cerrada en alcance MCU actual)

Skins y fuentes siguen viéndose **como antes**; el código está más **agrupado** en **`SkinCoordinator`** y el reloj digital ya no depende de **dos bloques** que podían diverger. Si notas alguna diferencia solo en el color del reloj al mezclar modo noche/día/clear, conviene probar en **hardware** y reportarlo.

---

## Fase 5 — Intents, widget, permisos

- [x] **`IntentRouter`** (`app/.../IntentRouter.java`): **`dispatchNewIntent`** (widget + reassert MCU K706/hijacker), **`scheduleK706McuListenerReassertAfterOem`**, **`dispatchPermissionsResult`**; códigos **`REQ_STORAGE_IMPORT_EXPORT`** / **`REQ_READ_PHONE_STATE_K706`**.
- [x] **`MainActivity`**: `onNewIntent` / `onRequestPermissionsResult` delegan; **`requestStoragePermissions`** usa el código nombrado.
- [x] **`WidgetBroadcastManager.sendUpdate`**: solo desde **`MainActivity.sendWidgetUpdate`** (vía `RadioUiHost` / `FrequencyChangeCoordinator`); sin nuevas rutas en este refactor.

### Para el usuario (Fase 5 — cierre técnico parcial)

Comportamiento esperado **igual** al abrir desde widget o volver con el hijacker K706. El cambio es **organización del código** para revisar entradas y permisos en un solo sitio. Conviene **probar** widget “info” / favoritos y permisos de almacenamiento + **READ_PHONE_STATE** en K706.

---

## Fase 6 — Estado y visibilidad de miembros

- [x] Sustituir campos **`public`** por **package-private** en miembros de instancia de **`MainActivity`** (consumo en **`ui.main`**). Estáticos **`sMainActivityResumed`** / **`sWheelMediaBridgeActive`**: package + API pública **`isMainActivityResumed`**, **`isWheelMediaBridgeActive`**, **`setWheelMediaBridgeActive`** para **`FactoryRadioHijackerService`**.
- [x] Agrupar flags relacionados en objetos de estado pequeños (mismo paquete): **`RdsLockUiTickState`** (`mRdsLockUiTick`), **`RdsLogoTransitionState`** (`mRdsLogoTransition`), **`StartupFreqPersistGuards`** (`mStartupFqGuards`). **`FrequencyStateManager`** conserva su copia para **`evaluateFrequencyChange`** / **`shouldBlockTransitionalRdsName`** hasta un posible unificación futura.
- [x] Grep externo: sin acceso a campos de instancia **`m*`** desde fuera de **`ui.main`**; el hijacker ya no depende de campos estáticos públicos.

### Para el usuario (Fase 6 — cerrada)

Cambio **interno**: misma radio, mismos widgets y mismo volante en segundo plano. La app **agrupa** en pocos objetos el estado de RDS en pantalla, invalidación de logos y guardas de arranque (bootstrap 87.5/87.6), y **reduce** la superficie pública de `MainActivity`. Conviene una **pasada rápida** en cabecera con hijacker (MEDIA con launcher al frente, K706/QS6) y comprobar ingeniería / layout estándar si usas esos menús.

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

*Última actualización: 2026-04-19 — inventario Fase 0; `FrequencyChangeCoordinator` migrado a `RadioUiHost` + métodos de host asociados. Documento originado para la rama `5.2.0.MCU`.*
