# Handoff — OpenRadioFM v5.7 + WIP 8667/K706

_Última actualización: 2026-11-17 (pausa hasta contestación)_

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` (**ahead ~6** vs `origin/main`, **sin push**) |
| Tag release | `v5.7` (local; no publicado) |
| Commit release | `79c0f5f1` — v5.7 HCN V31 / Junsun V7 |
| Último WIP | `45776fcf` — 8667 QUIT v2 |
| Working tree | limpio (al cierre) |
| App en WIP | `5.7.4-8667quit2` / versionCode `49` |
| Release cerrada | `5.7` / versionCode `45` |

## Binarios

| APK | Ruta |
|-----|------|
| Release v5.7 | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test 8667 QUIT v2 (**Csaba**) | `K706_RE\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk` |
| Test 8667 RDS (histórico OK) | `K706_RE\8667\OpenRadioFM-5.7.1-8667fc-TEST.apk` |
| Test 8667 QUIT v1 (FALLA) | `K706_RE\8667\OpenRadioFM-5.7.2-8667quit-TEST.apk` |
| Test K706 amp (**Luis**) | `K706_RE\K706_test\OpenRadioFM-5.7.3-k706bg-TEST.apk` |

Parche V7: `K706_RE\MT8768\OpenRadioFM-V7-patch.zip`  
Parche 8667: `K706_RE\8667\Factory_compatible.zip`

---

## Hecho

### v5.7 — Junsun V7 / MT8768
- HCN V31 + regresión MT8163; docs/web OK.
- **Csaba 17/11:** uso diario OK (recepción, seek/autoscan, RDS, logos).
- Pendiente publicar: push + tag + GitHub Release (cuando se pida).

### 8667 — RDS factory + QUIT
- RDS vía `ITsRadioCommon` / MainUI stock — OK en `5.7.1`.
- QUIT v1 (`5.7.2`) FALLA: motor zombie + mute MainUI.
- QUIT v2 (`5.7.4`): `closeDevice`≠`release`, clear shared, unmute, `finishAndRemoveTask`, binder RDS invalidable.
- **Pendiente:** validación Csaba de `5.7.4-8667quit2`.

### K706 — PowerOff amp (Luis)
- `SetChannel(4)` + abandon focus + `setMute(false)`.
- **Pendiente:** validación Luis de `5.7.3-k706bg`.
- Llamadas BT/HFP (Luis M-M): ticket aparte.

### K706 — PowerOff con streaming online (Luis, 18/11)
- **Bug:** PowerOff en K706 = minimizar (`moveTaskToBack`); ExoPlayer vive en la Activity y seguía sonando.
- **Fix:** `OnlineStreamManager.stopForPowerOff()` (libera ExoPlayer, `setOnlineStreamingActive(false)`, sin `switchToFmAudio`/handoff). Llamado en `minimizeToHomeKeepingMediaControl()` y `powerOffTopway8667()`.
- **APK test:** `K706_RE\K706_test\OpenRadioFM-5.7.6-k706stream-TEST.apk` (versionCode `51`).
- **Pendiente:** validación Luis.

---

## Pendiente (al retomar)

1. Contestación **Csaba** → `5.7.4-8667quit2` (RDS frío, QUIT, reabrir con audio+RDS).
2. Contestación **Luis** → `5.7.3-k706bg` (otras apps suenan tras PowerOff).
3. Si OK: consolidar WIP → release menor; push v5.7 cuando se autorice.

Fuera de alcance: Magisk K706 root, Z-Link/AA.

---

## Cómo verificar

```powershell
adb install -r "C:\@MIS PROYECTOS\K706_RE\8667\OpenRadioFM-5.7.4-8667quit2-TEST.apk"
adb logcat -v time -s MainActivity RadioServiceController MTK8259_8667Engine MTK8259_8667RM RadioMediaService
```

---

## Créditos
Csaba — V7/MT8768 y 8667 factory RDS.
