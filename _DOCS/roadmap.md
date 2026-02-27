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

---

## ✅ Sesión 25/Feb/2026 (Noche) — Completado (V.4.6 Beta)

### Localización y Hardware
- [x] **Multi-idioma:** Integración total de Rumano, Ucraniano y Serbio (6 idiomas total).
- [x] **QS6 G5:** Soporte inicial para hardware NWD G5 mediante `QS6Engine`.
- [x] **MT8163 Fixes:** Solucionado el problema de frecuencia 0.0 y sincronización de iconos AF/TA/TP.

### Arquitectura y Web
- [x] **Unificación:** `RadioEngine` es ahora el estándar. Rama de refactor fusionada en `main`.
- [x] **Web Premium:** Rediseño estético total (Glassmorphism + Mesh Gradients) y nuevas capturas.
- [x] **Despliegue:** Web oficial actualizada y en producción.

---

## ✅ Sesión 27/Feb/2026 — Completado (V.4.6.1 Stable Integration)

### Navegación y Control (Hardware)
- [x] **Navegación de Favoritos:** Rediseño de Layout 2 y Layout 3 para rodear la frecuencia con botones `Seek Down - Prev Fav - Next Fav - Seek Up`.
- [x] **Integración MCU (K706):** Envío de comandos `0x0E` (Prev Fav) y `0x0F` (Next Fav) directamente al hardware.
- [x] **Integración MT8163:** Implementada reflexión sobre `HiddenRadioPlayer` para saltos de emisora memorizada.

### Interfaz y Experiencia Premium
- [x] **Barra de Estado Opcional:** Añadida opción en el menú Premium para habilitar/deshabilitar la status bar de Android en el Layout 2.
- [x] **Easter Egg "Hacker":** Nuevo diálogo de créditos premium con imagen personalizada de Jimmy80 y fondo traslúcido.
- [x] **Corrección de Colores:** Tintado azul noche unificado para ST, PTY, AF, TA y TP en modo noche.

### Organización y Limpieza
- [x] **Consolidación de Docs:** Migración de logs de investigación y limpieza de la estructura de carpetas raíz.
- [x] **Documentación Actualizada:** README y Manual de Usuario reflejan las nuevas capacidades de la versión 4.6.

---

## 🔲 Próximos Pasos (V.5.0+)

### Prioridad 1: Estabilidad
- [ ] Monitorizar feedback de los nuevos botones de favoritos en mandos de volante.
- [ ] Refinar sensado de señal RDS en zonas de baja cobertura.

### Prioridad 2: Funcionalidades Premium
- [ ] **Visualización Espectro:** Implementar visualizador de audio integrado en la UI principal.
- [ ] **Android Auto:** Soporte para control de medios desde la unidad principal externa.

### Prioridad 3: Limpieza Continua
- [ ] Extraer lógica de presets a `PresetManager`.
- [ ] Implementar sistema de temas dinámicos basado en JSON.
