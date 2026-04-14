# Protocolo de Comunicación MCU K706 (QuickFish)

Basado en el análisis de `TunerCmdFactory.smali` y `DefaultMcuProessor.smali`.

## 1. Comandos de Control (ARM -> MCU)

Estos comandos se envían a través de `RPC_SendMcuMsgData(cmdType, data, len)`.
El `cmdType` para la mayoría de los comandos de radio es `0xA0`.

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
| `0xB0` | Info Frecuencia Actual | Devuelve banda y frecuencia actual. |
| `0xB1` | Info Lista Presets | Devuelve los favoritos almacenados en MCU. |
| `0xB2` | RDS PS Info | **RDS Station Name** (8 bytes de texto). |
| `0xB3` | RDS RT Info | **RDS Radio Text** (Texto dinámico). |
| `0xB4` | RDS Status Flags | Estado de AF, TA, TP y recepción RDS. |
| `0xB5` | Señal / RSSI | Fuerza de señal y calidad de audio. |
| `0xB6` | Estado Sintonizador | Indica si está escaneando, si es Stereo, etc. |
| `0xB7` | RDS PTY | Tipo de programa (News, Pop, etc.). |
| `0x22` | Luces Vehículo | Estado de los faros (para modo noche). |
| `0x23` | Freno de Mano | Estado del parking brake. |
| `0x24` | Estado ACC | Contacto del vehículo (On/Off). |

---

## 3. Estrategia para "Modo Master"

Para independizar OpenRadioFM totalmente de la app nativa:
1. **Hijacking de Audio:** Seguir usando `RPC_SetChannel(2)` para asegurar que el MCU dirija el audio de la radio a la salida aunque la app nativa intente cerrarlo.
2. **Sync de HUD:** Utilizar la reflexión de `requestSendCurrentFrequencyRdsInfo` o simular los broadcasts de QF.
3. **Mantenimiento de Estado:** Procesar activamente los paquetes `0xB2` y `0xB3` para mostrar RDS sin depender de servicios intermedios.
