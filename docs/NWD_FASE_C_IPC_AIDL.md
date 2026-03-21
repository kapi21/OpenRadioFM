# Fase C — IPC AIDL (`RadioFeature` / `RadioCallback`)

**Fuente:** `RadioFeature$Stub.smali`, `RadioCallback$Stub.smali`, `RadioService.smali`, `ArmRadioManager.smali` (`nwd_radio_service_decompiled`); `QS6Engine.java`.  
**Fecha:** 2026-03-21  

---

## C.1 Bind al servicio

| Campo | Valor |
|-------|--------|
| Paquete | `com.nwd.radio.service` |
| Clase servicio | `com.nwd.radio.service.RadioService` |
| Acción explícita (manifest) | `com.nwd.radio.service.ACTION_RADIO_SERVICE` |
| `onBind` | Devuelve **`mRadioFeature`**: instancia concreta de **`RadioFeatureAbs`** (subclase de **`RadioFeature$Stub`**) según plataforma (p. ej. `ArmRadioManager`, `SprdRadioManager`, `RadioManager`). |

OpenRadioFM usa `bindService` con el `ComponentName` / intent alineado con lo anterior.

---

## C.2 Comparación AIDL ↔ `TRANSACTION_*` del APK

Los **códigos de transacción** en el APK descompilado coinciden con el orden **estricto** de:

- `OpenRadioFM/app/src/main/aidl/com/nwd/radio/service/RadioFeature.aidl`
- `OpenRadioFM/app/src/main/aidl/com/nwd/radio/service/RadioCallback.aidl`

No reordenar métodos en los `.aidl` sin volver a generar stubs y verificar el binario OEM.

### `RadioFeature` — `RadioFeature$Stub.smali`

| # | Método (Java) | `TRANSACTION_*` (hex) |
|---|----------------|------------------------|
| 1 | `setCurrentFrequency` | `0x01` |
| 2 | `getCurrentFrequency` | `0x02` |
| 3 | `seek` | `0x03` |
| 4 | `search` | `0x04` |
| 5 | `changeBand` | `0x05` |
| 6 | `AMS` | `0x06` |
| 7 | `INTRO` | `0x07` |
| 8 | `setNearOn` | `0x08` |
| 9 | `isNearOn` | `0x09` |
| 10 | `isHasStrero` | `0x0a` |
| 11 | `setStreroOn` | `0x0b` |
| 12 | `isStreroOn` | `0x0c` |
| 13 | `setRadioBackServiceOn` | `0x0d` |
| 14 | `isRadioBackServiceOn` | `0x0e` |
| 15 | `setRDSState` | `0x0f` |
| 16 | `getRDSState` | `0x10` |
| 17 | `setPTYType` | `0x11` |
| 18 | `getPTYType` | `0x12` |
| 19 | `getPrefabPTYType` | `0x13` |
| 20 | `saveCurrentFrequency` | `0x14` |
| 21 | `getPrefabFrequency` | `0x15` |
| 22 | `getRadioPoint` | `0x16` |
| 23 | `getRadioState` | `0x17` |
| 24 | `registCallback` | `0x18` |
| 25 | `unRegistCallback` | `0x19` |
| 26 | `prefeb` | `0x1a` |
| 27 | `sendRadioCommand` | `0x1b` |
| 28 | `getRtMessage` | `0x1c` |
| 29 | `getRadioType` | `0x1d` |
| 30 | `getCurrentScanState` | `0x1e` |

**Descriptor:** `com.nwd.radio.service.RadioFeature`

Los nombres **`Strero`** / **`prefeb`** / **`prefebIndex`** son los del contrato OEM (typos conservados a propósito en OpenRadioFM).

### `RadioCallback` — `RadioCallback$Stub.smali`

| # | Método | `TRANSACTION_*` (hex) |
|---|--------|------------------------|
| 1 | `notifyState` | `0x01` |
| 2 | `notifyCurrentFrequency` | `0x02` |
| 3 | `notifyNearOn` | `0x03` |
| 4 | `notifyStereo` | `0x04` |
| 5 | `notifyStereoOn` | `0x05` |
| 6 | `notifyRDSStateChange` | `0x06` |
| 7 | `notifyCurrentPTYType` | `0x07` |
| 8 | `notifyPrefabFrequency` | `0x08` |
| 9 | `notifyPrefabPTYType` | `0x09` |
| 10 | `notifyRadioPoint` | `0x0a` |
| 11 | `notifyCurrentIsTA` | `0x0b` |
| 12 | `notifyRdsShowState` | `0x0c` |
| 13 | `notifyRtMessage` | `0x0d` |
| 14 | `notifyRadioScanState` | `0x0e` |

**Descriptor:** `com.nwd.radio.service.RadioCallback`

---

## C.3 Secuencia tras `onServiceConnected` (OpenRadioFM)

En **`QS6Engine`**, `onServiceConnected` corre en el **hilo principal**; el handshake AIDL pesado va en un **`Thread` nuevo** para no bloquear la UI:

1. `linkToDeath` sobre el `IBinder`.
2. **Hilo background:**  
   - `registCallback(mNwdCallback)`  
   - `setRadioBackServiceOn(true)`  
   - `INTRO()`  
   - `getRadioState()` (log diagnóstico)
3. **Main (`Handler` +350 ms):** segundo `setRadioBackServiceOn(true)` (“retry” handshake).

Cierre / power-off: `unRegistCallback`, `setRadioBackServiceOn(false)` (vía `performAidlCall`).

Esto es coherente con que las llamadas **entrantes** al servicio se ejecutan en el **hilo del binder** del proceso `com.nwd.radio.service`, mientras el cliente puede serializar llamadas desde otro hilo (`performAidlCall` está `synchronized` en el cliente).

---

## C.4 Implementación en el servicio OEM

- **`RadioFeatureAbs`** extiende **`RadioFeature$Stub`** pero muchos métodos son **vacíos** en la clase abstracta; la lógica real está en **`ArmRadioManager`** (y variantes **Sprd**/otras) que recibe el **`DataChangeListener`** y en **`registCallback`** reenvía al listener (`notifyRegistCallback`).
- **`RadioService.onBind`** retorna directamente esa instancia como **`IBinder`**.

Para profundizar en un método concreto (p. ej. `setCurrentFrequency`), hay que abrir **`ArmRadioManager.smali`** / **`RadioManager.smali`** según el `RadioService.onCreate` (rama Sprd vs “New Arm” vs otro).

---

## C.5 Hilos y callbacks hacia la app cliente

| Origen | Hilo típico | Nota |
|--------|-------------|------|
| Llamadas `mNwdService.*()` desde OpenRadioFM | Binder **sync** → hilo que invoca (`performAidlCall` puede ser main o el `Thread` de handshake) | Bloquea hasta respuesta del servicio |
| `RadioCallback.*` implementado en OpenRadioFM | **Hilo binder** del proceso OpenRadioFM (pool de hilos IPC) | **No** tocar UI directamente; `QS6Engine` hace `mMainHandler.post(...)` en los callbacks críticos |

Si se añaden operaciones AIDL lentas, conviene el mismo patrón: llamada desde background thread + resultados a UI en main.

---

## C.6 Tipos parcelable

`Frequency` y `RadioPoint` se declaran como `parcelable` en AIDL; las clases Java en OpenRadioFM deben mantener **orden de lectura/escritura** compatible con el OEM (campos `Frequency`, `RadioPoint` bajo `com.nwd.radio.service.data`).

---

## C.7 Plantilla hallazgo (Fase C)

```
ID: NWD-C001
Hecho: TRANSACTION IDs RadioFeature 0x01-0x1e coinciden con RadioFeature.aidl del proyecto
Evidencia: RadioFeature$Stub.smali
---
ID: NWD-C002
Hecho: Handshake post-bind: registCallback → setRadioBackServiceOn → INTRO → getRadioState
Código: QS6Engine.mConnection.onServiceConnected
```

---

## C.8 Estado Fase C

| Tarea | Estado |
|-------|--------|
| Comparar AIDL proyecto vs APK (`TRANSACTION_*`) | **Hecho** (§C.2) |
| Métodos tras bind + orden práctico | **Hecho** (§C.3) |
| Hilos binder vs UI | **Hecho** (§C.5) |

**Opcional:** diff fino campo a campo de `Frequency`/`RadioPoint` frente a `jadx` del APK 2.1.6 de unidad.

---

## C.9 Fase C+ — Cobertura OpenRadioFM vs contrato (sin otro firmware)

Sin un segundo firmware no se puede contrastar si **cambian los `TRANSACTION_*`**. Lo que sí aporta valor es un **mapa de uso real** en el cliente: qué métodos del `.aidl` están **invocados**, cuáles **no**, y en callbacks qué está **conectado a la UI** frente a **stubs vacíos** (el OEM puede seguir enviando esos eventos).

**Ámbito:** solo ruta NWD en **`QS6Engine.java`** (`mNwdService` / `mNwdCallback`). Otros motores (`K706Engine`, `MT8163Engine`, …) no usan este AIDL.

### `RadioFeature` — métodos que OpenRadioFM **sí** llama

| Método | Uso resumido |
|--------|----------------|
| `registCallback` / `unRegistCallback` | Suscripción al handshake y al apagar |
| `setRadioBackServiceOn` | `true` tras bind (+ retry), `false` al power-off |
| `INTRO` | Inicialización del stack radio tras bind |
| `getRadioState` | Log / diagnóstico tras handshake |
| `setCurrentFrequency` | Sintonía |
| `search` | Seek arriba/abajo (`seek(boolean)` del AIDL **no** se usa) |
| `AMS` | Escaneo |
| `changeBand` | Parar escaneo / ciclo de banda |
| `setStreroOn` | Stereo forzado (no se consulta `isStreroOn` / `isHasStrero`) |
| `setNearOn` / `isNearOn` | Toggle DX/local |
| `setRDSState` / `getRDSState` | TA/AF y refresco tras `notifyRDSStateChange` |
| `prefeb` | Siguiente/anterior favorito OEM |

### `RadioFeature` — métodos del contrato **no** usados por OpenRadioFM (hoy)

Útiles para futuras funciones o para parity con la app OEM; el servicio puede implementarlos igualmente.

| Método | Nota |
|--------|------|
| `seek` | El cliente usa `search` para seek |
| `getCurrentFrequency` | La frecuencia llega por **`notifyCurrentFrequency`** |
| `isHasStrero` / `isStreroOn` | Solo se fuerza stereo con `setStreroOn` |
| `isRadioBackServiceOn` | No se consulta tras activar |
| `setPTYType` / `getPTYType` / `getPrefabPTYType` | PTY no expuesto en UI vía AIDL directo |
| `saveCurrentFrequency` | Guardar preset por índice OEM |
| `getPrefabFrequency` | Lista de presets desde el servicio |
| `getRadioPoint` | Datos de emisoras / puntos |
| `sendRadioCommand` | Comando genérico `data0`/`data1` |
| `getRtMessage` | RT suele llegar por callback `notifyRtMessage` |
| `getRadioType` | Tipo de hardware / variante |
| `getCurrentScanState` | Estado de scan por consulta (hay callback `notifyRadioScanState`) |

### `RadioCallback` — implementado con lógica → UI (vía `mMainHandler`)

| Callback | Efecto principal |
|----------|------------------|
| `notifyCurrentFrequency` | Frecuencia, banda, PS |
| `notifyStereo` | Indicador estéreo |
| `notifyRDSStateChange` | Relee AF/TA con `getRDSState` y actualiza UI |
| `notifyCurrentIsTA` | Estado TA |
| `notifyRtMessage` | Radio Text (RT) |
| `notifyRadioScanState` | `onScanStatusChanged` |

### `RadioCallback` — override presente pero **cuerpo vacío**

El binder sigue siendo válido; si el OEM dispara estos eventos, **OpenRadioFM los ignora** por ahora.

| Callback |
|----------|
| `notifyState` |
| `notifyNearOn` |
| `notifyStereoOn` |
| `notifyCurrentPTYType` |
| `notifyPrefabFrequency` |
| `notifyPrefabPTYType` |
| `notifyRadioPoint` |
| `notifyRdsShowState` |

**Si en el futuro** dispones de otro APK/firmware: vuelve a comparar §C.2 (`TRANSACTION_*`) y, si el orden cambia, regenera stubs o reordena `.aidl` según el binario objetivo.

---

*Continúa en Fase D (RDS / Settings).*
