# Handoff — OpenRadioFM v5.7 + WIP 8667/K706

_Última actualización: 2026-11-18_

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` (**ahead ~9** vs `origin/main`, **sin push**) |
| Tag release | `v5.7` (local; no publicado) |
| Commit release | `79c0f5f1` — v5.7 HCN V31 / Junsun V7 |
| Último WIP | `b6243557` (+ limpieza código muerto MT8163 y auditoría QS6) |
| Working tree | WIP QS6 (bloques 1–5) **sin commit** (compila, APK generada) |
| App en WIP | `5.7.8-qs6fix` / versionCode `53` |
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
| Test QS6 fixes (**Usuario: OK en QS6 y MT8163**, faltan más tests) | `K706_RE\QS6_test\OpenRadioFM-5.7.8-qs6fix-TEST.apk` (vc `53`, acumulado: incluye todo lo anterior) |

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
- **Test usuario (18/11, con `5.7.8-qs6fix`):** funcionamiento general OK, salvo 2 bugs nuevos (abajo).

### MT8163 — bugs nuevos (18/11, detectados con `5.7.8-qs6fix`, SIN corregir)
- 🔴 **PowerOff no corta la FM:** el botón apagar minimiza la app, pero la radio FM sigue sonando. (Equivalente MT8163 de lo arreglado en K706: revisar qué hace `minimizeToHomeKeepingMediaControl()`/PowerOff con `MT8163Engine` — mute/`closeDevice`/canal de audio.)
- 🟠 **Mute desincronizado en UI:** al pulsar mute, el audio queda sin mutear pero el botón se queda en estado "mute activado". (Revisar `setMute` de `MT8163Engine` tras los cambios de 5.7.7 en `mRadioMuteDesired` / reintento de 120 ms, y de dónde lee la UI el estado de mute.)

### QS6 — auditoría revisada y fixes aplicados (18/11) → APK `5.7.8-qs6fix`

Auditoría contrastada con el código; todo lo aplicable está corregido (sin test en HW QS6).

1. **`QS6Engine.release(boolean persist)`** — con `persist=true` mantiene el motor vivo (antes caía al `default` → `closeDevice()` y cortaba la radio al recrear la Activity). Ruta: `LifecycleCoordinator.onDestroy()` → `DeviceManager.releaseAllResources(recreating)` → `mEngine.release(recreating)`. La radio va fija en el salpicadero (no rota), pero la recreación también ocurre por modo noche/`uiMode`, idioma o destrucción por memoria (`!isFinishing`).
2. **`CompositeRadioEngineCallback.combine(existing, added, addedFirst)`** — aplana y sustituye el receptor de la misma clase en vez de anidar. Usado en `MainActivity.onEngineReady()` y `RadioMediaService`. **También afecta a K706** (ya anidaba: su `release(true)` conserva callback) y al servicio en MT8163. Aplicado junto al punto 1 porque, solo, el 1 habría destapado el anidamiento en QS6.
3. **`QS6Engine.release()`** — `mReleased` + `mMainHandler.removeCallbacksAndMessages(null)` (warmup 2,2 s / play 450 ms) + `NWDTunerAdapter.clearCallback(mNwdCallback)`; callbacks NWD tardíos se descartan.
4. **`NWDTunerAdapter`** — flag `mWantBound`: `disconnect()` cancela reconexiones pendientes, hace `unlinkToDeath`, `unbindService` siempre (cubre bind en vuelo); `onServiceConnected` tardío tras disconnect se ignora; binder null → reintento; `binderDied` se procesa en main y solo reconecta si `mWantBound`; `setCallback()` des-registra el callback anterior. Campos `volatile`. (Doble `bind()` intencionado: se mantiene.)
5. **Hilo Binder / NPE** — todos los `notify*` hacen el trabajo dentro de `mMainHandler.post` leyendo `mCallback` en main (antes se comprobaba en Binder y se desreferenciaba en main → NPE si `release()` entre medias). `notifyRtMessage` ya no llama `onRdsText` en hilo Binder. `volatile` en estado compartido.
6. **`mcu_current_source`** — `syncToNwdSystemSettings(..., radioActive)` explícito: `switchToAndroidAudio()`/`closeDevice()` escriben `SOURCE_ANDROID` + `back_service_on=0`; `requestPlayAudio()` escribe RADIO; callbacks de frecuencia usan `isRadioAudioActive()` (`!mute && !streaming && !mAudioReleased`). Antes, al cerrar o en streaming quedaba `source=RADIO` y el siguiente arranque no pedía audio.
7. **Rebote de frecuencia** — ventana anti-rebote 1,5 s tras `tune()/tuneWithBand()`: `pollNwdSettingsAndFire()` ignora una frecuencia de Settings ≠ la pedida (log `poll settings: ignorando freq`). El callback AIDL cierra la ventana. *Plausible, no confirmado en HW: vigilar ese log.*
8. **`toggleRdsFeature`** — guarda `mAdapter`; `type 0` (RDS global, bit 0x80) alterna estado local (antes solo podía apagar); tipos desconocidos se ignoran.
9. **`TopwayTunerAdapter.java`** borrado (sin referencias ni reflexión).

**Test usuario 18/11:** QS6 OK hasta ahora (pendiente de más pruebas). 
**Qué probar en QS6:** arranque en frío con audio; cambio de emisora/seek sin saltos atrás; RDS PS/RT; AF/TA/RDS global ON↔OFF; streaming online ↔ FM; cerrar app → otras fuentes suenan y al reabrir hay audio; cambiar modo noche con la app abierta → la radio no se corta. **En K706 (Luis):** regresión rápida (UI + Android Auto/metadata sin eventos duplicados).
Logcat: `adb logcat -v time -s QS6Engine NWDTunerAdapter MainActivity RadioMediaService RadioServiceController`

---

## Pendiente (al retomar)

1. Contestación **Csaba** → `5.7.4-8667quit2` (RDS frío, QUIT, reabrir con audio+RDS).
2. Contestación **Luis** → `5.7.3-k706bg` (otras apps suenan tras PowerOff) y `5.7.6-k706stream` (PowerOff corta stream online).
3. MT8163: corregir los 2 bugs nuevos (PowerOff no corta FM; icono mute desincronizado) → nueva APK de test.
4. QS6: `5.7.8-qs6fix` OK en primeras pruebas del usuario; seguir probando + regresión K706 (Luis). Vigilar log `poll settings: ignorando freq` (si nunca aparece, valorar quitar la ventana anti-rebote).
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
