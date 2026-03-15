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

## ✅ Sesión 12/Marzo/2026 — Completado (V.19.5 Cloud ID)

### Identificación y Telemetría
- [x] **DeviceMetadataUtils:** Implementación de identificador único de dispositivo (HW ID / Android ID).
- [x] **Supabase Integration:** Actualización de esquema SQL y modelos Java para trazabilidad de unidades.

### UI & Layouts Premium
- [x] **Layout V3 Transparency:** Fondo de RDS RT invisible para estilo 100% glass.
- [x] **Layout 1 Polish:** Tamaño de icono de señal reducido a 44dp.
- [x] **V3 Spacing:** Desplazamiento vertical de frecuencia para evitar overlap con iconos de estado.
- [x] **K706 Bands:** Iconos AM1/AM2 corregidos.

### Mejoras de Motor
- [x] **Restauración MT8163:** RDS funcional de nuevo tras regresión.
- [x] **MTK8259 Fixes:** Mapeo PTY corregido y mute icon estático.
- [x] **Estabilidad Visual:** Implementada lógica para limpiar el logo al sintonizar y eliminar el jitter residual en el nombre de la estación.
- [x] **Autosize:** Mejora estructural en los layouts para evitar el corte de nombres largos.

---

## ✅ Sesión 13/Marzo/2026 — Completado (V.20.0 Robustness)

### Robustez Crítica Motor QS6 (NWD)
- [x] **AIDL Recovery Architecture**: Implementación de wrapper `performAidlCall` para captura de `DeadObjectException`.
- [x] **Auto-Rebind Algorithm**: Lógica de reintento (MAX_RETRIES=3) para recuperar el control sin reiniciar la app.
- [x] **Boot Stabilization**: Retardo de 500ms en `onEngineReady` para evitar colisión de comandos con el kernel post-inflado de layout.
- [x] **Life-Cycle Safety**: Persistencia de vínculo AIDL durante cambios de `Activity` por configuración.

### Mejoras UI & UX
- [x] **Instant UI Responsiveness**: Limpieza inmediata de buffer de logos al sintonizar.
- [x] **Layout Clearance**: Corrección de solapamiento de clics entre frecuencia y botones AF/TA/TP.
- [x] **MTK Volume Sync**: Implementado `setVolumeControlStream` and AudioFocus para corregir la "doble pulsación" en unidades Topway.

---

---

## ✅ Sesión 14/Marzo/2026 — Completado (V.4.9.0 Community Logo Edition)

### Comunidad y Lanzamiento V4.9
- [x] **Community Logo Edition**: Lanzamiento oficial de la v4.9.0.
- [x] **Web Gallery Renewal**: Renovación total del sitio de GitHub con galería de capturas v4.9 y actualización en 8 idiomas.
- [x] **Sincronización de Activos**: Los últimos pantallazos (V2 Night / V3 Premium) ya están integrados en la web.

### UI & Layout Polishing
- [x] **Fav Icon Repositioning**: Movido el icono de favorito a la derecha de la nube en Layout 2 y a la izquierda en Layout 3.
- [x] **Cloud Restoration**: Tamaño de la nube a 120dp con funcionamiento dinámico.
- [x] **Premium Aesthetics**: Unificación de tamaños y márgenes para una interfaz más limpia.

### Fixes de Audio y Hardware
- [x] **MTK8259 Audio Fix**: Implementado `mTsCommon.EnterMode(1/0)` para forzar el canal de audio FM en unidades Topway al iniciar/cerrar.
- [x] **QS6 Shadow Motor**: Motor híbrido que escucha broadcasts crudos para redundancia total.
- [x] **NWD Stability**: Gestión de ciclo de vida mejorada para los receptores de hardware.

---

## 🔲 Próximos Pasos (V.5.0+)

### Prioridad 1: V5.0 Fase 2 (Estructural)
- [ ] **RadioUIManager**: Extraer toda la lógica de búsqueda de vistas y setters de `MainActivity`.
- [ ] **RadioStatusPoller**: Mover el `Timer` de refresco a un manager independiente con soporte para diferentes intervalos por motor.
- [ ] **MediaSession Expansion**: Integrar carátulas de emisora en la notificación dinámicamente según el logo de Supabase.

### Prioridad 2: Funcionalidades Premium
- [ ] **Visualización Espectro:** Implementar visualizador de audio integrado en la UI principal.
- [ ] **Android Auto:** Soporte para control de medios desde la unidad principal externa.

### Prioridad 3: Limpieza Continua
- [ ] Implementar sistema de temas dinámicos basado en JSON.
- [ ] Migración parcial a Kotlin para nuevos módulos.
