# QS6 (NWD) — Informe MCU “directo” vía KernelService

Fecha: 2026-04-14  
Proyecto: OpenRadioFM  
Plataforma objetivo: QS6 / NWD (QS6 G5)

## Objetivo

Determinar si **OpenRadioFM** puede controlar el chip de radio FM en QS6 **sin depender del stack OEM de radio** (o como alternativa al `NWDTunerAdapter`), enviando comandos **directamente al MCU** mediante el servicio del sistema OEM:

- `com.nwd.kernel.service.KernelService`
- AIDL: `com.nwd.kernel.aidl.IKernelFeature`
- Método clave: `request([B)` → envía un `byte[]` que termina escribiéndose a UART/MCU.

## Hallazgos confirmados por RE (QS6 G5 dump)

### 1) Ruta “directa a MCU” existe (TX)

En el dump OEM:

- `KernelService` mantiene `UartCommunication` (UART) y expone `IKernelFeature` (Binder).
- `IKernelFeature.request([B)` termina llamando a `ProtocalUtil.writeDataToMCU(uartCommunication, aData)`.
- `ProtocalUtil` aplica checksum (`KernelProtocal.calCheckSumAndWriteEndOfData`) y escribe el frame a UART.

Conclusión: **sí existe un canal AIDL → UART → MCU** (al menos para TX).

### 2) Protocolo NWD (KernelProtocal) base

Constantes observadas:

- `HEADCODE = 0xF0`
- `TYPE_FM = 0x03`
- `TYPE_MCU_PASSTHROUGH = 0x20` (no usado en esta prueba)
- `dataType` define la operación específica.

Frame “corto” (longitudes típicas usadas por FM):

- `data[0] = 0xF0`
- `data[1] = length` (payload length “OEM”, limitado a `<= 0xFA` en el generador simple)
- `data[2] = type` (FM = `0x03`)
- `data[3] = dataType` (ej. `0x01` para “action”)
- `data[4]` reservado
- `data[5..]` payload
- `data[last]` checksum (suma OEM: bytes `1..len-2`, excluye head y excluye el checksum final)

### 3) Protocolo FM (TYPE_FM=0x03): comandos TX mapeados

Basado en `com.nwd.radio.service.RadioProtocalUtil` del dump OEM:

- **dataType `0x01`**: `requestAction(actionType, actionValue)`
  - Este `dataType` es el “mux” de acciones del tuner. Los valores **reales** (confirmados en `RadioManager.smali`) son:
    - **Seek estación** (`RadioManager.seek(Z)`), siempre con `actionValue=1`:
      - up: `(actionType=1, actionValue=1)`
      - down: `(actionType=2, actionValue=1)`
    - **Search/scan direccional** (`RadioManager.search(Z)`), siempre con `actionValue=1`:
      - up: `(3, 1)`
      - down: `(4, 1)`
    - **Cambio de banda** (`RadioManager.changeBand()`): `(5, 1)`
    - **AMS** (`RadioManager.AMS()`): `(6, 1)`
    - **INTRO** (`RadioManager.INTRO()`): `(7, 1)`
    - **Near / DX‑LOC** (`RadioManager.setNearOn(Z)`): `(8, 0|1)` (en el OEM el byte de valor parece **invertido**: ON→0, OFF→1)

Nota práctica: una confusión frecuente es asumir que `actionType` empieza en `0x00` para seek; **no**: en este stack el seek usa `0x01/0x02`.

- **dataType `0x03`**: `requestSetCurrentFrequency(freq, bandType, prefebIndex)`
  - En FM, `freq` va en **unidades NWD (10 kHz)** (ej.: 8750, 10150).

- **dataType `0x08`**: `requestSetRDSState(rdsState)`
- **dataType `0x09`**: `requestSetPTYIndex(idx)`
- **dataType `0x0C`**: `requestSaveCurrentFrequency(bandType, index, freq)`
  - payload compactado: `(band<<4) + index`, luego `freq L`, `freq H`.

- **dataType `0x0E`**: `requestRadioInfo()`
- **dataType `0x00`**: `requestSetBackServiceOn(on)` (mantener servicio “en background”)

### 4) RX (telemetría): dispatcher identificado (pendiente de suscripción real)

En el dump OEM, `RadioProtocalUtil.responseProtocal` contiene un `packed-switch` que despacha por `dataType` (valores `0x02..0x11`) hacia handlers como:

- `responseRadioState`
- `responseCurrentFrequency`
- `responseNear`
- `responseRDS`
- `responsePTY` / `responseCurrentPTY`
- `responseRadioRtMessage` (RadioText)
- `responseScanFrequencyInfo`
- etc.

**Punto crítico**: aunque el parsing RX está mapeado, falta confirmar si una app third‑party puede **recibir** esas tramas (callback AIDL, broadcast, ContentProvider, Settings, etc.). Este informe y la implementación adjunta se enfocan en **verificar TX/bind** de forma rápida.

## Implementación añadida en OpenRadioFM (para test en la unidad)

### 1) Cliente Binder sin stubs AIDL: `Qs6KernelMcuClient`

Archivo nuevo:

- `app/src/main/java/com/example/openradiofm/ui/main/Qs6KernelMcuClient.java`

Qué hace:

- Intenta `bindService()` contra `KernelService` usando:
  - action: `com.nwd.kernel.service.KernelService`
  - package: `com.nwd.kernel` (intent explícito)
- Ejecuta `IKernelFeature.request([B)` vía `IBinder.transact()`:
  - descriptor: `com.nwd.kernel.aidl.IKernelFeature`
  - transaction: `0x1`

Incluye helpers para construir frames FM (TYPE=0x03) para:

- `RADIO_INFO` (dataType `0x0E`)
- `BACK_SERVICE_ON/OFF` (dataType `0x00`)
- `ACTION` seek/band/AMS (dataType `0x01`)
- `TUNE` (dataType `0x03`, FM en 10 kHz)

### 2) Botonera en el menú de desarrollo QS6

Se añadió una sección en:

- `app/src/main/res/layout/dialog_qs6_engineering.xml`

Y el wiring en:

- `app/src/main/java/com/example/openradiofm/ui/main/QS6EngineeringDialog.java`

Botones:

- `BIND_KERNEL` (rebind duro)
- `RADIO_INFO`
- `BACK_ON / BACK_OFF`
- `STEP_0.5 << / >>` (código OEM: `RadioManager.seek`, pares `(2,1)/(1,1)` — en campo se percibe como **paso fino**)
- `SEEK_STN << / >>` (código OEM: `RadioManager.search`, pares `(4,1)/(3,1)` — en campo se percibe como **salto de emisora**)
- `BAND_CYC` + `AMS (FUERTE)`
- `TUNE 87.5 / TUNE 101.5`
- `INTRO` (OEM `INTRO`: `(7,1)`)
- `NEAR_DX / NEAR_LOC` (OEM `setNearOn`: `(8,0/1)`; nota: byte “invertido” vs nombre)
- `RDS_SHOW_STATE` (`dataType=0x0F`, frame vacío como OEM)
- `RDS=0`, `RDS=7`, `RDS^1`, `RDS^2`, `RDS^4` (`dataType=0x08`, **experimental**: puede cambiar comportamiento RDS)
- `PTY << / >>` (`dataType=0x09`)
- `SAVE_P1` (`dataType=0x0C`, usa frecuencia/banda actuales de OpenRadioFM)

UI adicional:

- `KERNEL_BIND`: `CONNECTED/DISCONNECTED`
- `LAST_MCU_CMD`: última acción enviada o error

Log del propio diálogo:

- Registra eventos `MCU` (TX y bind) y `MCU_ERR` (excepciones).

### Nota sobre nombres “OEM” vs sensación en radio

En el firmware NWD, los métodos Java se llaman `seek()` y `search()`, pero **no siempre coinciden** con la intuición “seek de emisora vs step fino” del usuario final. Por eso, en la UI de ingeniería se etiqueta por **efecto observado** (`STEP_0.5` vs `SEEK_STN`), manteniendo el mapeo binario exacto del OEM.

### Persistencia del `bind` al salir del menú

Por defecto, Android desconecta al cerrar un `Dialog` si el `ServiceConnection` vivía solo en ese objeto. OpenRadioFM mantiene un **`Qs6KernelMcuClient` compartido** para que el `KernelService` **siga conectado** entre aperturas del menú de desarrollo, y lo libera en `MainActivity.onDestroy()` cuando la Activity termina (`isFinishing()`).

Además, al abrir el menú se llama a `connect()` automáticamente (no hace falta pulsar `BIND_KERNEL` cada vez; el botón queda como “rebind duro” ante estados raros).

## Procedimiento de verificación (qué mirar)

1. Abrir el menú de ingeniería QS6 (Technical Matrix).
2. En la sección nueva, pulsar **BIND_KERNEL**.
3. Si queda en `CONNECTED`:
   - Pulsar `RADIO_INFO`
   - Probar `MCU_SEEK&gt;&gt;` o `TUNE 101.5`
4. Interpretación:
   - Si aparece `MCU TX ...` sin error: **el bind funciona y el TX probablemente sale**.
   - Si hay `SecurityException`, `bind=false` o se mantiene `DISCONNECTED`: probablemente **no es exportado o requiere permisos/uid de sistema**.
   - Si hay `DeadObjectException`/binder muerto: el servicio puede morir o rechazar transacciones de terceros.

## Validación en hardware (OpenRadioFM)

**Estado (2026-04-14): confirmado por prueba en unidad real** — la app **logra conectar/bind** con `KernelService` (el indicador `KERNEL_BIND` pasa a `CONNECTED` y el log muestra el bind OK).

Notas:

- Esto **valida el camino Binder → servicio OEM** para third‑party en esa unidad/firmware concreto.
- Observación de campo (misma sesión): **tune directo por `dataType=0x03` funciona** (87.5 / 101.5).
- Observación de campo (sesión siguiente, ya con mapeo correcto): los pares OEM `(1/2,1)` se perciben como **paso fino ~0.5**, y `(3/4,1)` como **seek de emisora**; **band cycle** OK; **AMS** como búsqueda “más fuerte”.
- `RADIO_INFO` y `BACK_ON/OFF` pueden **no tener efecto visible en UI** si solo disparan refresco interno del stack OEM o flags de “background service”, no parámetros audibles.
- Falta documentar aquí (cuando lo tengas a mano) **modelo exacto + build** para que el hallazgo sea reproducible en soporte.

## Riesgos / puntos abiertos

- **Acceso third‑party**:
  - En el dump parecía plausible; en hardware ya se confirmó **al menos el bind** en una unidad real. Aun así puede variar por firmware/firma/permisos:
    - `android:exported`, `permission`, `sharedUserId`, firmas, o checks runtime.
- **RX/subscripción**:
  - Incluso con TX funcionando, puede que no exista (para third‑party) una vía limpia para “escuchar” frames RX.
  - Alternativas a investigar si TX funciona:
    - si `KernelService` ofrece callback AIDL
    - si el OEM publica estado en `Settings.System` / propiedades / broadcasts
    - si el propio `RadioService` OEM es el que expone callbacks (y KernelService solo TX/RX interno).
- **Checksum/longitudes**:
  - Se implementó el checksum “suma OEM” para frames cortos. Si algún firmware usa variante distinta para ciertos tipos, habría que ajustar.
- **Unidades de frecuencia**:
  - En FM se usa 10 kHz (ej. 8750 para 87.5 MHz). En OpenRadioFM normalmente trabajamos con kHz (87500). En el test “directo MCU” usamos 10 kHz a propósito.

## Resultado esperado de esta fase

- Confirmar rápidamente si **QS6 permite bind y comandos TX** desde OpenRadioFM.
- **Bind: OK (confirmado en hardware).** Siguiente paso: validar **efecto audible/funcional** de `MCU_SEEK`/`TUNE` y, en paralelo, **enganchar RX/estado** (o inferirlo por Settings/broadcasts) para poder construir un motor “directo MCU” completo.

