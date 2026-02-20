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

---

## 🔴 Prioritario (Próxima Sesión)

### 1. PTY Filter Bug
- Al abrir la app, aplica un filtro PTY (busca solo emisoras de noticias)
- **Causa probable**: La MCU conserva un filtro PTY de sesiones anteriores
- **Solución**: Enviar reset PTY al inicio → `[0xA2, 0x00]` (setRdsPtyType=0)
- **Alternativa**: Verificar si el paquete 0xB5 se está interpretando como comando

### 2. LOC/DX sin comando MCU real
- El comando `0x0A` cambia la **región geográfica**, no LOC/DX
- Actualmente solo toggle visual (no envía nada a MCU)
- **Acción**: Capturar logs MCU de la app nativa al pulsar LOC/DX
- **Comando**: `adb logcat -s McuManager QFTunerManager | grep -i loc`

### 3. Seek — Verificación profunda
- La banda ya se mantiene ✅
- Falta verificar: ¿el seek encuentra emisoras correctamente en FM2/FM3?
- ¿La MCU reporta correctamente la frecuencia encontrada?

---

## 🟡 Mejoras Planificadas

### 4. Audio Path Fix
- `RPC_SetChannel(2)` funciona pero el audio a veces no arranca
- Implementar secuencia completa: `setMute(true)` → `RPC_SetChannel(2)` → `setMute(false)` → `setVolume(9)`

### 5. Broadcom FM Service
- Intentar binding para obtener:
  - RSSI/SNR en tiempo real (`setLiveAudioPolling`)
  - Barra de señal REAL
  - `setSnrThreshold()` para búsqueda configurable
  - `estimateNoiseFloorLevel()` para diagnóstico RF

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
- `setFMVolume()` vía Broadcom para control separado del sistema