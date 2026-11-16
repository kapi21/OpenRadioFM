# Handoff — OpenRadioFM v5.7 + WIP 8667 (sesión 2026-11-14)

Documento de continuidad al cierre de sesión.

## Estado git / versiones

| Ítem | Valor |
|------|--------|
| Rama | `main` (**ahead 2** vs `origin/main`, sin push) |
| Tag release | `v5.7` (local; no publicado en remoto) |
| Commit release | `79c0f5f1` — v5.7 HCN V31 / Junsun V7 |
| Commit WIP | `7a2b1e6c` — 8667 factory-compatible RDS |
| Working tree | limpio |
| App en WIP | `5.7.2-8667quit` / versionCode `47` (RDS factory + PowerOff real 8667) |
| Release cerrada | `5.7` / versionCode `45` |

## Binarios

| APK | Ruta |
|-----|------|
| Release v5.7 | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.apk` |
| Test 8667 RDS | `K706_RE\8667\OpenRadioFM-5.7.1-8667fc-TEST.apk` |
| Test 8667 RDS+QUIT | `K706_RE\8667\OpenRadioFM-5.7.2-8667quit-TEST.apk` |
| Mismo test (copia) | `OpenRadioFM\_RELEASES\OpenRadioFM — v5.7.2-8667quit TEST.apk` |

Archivo de parche V7 archivado en: `K706_RE\MT8768\OpenRadioFM-V7-patch.zip`

Fuente parche 8667: `K706_RE\8667\Factory_compatible.zip` (+ extract en `8667\_factory_compatible_extract\`)

---

## 1. Hecho en esta línea de trabajo

### 1.1 v5.7 — Junsun V7 / MT8768 (cerrado, vía verde)
- `HcnApiFactory` / `HcnV31Api`: contrato AIDL v25 vs v31.
- Regresión MT8163 OK; V7 OK (Csaba).
- Docs/web/README/CHANGELOG actualizados a v5.7.
- **Pendiente publicar:** push `main` + tag `v5.7` + GitHub Release.

### 1.2 WIP 8667 — RDS factory-compatible (gracias Csaba)
- Quitados métodos RDS custom de `ITsCommon.aidl`.
- `MTK8259_8667RadioManager`: RDS vía `getSpecialBinder("Radio")` → `ITsRadioCommon`.
- `TopwayTunerAdapter`: llamadas RDS a ITsCommon comentadas (ruta no usada en producción).
- **Csaba (16/11):** RDS OK con MainUI original.

### 1.3 WIP 8667 — PowerOff real (16/11)
- Causa: el botón Power llamaba `minimizeToHomeKeepingMediaControl()` (solo mute) en **todos** los motores.
- Fix: `handlePowerOffButton()` → en `MTK8259_8667` llama `closeDevice()` (`CloseRadioCh` + `EnterMode(0)`) y `finish()`; K706 sigue en modo B.
- APK test: `5.7.2-8667quit` — **pendiente validación Csaba**.

### Notas K706 (no en este WIP)
- Luis: tras PowerOff otras apps sin audio → mute global `RPC_SetVolumeMute` sin desmutear (ticket aparte).
- Luis M-M: FM audible en llamadas → ducking BT/HFP frágil (ticket aparte).

---

## 2. Pendiente inmediato (prioridad)

1. **Csaba — test APK `5.7.2-8667quit`**: RDS + botón Power (¿apaga de verdad o solo mute?).
2. Si OK: consolidar WIP 8667 → release menor; si no, ajustar secuencia.
3. **Cuando se pida:** push + release pública v5.7 (y Pages).
4. Tickets K706 PowerOff mute / llamadas: más adelante.

Fuera de alcance / no seguir en esta línea: Magisk K706 root, Z-Link/AA.

---

## 3. Comandos útiles

```powershell
# Test 8667
adb install -r "C:\@MIS PROYECTOS\K706_RE\8667\OpenRadioFM-5.7.1-8667fc-TEST.apk"

# Release v5.7
adb install -r "_RELEASES\OpenRadioFM — v5.7.apk"

# Rebuild
.\gradlew.bat assembleRelease
```

---

## 4. Créditos
- Junsun V7 (MT8768) y parche 8667 factory RDS: **Csaba**.
