# 🔇 Problema: Estéreo Siempre Apagado

**Fecha**: 7 Febrero 2026  
**Problema**: El indicador de estéreo nunca se activa en la radio  
**Impacto**: Afecta calidad de audio y detección de calidad de señal

---

## 🔍 Análisis del Problema

### Cómo Funciona la Detección de Estéreo

El chip FM detecta estéreo mediante el **tono piloto de 19 kHz**:

1. **Emisora transmite**:
   - Señal mono (L+R)
   - Señal estéreo (L-R) modulada a 38 kHz
   - **Tono piloto a 19 kHz** (indica que hay estéreo)

2. **Chip FM detecta**:
   - Si detecta tono piloto → `mIsStereo = true`
   - Si NO detecta tono piloto → `mIsStereo = false`

3. **Umbral de detección**:
   - `FM_RX_PRX_TH = 64` (Pilot Tone Threshold)
   - Si la señal del piloto es menor que 64 → NO detecta estéreo

---

## 🎯 Posibles Causas

### 1. **Umbral de Tono Piloto Demasiado Alto** ⭐ (Más Probable)

**Parámetro**: `FM_RX_PRX_TH = 64`

Este valor puede ser **demasiado alto** para tu antena/ubicación.

**Archivo**: `/vendor/firmware/fm_cust.cfg`
```ini
FM_RX_PRX_TH = 64  # ← Umbral de detección de tono piloto
```

**Solución** (requiere root):
```ini
FM_RX_PRX_TH = 32  # Reducir a la mitad para mayor sensibilidad
```

---

### 2. **Función stereo() Forzando Mono**

**Código encontrado**:
```java
// En RadioService.smali
private native int nativeSetStereo(int enable);

public void stereo(boolean enable) {
    nativeSetStereo(enable ? 1 : 0);
}
```

**Posible problema**:
- La app podría estar llamando `stereo(false)` al inicio
- Esto **fuerza modo mono** independientemente de la señal

**Verificación**:
```java
// En RadioInfo
mIsStereo = false;  // ← Siempre inicializado en false
```

**Solución**:
- La app debe llamar `stereo(true)` para permitir auto-detección
- O no llamar a `stereo()` para dejar el valor por defecto

---

### 3. **Antena Deficiente**

**Síntoma**: El tono piloto es débil incluso con señal FM fuerte

**Causas**:
- Antena del coche mal conectada
- Cable de antena dañado
- Antena amplificada sin alimentación

**Verificación**:
- Probar con emisoras locales muy fuertes
- Si incluso emisoras potentes no muestran estéreo → problema de antena

---

### 4. **Problema de Hardware**

**Menos probable**, pero posible:
- Circuito de detección de piloto defectuoso
- Problema en el chip FM

---

## 🔧 Soluciones Prácticas

### Solución 1: Ajustar Umbral PRX (Requiere Root)

**Pasos**:
1. Obtener acceso root
2. Editar `/vendor/firmware/fm_cust.cfg`
3. Cambiar `FM_RX_PRX_TH = 64` a `FM_RX_PRX_TH = 32`
4. Reiniciar el sistema

**Comando**:
```bash
adb root
adb remount
adb pull /vendor/firmware/fm_cust.cfg
# Editar localmente
adb push fm_cust.cfg /vendor/firmware/fm_cust.cfg
adb reboot
```

---

### Solución 2: Verificar Llamada a stereo()

**En la app AutoRadio**:
```java
// Asegurarse de que se llama:
radioPlayer.stereo(true);  // Habilitar auto-detección
```

**En OpenRadioFM**:
```java
// Al iniciar la radio:
RadioPlayer player = RadioPlayer.getRadioPlayer();
player.stereo(true);  // Permitir estéreo automático
```

---

### Solución 3: Mejorar Antena

**Verificaciones**:
1. Comprobar conexión de antena
2. Verificar cable no dañado
3. Si es antena amplificada, verificar alimentación

---

### Solución 4: Forzar Estéreo en OpenRadioFM

**Si nada funciona**, puedes:

#### Opción A: Asumir Estéreo Siempre
```java
// En OpenRadioFM
RadioInfo info = player.getRadioInfo();
// Ignorar mIsStereo del sistema
boolean isStereo = true;  // Asumir siempre estéreo
```

#### Opción B: Detectar por Frecuencia
```java
// Emisoras FM comerciales suelen ser estéreo
boolean isStereo = (freq >= 87500 && freq <= 108000);
```

#### Opción C: Usar Solo RDS para Calidad
```java
// Indicador de calidad sin depender de estéreo
public SignalQuality getSignalQuality(RadioInfo info) {
    boolean hasRDS = info.mRDSstate != null && !info.mRDSstate.isEmpty();
    boolean isLocal = info.mIsLocal;
    
    if (hasRDS && isLocal) {
        return SignalQuality.EXCELLENT;
    } else if (hasRDS) {
        return SignalQuality.GOOD;
    } else if (isLocal) {
        return SignalQuality.FAIR;
    } else {
        return SignalQuality.POOR;
    }
}
```

---

## 🧪 Pruebas de Diagnóstico

### Test 1: Verificar Valor de mIsStereo

```java
RadioInfo info = player.getRadioInfo();
Log.d("FM", "Freq: " + info.mFreq);
Log.d("FM", "IsStereo: " + info.mIsStereo);  // ← Verificar valor
Log.d("FM", "RDS: " + info.mRDSstate);
```

### Test 2: Forzar Estéreo Manualmente

```java
// Intentar forzar estéreo
player.stereo(true);
Thread.sleep(1000);
RadioInfo info = player.getRadioInfo();
Log.d("FM", "After stereo(true): " + info.mIsStereo);
```

### Test 3: Comparar con App Original

1. Abrir AutoRadio.apk original
2. Sintonizar emisora fuerte
3. Verificar si muestra estéreo
4. Si tampoco muestra → problema de hardware/configuración
5. Si sí muestra → problema en OpenRadioFM

---

## 📊 Valores de Referencia

### Umbrales Típicos de PRX_TH

| Valor | Sensibilidad | Uso |
|-------|--------------|-----|
| **32** | Alta | Antenas débiles, zonas rurales |
| **48** | Media | Uso general |
| **64** | Baja | Antenas potentes, zonas urbanas |
| **96** | Muy baja | Solo señales muy fuertes |

**Tu valor actual**: `FM_RX_PRX_TH = 64` (sensibilidad baja)

---

## 💡 Recomendación para OpenRadioFM

### Estrategia Sin Root

Ya que modificar `fm_cust.cfg` requiere root, la mejor estrategia para OpenRadioFM es:

#### 1. Intentar Habilitar Estéreo
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    RadioPlayer player = RadioPlayer.getRadioPlayer();
    
    // Intentar habilitar auto-detección de estéreo
    try {
        player.stereo(true);
    } catch (Exception e) {
        Log.e("FM", "Error enabling stereo", e);
    }
}
```

#### 2. Mostrar Icono de Estéreo Basado en RDS
```java
// Si no se detecta estéreo pero hay RDS, probablemente es estéreo
public boolean shouldShowStereoIcon(RadioInfo info) {
    // Si el sistema detecta estéreo, mostrarlo
    if (info.mIsStereo) {
        return true;
    }
    
    // Si hay RDS, asumir que es estéreo
    // (emisoras con RDS suelen ser estéreo)
    if (info.mRDSstate != null && !info.mRDSstate.isEmpty()) {
        return true;
    }
    
    // Si hay PS name, probablemente es estéreo
    if (info.mPSname != null && !info.mPSname.isEmpty()) {
        return true;
    }
    
    return false;
}
```

#### 3. Indicador de Calidad Alternativo
```java
public SignalQuality getSignalQuality(RadioInfo info) {
    boolean hasRDS = info.mRDSstate != null && !info.mRDSstate.isEmpty();
    boolean hasPS = info.mPSname != null && !info.mPSname.isEmpty();
    boolean isLocal = info.mIsLocal;
    
    // Excelente: RDS completo + modo local
    if ((hasRDS || hasPS) && isLocal) {
        return SignalQuality.EXCELLENT;
    }
    
    // Buena: RDS presente
    if (hasRDS || hasPS) {
        return SignalQuality.GOOD;
    }
    
    // Aceptable: modo local sin RDS
    if (isLocal) {
        return SignalQuality.FAIR;
    }
    
    // Pobre: sin indicadores
    return SignalQuality.POOR;
}
```

---

## 🎯 Conclusión

### Causa Más Probable
**Umbral de tono piloto demasiado alto** (`FM_RX_PRX_TH = 64`)

### Solución Inmediata (Sin Root)
1. Llamar `player.stereo(true)` al iniciar
2. Usar RDS como indicador de calidad en lugar de estéreo
3. Asumir estéreo si hay RDS/PS

### Solución Definitiva (Con Root)
1. Reducir `FM_RX_PRX_TH` de 64 a 32
2. Reiniciar sistema
3. Verificar detección de estéreo

### Para OpenRadioFM
**No depender de `mIsStereo`** para indicador de calidad. Usar combinación de:
- RDS state
- PS name
- Modo local

Esto proporcionará una mejor experiencia de usuario sin necesidad de root.

---

## 📝 Notas Adicionales

> [!IMPORTANT]
> El problema de detección de estéreo es **común en radios chinas** debido a:
> - Umbrales conservadores en configuración de fábrica
> - Antenas de baja calidad
> - Configuración optimizada para zonas urbanas con señales fuertes

> [!TIP]
> Para usuarios avanzados con root:
> - Experimentar con valores de `FM_RX_PRX_TH` entre 24-48
> - Hacer backup de `fm_cust.cfg` antes de modificar
> - Reiniciar después de cada cambio

> [!WARNING]
> - NO modificar otros parámetros sin conocimiento técnico
> - Valores muy bajos de PRX_TH pueden causar falsos positivos
> - Siempre hacer backup antes de modificar archivos del sistema
