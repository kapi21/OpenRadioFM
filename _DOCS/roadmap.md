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

## 🔲 Sesión 25/Feb — Siguiente

### Prioridad 0: Investigación RDS PI (identificación instantánea de emisoras)
- [ ] Capturar logcat dirigido con emisora RDS activa (RNE, SER, COPE)
- [ ] Localizar el paquete MCU que contiene datos RDS crudos (PI, no B1)
- [ ] Investigar acceso al PI via `IFmReceiverService` por reflexión
- [ ] Si se encuentra: diseñar función de extracción PI de ByteArray
- [ ] Crear base de datos PI → nombre para emisoras España (~500 entradas)
- [ ] Estructura PI español: `E` = España, 2º nibble = cobertura, 3-4 = red

### Prioridad 1: Completar migración RadioEngine
- [ ] Migrar EQ/DSP al engine (cada motor sabe qué app de sonido abrir)
- [ ] Migrar `initHiddenPlayer()` al engine MT8163 (ya lo hace `MT8163Engine.init()`)
- [ ] Migrar `gotoFreq()`, `refreshRadioStatus()` para usar `mEngine`
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
