# Roadmap OpenRadioFM

## ✅ Sesión 24/Feb/2026 — Completado

### Profesionalización del repositorio
- `.gitignore` robusto (builds, logs, APKs, certificados, RE, IDE)
- Limpieza masiva: **2.347 archivos eliminados** del tracking Git
- `LICENSE` Apache 2.0
- `README.md` profesional (badges, Mermaid, tabla comparativa HW)

### Funcionalidades nuevas
- AF/TA/TP funcional en MT8163 (`setAF`/`setTA` por reflexión en `HiddenRadioPlayer`)
- Callback `onRdsAfTaStatus` para feedback visual en tiempo real
- Selector de motor de radio movido del Settings al Engineering Menu

### Bug fixes
- TA persistente en K706 (paquete `0xB4` ya no sobreescribe estado)
- Alpha del botón DX/Local corregido en K706
- Icono stereo ya no desborda RDS text en Layout V3

### Refactor: RadioEngine (arquitectura)
- **Rama:** `refactor/radio-engine-interface`
- **Nuevos archivos:**
  - `RadioEngine.java` — interfaz unificada (18 métodos)
  - `RadioEngineCallback.java` — 10 callbacks
  - `K706Engine.java` — wraps K706RadioManager
  - `MT8163Engine.java` — wraps AIDL + HiddenRadioPlayer
- **MainActivity migrada:**
  - `mEngine` como capa de abstracción
  - AF/TA/TP: 44 líneas → 3 líneas
  - Seek invertido MT8163: gestionado por engine
  - LOC/DX: 10 líneas → 3 líneas
  - Bifurcaciones `if (mMode == ...)`: **12 → 5** (las 5 restantes son decisiones de arranque)

---

## ✅ Sesión 25/Feb/2026 — Completado

### RDS PI Investigation (Resultado: NO viable en K706)
- [x] ITunerTool proxy implementado vía `Proxy.newProxyInstance`
- [x] Logcat en dispositivo real confirma: **QFTunerManager NOT FOUND**
- [x] MCU no reenvía PI crudo (solo PS, RT, PTY, flags)
- [x] Conclusión: Identificación cross-frecuencia requiere otro enfoque

### RadioEngine V11
- [x] `openEq(Context)` añadido a `RadioEngine`
- [x] `onRdsPi(String)` añadido a `RadioEngineCallback`
- [x] K706Engine: EQ abre `com.qf.soundeffect`
- [x] MT8163Engine: EQ inyecta MCU key `0x134`
- [x] EQ en MainActivity: **25 líneas → 3 líneas** (delegado al engine)

### RdsDatabase (Infraestructura lista)
- [x] `RdsDatabase.java`: PI→Nombre y PI→Logo via SharedPreferences
- [x] Aprendizaje automático en `onRdsName` + lookup en `onRdsPi`
- [x] (Pendiente de PI real para activarse — funcional pero inactivo)

---

## 🔲 Sesión 25/Feb (continuación) — En Progreso

### Prioridad 1: Completar migración RadioEngine
- [ ] Migrar `refreshRadioStatus()` para que use `mEngine` en vez de `mRadioService`
- [ ] Eliminar `initHiddenPlayer()` duplicado en MainActivity
- [ ] Eliminar `execRemote()` cuando toda la lógica use `mEngine`

### Prioridad 2: Unificar Engineering Dialogs
- [ ] Crear `EngineeringDialog` base que se adapte al engine activo
- [ ] Eliminar duplicación entre `EngineeringModeDialog` y `K706EngineeringDialog`

### Prioridad 3: Reducir MainActivity
- [ ] Extraer lógica de presets a `PresetManager`
- [ ] Extraer lógica de skins/temas a helper separado
- [ ] Objetivo: `MainActivity` de ~3500 líneas a <1500

### Prioridad 4: Merge y testing
- [ ] Merge `refactor/radio-engine-interface` → `feature/k706-mt8163-unification`
- [ ] Test completo en K706: tune, seek, RDS, AF/TA, presets
- [ ] Test completo en MT8163: seek invertido, AF/TA por HiddenRadioPlayer
