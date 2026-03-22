# Comparativa — motor K706 OpenRadioFM vs radio OEM (`com.android.fmradio.ext`)

Solo **comparativa** (sin juicio de “mejor/peor”). Fuentes: `K706Engine.java`, `K706RadioManager.java` y estudio OEM ([`ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md`](ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md) §4–7, §11). **§8–§9:** scan y RDS.

---

## 1. Rol en el sistema

| Aspecto | OEM | OpenRadioFM |
|--------|-----|-------------|
| Paquete / identidad | `com.android.fmradio.ext`, **`android.uid.system`** | `com.example.openradiofm`, app usuario |
| Entrada al tuner | `FmService` + `FmManagerSelect` + `TunerManagerForExt` | **`K706RadioManager`** (no extiende la app OEM) |
| API “oficial” radio coche | Servicio `IFmRadioService` (acción `com.android.fmradio.IFmRadioService`) | **`IRadioServiceAPI`** (`com.hcn.autoradio`) vía AIDL + **reflexión** sobre MCU / QF / Broadcom |

**Lectura:** mismo ecosistema **QuickFish + Broadcom**, distinto **envoltorio**: la OEM es la app de sistema; OpenRadioFM **reimplementa** el control con reflexión y AIDL HCN.

---

## 2. Pila de hardware / SDK

| Capa | OEM (estudio) | OpenRadioFM |
|------|----------------|-------------|
| MCU / audio | `McuManager` implícito en el stack del servicio | **`RPC_SetChannel`**, `RPC_SetVolumeMute`, comandos `0xA0` + subcomandos (`TunerCmdFactory`) |
| Tuner alto nivel | **`QFTunerManager`** (`TunerManagerForExt`) | **`QFTunerManager`** por reflexión (`initQFTunerManager`) |
| RDS chip | Broadcom vía `FmReceiverService` en el flujo AOSP | **`initBroadcomFmReceiverService`** + `setRdsMode` vía reflexión |
| Frecuencia / seek | `FmService` → manager | `gotoFreq`, `onSeekUp/Down`, scan vía QF + MCU según caso |

**Coincidencia fuerte:** ambos usan **QF + Broadcom**; OpenRadioFM **documenta** explícitamente el paralelo con la app nativa.

---

## 3. AudioFocus

| Aspecto | OEM | OpenRadioFM |
|--------|-----|-------------|
| API | `requestAudioFocus(listener, STREAM_MUSIC, GAIN)` clásico | **`AudioFocusRequest`** / `AudioAttributes` + `OnAudioFocusChangeListener` (API 26+ / fallback) |
| Pérdida permanente | `handlePowerDown()` → apaga FM lógico (`powerDown`) | **`AUDIOFOCUS_LOSS`**: mute, `SetChannel(4)`, broadcast OEM, abandono de foco según flujo |
| Pérdida transitoria | `handlePowerDown` + `forceToHeadsetMode` + flag transitorio | **`LOSS_TRANSIENT`**: mismo patrón canal 4 + mute + recuperación / anti-spurios |
| Duck | `setMute(true)` en CAN_DUCK | Cubierto en la misma cadena de foco (según rama) |

**Lectura:** **misma semántica** Android (LOSS / TRANSIENT / GAIN); la OEM **no** expone `PhoneState`; OpenRadioFM **sí** añade telefonía (§4).

---

## 4. “Apagado” FM ante interrupción

| OEM (`powerDown` en estudio) | OpenRadioFM (`K706RadioManager`) |
|------------------------------|-----------------------------------|
| `setMute(true)` | `setMute(true)` (vía reflexión MCU) |
| RDS off | RDS / toggles según implementación y QF |
| `enableFmAudio(false)` / patch | **`SetChannel(4)`** + lógica de recuperación / streaming |
| `FmManagerSelect.powerDown()` | Cierre vía **`closeDevice`**, heartbeats, flags `mIsRadioActive`, etc. |

**Lectura:** OEM **encierra** apagado en `FmService`; OpenRadioFM **descompone** en MCU + foco + estados propios + **PhoneStateListener** para llamadas.

---

## 5. Llamadas telefónicas

| OEM | OpenRadioFM |
|-----|-------------|
| No hay **`TelephonyManager` / `PhoneStateListener`** en el FM analizado; depende del **dialer** tomando foco | **`PhoneStateListener`** explícito: RINGING/OFFHOOK → mute + **`SetChannel(4)`**; IDLE → restaurar canal 2 + audio |

**Lectura:** OpenRadioFM **refuerza** lo que en la OEM asume el **stack de audio como app sistema**; coherente con permisos y UID distintos.

---

## 6. Capas altas de la app

| OEM | OpenRadioFM |
|-----|-------------|
| `FmMainActivity`, vistas OEM, `FmProvider` | **`MainActivity`**, `PlaybackManager`, temas, Android Auto, etc. |
| `MediaSession` dentro de `FmService` | **`RadioMediaService`** + `K706RadioManager` compartido / broadcasts `ACTION_OEM_AUDIO_FOCUS` |

**Lectura:** misma necesidad (foco + sesión); arquitectura UI **distinta** (OpenRadioFM es app única multi-motor).

---

## 8. Scan / AutoScan / parada

| Aspecto | OEM (estudio smali) | OpenRadioFM (`K706RadioManager` / `K706Engine`) |
|--------|----------------------|--------------------------------------------------|
| **API principal** | `TunerManagerForExt` → **`QFTunerManager.autoScan()`**, **`stopScan()`** (y seek/preset vía QF) | Igual criterio: primero **`QFTunerManager.autoScan()`** / **`stopScan()`** por reflexión |
| **Arranque scan** | Flujo encapsulado en app OEM | **`onScanEvent()`**: si existe `mTunerAutoScan` → `invoke(autoScan)` y `mIsScanning = true`; si no → **`sendCmd(SUB_TUNE_AS, …)`** = MCU **0x08** (`SUB_TUNE_AS`) |
| **Parada scan** | `stopScan()` en QF | **`onPSEvent()`** (nombre heredado del AIDL; actúa como *stop scan*): **`stopScan()`** en QF; fallback **`sendCmd(SUB_AUTO_SCAN_STOP)`** = **0x0C** |
| **Estado en UI** | Servicio + callbacks internos | **`fireEvent(108, …)`** → `K706Engine` → **`onScanStatusChanged`**; `IsScan()` / `isScanning()` reflejan `mIsScanning` |
| **Seek** (relacionado) | `onSeek(Z)` / `QFTunerManager` | **`onSeekUp/DownEvent`** vía QF (`onSeek`) o MCU según implementación |

**Lectura:** la OEM y OpenRadioFM **coinciden en la estrategia**: scan “de verdad” por **`QFTunerManager`**, con posible **plan B por MCU** (`0x08` / `0x0C`) si la reflexión falla. OpenRadioFM deja el fallback **explícito** en código.

---

## 9. RDS (recepción, toggles, chip)

### 9.1 Recepción de datos (PS, RT, PTY, PI, flags)

| OEM | OpenRadioFM |
|-----|-------------|
| Callbacks del stack QF / Broadcom hacia `FmService` / UI | **Paquetes MCU** (`handleMcuData`: p.ej. **0xB3** AF/TA flags) + flujo **PS/RT/PTY** parseado en `K706RadioManager` → **`fireEvent`** códigos **103–107**, **111** |
| — | Códigos unificados en **`K706Engine.handleCallback`**: 103 PS, 104 RT, 105 PTY, 107 PI, 111 AF/TP vía prefijos `AF:` / `TP:` |

**Lectura:** misma **información RDS** al final (nombre, texto, tipo, PI); la OEM la **empaqueta** en su servicio; OpenRadioFM la **normaliza** a la interfaz `RadioEngineCallback`.

### 9.2 Activación / toggles (RDS global, AF, TA, PTY)

| Función | OEM (estudio `QFTunerManager`) | OpenRadioFM |
|---------|---------------------------------|-------------|
| RDS global | `setRdsSwitch(B)` | **`toggleRdsFeature(0)`**: Broadcom **`setRdsMode`** (si hay servicio) + **`QFTunerManager.setRdsSwitch(1)`** |
| AF | `setRdsAFSwitch()` | **`toggleRdsFeature(1)`**: **`setRdsAFSwitch()`** o fallback MCU **`SUB_RDS_AF` (0x11)** + Broadcom **`enableSilentlyRdsFeatures`** |
| TA | `setRdsTASwitch()` (en OEM está en API) | **`toggleRdsFeature(2)`**: **solo estado software** — no se llama `setRdsTASwitch()` en caliente (comentario en código: evita *TA seek* no deseado) |
| PTY / filtros | `setRdsPtyType(B)` | Tras encender audio: **`setRdsPtyType(0)`** vía QF; **`sendRdsCmd`** con prefijo MCU **0xA2** para otros RDS |
| Chip Broadcom | `FmReceiverService` / `setRdsMode` en flujo AOSP | **`enableBroadcomRdsFeatures`**, **`enableSilentlyRdsFeatures`**, reflexión **`setRdsMode(rdsMode, rdsFeatures, afMode, afThreshold)`** |

**Lectura:** **mismas piezas** (QF + Broadcom + MCU); OpenRadioFM añade **fallbacks MCU** y **reglas de producto** (p. ej. TA sin llamada QF que dispare seek).

### 9.3 Arranque FM y RDS

| OEM | OpenRadioFM |
|-----|-------------|
| Secuencia dentro de `FmService` / `powerUp` | Secuencia **`setAudioParams` / `enforceAudioChannelRecovery`**: tras **`requestAudioFocus`**, **`setRdsSwitch(1)`**, **`setRdsPtyType(0)`**, luego **`enableBroadcomRdsFeatures`** |

**Lectura:** objetivo alineado: **RDS encendido** tras tener audio/foco; la OEM lo hace en un solo servicio; OpenRadioFM lo **desglosa** con comentarios de depuración (PID 4140, canal 2, etc.).

---

## 10. Resumen en una frase

- **OEM:** un **`FmService`** de sistema que baja FM con **AudioFocus** y **`powerDown`** sobre **QF/Broadcom**; scan y RDS pasan por **`TunerManagerForExt` / `QFTunerManager`** como en el stack QuickFish.  
- **OpenRadioFM:** **`K706RadioManager`** usa las **mismas APIs QF/Broadcom** (por reflexión), **mismos fallbacks MCU** para scan (0x08/0x0C) y RDS (0x11, 0xA2…), y la **misma separación conceptual** scan=QF, tune base=MCU; añade **MCU explícito** en audio (`SetChannel`), **AIDL HCN**, **recuperación ante LOSS**, **`PhoneStateListener`**, y **política propia** en TA/PTY.

Este documento es solo **comparativa**; el estudio detallado de la OEM sigue en [`ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md`](ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md).
