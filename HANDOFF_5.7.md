# Handoff — OpenRadioFM v5.7 + WIP 8667/K706

_Última actualización: 2026-11-18_

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` (**ahead ~9** vs `origin/main`, **sin push**) |
| Tag release | `v5.7` (local; no publicado) |
| Commit release | `79c0f5f1` — v5.7 HCN V31 / Junsun V7 |
| Último WIP | `b6243557` (+ limpieza código muerto MT8163 y auditoría QS6) |
| Working tree | limpio (al cierre) |
| App en WIP | `5.7.7-mt8163fix` / versionCode `52` |
| Release cerrada | `5.7` / versionCode `45` |

## Binarios

| APK | Ruta |
|-----|------|
| Release v5.7 | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test 8667 QUIT v2 (**Csaba**) | `K706_RE\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk` |
| Test 8667 RDS (histórico OK) | `K706_RE\8667\OpenRadioFM-5.7.1-8667fc-TEST.apk` |
| Test 8667 QUIT v1 (FALLA) | `K706_RE\8667\OpenRadioFM-5.7.2-8667quit-TEST.apk` |
| Test K706 amp (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.3-k706bg-TEST.apk` |
| Test K706 stream PowerOff (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.6-k706stream-TEST.apk` |
| Test MT8163 fixes (**Usuario**) | `K706_RE\MT8163_test\OpenRadioFM-5.7.7-mt8163fix-TEST.apk` |

Parche V7: `K706_RE\MT8768\OpenRadioFM-V7-patch.zip`  
Parche 8667: `K706_RE\8667\Factory_compatible.zip`

---

## Hecho

### v5.7 — Junsun V7 / MT8768
- HCN V31 + regresión MT8163; docs/web OK.
- **Csaba 17/11:** uso diario OK (recepción, seek/autoscan, RDS, logos).
- Pendiente publicar: push + tag + GitHub Release (cuando se pida).

### 8667 — RDS factory + QUIT
- RDS vía `ITsRadioCommon` / MainUI stock — OK en `5.7.1`.
- QUIT v1 (`5.7.2`) FALLA: motor zombie + mute MainUI.
- QUIT v2 (`5.7.4`): `closeDevice`≠`release`, clear shared, unmute, `finishAndRemoveTask`, binder RDS invalidable.
- **Pendiente:** validación Csaba de `5.7.4-8667quit2`.

### K706 — PowerOff amp (Luis)
- `SetChannel(4)` + abandon focus + `setMute(false)`.
- **Pendiente:** validación Luis de `5.7.3-k706bg`.
- Llamadas BT/HFP (Luis M-M): ticket aparte.

### K706 — PowerOff con streaming online (Luis, 18/11)
- **Bug:** PowerOff en K706 = minimizar (`moveTaskToBack`); ExoPlayer vive en la Activity y seguía sonando.
- **Fix:** `OnlineStreamManager.stopForPowerOff()` (libera ExoPlayer, `setOnlineStreamingActive(false)`, sin `switchToFmAudio`/handoff). Llamado en `minimizeToHomeKeepingMediaControl()` y `powerOffTopway8667()`.
- **APK test:** `K706_RE\K706_test\OpenRadioFM-5.7.6-k706stream-TEST.apk` (versionCode `51`).
- **Pendiente:** validación Luis.

### MT8163 — revisión de bugs (18/11)
- Crash al cerrar: `switchToAndroidAudio()` hacía `mPollingHandler.postDelayed` en hilo IO tras `release()` (NPE).
- `mRadioMuteDesired` quedaba `true` tras `release()` → FM muda en re-init.
- Reintento de `setMute` (120 ms) con valor obsoleto → mute pegado.
- `DeadObjectException` en `isStereo/isDxLocal/toggleDxLocal` → ahora `handleDeadService` (reconecta).
- Guarda `binder == null` en `onServiceConnected`.
- `MT8163TunerAdapter` + `mAudioFocusListener`: código muerto eliminado.
- **APK test:** `K706_RE\MT8163_test\OpenRadioFM-5.7.7-mt8163fix-TEST.apk` (versionCode `52`, incluye fix K706 stream).
- **Pendiente:** test usuario en MT8163.

### QS6 — auditoría de bugs (18/11, pendiente de aplicar)
- **🔴 Falta `release(boolean persist)` en `QS6Engine` (corta la radio al recrear UI o en background):** `LifecycleCoordinator.onDestroy()` pasa `recreating = isChangingConfigurations() || !isFinishing()` a `mEngine.release(recreating)`. Todos los motores (`K706`, `MT8163`, `8667`, `Jancar`, `Spd`) implementan `release(boolean)` salvo `QS6Engine`, que cae al `default` de `RadioEngine` y ejecuta `release()` → `closeDevice()` (`ACTION_EXIT_ARM_FM_RAIDO` + `SOURCE_ANDROID` + desconexión AIDL/MCU).
- **🔴 Anidamiento infinito y leak de `MainActivity` en `CompositeRadioEngineCallback` (QS6 y K706):** al reutilizar `sSharedLocalEngine`, `MainActivity.onEngineReady()` compara `existingCb != MainActivity.this` (siempre `true`, el callback es `mEngineCallbackCoordinator`) y envuelve `existingCb` en otro `CompositeRadioEngineCallback` sin desempaquetar el anterior; cada recreación/reapertura acumula referencias a `MainActivity` muertas y multiplica eventos de frecuencia/RDS.
- **🟠 Reconexión zombi y leak en `QS6Engine.release()`:** no cancela `postDelayed` de `mMainHandler` (`scheduleWarmupRebind` 2,2 s / `maybeRequestPlayAudioOnStartup` 450 ms) ni llama a `mAdapter.setCallback(null)` sobre el singleton `NWDTunerAdapter` (retiene `QS6Engine`/`mContext` y re-pide `SOURCE_RADIO` tras `closeDevice()`).
- **🟠 Carrera y reconexión en `NWDTunerAdapter`:** `disconnect()` no cancela `mMainHandler` ni llama `unlinkToDeath`; `bind()` solo mira `mIsBound` y no un bind en vuelo (una segunda llamada ejecuta `unbindService`); `onServiceConnected` marca `mIsBound=true` aunque `service==null`; `mDeathRecipient` muta estado sin sincronización desde hilo Binder.
- **🟠 Rebote de frecuencia en `Settings.System`:** `tune()` actualiza `mCurrentFreq` antes de que el MCU escriba `nwd_radio_current_freq`; si el `ContentObserver` dispara antes (`pollNwdSettingsAndFire()`), revierte `mCurrentFreq` y la UI a la emisora anterior.
- **🟡 NPE en `QS6Engine.toggleRdsFeature()`:** usa `mAdapter.setRDSState(...)` sin comprobar `mAdapter != null`.
- **🟡 Hilo Binder y visibilidad:** `notifyRtMessage()` invoca `mCallback.onRdsText()` directo en hilo Binder (el resto usa `mMainHandler.post`); `mCurrentFreq`, `mCurrentBand` y `mLastRt` carecen de `volatile`.
- **⚪ Código muerto:** `TopwayTunerAdapter.java` (305 líneas) sin referencias en el proyecto.

---

## Pendiente (al retomar)

1. Contestación **Csaba** → `5.7.4-8667quit2` (RDS frío, QUIT, reabrir con audio+RDS).
2. Contestación **Luis** → `5.7.3-k706bg` (otras apps suenan tras PowerOff) y `5.7.6-k706stream` (PowerOff corta stream online).
3. Test usuario → `5.7.7-mt8163fix` en MT8163.
4. Decidir si aplicar fixes de QS6 (`QS6Engine` + `NWDTunerAdapter` + `CompositeRadioEngineCallback`) y borrar `TopwayTunerAdapter.java`.
5. Si OK: consolidar WIP → release menor; push v5.7 cuando se autorice.

Fuera de alcance: Magisk K706 root, Z-Link/AA.

---

## Cómo verificar

```powershell
adb install -r "C:\@MIS PROYECTOS\K706_RE\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk"
adb logcat -v time -s MainActivity RadioServiceController MTK8259_8667Engine MTK8259_8667RM RadioMediaService
```

---

## Créditos
Csaba — V7/MT8768 y 8667 factory RDS.
