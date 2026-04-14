# Protocolo de Comunicación MCU K706 (QuickFish)

Basado en análisis del stack OEM (smali) y validación en campo:
- `Radio_Decompiled/smali/com/qf/clientsdk/tuner/TunerCmdFactory.smali` (ARM→MCU: comandos del sintonizador)
- `Radio_Decompiled/smali/com/qf/clientsdk/DefaultMcuProessor.smali` + `Radio_Decompiled/smali/com/qf/clientsdk/McuConstant.smali` (MCU→ARM: constantes/encaminado)
- `framework_decompiled/smali_classes2/android/qf/backlight/QFBacklight.smali` y `framework_decompiled/smali_classes2/android/qf/os/QFSleepWakeup.smali` (eventos “vehículo” a nivel sistema: BACKCAR/ACC)

## 1. Comandos de Control (ARM -> MCU)

Estos comandos se envían a través de `RPC_SendMcuMsgData(cmdType, data, len)`.
El `cmdType` para la mayoría de los comandos de radio es `0xA0` (normalmente arrays de 4 bytes).

### Tipo 0xA0: Control del Sintonizador
| Sub-comando | Función | Datos adicionales |
| :--- | :--- | :--- |
| `0x00` | Sintonizar Frecuencia | `[byte1, byte2]` (Frecuencia en kHz) |
| `0x01` | Seek Up | - |
| `0x02` | Seek Down | - |
| `0x03` | Fine Up | - |
| `0x04` | Fine Down | - |
| `0x05` | Iniciar Auto Scan | - |
| `0x06` | Cambiar Banda | `[band]` (0:FM1, 1:FM2, 2:FM3, 3:AM1, 4:AM2) |
| `0x07` | Conmutar LOC/DX | `[mode]` (0:LOC, 1:DX) |
| `0x08` | Auto Store (AS) | - |
| `0x09` | Preset Scan (PS) | - |
| `0x0A` | Cambiar Área Radio | `[area]` (Europe, USA, etc.) |
| `0x0B` | Guardar Preset | `[index]` |
| `0x0C` | Detener Escaneo | - |
| `0x0D` | Seleccionar Preset | `[index]` |
| `0x0E` | Siguiente Favorito | - |
| `0x0F` | Anterior Favorito | - |
| `0x10` | Stereo/Mono | `[mode]` (0:Mono, 1:Stereo) |
| `0x11` | RDS AF Toggle | - |
| `0x12` | RDS TA Toggle | - |
| `0x13` | RDS REG Toggle | - |
| `0x15` | RDS PTY Select | `[pty_type]` |

### Tipo 0xA1: Sincronización de Presets
Se utiliza para enviar la lista completa de favoritos al hardware.

### Tipo 0xA2: RDS Master Control
| Código | Función |
| :--- | :--- |
| `0xA2 0x01` | Activar RDS (Master ON) |
| `0xA2 0x00` | Desactivar RDS (Master OFF) |

### Tipo 0x18: Comandos de Sistema
| Código | Función |
| :--- | :--- |
| `0x18 0x02 [vol]` | Ajustar Volumen del Amplificador |
| `0x18 0x05 [type]` | Configurar Tipo de Teclas de Volante |

---

## 2. Telemetría y Feedback (MCU -> ARM)

Estos códigos se reciben en el callback `onMcuInfoChanged`.

| Código | Función | Descripción / Estructura |
| :--- | :--- | :--- |
| `0xB0` | Estado del sintonizador (flags) | Flags de seeking/scanning/stereo/loc/dx… (la UI debe derivar “scanning” solo de AutoStore/Scan; no de seek). |
| `0xB1` | Presets / station info | Usado por el OEM para listas/preset info (implementación dependiente de firmware). |
| `0xB3` | RDS AF/TA *o* RT (ambiguo) | Algunos firmwares lo usan como flags AF/TA; otros lo usan para texto. Estrategia robusta: si el payload “parece texto” → tratar como RT, si no → flags AF/TA. |
| `0xB4` | RDS flags (TP) | En K706/QF se ha observado que **TP** se deriva de `0xB4`; el estado TA Switch se deriva de `0xB3`. |
| `0xB5` | RDS PTY | El PTY suele venir en `data[2]`. |
| `0xB6` | RDS PS (Station Name) | Texto PS (normalmente 8 chars, limpieza necesaria). |
| `0xB7` | RDS RT (RadioText) | Texto RT (limpieza necesaria). En muchos firmwares K706/QF, RT llega principalmente por `0xB7`. |
| `0xB8` | Lista PS / investigación | Paquete visto como “preset list/PS list” en algunos dumps. |
| `0x41` | Telemetría de señal | RSSI/SNR (según firmware; usado como métrica en OpenRadioFM). |
| `0x22` | Luces Vehículo | Headlights ON/OFF (para modo noche). En el OEM se ha observado lógica invertida en algún componente (revisar antes de asumir 1=ON). |
| `0x23` | Handbrake **(OEM)** / Reverse **(campo)** | En la radio OEM (`DefaultMcuProessor`), `0x23` se interpreta como **handbrake**. En campo, OpenRadioFM lo usa como **reverse** para automatizaciones. Tratarlo como “dependiente de firmware” y preferir BACKCAR a nivel sistema cuando exista. |
| `0x24` | Estado ACC | ACC ON/OFF (contacto). En la radio OEM no se vio consumo directo; a nivel sistema existe `Settings.System("acc_state")` como fuente alternativa. |

---

## 3. Estrategia para "Modo Master"

Para independizar OpenRadioFM totalmente de la app nativa:
1. **Hijacking de Audio:** Seguir usando `RPC_SetChannel(2)` para asegurar que el MCU dirija el audio de la radio a la salida aunque la app nativa intente cerrarlo.
2. **Sync de HUD:** Utilizar la reflexión de `requestSendCurrentFrequencyRdsInfo` o simular los broadcasts de QF.
3. **Mantenimiento de Estado:** Procesar activamente RDS desde `0xB6` (PS) y `0xB7` (RT) y tratar `0xB3` como caso ambiguo (flags o RT) para mostrar RDS sin depender de servicios intermedios.

---

## 4. Hallazgos confirmados (smali/framework) y “fuentes alternativas”

### 4.1. Constantes MCU→ARM (OEM)

En `com/qf/clientsdk/McuConstant.smali` están definidos, entre otros:
- `CMD_MCU2ARM_HEADLIGHT = 0x22`
- `CMD_MCU2ARM_HANDBRAKE = 0x23`
- `CMD_MCU2ARM_ACC_STATUS = 0x24`

Esto confirma que **`0x24` existe como ACC**, aunque puede no ser consumido por la app de radio OEM.

### 4.2. BACKCAR / marcha atrás (recomendado para “bajar volumen”)

En el framework QF, el modo cámara/backcar se publica como broadcast:
- `com.qf.action.BACKCAR_START`
- `com.qf.action.BACKCAR_STOP`

y se refleja también en propiedades como `sys.qf.backcar_state` (ver `android/qf/backlight/QFBacklight.smali`).

Implicación: si el objetivo es **bajar volumen al meter marcha atrás**, este broadcast suele ser una señal más estable que inferir reverse desde `0x23` (que puede ser handbrake según OEM).

### 4.3. ACC (contacto) a nivel sistema

En `android/qf/os/QFSleepWakeup.smali` se observa:
- Persistencia del estado en `Settings.System.putInt(..., "acc_state", 1/0)`
- Ajustes de audio mediante `AudioManager.setParameters("acc_off=0|1")`

Implicación: si `0x24` no llega o no es fiable, se puede usar `Settings.System("acc_state")` como fallback para saber si ACC está ON/OFF.
