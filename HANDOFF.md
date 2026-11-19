# HANDOFF — OpenRadioFM (único handoff activo)

_Última actualización: 2026-11-19 (cierre sesión)_

Workspace `K706_RE`: RE/firmware de autoradios Android. Trabajo activo: **OpenRadioFM**.
Otros handoffs (no activos): `Junsun V9 Plus/README_SPD_HANDOFF.md`, `HANDOFF_K706_ROOT.md`, `_DOCS/` (histórico).

## Objetivo actual
Seguimiento post-release **v5.8**: feedback de hardware y bugs menores. No regenerar APK solo por créditos About (decisión 19/11: mantener así).

---

## Estado

| Ítem | Valor |
|------|--------|
| Rama | `main` = `origin/main` (limpia) |
| Release GitHub | **[v5.8](https://github.com/kapi21/OpenRadioFM/releases/tag/v5.8)** (Latest) |
| Commit tag `v5.8` | `ac0ca3ee` |
| HEAD | `1e0f6191` (docs naming + agradecimientos; posterior al tag) |
| App en tag | `5.8` / versionCode `55` |
| Web | https://kapi21.github.io/OpenRadioFM/ (`docs/`) |
| Release anterior | `v5.6-fusion` |
| Tag local histórico | `v5.7` (solo local; absorbido en 5.8) |

### Hecho (sesión 2026-11-19)
- Publicado **v5.8** con todo lo post-`v5.6-fusion` (V7 + WIP MTK8259_8667 / K706 / MT8163 / NWD).
- Naming público: QS6 → **NWD (QS, G5)**; 8667 → **MTK8259_8667**.
- Agradecimientos: Csaba, Luis @luisfandi, Al @a87lv (web, release notes, About en código).
- Mensaje Telegram preparado (no técnico).
- Decisión: no regenerar APK 5.8 solo por textos About.

### Pendiente (próxima sesión)
1. Feedback HW con APK **5.8** (MT8163, MTK8259_8667, K706, NWD).
2. Si luces K706 OK con switch → valorar inversión por defecto.
3. Vigilar anti-rebote NWD (`poll settings: ignorando freq`).
4. Fuera de alcance: Magisk K706 root, Z-Link/AA, BT/HFP.

### Árbol de decisión feedback
- Luis / luces: OK → invertir por defecto; NO → logcat `MCU[0x22]`.
- Csaba NO OK → `MTK8259_8667Engine` / `MTK8259_8667RM` / `RadioServiceController`.
- MT8163 NO OK → `PlaybackManager MT8163Engine StatusRefreshCoordinator MainActivity`.

---

## Decisiones
- **5.8** = primer GitHub Release tras `v5.6-fusion`; `v5.7` local no se publicó.
- Créditos About en `main` pero **APK de release sin ese delta** (aceptado).
- PowerOff: MTK8259_8667 = CloseRadioCh+EnterMode(0); K706 = minimizar+SetChannel(4); MT8163 = mute MCU `muteForPowerOff`.
- Luces K706: `pref_hw_lights_invert` OFF por defecto.

## Binarios
| APK | Ruta |
|-----|------|
| **Release v5.8** | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.8.apk` |
| Histórico v5.7 | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test pre-5.8 MT8163 | `K706_RE\MT8163_test\OpenRadioFM-5.7.9-mt8163mute-TEST.apk` |

## Archivos clave
- PowerOff / mute: `MainActivity`, `PlaybackManager`, `MT8163Engine`
- MTK8259_8667: `MTK8259_8667Engine.java`, `MTK8259_8667RadioManager.java`
- K706: `K706RadioManager`, `K706EngineeringDialog`
- NWD: `QS6Engine`, `NWDTunerAdapter`
- Créditos UI: `dialog_about.xml`, `about_contrib_*` en `strings.xml`
- Web: `docs/*/index.html` (`#credits-v58`)

## Cómo verificar
```powershell
cd "C:\@MIS PROYECTOS\K706_RE\OpenRadioFM"
adb install -r "C:\@MIS PROYECTOS\K706_RE\OpenRadioFM\_RELEASES\OpenRadioFM — v5.8.apk"
```

## Riesgos
- MT8163: mute por tecla en memoria (proceso muerto → desync hasta pulsar).
- Parte del WIP 5.7.9 entró a 5.8 sin validación final en todos los HU.
- HEAD docs > tag `v5.8` (naming/créditos); APK publicado = contenido del tag.

## Historial
- 2026-11-19 (cierre): v5.8 publicada; naming NWD/MTK8259_8667; créditos web+release; About en código sin rebuild APK; msg Telegram.
- 2026-11-18: WIP 5.7.1–5.7.9; tag `v5.7` local sin publicar.
- 2026-11-14: cierre local v5.7 V7/MT8768.

## Créditos
- **Csaba** — V7/MT8768 y MTK8259_8667.
- **Luis [@luisfandi](https://github.com/luisfandi)** — K706.
- **Al @a87lv** — pruebas HW.
