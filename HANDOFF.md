# HANDOFF — OpenRadioFM (único handoff activo)

_Última actualización: 2026-11-18_

Workspace `K706_RE`: RE/firmware de autoradios Android. El trabajo activo es **OpenRadioFM**, una radio FM multiplataforma (K706, MT8163/8768, QS6, Topway 8259/8667, SPD, FYT).
Otros handoffs del workspace (no activos / proyecto aparte): `Junsun V9 Plus/README_SPD_HANDOFF.md` (SPD), `HANDOFF_K706_ROOT.md` (Magisk, fuera de alcance), `HANDOFF_5.5_*`/`5.6`, `_DOCS/` (histórico).

## Objetivo actual
Cerrar v5.7 (V7) y estabilizar: 8667 (RDS factory + QUIT), K706 (PowerOff amp, luces, stream), QS6 (auditoría) y MT8163 (PowerOff + icono mute).

---

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` **sincronizada con `origin/main`** (push 18/11; incluye WIP sin probar en HW) |
| Tag release | `v5.7` (**solo local; no publicado**, sin GitHub Release) |
| Commit release | `79c0f5f1` — v5.7 HCN V31 / Junsun V7 |
| Último WIP | `44f412d3` — MT8163 PowerOff + icono mute (5.7.9-mt8163mute) + handoff unificado |
| Working tree | limpio |
| App en WIP | `5.7.9-mt8163mute` / versionCode `54` |
| Release cerrada | `5.7` / versionCode `45` |

## Binarios

| APK | Ruta |
|-----|------|
| Release v5.7 | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test 8667 QUIT v2 (**Csaba**) | `K706_RE\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk` |
| Test 8667 RDS (histórico OK) | `K706_RE\8667\OpenRadioFM-5.7.1-8667fc-TEST.apk` |
| Test 8667 QUIT v1 (FALLA) | `K706_RE\8667\OpenRadioFM-5.7.2-8667quit-TEST.apk` |
| Test K706 amp (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.3-k706bg-TEST.apk` |
| Test K706 luces (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.5-k706lights-TEST.apk` (vc 50) |
| Test K706 stream PowerOff (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.6-k706stream-TEST.apk` (vc 51) |
| Test MT8163 fixes | `K706_RE\MT8163_test\OpenRadioFM-5.7.7-mt8163fix-TEST.apk` (vc 52) |
| Test QS6 fixes (**usuario: OK en QS6 y MT8163**) | `K706_RE\QS6_test\OpenRadioFM-5.7.8-qs6fix-TEST.apk` (vc 53) |
| **Test MT8163 PowerOff + icono mute (usuario)** | `K706_RE\MT8163_test\OpenRadioFM-5.7.9-mt8163mute-TEST.apk` (vc 54, **acumulado: incluye todo**) |

Parche V7: `K706_RE\MT8768\OpenRadioFM-V7-patch.zip` · Parche 8667: `K706_RE\8667\Factory_compatible.zip`

---

## Pendiente (al retomar)

1. **MT8163 (usuario)** → probar `5.7.9-mt8163mute`:
   (a) PowerOff corta la FM y al reabrir vuelve a sonar; (b) mute/unmute varias veces, esperando >3 s entre pulsaciones: icono = audio; (c) tras PowerOff, otra app de música suena normal.
2. **Csaba** → test completo `5.7.4-8667quit2` (RDS frío, QUIT, reabrir con audio+RDS).
3. **Luis (K706)** → activar switch luces invertidas (mantener 6 s botón GPS → HARDWARE_AUTOMATION_MODULE) y confirmar modo noche; validar `5.7.3-k706bg` (otras apps suenan tras PowerOff) y `5.7.6-k706stream` (PowerOff corta stream); regresión QS6 fixes en K706.
4. **QS6** → seguir probando `5.7.8`; vigilar log `poll settings: ignorando freq` (si nunca aparece, valorar quitar la ventana anti-rebote).
5. Si todo OK → consolidar release menor 5.7.x; push tag v5.7 + GitHub Release **solo cuando se autorice**.

Bloqueos: sin tag/release publicados hasta feedback (el código WIP ya está en `origin/main`).
Fuera de alcance: Magisk K706 root, Z-Link/AA, llamadas BT/HFP de Luis (ticket aparte), hilo Micra K12 (descartado 18/11).

### Árbol de decisión al recibir feedback
- Luis: si el switch de luces arregla → valorar invertir por defecto/auto-detección; si no → pedir logcat `MCU[0x22]` (luces ON/OFF).
- Csaba NO OK → logcat tags `MTK8259_8667Engine` / `MTK8259_8667RM` / `RadioServiceController`.
- MT8163 NO OK → logcat `PlaybackManager MT8163Engine StatusRefreshCoordinator MainActivity` (buscar `tecla MCU mute → hwKeyMuted=` y `muteForPowerOff`).

---

## Decisiones tomadas
- **PowerOff por motor** (`MainActivity.handlePowerOffButton`):
  - 8667 = `CloseRadioCh` + `EnterMode(0)` + unmute + `finishAndRemoveTask`.
  - K706 = modo B (minimizar) + `SetChannel(4)` + abandon focus + unmute amp.
  - MT8163 = minimizar + `PlaybackManager.muteForPowerOff()` (tecla MCU, sin `STREAM_MUSIC=0`); se recupera al volver.
  - Todos cortan antes el streaming online (`OnlineStreamManager.stopForPowerOff()`).
- 8667: `closeDevice()` ≠ `release()`; limpiar `sSharedLocalEngine` al liberar.
- Luces K706: el significado del byte MCU `0x22` (01=ON) era suposición → ajuste de inversión manual `pref_hw_lights_invert`, OFF por defecto.
- MT8163 mute: la tecla MCU `VOLUME_MUTE` es toggle → su estado se rastrea (`PlaybackManager.sMt8163HwKeyMuted`) y nunca se inyecta a ciegas.

---

## Historial técnico (hecho)

### v5.7 — Junsun V7 / MT8768
- HCN V31 + regresión MT8163; docs/web OK. Csaba 17/11: uso diario OK (recepción, seek/autoscan, RDS, logos).

### 8667 — RDS factory + QUIT
- RDS vía `ITsRadioCommon` / MainUI stock — OK en `5.7.1`.
- QUIT v1 (`5.7.2`) FALLA: motor zombie + mute MainUI.
- QUIT v2 (`5.7.4`): `closeDevice`≠`release`, clear shared, unmute, `finishAndRemoveTask`, binder RDS invalidable. Csaba confirma que funciona (falta test completo).

### K706 — PowerOff amp / luces / stream (Luis)
- Amp: `SetChannel(4)` + abandon focus + `setMute(false)` (`5.7.3`) — Luis confirma OK.
- Luces invertidas (apagadas=noche): ajuste `INVERT_LIGHTS_SIGNAL (0x22)` en diálogo ingeniería K706, aplicado en `MainActivity.handleHwLightsAutomation` (`5.7.5`, commit `4ad37c78`).
- Stream: PowerOff = `moveTaskToBack`; ExoPlayer vive en la Activity y seguía sonando → `OnlineStreamManager.stopForPowerOff()` (`5.7.6`).

### MT8163 — revisión de bugs (`5.7.7`)
- Crash al cerrar: `switchToAndroidAudio()` hacía `mPollingHandler.postDelayed` en hilo IO tras `release()` (NPE).
- `mRadioMuteDesired` quedaba `true` tras `release()` → FM muda en re-init.
- Reintento de `setMute` (120 ms) con valor obsoleto → mute pegado.
- `DeadObjectException` en `isStereo/isDxLocal/toggleDxLocal` → `handleDeadService` (reconecta). Guarda `binder == null` en `onServiceConnected`. Código muerto eliminado.

### MT8163 — PowerOff + icono mute (`5.7.9`, SIN probar en HW, sin commit)
- **PowerOff no cortaba la FM.** `minimizeToHomeKeepingMediaControl()` solo llamaba `mEngine.setMute(true)` (HAL `fm_mute=`/carsource), que HCN ignora; el mute audible es la **tecla MCU `VOLUME_MUTE`**, que solo se enviaba en gestos de usuario.
  - Fix: `PlaybackManager.muteForPowerOff()` → mute motor + tecla MCU, sin `STREAM_MUSIC=0`, icono a mute, marcado como mute de sistema → `resumeIfMutedBySystem()` recupera la FM al volver (si el usuario ya tenía mute, sigue muteado).
- **Icono mute invertido** (audio OK, icono al revés). La tecla es toggle y nadie rastreaba el estado; `StatusRefreshCoordinator` (sync por `STREAM_MUSIC`, que no refleja el mute por tecla) hacía `setMute(false)` sin tecla tras la guarda de 2,8 s → icono "sonando" con FM muda → pulsaciones posteriores invertidas.
  - Fix: `sMt8163HwKeyMuted` (estático, compartido Activity/servicio): la tecla solo se inyecta si el estado pedido difiere; rutas automáticas solo desmutean por tecla, nunca mutean; mientras está muteado por tecla, el sync por STREAM_MUSIC queda suprimido. `MT8163Engine.applyUserOemMuteThroughMcuKey()` devuelve `boolean`.
- Riesgo conocido: desmutear con la ruleta del HU (fuera de la app) puede dejar el icono en mute hasta pulsar.

### QS6 — auditoría y fixes (`5.7.8`, commit `22d75841`)
1. `QS6Engine.release(persist)`: con `persist=true` mantiene el motor vivo al recrear la Activity (modo noche/`uiMode`, idioma, memoria).
2. `CompositeRadioEngineCallback.combine()`: aplana y sustituye en vez de anidar (afecta también a K706 y al servicio en MT8163).
3. `QS6Engine.release()`: `mReleased` + cancelar tareas retardadas + `clearCallback`; callbacks NWD tardíos descartados.
4. `NWDTunerAdapter`: `mWantBound`, `unlinkToDeath`, cancelar reconexiones, unbind con bind en vuelo, `binderDied` en main.
5. Callbacks Binder procesados en main (NPE en `mCallback`).
6. `mcu_current_source` = ANDROID al soltar audio / streaming; RADIO en `requestPlayAudio()`.
7. Ventana anti-rebote 1,5 s de frecuencia Settings tras `tune()` (*plausible, no confirmado en HW*).
8. `toggleRdsFeature`: RDS global alterna en ambos sentidos.
9. `TopwayTunerAdapter.java` borrado.
- Qué probar en QS6: arranque en frío con audio; seek sin saltos atrás; RDS PS/RT; AF/TA/RDS ON↔OFF; streaming ↔ FM; cerrar app → otras fuentes suenan y al reabrir hay audio; modo noche con app abierta sin corte.

---

## Archivos clave
- PowerOff: `MainActivity.handlePowerOffButton()` / `powerOffTopway8667()` / `minimizeToHomeKeepingMediaControl()`
- Mute: `PlaybackManager` (`setMute`, `muteForPowerOff`, `syncMt8163HwKeyMute`), `StatusRefreshCoordinator` (sync mute), `MT8163Engine.setMute()` / `applyUserOemMuteThroughMcuKey()`
- 8667: `MTK8259_8667Engine.java` / `MTK8259_8667RadioManager.java`, `RadioServiceController.checkAndStartTsEngine()`
- K706: `K706RadioManager` (paquete MCU `0x22` luces), `K706EngineeringDialog` / `dialog_k706_engineering.xml`, `MainActivity.handleHwLightsAutomation()`
- QS6: `QS6Engine`, `NWDTunerAdapter`, `CompositeRadioEngineCallback`
- Servicio: `RadioMediaService` (PlaybackManager propio + `RadioSessionController` compartido)

## Cómo verificar
```powershell
cd "C:\@MIS PROYECTOS\K706_RE\OpenRadioFM"; .\gradlew assembleDebug
adb install -r "C:\@MIS PROYECTOS\K706_RE\MT8163_test\OpenRadioFM-5.7.9-mt8163mute-TEST.apk"
adb logcat -v time -s PlaybackManager MT8163Engine StatusRefreshCoordinator MainActivity RadioMediaService
```
Otros tags: 8667 `MTK8259_8667Engine MTK8259_8667RM RadioServiceController`; QS6 `QS6Engine NWDTunerAdapter`.

## Riesgos
- MT8163: el estado del mute por tecla es un rastreo en memoria; si el proceso muere con el HU muteado por tecla, al reiniciar la app cree "no muteado" (el usuario pulsaría dos veces).
- `CompositeRadioEngineCallback` cambia el cableado de callbacks en todos los motores → regresión K706 pendiente.

## Historial
- 2026-11-18 (noche, 3): commit `44f412d3` + push de `main` a GitHub (14 commits desde v5.7). Tag `v5.7` sin publicar por decisión del usuario.
- 2026-11-18 (noche, 2): fixes MT8163 PowerOff + icono mute → `5.7.9-mt8163mute`; handoffs unificados en este archivo (eliminados `K706_RE\HANDOFF.md` y `HANDOFF_5.7.md`); hilo Micra K12 descartado.
- 2026-11-18 (noche): usuario prueba `5.7.8-qs6fix` → OK en QS6 y MT8163; 2 bugs MT8163 detectados.
- 2026-11-18 (tarde): fixes K706 stream PowerOff (5.7.6), MT8163 (5.7.7), auditoría QS6 (5.7.8, `22d75841`).
- 2026-11-18: Csaba OK QUIT v2; Luis OK PowerOff; luces K706 → ajuste invertir señal (5.7.5).
- 2026-11-17: QUIT v2 8667; MT8768 confirmado uso diario.
- 2026-11-16: PowerOff 8667 v1 falló; fix K706 amp Luis; RDS factory OK en 5.7.1.
- 2026-11-14: cierre v5.7 V7/MT8768 + tag local.

## Créditos
Csaba — V7/MT8768 y 8667 factory RDS.
