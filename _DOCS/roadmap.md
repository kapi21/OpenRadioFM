# 🗺️ Roadmap — OpenRadioFM K706

## ✅ Completado (V9.4d — 19 Feb 2026)

- [x] **Arranque independiente** — La app arranca y controla la radio sin necesidad de la app nativa
- [x] **Audio FM directo** — `RPC_SetChannel(2)` implementado vía reflection
- [x] **Auditoría MCU completa** — Todos los sub-comandos verificados contra ingeniería inversa
- [x] **Seek corregido** — Usa 0x0C/0x0D correctos (antes usaba 0x01/0x02 = Tune)
- [x] **Banda persistente** — Seek ya no resetea a FM1
- [x] **Fine tuning unificado** — Un solo comando 0x03 con dirección
- [x] **PTY offset corregido** — Usa data[1] correcto
- [x] **Constantes MCU limpias** — Eliminados 6 duplicados/conflictos
- [x] **Botón EQ** — Apunta a `com.qf.soundeffect` con `getLaunchIntentForPackage()`
- [x] **RDS (PS + RT)** — Nombre y texto de emisora funcionando
- [x] **Fix Crash Luces** — Bloqueada recreación de Activity en `uiMode` y `nightMode`

---

## 🔴 Prioritario (Próxima Sesión) — 🚀 ESTRATEGIA "LEVEL UP"

Siguiendo el feedback técnico, mañana iniciaremos la transformación profunda de la arquitectura para convertir OpenRadioFM en un producto de ingeniería sólido.

### 1. Fase de Modularización & Kotlin Migration
- **Módulo `:core`**: Extraer parsers RDS y lógica de negocio a Kotlin puro.
- **Módulo `:engines`**: Implementar el patrón Strategy para separar el hardware.
- **MockEngine**: Implementar un simulador de tráfico MCU para desarrollo offline.
- **Kotlin First**: Migrar `K706RadioManager` y `HiddenRadioPlayer` a Kotlin, integrando **Corrutinas** para el manejo asíncrono.

### 2. RadioEngine Reactivo (StateFlow)
- Eliminar la dependencia de callbacks y polling.
- Exponer un `StateFlow<RadioStatus>` único desde el core.
- Transformar la UI para que sea "Stateless" y solo reaccione al hardware.

### 3. Bugs Pendientes (Integrar en la nueva arquitectura)
- **TA Bug Fix**: Investigar por qué el botón TA no muestra feedback visual y por qué entra en un bucle sin fin al activarse.
- **PTY Filter Reset**: Enviar `[0xA2, 0x00]` al inicio.
- **LOC/DX Command**: Capturar logs para identificar el comando real de sensibilidad.

---

## 🟡 Mejoras Planificadas

### 4. Audio Path Fix & Heartbeat
- Refinar secuencia: `setMute(true)` → `RPC_SetChannel(2)` → `setMute(false)`.
- **Auto-Corrección**: Implementar el Heartbeat inteligente para recuperar el canal de audio si el sistema lo "roba".

### 5. Broadcom FM Service
- Obtener RSSI/SNR real y barra de señal dinámica.

---

## 🎨 V11.0 — Visual WOW & Hacking
- **Espectro Dinámico**: Dibujar señal FFT basada en datos del tuner.
- **Glassmorphism Dinámico**: Desenfoque reactivo a la señal.
- **Analizador Lógico**: Herramientas integradas para exportar tramas a PulseView.

### 6. Funciones RDS Avanzadas
- PI Code (identificador único de emisora)
- TA (interrupciones de tráfico automáticas)
- Búsqueda por tipo de programa (`seekRdsStation`)

### 7. DAB Detection
- El hardware puede tener módulo DAB
- Callbacks `onDABSignalFound` disponibles en QF SDK

---

## 🟢 Infraestructura / UI

### 8. Presets masivos
- `setPresetList()` vía `0xA1` para carga/backup completo

### 9. Control de región
- UI para cambiar región FM (Europa/USA/Japón/etc.)
- Usa `0x0A` que ya sabemos que funciona

### 10. Volumen FM independiente
    - Usar la Raspberry Pi 400 como servidor de logs remoto por ADB inalámbrico para depurar sin cables.
    - Scripting en Python (desde la Pi) para automatizar pruebas de estrés sobre la placa MCU.

### 🎨 Nivel 2: UI/UX de Élite (Visual WOW)
- **Espectro Dinámico (FFT)**: Intentar obtener los niveles de portadora/ruido del Broadcom para dibujar un espectro real mientras se hace "Fine Tune".
- **Glassmorphism Dinámico**: Fondos que reaccionan sutilmente a la intensidad de la señal (blur variable).
- **Logos Cloud Pro**: Integración con API de Radio-Browser para descargar logos automáticamente por geolocalización.

### 🧠 Nivel 3: Inteligencia & Persistencia
- **Database 2.0**: Migración de SharedPreferences a SQLite/Room para gestionar miles de emisoras con metadatos extendidos (Género, País, Cobertura).
- **Auto-Corrección**: Heartbeat inteligente que detecta micro-cortes de audio y restablece el canal `0x02` en milisegundos sin que el usuario lo note.
