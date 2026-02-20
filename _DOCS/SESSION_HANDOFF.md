# 🔄 Session Handoff — 19 Feb 2026

**Versión actual**: V9.4d  
**Estado**: Funcional con bugs menores  
**Próxima sesión**: 20 Feb 2026

---

## 🏆 Logro del Día

**La app arranca y controla la radio FM de forma independiente, sin necesidad de la app nativa.**

---

## Lo que funciona ✅

- Sintonización de frecuencia (tune, fine step up/down)
- Seek up/down (0x0C/0x0D) con banda persistente
- Cambio de banda FM1/FM2/FM3 (se mantiene al hacer seek)
- RDS: nombre de emisora (PS) y texto (RT)
- AutoScan
- Presets (seleccionar y guardar)
- Botón EQ → abre `com.qf.soundeffect`
- Audio FM vía `RPC_SetChannel(2)`

## Lo que falla ❌

1. **PTY aplica filtro al abrir** — La MCU filtra por PTY (noticias). Debe solo mostrar el PTY actual, no filtrar
2. **LOC/DX** — Sin comando MCU identificado. Solo toggle visual
3. **Audio intermitente** — A veces el audio no arranca al primer intento

## Contexto técnico clave

- Todos los sub-comandos MCU están verificados contra `INGENIERIA_INVERSA_K706_FM.md`
- Tabla de comandos actualizada en `K706RadioManager.java` líneas 32-48
- El comando `0x0A` es **cambio de región geográfica**, NO LOC/DX
- `handlePresetList()` ya NO actualiza `mCurrentBand` (fix del reset FM1)
- Hay dos stacks FM disponibles: **QF SDK** (activo) y **Broadcom FM** (dormido)

## Archivos principales

| Archivo | Rol |
|---|---|
| `K706RadioManager.java` | Driver MCU — comandos y callbacks |
| `MainActivity.java` | UI — botones, display, presets |
| `INGENIERIA_INVERSA_K706_FM.md` | Fuente de verdad de comandos MCU |
| `auditoria_mcu.md` | Informe de discrepancias (artifact) |

## Primera tarea mañana

1. **Fix PTY filter**: Enviar `[0xA2, 0x00]` al inicio para resetear el filtro PTY
2. **Capturar logs LOC/DX** de la app nativa: `adb logcat -s McuManager QFTunerManager`
3. Probar Broadcom FM Service binding
