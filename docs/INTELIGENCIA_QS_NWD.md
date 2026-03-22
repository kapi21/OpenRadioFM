# Inteligencia consolidada — pila NWD / QS6 (OpenRadioFM)

**Versión:** 1.0 (consolidado de fases A–D + implementación `QS6Engine`)  
**Fecha:** 2026-03-21  
**Unidad de referencia:** Carkit — `8925_Carkit_RadioService_VP2.1.6` / `8925_Carkit_KernelService_VP2.3.2` (capturas ADB + smali en `K706_RE\QS NWD\tools\`).

> **Ámbito legal:** análisis sobre firmware/APKs de **tu hardware** o copias que puedas analizar; objetivo **interoperabilidad** (radio de terceros), sin redistribuir binarios OEM.

---

## Tabla de contenidos

1. [Resumen ejecutivo](#1-resumen-ejecutivo)  
2. [Inventario software y versiones](#2-inventario-software-y-versiones)  
3. [ApplicationList y AppID 8](#3-applicationlist-y-appid-8)  
4. [Audio, fuente y orden de arranque FM](#4-audio-fuente-y-orden-de-arranque-fm)  
5. [Broadcasts críticos (mapa)](#5-broadcasts-críticos-mapa)  
6. [IPC AIDL: RadioFeature / RadioCallback](#6-ipc-aidl-radiofeature--radiocallback)  
7. [Settings.System y shadow motor](#7-settingssystem-y-shadow-motor)  
8. [RDS: máscara, AF/TA/TP y escritura OEM](#8-rds-máscara-aftatp-y-escritura-oem)  
9. [Banda FM1–FM3 y UI](#9-banda-fm1fm3-y-ui)  
10. [OpenRadioFM — implementación (`QS6Engine`)](#10-openradiofm--implementación-qs6engine)  
11. [Herramientas y rutas de trabajo](#11-herramientas-y-rutas-de-trabajo)  
12. [Hallazgos indexados (IDs)](#12-hallazgos-indexados-ids)  
13. [Pendientes y regresión](#13-pendientes-y-regresión)  
14. [Anexos (documentos por fase)](#14-anexos-documentos-por-fase)

---

## 1. Resumen ejecutivo

La radio **QS6** en unidades NWD se apoya en:

| Capa | Paquete / componente | Rol |
|------|------------------------|-----|
| UI OEM | `com.nwd.radio` | Actividad launcher, permisos FM |
| Servicio tuner + AIDL | `com.nwd.radio.service` (`RadioService`) | `RadioFeature` / `RadioCallback`, managers Sprd/AW/Arm |
| Kernel / fuentes | `com.nwd.kernel` + `com.nwd.setting.service` | `SourceMgr`, `KernelUtils`, routing audio |

**OpenRadioFM** integra esta pila mediante **`QS6Engine`**: bind AIDL, handshake, broadcasts implícitos (`ACTION_CHANGE_SOURCE`, `ACTION_APP_IN_OUT` con **`extra_app_id=8`**), AudioFocus, **shadow** (broadcasts + `ContentObserver` sobre claves `nwd_radio_*`), y reglas RDS/banda validadas frente a smali OEM.

---

## 2. Inventario software y versiones

*(Captura ADB real — reproducir tras OTA.)*

| Paquete | versionName | versionCode | Notas ruta unidad |
|---------|-------------|-------------|-------------------|
| `com.nwd.radio` | 1.1.0.3 | 1103 | Ej. OTA `/data/smallota/...` |
| `com.nwd.radio.service` | **2.1.6** | 216 | Ej. `/system/app/8925_Carkit_RadioService_VP2.1.6/...apk` |
| `com.nwd.kernel` | **2.3.2** | 232 | Ej. `/system/app/8925_Carkit_KernelService_VP2.3.2/...apk` |

**Carpeta herramientas PC:** `C:\@MIS PROYECTOS\K706_RE\QS NWD\tools\` — APK pull + `nwd_radio_service_decompiled`, `nwd_kernel_service_decompiled`.

Otros paquetes `com.nwd.*` (música, vídeo, BT, CAN, skins, etc.) en inventario extendido → anexo **Fase A**.

---

## 3. ApplicationList y AppID 8

Archivo kernel: `assets/ApplicationList.xml`.

| AppID | Paquete | Source audio |
|-------|---------|--------------|
| **8** | **`com.nwd.radio`** | Sí (crítico) |
| 4 | `com.android.launcher` | No |

**`SprdRadioManager$1` / `AWRadioManager$1`:** `getIntExtra("extra_app_id", **4**)` — default = launcher.  
Sin **`extra_app_id=8`** en `ACTION_APP_IN_OUT`, **no** se ejecuta la rama que llama **`InitFM()`** / enrutado correcto para la app radio.

OpenRadioFM: `QS6Engine.NWD_APPLICATION_ID_RADIO = 8`.

---

## 4. Audio, fuente y orden de arranque FM

### Constantes fuente (`SourceConstant`)

| Nombre | Valor | Uso |
|--------|-------|-----|
| `SOURCE_ANDROID` | `0x00` | Extra **`byte`** `extra_source_id` |
| `SOURCE_RADIO` | `0x04` | Radio FM |

### `ACTION_CHANGE_SOURCE`

- Extra: **`extra_source_id`** como **`byte`**.
- Sprd/AW: `0x04` → `InitFM`; distinto → `ExitFm`.

### `ACTION_APP_IN_OUT`

- Mínimo alineado con `KernelUtils.appStart`: `extra_app_id`, `extra_app_operation=1`, `extra_app_event=0`.
- OpenRadioFM añade extras adicionales compatibles (`extra_app_in_out`, etc.).

### Orden recomendado (abrir FM)

1. AudioFocus (`USAGE_MEDIA` coherente con OEM).  
2. Opcional: `ACTION_START_NWD_ACTIVITY` (`pkg=com.nwd.radio`).  
3. `ACTION_CHANGE_SOURCE` + `0x04`.  
4. `ACTION_APP_IN_OUT` + **`extra_app_id=8`**.  
5. Bind AIDL + handshake.

**Throttle:** no repetir `ACTION_CHANGE_SOURCE` / `APP_IN_OUT` en ráfaga (&lt; ~2,4 s) — `QS6Engine` coalescence.

### Cerrar FM

1. `com.nwd.android.ACTION_EXIT_ARM_FM_RAIDO` (typo OEM).  
2. `ACTION_CHANGE_SOURCE` / request con `0x00`.  
3. Abandonar AudioFocus.

### Salida ARM

- `ACTION_EXIT_ARM_FM_RAIDO` → `ExitFm()` en stack Sprd.

---

## 5. Broadcasts críticos (mapa)

| Acción | Emisor típico | Consumidor / nota |
|--------|---------------|-------------------|
| `ACTION_APP_IN_OUT` | Apps, `KernelUtils` | Sprd/AW, `SourceMgr` |
| `ACTION_CHANGE_SOURCE` | Radio, kernel | Sprd, `RadioService`, SourceMgr |
| `ACTION_SEND_RADIO_FREQUENCE_NEW` | Servicio ARM | UI NWD, **shadow OpenRadioFM** |
| `ACTION_SEND_RADIO_RDS_RT` | Servicio | RT en shadow |
| `ACTION_REQUEST_CHANGE_SOURCE` | Varios | Converge en SourceMgr |

**OpenRadioFM shadow:** registra `_FREQUENCE_NEW` + `_RDS_RT` con **`RECEIVER_EXPORTED`** (API 33+).

Diagrama flujo: OpenRadioFM → `APP_IN_OUT` / `CHANGE_SOURCE` → kernel; bind → `RadioService`; servicio → broadcast frecuencia → OpenRadioFM.

---

## 6. IPC AIDL: RadioFeature / RadioCallback

### Bind

- Paquete: `com.nwd.radio.service`  
- Acción: `com.nwd.radio.service.ACTION_RADIO_SERVICE`  
- `onBind` → instancia `RadioFeatureAbs` (subclase `RadioFeature$Stub`).

### `RadioFeature` — orden = `TRANSACTION_*` (no reordenar `.aidl`)

Métodos 1–30: `setCurrentFrequency` … `getCurrentScanState` (`0x01`–`0x1e`).  
Descriptor: `com.nwd.radio.service.RadioFeature`.

Typos OEM conservados: **`Strero`**, **`prefeb`**.

### `RadioCallback` — 14 métodos (`0x01`–`0x0e`)

Incluye: `notifyCurrentFrequency`, `notifyRDSStateChange`, `notifyCurrentIsTA`, `notifyRtMessage`, `notifyRadioScanState`, etc.  
Descriptor: `com.nwd.radio.service.RadioCallback`.

### Handshake OpenRadioFM (post-`onServiceConnected`)

1. `linkToDeath`  
2. Hilo: `registCallback` → `setRadioBackServiceOn(true)` → `INTRO()` → `getRadioState()` + sync estado UI (scan, Near, RDS, estéreo).  
3. Main +350 ms: segundo `setRadioBackServiceOn(true)`.

Cierre: `unRegistCallback`, `setRadioBackServiceOn(false)`.

### Hilos

- Llamadas salientes: binder sync (`performAidlCall` synchronized).  
- Callbacks entrantes: hilo binder → **`Handler` main** antes de UI.

### Cobertura API en cliente

Muchos métodos `RadioFeature` no usados aún (`seek`, `getRadioPoint`, …); callbacks PTY/prefabs parcialmente conectados — detalle en anexo **Fase C+**.

---

## 7. Settings.System y shadow motor

### Claves principales

| Clave | Uso |
|-------|-----|
| `nwd_radio_current_freq` | int → kHz vía heurística `nwdSystemSettingFreqToKhz` |
| `nwd_radio_current_ps_data` | String; a menudo **hex** PS → `normalizeNwdPsDisplay` |
| `nwd_radio_current_band` | int; banda OEM → UI FM1–FM3 / AM |
| `nwd_radio_rds_enable` | int máscara RDS (MCU); bit **TP** |
| `nwd_radio_rds_mask` / RDS | ver §8 |
| `nwd_radio_dx_loc_enable` | **0** = modo **LOC** (local), **1** = modo **DX** — ver §7.1 |

**Escritura freq/PS/banda** (NWD-D001): `RadioProtocalUtil.responseCurrentFrequency` → `SettingTableKey.writeDataToTable` → `Settings.System`, si `mcu_current_source == 4`.

**Lectura MCU:** `MCUDeviceManager.sendRadioCurrentData()` lee varias `nwd_radio_*` para reenvío al MCU.

### 7.1 DX / LOC y `setNearOn` (logcat OEM `com.nwd.radio`, 2026-03-22)

Una pulsación del botón LOC/DX (`PlayradiocmdCommFunOnOFF__Loc`) llama a **`RadioFeature.setNearOn(boolean)`** (`actionType = 8`). Convención **Java / AIDL** (la que usa OpenRadioFM):

| Modo en cabecera | `setNearOn` / `isNearOn` | `UPDATE_LOC_DX_STATE` | `nwd_radio_dx_loc_enable` | Log `near state` (MCU) |
|------------------|--------------------------|------------------------|---------------------------|-------------------------|
| **LOC** (local) | **true** | `[true]` | **0** | **0** |
| **DX** (distancia) | **false** | `[false]` | **1** | **1** |

- **`isNearOn() == true`** ⇔ **LOC** ⇔ OpenRadioFM `RadioEngine.isDxLocal() == true` (nombre heredado: “DxLocal” = *estás en local*).
- El entero **`near state`** en `RadioProtocalUtil` y el **setting** `dx_loc_enable` siguen la pista **0 = LOC, 1 = DX**; no confundir con el booleano AIDL (son capas distintas; el servicio traduce).

---

## 8. RDS: máscara, AF/TA/TP y escritura OEM

### `RadioManager.getRDSState(int)`

Implementación base: **`(mRDSState & aRDSType) != 0`** (máscara de bits del MCU).

Constantes tipo `RadioConstant.RDSType` (smali):

| Bit / valor | Significado |
|-------------|-------------|
| `0x1` | AF |
| `0x2` | TA (interruptor / bit en máscara) |
| `0x8` | **TP** (Traffic Program) |
| `0x80` | RDS global |

### TP “real” en OpenRadioFM

- **`getRDSState(0x8)`** cuando el stub usa máscara (`RadioManager`).  
- **`ArmRadioManager.getRDSState`** solo trata `0x80`, `1`, `2` → TP por AIDL puede fallar → fallback **`Settings.System` `nwd_radio_rds_enable`** (misma máscara que escribe `responseRDS` / `setRDSState_internal`).

### `toggleRdsFeature` (contrato `RadioEngine`)

- Tipo **1** → AF: `setRDSState((byte)1, …)`  
- Tipo **2** → TA: `setRDSState((byte)2, …)` — **no** confundir con TP.  
- Tipo **0** → RDS global: `setRDSState((byte)0x80, …)`  
- **`notifyCurrentIsTA`:** anuncio **on-air** (icono TA); no es el bit TP.

### `responseRDS` (MCU → servicio)

Actualiza `mRDSState`, `notifyDataChange(CHANGE_RDS_STATE)` → `notifyRDSStateChange` en cliente.

---

## 9. Banda FM1–FM3 y UI

### Problema resuelto

Coerción antigua: toda frecuencia en aire FM (65–120 MHz) se mapeaba a **FM1**, ocultando FM2/FM3 hasta saltar a AM.

### Regla actual (`coerceQs6BandForDisplay`)

- En rango FM kHz: si NWD envía banda **0, 1 o 2**, se **respeta** (FM1/FM2/FM3).  
- Valores raros en FM → FM1.  
- MW/SW: banda ≥ 3 o forzar AM mínimo según frecuencia.

### Sincronía adicional

- Shadow **broadcast**: `extra_band` → `onBandChanged` si cambia banda aunque la frecuencia sea igual.  
- **`ContentObserver`** sobre **`nwd_radio_current_band`**.  
- Tras **`changeBand()`**: `getCurrentFrequency()` AIDL → empuje UI (`pushUiFromCurrentFrequencyAidl`).

---

## 10. OpenRadioFM — implementación (`QS6Engine`)

**Archivo:** `app/src/main/java/com/example/openradiofm/data/source/QS6Engine.java`  
**AIDL:** `app/src/main/aidl/com/nwd/radio/service/*.aidl`  
**Cableado:** `RadioServiceController` (motor compartido), `MainActivity` modo `FM_QS6`.

### Funciones clave

| Área | Comportamiento |
|------|----------------|
| Fuente / audio | `requestPlayAudioInternal`, `notifyNwdThirdPartyRadioAppInOut`, `sendSourceSystemBroadcast`, AudioFocus reclaim |
| AIDL | Handshake, `performAidlCall`, `DeadObject` / rebind |
| Shadow | `BroadcastReceiver` + `ContentObserver` (freq, PS, **band**, **RDS mask**) |
| RDS | `refreshTpTrafficProgramFromNwd`, `toggleRdsFeature` por máscara, callbacks RDS/PTY/raw |
| Banda | `coerceQs6BandForDisplay`, `updateLocalState(..., band)`, `pushUiFromCurrentFrequencyAidl` |
| Estéreo | Piloto + decodificador (`notifyStereo` / `notifyStereoOn`) |
| **Ecualizador / ajustes audio (botón EQ)** | **Logcat OEM:** evento `PlayradiocmdCommon__GotoAudioSetting` → **`com.nwd.audioset/.home_horizontalActivity`**. `openEq`: (1) `ComponentName` explícito + `getLaunchIntentForPackage("com.nwd.audioset")`; (2) reserva **`com.nwd.eq`**; (3) bridge **`ACTION_START_NWD_ACTIVITY`** + `pkg` primero **`com.nwd.audioset`**, luego **`com.nwd.eq`**; fallback **`Settings.ACTION_SOUND_SETTINGS`**. |
| **Menú ingeniería (Technical Matrix)** | Mismo easter egg que K706/MT8163: **icono GPS ×5** en ≤3 s. Abre `QS6EngineeringDialog` (`dialog_qs6_engineering.xml`): bind AIDL, AF/TA/TP, piloto/decodificador estéreo, RDS, latencia `getCurrentFreq`, OEM Media prefs, log terminal; botones **OPEN_EQ_OEM** y **WAKE_NWD** (`wakeNwdRadioFromEngineeringMenu`). |

### Documentación de producto

- `docs/INFORME_MOTOR_QS6_NWD.md`  
- `README.md` (notas QS6)

---

## 11. Herramientas y rutas de trabajo

1. **ADB:** `scripts/nwd_pm_info.bat`, `docs/nwd_adb_comandos_windows.md`  
2. **Apktool** → smali + manifest  
3. **JADX** → lectura Java aproximada  
4. **Logcat** correlacionado con `MediaFocusControl`, `BroadcastQueue`

Comandos típicos:

```bat
adb shell pm path com.nwd.radio.service
adb pull /system/app/.../8925_Carkit_RadioService_VP2.1.6.apk "C:\@MIS PROYECTOS\K706_RE\QS NWD\tools\"
```

---

## 12. Hallazgos indexados (IDs)

| ID | Resumen |
|----|---------|
| **NWD-B001** | `APP_IN_OUT` default `extra_app_id=4` → InitFM solo con **8** |
| **NWD-B002** | `ACTION_CHANGE_SOURCE` byte `0x04` / `0x00` |
| **NWD-C001** | `TRANSACTION_*` AIDL alineados con stubs APK |
| **NWD-C002** | Handshake post-bind documentado |
| **NWD-D001** | Escritura Settings: `RadioProtocalUtil.responseCurrentFrequency` + `SettingTableKey` |

Plantilla ampliada: ver `ESTUDIO_INGENIERIA_INVERSA_APP_NATIVA_NWD.md` §5.

---

## 13. Pendientes y regresión

| Ítem | Estado |
|------|--------|
| Comparar `TRANSACTION_*` entre dos firmwares | Pendiente (sin segundo APK) |
| **Fase E:** tabla versiones MCU/build vs diferencias | Pendiente |
| Diff parcelables `Frequency`/`RadioPoint` vs jadx | Opcional |
| Botón EQ → `com.nwd.audioset` / `home_horizontalActivity` | Cubierto en §10 y `QS6Engine.openEq` (logcat 2026-03-22) |

Tras **OTA**: repetir `adb pull`, versiones en §2, y validar `QS6Engine` en unidad.

---

## 14. Anexos (documentos por fase)

| Anexo | Contenido |
|-------|-----------|
| [`NWD_FASE_A_MAPA_SISTEMA.md`](NWD_FASE_A_MAPA_SISTEMA.md) | Paquetes, ADB, broadcasts, ApplicationList, mermaid |
| [`NWD_FASE_B_AUDIO_Y_FUENTE.md`](NWD_FASE_B_AUDIO_Y_FUENTE.md) | Fuente, Sprd/AW, SourceMgr, orden abrir/cerrar |
| [`NWD_FASE_C_IPC_AIDL.md`](NWD_FASE_C_IPC_AIDL.md) | Tablas TRANSACTION, handshake, C+ cobertura API |
| [`NWD_FASE_D_RDS_Y_SETTINGS.md`](NWD_FASE_D_RDS_Y_SETTINGS.md) | Settings, broadcasts consumidos, NWD-D001 extendido |
| [`ESTUDIO_INGENIERIA_INVERSA_APP_NATIVA_NWD.md`](ESTUDIO_INGENIERIA_INVERSA_APP_NATIVA_NWD.md) | Plan maestro, checklist, plantilla hallazgos |
| [`INFORME_MOTOR_QS6_NWD.md`](INFORME_MOTOR_QS6_NWD.md) | Informe integración desde perspectiva app |

---

**Rama Git recomendada:** `QS_NWD` — cambios de motor, AIDL y esta documentación.

*Documento único de inteligencia — OpenRadioFM / NWD QS6.*
