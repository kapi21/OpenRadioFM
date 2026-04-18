# Plan de refactor — OpenRadioFM 5.2.0.MCU

**Rama de trabajo:** `5.2.0.MCU` (desde `MCU2`).  
**Objetivo:** Reducir el acoplamiento de `MainActivity`, acotar la superficie pública y agrupar responsabilidades sin cambiar el comportamiento observable en hardware.

**Referencias:** `CHANGELOG_REFACTOR.md`, `README.md` (arquitectura), informes en `_DOCS/`.

**Notas para el usuario final:** al cerrar cada fase, este documento incluye un apartado **«Para el usuario»** (lenguaje sencillo: qué notarás o qué mejora a medio plazo). La Fase 0 no cambia la experiencia en pantalla; fases posteriores pueden hacerlo solo si el comportamiento lo merece (siempre probado en radio).

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

### Para el usuario (Fase 0 — cerrada)

No verás cambios nuevos en botones, sonido ni pantallas: la radio se comporta igual que antes. Lo que hemos hecho es **ordenar por dentro** cómo la app habla consigo misma al arrancar, al usar teclas del volante o al mostrar el medidor de señal. Eso **facilita corregir fallos y añadir mejoras** sin tocar cientos de sitios a la vez, y reduce el riesgo de que un arreglo en una radio rompa otra. Es la base de la línea **5.2.0.MCU** hacia un código más fiable a largo plazo.

---

## Fase 1 — Bootstrap de `onCreate`

- [x] Secuencia tras `super.onCreate` movida a **`MainActivityBootstrap.runAfterSuper(MainActivity, Bundle)`** (`app/.../MainActivityBootstrap.java`).
- [x] `onCreate` queda en: `setTheme` → `super.onCreate` → `MainActivityBootstrap.runAfterSuper`.
- [x] Visibilidad **package** en campos que el bootstrap asigna (vistas, flags de arranque, `mServiceListener`, constantes de banda / prefs bootstrap, `TAG`) para no exponer API pública innecesaria.
- [x] Script opcional de mantenimiento: `tools/gen_main_activity_bootstrap.py` (regenerar desde `MainActivity` si se edita el orden; revisar `R.id` y claves de `Bundle` a mano).
- [ ] *Opcional siguiente paso:* trocear `runAfterSuper` en métodos privados nombrados dentro de la misma clase (volumen/prefs/layout/managers/…) sin cambiar orden.

### Para el usuario (Fase 1 — cerrada)

La pantalla y la radio deben comportarse **igual que antes** al abrir la app, cambiar de layout o recuperar estado tras rotar la pantalla. Lo mejorado es **interno**: el arranque está concentrado en un solo módulo, lo que facilita revisar el orden de inicialización y detectar fallos sin recorrer miles de líneas. No es una función nueva para el conductor; es **calidad y mantenimiento** para que las próximas versiones salgan más estables.

---

## Fase 2 — Presentación de emisora (freq / RDS / logo / coalescing)

- [ ] Agrupar **`handleFrequencyChange`**, **`runPendingFrequencyChangeHeavy`**, estado **`mFreqHeavy*`** en **`StationPresentationController`** o **`FrequencyChangeCoordinator`**
- [ ] Mantener **`StatusRefreshCoordinator`** como colaborador (orden reset RDS + MHz antes del executor)
- [ ] Unificar entrada única para “cambió la frecuencia desde el motor” vs “desde la UI”
- [ ] Validar en **QS6** (ráfagas OEM) y **K706** (zapping rápido)

### Para el usuario (Fase 2 — pendiente)

*(Completar al cerrar la fase.)* Objetivo: **nombre de emisora, RDS y logo más estables** al cambiar de frecuencia rápido, sin parpadeos o textos «pegados» de la emisora anterior, sobre todo en radios con muchos avisos del sistema.

---

## Fase 3 — Streaming y conectividad

- [ ] Extraer **`setupOnlineStreaming`** y lógica relacionada a **`StreamingUiCoordinator`** (o extensión de `OnlineStreamManager`)
- [ ] Separar claramente: **FM hardware** vs **ExoPlayer / red**
- [ ] Indicador de actividad de datos / offline: API única desde el nuevo módulo

### Para el usuario (Fase 3 — pendiente)

*(Completar al cerrar la fase.)* Objetivo: **menos choques entre radio FM y streaming por internet** (quién tiene el sonido, icono de nube, cortes al volver de otra app). Comportamiento más predecible al alternar emisora online y FM.

---

## Fase 4 — Apariencia (skins, fuentes, icon packs)

- [ ] Mover restos de **`applySkin`**, **`applyFonts`**, tintes sueltos hacia **`SkinCoordinator`** / **`AppearanceController`**
- [ ] Evitar duplicar lógica entre `NightModeManager` / `DayModeManager` y la activity

### Para el usuario (Fase 4 — pendiente)

*(Completar al cerrar la fase.)* Objetivo: **misma apariencia** (modo noche, día, iconos); menos incoherencias al cambiar de skin o pack de iconos. Si se unifica bien, menos parpadeos al rotar tema.

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
