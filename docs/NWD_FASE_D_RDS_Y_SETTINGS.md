# Fase D — RDS, `Settings.System` y broadcasts NWD

**Objetivo:** documentar cómo la pila OEM publica **frecuencia**, **PS** y **Radio Text (RT)** fuera del AIDL, y cómo OpenRadioFM lo consume (shadow motor).

**Fuentes en repo:** `QS6Engine.java` (constantes, `BroadcastReceiver`, `ContentObserver`, normalización PS/frecuencia).  
**Fase A (contexto emisor):** [`NWD_FASE_A_MAPA_SISTEMA.md`](NWD_FASE_A_MAPA_SISTEMA.md) §A.3 (`ACTION_SEND_RADIO_FREQUENCE_NEW` desde `SprdRadioManager` / servicio radio).

**Fecha:** 2026-03-21  

---

## D.1 `Settings.System` — claves NWD

| Clave | Tipo API Android | Uso en OpenRadioFM |
|-------|------------------|---------------------|
| `nwd_radio_current_freq` | **`Settings.System.getInt`** | Frecuencia “cruda”; se convierte a **kHz** con `nwdSystemSettingFreqToKhz` |
| `nwd_radio_current_ps_data` | **`Settings.System.getString`** | PS RDS; a menudo **hex** (ver §D.3) |

**Registro:** `ContentObserver` sobre `Settings.System.getUriFor(...)` para **ambas** claves (`QS6Engine.setupShadowMotor`). Los cambios se procesan en el **main handler** asociado al observer.

**Efecto:** si el AIDL falla o se retrasa, la UI puede seguir al día vía Settings + broadcasts (evita “parálisis” descrita en `README.md` / informe QS6).

---

## D.2 Formato de `nwd_radio_current_freq` (lado cliente)

OpenRadioFM **no define** el valor que escribe el OEM; **interpreta** `freqRaw` así (`nwdSystemSettingFreqToKhz`):

| Rango típico `freqRaw` | Interpretación → kHz |
|-------------------------|----------------------|
| `8750`–`10800` | FM en **décimas de MHz** → `× 10` (ej. `9690` → `96900` kHz) |
| `87500`–`108000` | Ya en **kHz** FM |
| `76000`–`87499` | Banda extendida FM (kHz) |
| `100`–`1999` | MW/SW u otras bandas (kHz directos) |
| Otros (p. ej. divisible por 10 en rango medio) | Heurística para corregir ×10 erróneo en MW |

Esto está alineado con el mismo criterio que en **AIDL** `notifyCurrentFrequency` (FM `band < 3` → escala ×10).

---

## D.3 Formato de `nwd_radio_current_ps_data` (hex vs texto)

El firmware puede guardar:

1. **Cadena hexadecimal** de bytes RDS PS (p. ej. 8 bytes → **16** caracteres hex; a veces **32**).  
2. **Texto legible** (como en el extra AIDL `psName`).

OpenRadioFM detecta “blob hex” con `looksLikeNwdHexPsBlob` y decodifica con `decodeHexPsToAscii`. Si la cadena parece **solo dígitos** con forma de frecuencia (`looksLikeNumericFrequencyMasqueradingAsPs`), se **descarta** como PS para no mostrar “87600000” como nombre de emisora.

**Referencia código:** `normalizeNwdPsDisplay` (usado también en shadow **broadcast** y en callback AIDL).

---

## D.4 Dónde escribe el OEM (ingeniería inversa)

**APK / smali analizado:** `8925_Carkit_RadioService_VP2.1.6.apk` (`com.nwd.radio.service` VP2.1.6) — descompilado en  
`K706_RE\QS NWD\tools\nwd_radio_service_decompiled\` (y clases `com.nwd.kernel.*` embebidas en el mismo DEX).

### NWD-D001 — Escritura de `nwd_radio_current_freq` y `nwd_radio_current_ps_data`

| Campo | Valor |
|-------|--------|
| **Clase** | `com.nwd.radio.service.RadioProtocalUtil` |
| **Método** | `responseCurrentFrequency(byte[] aData, RadioManager aRadioManager)` (smali: `responseCurrentFrequency`) |
| **Cadena de llamadas** | Tráfico MCU → `RadioProtocalUtil.responseProtocal` → según tipo de mensaje → **`responseCurrentFrequency`** (`RadioManager.smali` ~L1673). |
| **API real** | `com.nwd.kernel.source.SettingTableKey.writeDataToTable(ContentResolver, key, int\|String)` → internamente **`Settings.System.putInt` / `putString`** (`SettingTableKey.smali`, líneas ~304 y ~407). |

**Condición para escribir en `Settings`:** solo si  
`SettingTableKey.getIntValue(cr, "mcu_current_source") == 4`  
(es decir, la fuente actual del MCU en tabla de ajustes es **4** — en esta pila suele asociarse a **radio FM**).

**Qué se escribe (mismo bloque, orden lógico):**

| Clave `Settings.System` | Tipo | Origen en protocolo |
|-------------------------|------|----------------------|
| `nwd_radio_current_freq` | **int** | Frecuencia parseada del buffer (`byte2int`, campo `frequency`) |
| `nwd_radio_current_band` | **int** | `bandType` (byte en payload) |
| `nwd_radio_current_freq_index` | **int** | `isPrefabFrequency` (byte tras banda) |
| `nwd_radio_prestore_index` + **sufijo banda** | **int** | Misma clave compuesta (`StringBuilder` + `bandType`) |
| `nwd_radio_current_ps_data` | **String** | **Hex** de **8 bytes** RDS PS: `RadioProtocalUtil.byteToHexString(aData, offset+4, 8)` — solo si `getProtocalDataLength(aData) > 5` |

Las constantes de nombre también están en `com.nwd.radio.service.RadioConstant` (`KEY_RADIO_CURRENT_FREQ`, `KEY_RADIO_CURRENT_PS_DATA`, etc.).

**Formato PS en Settings:** coincide con §D.3: el OEM guarda **cadena hexadecimal** (8 bytes → 16 caracteres hex), no texto ASCII directo.

### Lectura relacionada (otro APK / mismo ecosistema)

En `nwd_kernel_service_decompiled`, **`MCUDeviceManager.sendRadioCurrentData()`** **lee** `nwd_radio_current_freq`, `nwd_radio_current_band`, `nwd_radio_current_freq_index` (y otras claves `nwd_radio_*`) vía `SettingTableKey.getIntValue` para **reenviar estado radio al MCU**. No es la escritura principal de PS/freq desde el tuner, pero confirma que esas claves son el **canal Settings ↔ MCU**.

### Si repites el análisis en otro firmware

1. Ripgrep / JADX: `nwd_radio_current_freq` en `nwd_radio_service_decompiled/smali`.
2. Confirmar que sigue siendo `RadioProtocalUtil.responseCurrentFrequency` y la condición `mcu_current_source == 4`.

---

## D.5 Broadcasts — contrato consumido por OpenRadioFM

### `com.nwd.action.ACTION_SEND_RADIO_FREQUENCE_NEW`

| Extra | Tipo esperado (OpenRadioFM) | Notas |
|-------|------------------------------|--------|
| `extra_frequence` | numérico (`int` preferido; a veces `Byte`/`Short`/`String`) | `getNumericExtraAsInt` tolera tipos OEM |
| `extra_band` | numérico | `band < 3` → FM: `freqKhz = freqRaw * 10`; si no, `freqRaw` como kHz |
| `extra_ps_name` | `String` | PS; se normaliza igual que Settings (`normalizeNwdPsDisplay`) |

**Emisor típico (Fase A):** cadena **servicio ARM** → `SprdRadioManager` / managers → broadcast; receptores: UI NWD, **OpenRadioFM** (shadow).

**Convivencia con AIDL:** solo se llama `updateLocalState` si cambian frecuencia o PS respecto al estado local, para **reducir bucles** con callbacks AIDL.

### `com.nwd.action.ACTION_SEND_RADIO_RDS_RT`

| Extra | Tipo | Notas |
|-------|------|--------|
| `extra_rds_rt` | `String` | Radio Text; se limpia con `MetadataUtils.cleanRdsText` y se envía a `onRdsText` en el hilo principal |

**Paralelo AIDL:** el mismo RT puede llegar por `RadioCallback.notifyRtMessage`; shadow y AIDL pueden duplicar eventos — el cliente debe tolerar refrescos repetidos.

### Otras acciones (no registradas en OpenRadioFM)

En el mapa Fase A aparecen `ACTION_SEND_RADIO_FREQUENCE` (sin `_NEW`) y `ACTION_SEND_SCAN_RADIO_FREQUENCE`. OpenRadioFM **solo** escucha `_NEW` y `ACTION_SEND_RADIO_RDS_RT`. Si un firmware dejara de emitir `_NEW`, habría que ampliar el `IntentFilter`.

---

## D.6 Android 13+ (`API 33+`)

El receptor shadow se registra con **`Context.RECEIVER_EXPORTED`** porque los broadcasts provienen de **otra aplicación** (`com.nwd.*`). Sin este flag, el registro falla o no recibe intents en versiones recientes.

---

## D.7 Relación con la Fase C (AIDL)

| Canal | Frecuencia | PS | RT |
|-------|------------|----|----|
| `RadioCallback` | `notifyCurrentFrequency` | mismo callback (`psName`) | `notifyRtMessage` |
| Settings | `nwd_radio_current_freq` | `nwd_radio_current_ps_data` | — |
| Broadcast | `ACTION_SEND_RADIO_FREQUENCE_NEW` | `extra_ps_name` | `ACTION_SEND_RADIO_RDS_RT` |

Los tres pueden estar activos a la vez; OpenRadioFM deduplica parcialmente en shadow (`mLastReportedFreq` / `mLastReportedPs`) y en la lógica de notificación a la UI.

---

## D.8 Estado Fase D

| Tarea | Estado |
|-------|--------|
| Contrato Settings (`getInt` / `getString`, interpretación freq y PS) | **Hecho** (§D.1–D.3) |
| Contrato broadcasts + extras | **Hecho** (§D.5) |
| Clase OEM que hace `putInt`/`putString` | **Hecho** — §D.4 **NWD-D001** (`RadioProtocalUtil` → `SettingTableKey`) |

---

*Siguiente: Fase E (regresión multi-firmware) o completar NWD-D001 en §D.4 con JADX.*
