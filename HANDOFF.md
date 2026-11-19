# HANDOFF — OpenRadioFM (único handoff activo)

_Última actualización: 2026-11-19_

Workspace `K706_RE`: RE/firmware de autoradios Android. El trabajo activo es **OpenRadioFM**, una radio FM multiplataforma (K706, MT8163/8768, QS6, Topway 8259/8667, SPD, FYT).
Otros handoffs del workspace (no activos / proyecto aparte): `Junsun V9 Plus/README_SPD_HANDOFF.md` (SPD), `HANDOFF_K706_ROOT.md` (Magisk, fuera de alcance), `HANDOFF_5.5_*`/`5.6`, `_DOCS/` (histórico).

## Objetivo actual
Release **v5.8** publicada en GitHub (consolida V7 + WIP post-5.7). Seguimiento de feedback en HW y bugs menores.

---

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` |
| Tag release | `v5.8` (GitHub Release) |
| Commit release | (ver `git rev-parse v5.8`) |
| App | `5.8` / versionCode `55` |
| Release anterior en GitHub | `v5.6-fusion` |
| Tag local histórico | `v5.7` (solo local; contenido absorbido en 5.8) |

## Binarios

| APK | Ruta |
|-----|------|
| **Release v5.8** | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.8.apk` (+ `OpenRadioFM.v5.8.apk`) |
| Release v5.7 (histórico local) | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test MT8163 mute (pre-5.8) | `K706_RE\MT8163_test\OpenRadioFM-5.7.9-mt8163mute-TEST.apk` |

Parche V7: `K706_RE\MT8768\OpenRadioFM-V7-patch.zip` · Parche 8667: `K706_RE\8667\Factory_compatible.zip`

---

## Pendiente (al retomar)

1. Feedback post-release en MT8163 / 8667 / K706 / QS6 con APK **5.8**.
2. Si el switch de luces K706 se confirma → valorar inversión por defecto/auto-detección.
3. Vigilar anti-rebote QS6 (`poll settings: ignorando freq`).
4. Fuera de alcance: Magisk K706 root, Z-Link/AA, llamadas BT/HFP.

### Árbol de decisión al recibir feedback
- Luis: si el switch de luces arregla → valorar invertir por defecto; si no → logcat `MCU[0x22]`.
- Csaba NO OK → logcat tags `MTK8259_8667Engine` / `MTK8259_8667RM` / `RadioServiceController`.
- MT8163 NO OK → logcat `PlaybackManager MT8163Engine StatusRefreshCoordinator MainActivity`.

---

## Decisiones tomadas
- **5.8** es el primer GitHub Release tras `v5.6-fusion`; el tag local `v5.7` no se publicó.
- PowerOff por motor: 8667 = CloseRadioCh+EnterMode(0); K706 = minimizar + SetChannel(4); MT8163 = mute MCU vía `muteForPowerOff`.
- Luces K706: inversión manual `pref_hw_lights_invert` OFF por defecto.
- MT8163 mute: tecla MCU toggle rastreada (`sMt8163HwKeyMuted`).

---

## Archivos clave
- PowerOff: `MainActivity.handlePowerOffButton()` / `powerOffTopway8667()` / `minimizeToHomeKeepingMediaControl()`
- Mute: `PlaybackManager`, `StatusRefreshCoordinator`, `MT8163Engine`
- 8667: `MTK8259_8667Engine.java` / `MTK8259_8667RadioManager.java`
- K706: `K706RadioManager`, `K706EngineeringDialog`
- QS6: `QS6Engine`, `NWDTunerAdapter`, `CompositeRadioEngineCallback`

## Cómo verificar
```powershell
cd "C:\@MIS PROYECTOS\K706_RE\OpenRadioFM"; .\gradlew assembleRelease
adb install -r "C:\@MIS PROYECTOS\K706_RE\OpenRadioFM\_RELEASES\OpenRadioFM — v5.8.apk"
```

## Riesgos
- MT8163: mute por tecla es estado en memoria (proceso muerto → desync hasta pulsar).
- Parte del WIP 5.7.9 llegó a release sin validación final en todos los HU.

## Historial
- 2026-11-19: release **v5.8** (versionCode 55) — CHANGELOG/README/docs + GitHub Release desde cambios post-`v5.6-fusion`.
- 2026-11-18: WIP 5.7.1–5.7.9 (8667/K706/MT8163/QS6); tag `v5.7` local sin publicar; push `main`.
- 2026-11-14: cierre local v5.7 V7/MT8768.

## Créditos
Csaba — V7/MT8768 y 8667 factory RDS. Luis — validación K706 PowerOff/luces.
