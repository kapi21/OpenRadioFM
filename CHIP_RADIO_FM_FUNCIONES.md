# 📻 Funciones del Chip de Radio FM - Recopilación Completa

**Fecha**: 7 Febrero 2026  
**Firmware**: RadioChina Android MT8163  
**Chip**: MediaTek MT8163 (FM integrado en SoC)

---

## 🎯 Resumen Ejecutivo

El chip de radio FM está **integrado en el SoC MediaTek MT8163** y proporciona funcionalidades completas de **FM/AM con soporte RDS completo**. El sistema utiliza un servicio Android (`fmradio`) que se comunica con el driver del kernel (`fmradio_drv.ko`) para controlar el hardware.

### Componentes Identificados

1. **Driver del Kernel**: `fmradio_drv.ko` (módulo en `/vendor/lib/modules/`)
2. **Servicio del Sistema**: `fmradio` (servicio Android registrado)
3. **API Java**: `android.radio.RadioPlayer` y `android.radio.IRadioPlayer`
4. **APK de Usuario**: `AutoRadio.apk` (interfaz de usuario)

---

## 📡 Funciones RDS (Radio Data System)

El chip soporta **RDS completo** con las siguientes funcionalidades:

### 1. PS (Programme Service Name)
**Función**: Nombre de la emisora de radio

- **Campo**: `mPSname` (String)
- **Evento**: `EVENT_PS_MESSAGE` (0x26)
- **Evento de finalización**: `EVENT_PS_DONE` (0x8)
- **Descripción**: Muestra el nombre de la estación de radio (máximo 8 caracteres)

### 2. RT (Radio Text)
**Función**: Texto informativo de la emisora

- **Campo**: `mRTtype` (String)
- **Evento**: `EVENT_RT_MESSAGE` (0x29)
- **Descripción**: Texto adicional enviado por la emisora (hasta 64 caracteres)

### 3. PTY (Programme Type)
**Función**: Tipo de programa

- **Campo**: `mPTYtype` (String)
- **Evento**: `EVENT_PTY_TYPE` (0x22)
- **Método**: `setPTY(int type)`
- **Estado (v5.3)**: Lógica implementada pero oculto en Layout (Diferido).
- **Descripción**: Categoría del programa (Noticias, Música, Deportes, etc.)

### 4. TA (Traffic Announcement)
**Función**: Anuncios de tráfico

- **Método**: `setTA(boolean enable)`
- **Acción**: `ACTION_TA_MESSAGE` = "radio.RadioPlayer.TA_MESSAGE"
- **Descripción**: Interrumpe automáticamente para anuncios de tráfico

### 5. AF (Alternative Frequencies)
**Función**: Frecuencias alternativas

- **Método**: `setAF(boolean enable)`
- **Descripción**: Cambia automáticamente a frecuencias alternativas con mejor señal de la misma emisora

### 6. EON (Enhanced Other Networks)
**Función**: Información de otras redes

- **Método**: `setEON(boolean enable)`
- **Descripción**: Información sobre otras emisoras de la red

### 7. REG (Regional)
**Función**: Modo regional

- **Método**: `setREG(boolean enable)`
- **Descripción**: Limita AF a emisoras regionales

### 8. Estado RDS
**Campo**: `mRDSstate` (String)
**Evento**: `EVENT_RDS_STATE` (0x4)
**Descripción**: Estado general del sistema RDS (activo/inactivo)

---

## 🎛️ Funciones de Control de Frecuencia

### Sintonización Manual

#### 1. Establecer Frecuencia
```java
setUibandIndexFreq(BAND uiband, int index, int freq)
```
- Establece una frecuencia específica en una banda
- Parámetros:
  - `uiband`: Banda (FM1, FM2, FM3, AM1, AM2)
  - `index`: Índice del preset (0-17)
  - `freq`: Frecuencia en kHz

#### 2. Obtener Frecuencia Actual
```java
int getFreq()
```
- Devuelve la frecuencia actual en kHz
- Retorna `-1` en caso de error

#### 3. Paso a Paso (Step)
```java
int stepUp()    // Incrementa un paso (100 kHz en FM)
int stepDown()  // Decrementa un paso
```
- Cambia la frecuencia en incrementos fijos
- Retorna la nueva frecuencia

---

## 🔍 Funciones de Búsqueda (Scan/Seek)

### 1. Búsqueda Automática Completa
```java
autoScan(BAND uiband, int count)
```
- **Tipo**: `SEEK_ALL` (0x1)
- Busca todas las emisoras disponibles
- `count`: Número máximo de emisoras a encontrar

### 2. Búsqueda Ascendente
```java
seekUp(BAND uiband)
```
- **Tipo**: `SEEK_UP` (0x3)
- Busca la siguiente emisora con señal fuerte

### 3. Búsqueda Descendente
```java
seekDown(BAND uiband)
```
- **Tipo**: `SEEK_DOWN` (0x4)
- Busca la emisora anterior con señal fuerte

### 4. Búsqueda y Reproducción
```java
seekPlay(BAND uiband)
```
- **Tipo**: `SEEK_PLAY` (0x2)
- Busca y reproduce automáticamente

### 5. Escaneo de Presets
```java
presetScan(int seconds)
```
- **Tipo**: `PRESET_PLAY` (0x5)
- Reproduce cada preset durante X segundos

### 6. Escaneo RDS
- **Tipo**: `RDS_SCAN` (0x6)
- Busca emisoras con RDS activo

### Evento de Finalización
- **Evento**: `EVENT_SCAN_DONE` (0x2)
- Se dispara cuando termina cualquier búsqueda

---

## 💾 Funciones de Presets (Emisoras Guardadas)

### 1. Obtener Presets
```java
int[] getPreset(BAND band)
```
- Devuelve array de frecuencias guardadas
- Máximo 18 presets por banda

### 2. Guardar Presets
```java
setPreset(BAND band, int[] preset)
```
- Guarda array de frecuencias en una banda
- Valida que el array no esté vacío

### 3. Navegación entre Presets
```java
int next()  // Siguiente preset
int prev()  // Preset anterior
```
- Retorna la frecuencia del preset

---

## 📻 Bandas de Frecuencia

### Enum BAND
```java
public enum BAND {
    FM1,  // FM Banda 1
    FM2,  // FM Banda 2
    FM3,  // FM Banda 3
    AM1,  // AM Banda 1
    AM2   // AM Banda 2
}
```

### Métodos de Banda
```java
BAND getUiband()           // Obtiene banda actual
int getRegion()            // Obtiene región (USA, Europe, Japan, etc.)
```

### Regiones Soportadas
Basado en los recursos encontrados en `AutoRadio.apk`:
- **USA** (87.5-108.0 MHz, paso 200 kHz)
- **Europe/Latin** (87.5-108.0 MHz, paso 100 kHz)
- **Japan** (76.0-90.0 MHz, paso 100 kHz)
- **China** (87.0-108.0 MHz, paso 100 kHz)
- **OIRT** (65.8-74.0 MHz, paso 30 kHz) - Rusia/Europa del Este

---

## 🎚️ Funciones de Audio

### 1. Silencio (Mute)
```java
setMute(boolean mute)
```
- Activa/desactiva el silencio

### 2. Modo Estéreo/Mono
```java
stereo(boolean enable)
```
- `true`: Fuerza modo estéreo
- `false`: Fuerza modo mono
- **Campo**: `mIsStereo` (boolean)

### 3. Modo Local
```java
local(boolean enable)
```
- Activa modo "local" (solo emisoras con señal fuerte)
- **Campo**: `mIsLocal` (boolean)
- Reduce sensibilidad para evitar interferencias

---

## 📊 Información del Estado (RadioInfo)

### Clase RadioInfo
Objeto Parcelable que contiene toda la información del estado actual:

```java
public class RadioInfo {
    int mFreq;              // Frecuencia actual (kHz)
    int mScanType;          // Tipo de búsqueda activa
    boolean mIsStereo;      // Modo estéreo activo
    boolean mIsLocal;       // Modo local activo
    String mUiBand;         // Banda actual (FM1, FM2, etc.)
    int mIndexof;           // Índice del preset actual
    String mPSname;         // Nombre RDS (PS)
    String mRTtype;         // Texto RDS (RT)
    String mRDSstate;       // Estado RDS
    String mPTYtype;        // Tipo de programa RDS (PTY)
}
```

### Obtener Información
```java
RadioInfo getRadioInfo()
```
- Devuelve objeto completo con todo el estado

---

## 🔔 Sistema de Eventos (Callbacks)

### Interface OnEventListener
```java
public interface OnEventListener {
    void onEvent(int event, Object data);
}
```

### Eventos Disponibles

| Evento | Código | Descripción |
|--------|--------|-------------|
| `EVENT_STATE` | 0x1 | Cambio de estado general |
| `EVENT_SCAN_DONE` | 0x2 | Búsqueda completada |
| `EVENT_RDS_STATE` | 0x4 | Estado RDS cambió |
| `EVENT_LIST_CHANGED` | 0x7 | Lista de presets cambió |
| `EVENT_PS_DONE` | 0x8 | PS recibido completamente |
| `EVENT_PTY_TYPE` | 0x22 | Tipo de programa recibido |
| `EVENT_PS_MESSAGE` | 0x26 | Mensaje PS recibido |
| `EVENT_RT_MESSAGE` | 0x29 | Mensaje RT recibido |

### Registro de Callbacks
```java
setOnEventListener(OnEventListener listener)
```
- Registra listener para recibir eventos
- Pasar `null` para desregistrar

---

## 🔧 Arquitectura del Sistema

### Servicio del Sistema
```java
// Obtener servicio
IBinder binder = ServiceManager.getService("fmradio");
IRadioPlayer service = IRadioPlayer.Stub.asInterface(binder);
```

### Patrón Singleton
```java
RadioPlayer player = RadioPlayer.getRadioPlayer();
```

### Comunicación IPC
- Utiliza **Binder IPC** (Inter-Process Communication)
- Interface: `IRadioPlayer` (AIDL)
- Callbacks: `IRadioCallback` (AIDL)

---

## 🛠️ Driver del Kernel

### Módulo
**Archivo**: `/vendor/lib/modules/fmradio_drv.ko`

### Propiedades del Sistema
```properties
fmradio.driver.enable=1
```

### Chip WiFi/FM Integrado
```properties
mediatek.wlan.chip=CONSYS_MT8163
```
> **Nota**: El chip FM está integrado en el mismo módulo que WiFi/BT

---

## 📱 Aplicación de Usuario (AutoRadio.apk)

### Package
`com.hcn.autoradio`

### Componentes Principales
- `RadioMain` - Actividad principal
- `IRadioServiceAPI` - API del servicio
- `IRadioCallBack` - Callbacks de eventos
- `RadioDigitFreq` - Control de frecuencia digital
- `RadioIcon` - Iconos de estado

### Recursos Visuales
Soporta múltiples regiones con gráficos específicos:
- `radio_scroll_fm_usa.png`
- `radio_scroll_fm_china.png`
- `radio_scroll_fm_japan.png`
- `radio_scroll_fm_latin2.png`
- `radio_scroll_fm_oirt.png`

---

## 🎯 Funciones Avanzadas Identificadas

### 1. Detección de Módulo
```java
boolean hasRadiomodule()
```
- Actualmente retorna `false` (posiblemente no implementado)
- Podría usarse para detectar hardware FM externo

### 2. Scan Interno
```java
private void scan(int type, BAND band, int count)
```
Tipos de scan:
- `SEEK_ALL` (1): Búsqueda completa
- `SEEK_PLAY` (2): Buscar y reproducir
- `SEEK_UP` (3): Buscar arriba
- `SEEK_DOWN` (4): Buscar abajo
- `PRESET_PLAY` (5): Reproducir presets
- `RDS_SCAN` (6): Buscar con RDS

---

## 💡 Capacidades del Chip - Resumen

### ✅ Funciones Confirmadas

#### RDS Completo
- ✅ PS (Programme Service)
- ✅ RT (Radio Text)
- ✅ PTY (Programme Type) - **Actualizado v5.2**: Actualización reactiva activada.
- ✅ TA (Traffic Announcement)
- ✅ AF (Alternative Frequencies)
- ✅ EON (Enhanced Other Networks)
- ✅ REG (Regional mode)

#### Control de Sintonización
- ✅ Sintonización manual por frecuencia
- ✅ Paso a paso (step up/down)
- ✅ Búsqueda automática (seek)
- ✅ Escaneo completo de banda
- ✅ Escaneo RDS
- ✅ 18 presets por banda (5 bandas = 90 presets totales)

#### Modos de Audio
- ✅ Estéreo/Mono forzado
- ✅ Modo local (sensibilidad reducida)
- ✅ Mute

#### Bandas Soportadas
- ✅ FM (87.5-108.0 MHz)
- ✅ AM (posiblemente 522-1620 kHz)
- ✅ Múltiples regiones (USA, Europe, Japan, China, OIRT)

#### Sistema de Eventos
- ✅ Callbacks asíncronos
- ✅ Eventos RDS en tiempo real
- ✅ Notificaciones de cambio de estado

---

## 🔍 Posibles Funciones No Expuestas

Basado en chips FM típicos de MediaTek, podrían existir funciones adicionales no expuestas en la API:

### Potencialmente Disponibles en el Driver
- 🔹 RSSI (Indicador de intensidad de señal)
- 🔹 SNR (Relación señal/ruido)
- 🔹 Control de ganancia de antena
- 🔹 Deemphasis (50µs/75µs)
- 🔹 Pilot tone detection
- 🔹 Multipath detection
- 🔹 Soft mute threshold
- 🔹 Blend threshold (stereo/mono)

### Para Investigar
> [!NOTE]
> Estas funciones podrían estar disponibles mediante:
> - Comandos ioctl directos al driver
> - Modificación del servicio `fmradio`
> - Ingeniería inversa del binario del servicio

---

## 📂 Archivos Clave para Análisis Profundo

### Código Decompilado
1. [`AutoMcuUpgrade/smali/android/radio/RadioPlayer.smali`](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoMcuUpgrade/smali/android/radio/RadioPlayer.smali)
2. [`AutoMcuUpgrade/smali/android/radio/IRadioPlayer.smali`](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoMcuUpgrade/smali/android/radio/IRadioPlayer.smali)
3. [`AutoMcuUpgrade/smali/android/radio/RadioInfo.smali`](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoMcuUpgrade/smali/android/radio/RadioInfo.smali)
4. [`AutoRadio/smali/com/hcn/autoradio/RadioMain.smali`](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoRadio/smali/com/hcn/autoradio/RadioMain.smali)

### Binarios del Sistema
1. `/vendor/lib/modules/fmradio_drv.ko` - Driver del kernel
2. Servicio `fmradio` (ubicación por determinar en `/vendor/bin` o `/system/bin`)

---

## 🚀 Próximos Pasos Recomendados

### 1. Análisis del Driver
```bash
# Extraer strings del driver
strings fmradio_drv.ko > fmradio_driver_strings.txt

# Buscar símbolos
nm fmradio_drv.ko

# Análisis con Ghidra/IDA
# Buscar funciones ioctl y estructuras de datos
```

### 2. Análisis del Servicio
```bash
# Localizar binario del servicio fmradio
find /vendor/bin /system/bin -name "*radio*" -o -name "*fm*"

# Extraer strings
strings <servicio_binario> > fmradio_service_strings.txt
```

### 3. Interceptar Comunicación
```bash
# Usar strace para ver llamadas al sistema
adb shell strace -p <pid_del_servicio>

# Logcat filtrado
adb logcat | grep -i "radio\|fm\|rds"
```

### 4. Modificaciones Posibles
- ✏️ Exponer funciones RSSI/SNR en la API
- ✏️ Crear app personalizada con más control
- ✏️ Modificar umbrales de búsqueda
- ✏️ Habilitar funciones ocultas del chip

---

## ⚠️ Notas Importantes

> [!IMPORTANT]
> - El chip FM está **integrado en el SoC MT8163**, no es un chip externo
> - Comparte recursos con WiFi/Bluetooth (CONSYS)
> - El driver es propietario de MediaTek
> - Algunas funciones pueden estar limitadas por región/regulación

> [!WARNING]
> - Modificar el driver puede causar inestabilidad del sistema
> - Cambios en frecuencias fuera de rango pueden dañar el hardware
> - Respetar regulaciones locales de radiofrecuencia

---

## 📝 Conclusiones

El chip de radio FM del MT8163 es **sorprendentemente completo** y ofrece:

1. ✅ **RDS completo** con todas las funciones estándar
2. ✅ **Múltiples bandas y regiones** soportadas
3. ✅ **Sistema de búsqueda avanzado** con varios modos
4. ✅ **90 presets totales** (18 por banda × 5 bandas)
5. ✅ **API bien estructurada** con eventos asíncronos
6. ✅ **Integración con el sistema** mediante servicio Android

### Funcionalidad PTY
El soporte de **PTY ha sido optimizado en v5.2** para actualizarse en tiempo real sin perderse durante los ciclos de refresco de la UI.

### Hardware Genérico
Se ha ampliado la detección de servicios para incluir `com.android.fmradio.FmRadioService`, mejorando la compatibilidad con dispositivos MTK genéricos.
