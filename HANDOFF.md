# HANDOFF - OpenRadioFM

_Última actualización: 2026-11-18_

## Objetivo
Radio FM multiplataforma (K706, MT8163/8768, QS6, Topway 8259/8667, SPD, FYT). Cerrar v5.7 (V7) y estabilizar 8667 (RDS factory + QUIT) y K706 (PowerOff amp, luces).

## Estado
- Hecho:
  - v5.7 local (MT8768 OK con Csaba); RDS 8667 factory OK.
  - 8667 QUIT v2 (`5.7.4-8667quit2`): Csaba confirma que funciona (a falta de test completo).
  - K706 PowerOff amp (`5.7.3-k706bg`): Luis confirma OK.
  - K706 luces invertidas (apagadas=noche): añadido ajuste `INVERT_LIGHTS_SIGNAL (0x22)` en diálogo ingeniería K706 (`pref_hw_lights_invert`, default OFF, aplicado en `MainActivity.handleHwLightsAutomation`).
  - APK acumulado: `..\K706_test\OpenRadioFM-5.7.5-k706lights-TEST.apk` (`5.7.5-k706lights`, vc 50; incluye fixes 8667 + K706). Commit local `4ad37c78`.
- Pendiente (esperando respuesta, nada que hacer hasta entonces):
  1. Luis: activar el switch (mantener 6 s botón GPS → HARDWARE_AUTOMATION_MODULE) y confirmar luces/modo noche.
  2. Csaba: test completo de 8667 (RDS frío, QUIT, reabrir con audio+RDS).
- Bloqueos: sin push/release hasta feedback.

## Decisiones tomadas
- PowerOff por motor: 8667 = CloseRadioCh+EnterMode(0); K706 = modo B + SetChannel(4)+unmute
- 8667: `closeDevice()` ≠ `release()`; limpiar `sSharedLocalEngine` al liberar
- Luces K706: el significado del byte MCU `0x22` (01=ON) era suposición; se deja ajuste de inversión manual, desactivado por defecto, para no afectar otras unidades.
- Fuera de alcance: Magisk K706 root, Z-Link/AA, llamadas BT/HFP de Luis (ticket aparte)

## Archivos clave
- `HANDOFF_5.7.md` — detalle commits, APKs, historial técnico
- `MTK8259_8667Engine.java` / `MTK8259_8667RadioManager.java`
- `MainActivity.powerOffTopway8667()` / `RadioServiceController.checkAndStartTsEngine()`
- `MainActivity.handleHwLightsAutomation()` / `K706EngineeringDialog` / `dialog_k706_engineering.xml`
- `K706RadioManager` (paquete MCU `0x22` luces)
- APK Csaba (8667): `..\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk` (también incluido en 5.7.5)
- APK Luis (K706): `..\K706_test\OpenRadioFM-5.7.5-k706lights-TEST.apk`

## Cómo retomar
1. Leer este archivo + `HANDOFF_5.7.md`
2. Luis: si el switch arregla → valorar invertir por defecto/auto-detección; si no → pedir logcat `MCU[0x22]` (luces ON/OFF).
3. Csaba OK en 8667 → consolidar WIP, release menor (5.7.x) + push/tag v5.7 cuando se autorice.
4. Si Csaba NO OK → logcat tags `MTK8259_8667Engine` / `MTK8259_8667RM` / `RadioServiceController`
5. Git: `main` ahead local (sin push); último commit `22d75841`

## Historial
- 2026-11-18 (noche): usuario prueba `5.7.8-qs6fix` → OK en QS6 y MT8163; 2 bugs nuevos MT8163 sin corregir (PowerOff no corta FM; icono mute desincronizado). Ver `HANDOFF_5.7.md`.
- 2026-11-18 (tarde): fixes K706 stream PowerOff (5.7.6) y MT8163 (5.7.7); auditoría QS6 revisada y corregida → APK `..\QS6_test\OpenRadioFM-5.7.8-qs6fix-TEST.apk` (vc 53, commit `22d75841`). **Estado vigente en `HANDOFF_5.7.md`.**
- 2026-11-18: Csaba OK QUIT v2; Luis OK PowerOff; luces K706 invertidas → ajuste invertir señal + APK 5.7.5; pausa hasta feedback
- 2026-11-17: QUIT v2 8667; MT8768 confirmado uso diario; pausa hasta contestación Csaba/Luis
- 2026-11-16: PowerOff 8667 v1 falló; fix K706 amp Luis; RDS factory OK en 5.7.1
- 2026-11-14: cierre v5.7 V7/MT8768 + tag local
