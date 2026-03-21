# Fase B — Audio y fuente NWD (FM)

**Fuente:** smali de `nwd_kernel_service_decompiled` + `nwd_radio_service_decompiled` (`QS NWD/tools`).  
**Fecha:** 2026-03-21  
**Relación OpenRadioFM:** `QS6Engine` (`requestPlayAudioInternal`, `notifyNwdThirdPartyRadioAppInOut`, `sendSourceSystemBroadcast`).

---

## B.1 Constantes de fuente (`SourceConstant`)

| Constante | Valor | Tipo en API |
|-----------|-------|-------------|
| `SOURCE_ANDROID` | **0x00** | `byte` |
| `SOURCE_RADIO` | **0x04** | `byte` |

Archivo: `com/nwd/kernel/source/SourceConstant.smali`.

**Uso en intents:** el extra se lee con **`getByteExtra`** — hay que enviar **`byte` (0x04)**, no solo `int` sin cuidado el tipo en algunos stacks.

---

## B.2 `ACTION_CHANGE_SOURCE`

| Campo | Valor |
|-------|--------|
| Acción | `com.nwd.action.ACTION_CHANGE_SOURCE` |
| Extra obligatorio | **`extra_source_id`** → **`byte`**: `0x04` = radio, `0x00` = Android |

### Quién escucha

| Componente | Archivo (referencia) | Comportamiento |
|------------|----------------------|----------------|
| `SprdRadioManager$1` | `SprdRadioManager$1.smali` | Si `newSource != 0x04` → `SprdFMFeature.ExitFm()`; si `== 0x04` → `InitFM()` |
| `AWRadioManager$1` | (misma lógica AW, ramas extra por plataforma) | Equivalente para Allwinner |
| `RadioService$4` | `RadioService$4.smali` | Delega en `RadioFeatureAbs.handleSourceChange(byte)` |
| `SourceMgr$3` | vía `ACTION_REQUEST_CHANGE_SOURCE` | Ver §B.3 |

### Emisión “oficial” indirecta

`KernelUtils.requestChangeSourceDirect(Context, byte sourceID)` construye:

- Acción: `com.nwd.action.ACTION_REQUEST_CHANGE_SOURCE`
- Extra: `extra_source_id` (byte)

`SourceMgr$3` recibe **`ACTION_REQUEST_CHANGE_SOURCE`**, lee `extra_source_id` y aplica regla `DirectChangeSourceRule` (no es el mismo string que `ACTION_CHANGE_SOURCE`, pero converge en el gestor de fuentes).

Las apps de terceros suelen emitir **`ACTION_CHANGE_SOURCE`** directamente (como OpenRadioFM con broadcast implícito), para que **radio service** y **Sprd/AW** reciban el cambio sin pasar solo por el request.

---

## B.3 `ACTION_APP_IN_OUT`

### Emisor de referencia — `KernelUtils.appStart`

```text
appStart(Context ctx, int appid)
  → appStart(ctx, appid, operation=1, event=0)

appStart(Context ctx, int appid, int operation, int event):
  Intent("com.nwd.action.ACTION_APP_IN_OUT")
  putExtra("extra_app_id", appid)
  putExtra("extra_app_operation", operation)
  putExtra("extra_app_event", event)
  sendBroadcast(intent)
```

Archivo: `KernelUtils.smali` (líneas ~58–93).

Constantes de nombre en `KernelConstant`: `EXTRA_APPID` / `EXTRA_OPERATION` / `EXTRA_EVENT` (strings anteriores).

### Valor por defecto crítico

En **`SprdRadioManager$1`** y **`AWRadioManager$1`** y **`SourceMgr$3`**:

```text
getIntExtra("extra_app_id", default)
```

El **default es 4** (AppID del **launcher** en `ApplicationList.xml`).

Si **no** envías `extra_app_id`, el sistema asume **launcher**, **no** radio → **no** se ejecuta la rama `appid == 8` y **no** se llama a `InitFM()` / `SendArmFmMediaPlay()` como corresponde a la app radio.

### Rama `appid == 8` (Spreadtrum) — `SprdRadioManager$1`

Si `action` es `ACTION_APP_IN_OUT` y `extra_app_id == 8`:

1. `SendArmFmMediaPlay()`
2. `AudioManager.setNwdStreamMute(3, true)` — stream **3** = típicamente música / uso OEM
3. Handler delayed `0x708` ms (~1800 ms) — mensaje 0
4. `SprdFMFeature.InitFM()`

### Rama `appid == 8` (Allwinner) — `AWRadioManager$1`

Misma condición `0x8`; pasos análogos con comprobaciones extra (`getSubPlatform`, cola de mensajes).

### Salida FM ARM

| Acción | Efecto en Sprd |
|--------|----------------|
| `com.nwd.android.ACTION_EXIT_ARM_FM_RAIDO` | `SprdFMFeature.ExitFm()` |
| `com.nwd.ACTION_MEDIA_PLAY` con `extra_app_id != 8` | `ExitFm()` |
| `ACTION_CHANGE_SOURCE` con `extra_source_id != 4` | `ExitFm()` |

---

## B.4 `SourceMgr` y `APP_IN_OUT` con `appid == 4`

En `SourceMgr$3`, si `extra_app_id == 4` y han pasado **&lt; 2500 ms** (`0x9c4`) desde un timestamp interno, se **ignora** (“ignore launcher in”) para evitar ráfagas al volver al launcher.

Para **appid 8** no aplica ese filtro en el mismo bloque — se deriva a `SourceMgr.access$31(...)` (gestión de aplicación / fuente).

---

## B.5 Orden recomendado al **abrir** FM (terceros)

Coherente con lo probado en QS6 y el código OEM:

1. **AudioFocus** (`USAGE_MEDIA` / equivalente OEM).
2. Opcional pero útil: `ACTION_START_NWD_ACTIVITY` (despertar actividad/stack NWD).
3. **`ACTION_CHANGE_SOURCE`** con **`extra_source_id = (byte) 0x04`**.
4. **`ACTION_APP_IN_OUT`** con **`extra_app_id = 8`**, **`extra_app_operation = 1`**, **`extra_app_event = 0`**.  
   - OpenRadioFM añade además `extra_app_in_out` / `extra_app_reset` alineados con otras rutas OEM; no están en el `appStart` mínimo pero son compatibles con intents explícitos.

5. **Bind AIDL** + handshake (`setRadioBackServiceOn`, etc.) según `QS6Engine`.

Entre 3 y 4, repetir en &lt; ~2,4 s puede provocar doble `InitFM` (ver coalescencia en `QS6Engine`).

### Orden al **cerrar** FM

1. `ACTION_EXIT_ARM_FM_RAIDO` (typo del firmware).
2. **`ACTION_CHANGE_SOURCE`** con **`extra_source_id = 0x00`** (Android).
3. Abandonar AudioFocus.

---

## B.6 Extras relacionados (teclas / frecuencia)

| Acción | Extra | Tipo | Uso |
|--------|-------|------|-----|
| `ACTION_KEY_VALUE` | `extra_key_value` | `byte` o `int` (TEST_KEY) | Panel físico → `RadioService$4` |
| `ACTION_SET_RADIO_FREQUENCE` | `extra_radio_frequence` | `int` | Sintonía vía broadcast |
| `ACTION_TEST_KEY` | `extra_key_value` | `int` | Pruebas |

---

## B.7 Plantilla hallazgo (Fase B)

```
ID: NWD-B001
Hecho: SprdRadioManager$1 solo ejecuta InitFM si extra_app_id==8 (default 4=launcher)
Archivo: nwd_radio_service_decompiled/.../SprdRadioManager$1.smali
OpenRadioFM: QS6Engine.notifyNwdThirdPartyRadioAppInOut
---
ID: NWD-B002
Hecho: ACTION_CHANGE_SOURCE usa extra_source_id byte; 0x04=InitFM, otro=ExitFm
Archivo: SprdRadioManager$1.smali
OpenRadioFM: QS6Engine (SOURCE_RADIO / SOURCE_ANDROID)
---
ID: NWD-B003
Hecho: KernelUtils.appStart → APP_IN_OUT + operation=1 + event=0
Archivo: KernelUtils.smali
OpenRadioFM: alineado
```

---

## B.8 Estado Fase B

| Tarea | Estado |
|-------|--------|
| `ACTION_CHANGE_SOURCE` + valores fuente | **Hecho** (§B.1–B.2) |
| `ACTION_APP_IN_OUT` KernelUtils + SourceMgr + Sprd/AW | **Hecho** (§B.3–B.4) |
| Extras y orden abrir/cerrar FM | **Hecho** (§B.5–B.6) |

**Pendiente opcional:** trazar en `SourceMgr.smali` el camino exacto desde `access$31` hasta el broadcast final de `ACTION_CHANGE_SOURCE` hacia el bus (profundización Fase B+).

---

*Continúa en Fase C (IPC AIDL).*
