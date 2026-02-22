# OpenRadioFM - Tareas Pendientes y Problemas Conocidos

Este documento rastrea los bugs pendientes de solución, mejoras futuras y problemas derivados de la integración de la app con el hardware nativo del coche (K706).

## 🛠️ Sistema de Audio y Foco (Heartbeat)

Actualmente, el mecanismo "Heartbeat" (que fuerza el Canal 2 cada segundo) garantiza que la radio FM se recupere de los secuestros del sistema nativo (pérdida de Bluetooth, marcha atrás, etc.). Sin embargo, su agresividad genera los siguientes efectos secundarios pendientes de pulir:

### 1. Salto de Vólumen ('Pop') al Recuperar
- **Problema:** Hay un leve salto o estallido en el volumen cuando el Heartbeat reinicia la reproducción de la FM tras desconectar el Bluetooth.
- **Análisis:** La radio nativa transiciona de forma más suave. Nuestro comando seco de `RPC_SetChannel(2)` y posterior un-mute abre los amplificadores físicos de golpe.
- **Solución Propuesta:** Investigar si hay comandos intermedios de "fade in" o retrasar la llamada de `setMute(false)` un par de milisegundos para suavizar la entrada del amplificador.

### 2. Conflicto con Spotify en Android Auto
- **Problema:** Al lanzar Spotify desde Android Auto, la música comienza silenciada (Mute). Se arregla inmediatamente al tocar la ruleta física del volumen.
- **Análisis:** Ocurre un choque de trenes. Spotify pide legalmente a Android usar el router (Canal 4). Nuestro Heartbeat lo detecta, entra en pánico asumiendo que es un "secuestro" nativo, y fuerza el Canal 2 de nuevo, dejando el canal 4 silenciado en segundo plano. Al modificar el volumen, Android Auto revalida su Foco y aplasta finalmente a nuestro Heartbeat.
- **Solución Propuesta:** Rediseñar la gestión del estado en `onAudioFocusChange`. Cuando una fuente *legítima* (como Spotify) pida Foco Exclusivo de hardware, debemos setear `mIsRadioActive = false` (o un flag interno de "pausa forzada") para evitar que el Heartbeat pelee contra el conductor que quiere escuchar música.

### 3. Mezcla de Audios con el Asistente de Voz de Google (Ducking Fallido)
- **Problema:** Al usar comandos de voz en Android Auto, la radio FM sigue sonando a volumen máximo por encima de lo que responde el asistente de Google.
- **Análisis:** Las radios Android funcionan bajando el volumen a las apps de fondo (Ducking) cuando emiten un `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`. Como el audio de nuestra FM viene de los chips físicos y **no** de la placa de Android, Android no puede bajarle el volumen por software. Además, el Heartbeat se empeña activamente en no dejar que el sonido se interrumpa.
- **Solución Propuesta:** Manejar explícitamente el evento `*_CAN_DUCK` en el Receiver. En ese momento, en lugar de pelearnos contra el sistema, mandar nosotros la orden directa al chip de radio (`mSetMute(true)` o bajando el volumen por hardware momentáneamente) hasta que el asistente termine de hablar (`AUDIOFOCUS_GAIN`).

---

## 📻 Interfaz y Diseño (Completado)
- **Implementación de Iconos RDS (AF, TA, TP):** Se ha completado la integración visual y la lógica de decodificación de paquetes MCU `0xB3` y `0xB4`. Los indicadores ya responden a los cambios de estado enviados por el hardware.

## ⚙️ Conectividad y Hardware
*(Añadir aquí tareas de Bluetooth, stack JNI, etc.)*
