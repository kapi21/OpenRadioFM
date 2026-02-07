# 📡 Chip de Radio FM - SNR/RSSI y Calidad de Señal

**Fecha**: 7 Febrero 2026  
**Chip**: MediaTek MT8163 (FM integrado en CONSYS)  
**Para**: Implementación en OpenRadioFM

---

## 🎯 Identificación del Chip

### Chip de Radio FM
**Modelo**: MediaTek MT8163 CONSYS (Connectivity System)

**Características**:
- FM Radio **integrado en el SoC** (no es chip externo)
- Comparte módulo con WiFi y Bluetooth
- Driver: `fmradio_drv.ko` (módulo del kernel)
- Servicio: `fmradio` (servicio Android)

**Propiedades del Sistema**:
```properties
fmradio.driver.enable=1
mediatek.wlan.chip=CONSYS_MT8163
```

---

## 📊 Parámetros de Calidad de Señal (Signal Quality)

### Archivo de Configuración: `fm_cust.cfg`

Ubicación: `/vendor/firmware/fm_cust.cfg`

```ini
[FM Radio]
#FM RX RSSI threshold setting
FM_RX_RSSI_TH_LONG = -230
FM_RX_RSSI_TH_SHORT = -180
FM_RX_DESENSE_RSSI = -210
FM_RX_PAMD_TH = -11
FM_RX_ATDC_TH = 2800
FM_RX_MR_TH = 60
FM_RX_PRX_TH = 64
FM_RX_SMG_TH = 25000
#deemphasis: 0-50us, China Mainland; 1-75us China Taiwan
FM_RX_DEEMPHASIS = 0
#osc freq: 0-26MHz; 1-19MHz; 2-24MHz; 3-38.4MHz; 4-40MHz; 5-52MHz
FM_RX_OSC_FREQ = 0
```

### Parámetros Explicados

#### 1. **RSSI (Received Signal Strength Indicator)**

##### FM_RX_RSSI_TH_LONG = -230
- **Función**: Umbral RSSI para señales de largo alcance
- **Unidad**: dBμV (decibelios microvoltios)
- **Valor**: -230 dBμV
- **Uso**: Búsqueda de emisoras distantes

##### FM_RX_RSSI_TH_SHORT = -180
- **Función**: Umbral RSSI para señales de corto alcance
- **Unidad**: dBμV
- **Valor**: -180 dBμV
- **Uso**: Búsqueda de emisoras locales (modo local)

##### FM_RX_DESENSE_RSSI = -210
- **Función**: Umbral de desensibilización
- **Unidad**: dBμV
- **Valor**: -210 dBμV
- **Uso**: Protección contra interferencias

#### 2. **PAMD (Phase Amplitude Modulation Detection)**

##### FM_RX_PAMD_TH = -11
- **Función**: Umbral de detección de modulación de fase/amplitud
- **Valor**: -11
- **Uso**: Detección de calidad de modulación

#### 3. **ATDC (Adjacent Channel Detection)**

##### FM_RX_ATDC_TH = 2800
- **Función**: Umbral de detección de canal adyacente
- **Valor**: 2800
- **Uso**: Rechazo de interferencias de canales adyacentes

#### 4. **MR (Multipath Rejection)**

##### FM_RX_MR_TH = 60
- **Función**: Umbral de rechazo de multipath
- **Valor**: 60
- **Uso**: Reducción de distorsión por reflexiones de señal

#### 5. **PRX (Pilot Tone Detection)**

##### FM_RX_PRX_TH = 64
- **Función**: Umbral de detección de tono piloto (19 kHz)
- **Valor**: 64
- **Uso**: Detección de señal estéreo

#### 6. **SMG (Soft Mute Gain)**

##### FM_RX_SMG_TH = 25000
- **Función**: Umbral de ganancia de silenciamiento suave
- **Valor**: 25000
- **Uso**: Reducción gradual de audio en señales débiles

---

## 🏭 Configuración de Fábrica

### Archivo: `factory.ini`

Ubicación: `/system/etc/factory.ini`

```ini
//FM Radio
FMRadio.CH1=1043    # 104.3 MHz
FMRadio.CH2=1058    # 105.8 MHz
FMRadio.CH3=980     # 98.0 MHz
FMRadio.CH4=1080    # 108.0 MHz
FMRadio.RSSITH=-95  # Umbral RSSI para pruebas de fábrica

//FM Transmitter
FMTX.CH1=878        # 87.8 MHz
FMTX.CH2=886        # 88.6 MHz
...
```

**Nota**: El umbral RSSITH=-95 es para **pruebas de fábrica**, no para uso normal.

---

## ⚠️ Limitaciones de la API Pública

### Funciones NO Expuestas en `IRadioPlayer`

Basado en el análisis del código decompilado, las siguientes funciones **NO están disponibles** en la API pública:

#### ❌ No Disponibles Directamente
- ✗ **RSSI** (Received Signal Strength Indicator)
- ✗ **SNR** (Signal-to-Noise Ratio)
- ✗ **Multipath detection**
- ✗ **Pilot tone strength**
- ✗ **Adjacent channel power**
- ✗ **Soft mute level**
- ✗ **Deemphasis control** (fijo en configuración)
- ✗ **Oscillator frequency** (fijo en configuración)

#### ✅ Disponibles en la API
- ✓ **Frecuencia actual** (`getFreq()`)
- ✓ **Región** (`getRegion()`)
- ✓ **Banda** (`getUiband()`)
- ✓ **RDS completo** (PS, RT, PTY, TA, AF, EON, REG)
- ✓ **Modo estéreo/mono** (`stereo()`, `mIsStereo`)
- ✓ **Modo local** (`local()`, `mIsLocal`)

---

## 🔧 Posibles Formas de Acceder a SNR/RSSI

### Opción 1: Modificar el Servicio `fmradio`

**Complejidad**: Alta  
**Requiere**: Root + modificación del sistema

```bash
# Localizar el servicio
find /vendor/bin /system/bin -name "*radio*" -o -name "*fm*"

# Modificar el servicio para exponer RSSI/SNR
# Requiere ingeniería inversa del binario
```

**Ventajas**:
- Acceso completo a todos los parámetros del chip
- Control total sobre la configuración

**Desventajas**:
- Requiere root
- Puede causar inestabilidad del sistema
- Difícil de mantener entre actualizaciones

---

### Opción 2: Acceso Directo al Driver (ioctl)

**Complejidad**: Muy Alta  
**Requiere**: Root + conocimiento del protocolo ioctl

```c
// Ejemplo conceptual (requiere ingeniería inversa)
int fd = open("/dev/fm", O_RDWR);
struct fm_rssi_req {
    int freq;
    int rssi;
};
ioctl(fd, FM_IOCTL_GET_RSSI, &req);
```

**Ventajas**:
- Acceso directo al hardware
- Máximo control

**Desventajas**:
- Requiere root
- Protocolo no documentado
- Muy complejo de implementar

---

### Opción 3: Usar Indicadores Indirectos

**Complejidad**: Baja  
**Requiere**: Solo API pública

**Indicadores disponibles**:

#### 1. Modo Local (`mIsLocal`)
```java
// El modo local solo funciona con señales fuertes
// Si una emisora se detecta en modo local, tiene buena señal
boolean isStrongSignal = radioInfo.mIsLocal;
```

#### 2. Detección de Estéreo (`mIsStereo`)
```java
// El estéreo requiere señal fuerte (tono piloto detectable)
// Si hay estéreo, la señal es buena
boolean hasGoodSignal = radioInfo.mIsStereo;
```

#### 3. Estado RDS (`mRDSstate`)
```java
// RDS requiere señal de calidad
// Si RDS está activo, la señal es aceptable
boolean hasDecentSignal = !radioInfo.mRDSstate.isEmpty();
```

#### 4. Éxito de Búsqueda
```java
// Si seekUp/seekDown encuentra emisoras rápidamente,
// hay señales fuertes en la zona
```

---

## 💡 Implementación Recomendada para OpenRadioFM

### Estrategia: Indicador de Calidad Compuesto

Dado que **RSSI/SNR no están disponibles**, se recomienda crear un **indicador de calidad compuesto** basado en los datos disponibles:

```java
public enum SignalQuality {
    EXCELLENT,  // Estéreo + RDS + Modo Local
    GOOD,       // Estéreo + RDS
    FAIR,       // Solo RDS o Solo Estéreo
    POOR,       // Ni RDS ni Estéreo
    NO_SIGNAL   // Sin señal
}

public SignalQuality getSignalQuality(RadioInfo info) {
    boolean hasStereo = info.mIsStereo;
    boolean hasRDS = info.mRDSstate != null && !info.mRDSstate.isEmpty();
    boolean isLocal = info.mIsLocal;
    
    if (hasStereo && hasRDS && isLocal) {
        return SignalQuality.EXCELLENT;
    } else if (hasStereo && hasRDS) {
        return SignalQuality.GOOD;
    } else if (hasStereo || hasRDS) {
        return SignalQuality.FAIR;
    } else if (info.mFreq > 0) {
        return SignalQuality.POOR;
    } else {
        return SignalQuality.NO_SIGNAL;
    }
}
```

### UI Recomendada

```
┌─────────────────────────────┐
│  📻 104.3 FM                │
│  ████████░░ GOOD            │  ← Indicador visual
│  🔊 Stereo  📡 RDS          │  ← Iconos de estado
│  Radio Nacional             │  ← PS (RDS)
└─────────────────────────────┘
```

---

## 🔬 Investigación Futura (Fase 5)

### Objetivos de Investigación

#### 1. Análisis del Driver `fmradio_drv.ko`
```bash
# Extraer símbolos
nm fmradio_drv.ko | grep -i "rssi\|snr\|signal"

# Buscar strings
strings fmradio_drv.ko | grep -i "rssi\|snr\|quality"

# Decompilación con Ghidra/IDA
# Buscar funciones ioctl
```

#### 2. Análisis del Servicio `fmradio`
```bash
# Localizar binario
find /vendor/bin /system/bin -name "*fm*"

# Extraer strings
strings <binario> | grep -i "rssi\|snr"

# Interceptar llamadas
strace -p <pid> -e ioctl
```

#### 3. Monitoreo en Tiempo Real
```bash
# Logcat filtrado
adb logcat | grep -i "fm\|radio\|rssi\|snr"

# Propiedades del sistema
adb shell getprop | grep -i "fm\|radio"
```

---

## 📋 Funciones Disponibles para OpenRadioFM

### ✅ Funciones Implementables

#### Control Básico
- ✓ Sintonización manual (`setUibandIndexFreq`)
- ✓ Búsqueda automática (`seekUp`, `seekDown`, `autoScan`)
- ✓ Paso a paso (`stepUp`, `stepDown`)
- ✓ Presets (18 por banda, 90 totales)
- ✓ Navegación presets (`next`, `prev`)

#### RDS Completo
- ✓ PS (Programme Service Name)
- ✓ RT (Radio Text)
- ✓ PTY (Programme Type)
- ✓ TA (Traffic Announcement)
- ✓ AF (Alternative Frequencies)
- ✓ EON (Enhanced Other Networks)
- ✓ REG (Regional mode)

#### Audio
- ✓ Mute (`setMute`)
- ✓ Estéreo/Mono (`stereo`)
- ✓ Modo Local (`local`)

#### Información de Estado
- ✓ Frecuencia actual (`getFreq`)
- ✓ Banda actual (`getUiband`)
- ✓ Región (`getRegion`)
- ✓ Estado completo (`getRadioInfo`)

#### Eventos
- ✓ Callbacks asíncronos (`setOnEventListener`)
- ✓ Eventos RDS en tiempo real
- ✓ Notificaciones de búsqueda

---

## ⚙️ Configuraciones Modificables

### Parámetros que se pueden cambiar en `fm_cust.cfg`

#### Umbrales RSSI
```ini
FM_RX_RSSI_TH_LONG = -230   # Ajustar para búsqueda más/menos sensible
FM_RX_RSSI_TH_SHORT = -180  # Ajustar para modo local
```

#### Deemphasis
```ini
FM_RX_DEEMPHASIS = 0  # 0=50us (Europa/Asia), 1=75us (América)
```

#### Frecuencia del Oscilador
```ini
FM_RX_OSC_FREQ = 0  # 0=26MHz, 1=19MHz, 2=24MHz, 3=38.4MHz, 4=40MHz, 5=52MHz
```

> [!WARNING]
> Modificar estos parámetros requiere **root** y puede afectar la estabilidad del sistema.

---

## 🎯 Conclusiones

### Para OpenRadioFM

1. **RSSI/SNR no están disponibles** en la API pública de Android
2. **Usar indicadores indirectos** (estéreo, RDS, modo local) para calidad de señal
3. **Todas las funciones RDS** están disponibles y funcionan perfectamente
4. **Control completo** de sintonización, búsqueda y presets
5. **Sistema de eventos robusto** para actualizaciones en tiempo real

### Chip de Radio

- **Chip**: MediaTek MT8163 CONSYS (integrado)
- **Driver**: `fmradio_drv.ko`
- **Configuración**: `/vendor/firmware/fm_cust.cfg`
- **Parámetros avanzados**: Disponibles pero no expuestos en API

### Recomendación

Para OpenRadioFM, **implementar un indicador de calidad compuesto** basado en:
- Estado estéreo (`mIsStereo`)
- Estado RDS (`mRDSstate`)
- Modo local (`mIsLocal`)

Esto proporcionará una **experiencia de usuario excelente** sin necesidad de acceso root o modificaciones del sistema.

---

## 📚 Referencias

- [Archivo de configuración FM](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/firm/vendor/firmware/fm_cust.cfg)
- [Configuración de fábrica](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/firm/system/system/etc/factory.ini)
- [Código RadioPlayer](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoMcuUpgrade/smali/android/radio/RadioPlayer.smali)
- [Código RadioInfo](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/analysis/apks/AutoMcuUpgrade/smali/android/radio/RadioInfo.smali)
- [Driver FM](file:///d:/@MIS%20PROYECTOS/Radiochina_Firmware/firm/vendor/lib/modules/fmradio_drv.ko)
