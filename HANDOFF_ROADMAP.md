# Handoff y Roadmap de Desarrollo (V16.3)

Este documento sirve como guía para futuros desarrolladores y establece los próximos pasos para OpenRadioFM tras la refactorización profesional.

## Estado Actual (Handoff)

- **Núcleo**: La radio está completamente desacoplada del hardware mediante la interfaz `RadioEngine`.
- **Detección**: `RadioServiceController` gestiona la lógica de conexión automática y manual.
- **Android Auto**: Implementado mediante `MediaSession` y `RadioMediaService`.
- **UI**: Soporta 9 idiomas y 3 layouts (V1, V2, V3).
- **RDS**: Implementado mediante un sistema de callbacks asínconos.
- **Modo Nocturno**: `NightModeManager` gestiona detección horaria/sistema y colores azul noche.
- **Historial**: `HistoryManager` centraliza emisoras recientes y export/import de favoritos.
- **Logos Online**: Sistema de búsqueda centralizado (Supabase) con caché negativa y contribución comunitaria.

### Puntos de Atención
- El motor **K706** requiere permisos de Root para interactuar con la consola `/dev/ttyMT1`.
- El motor **MT8163** utiliza una combinación de AIDL (`IRadioServiceAPI`) y reflexión para el RDS oculto.
- **MediaSession**: El sistema de medios de Android Auto se desconecta explícitamente en `onDestroy` para liberar recursos.
- **Investigación K706 (Marzo 2026)**: El motor está instrumentado con prefijos `🔬 [RESEARCH]` en el Logcat para cazar PI Codes y la fuerza de señal real (RSSI).
- **Sistema de Logos Online (V16.3)**: El toggle `pref_logos_online` se almacena en `RadioPresets`. Cuando está desactivado, no se realizan peticiones de red ni se sube nada al servidor.

## Cambios Sesión 4 Marzo 2026

### Implementados
- ✅ **Caché Negativa de Logos**: `RadioRepository` ahora guarda `NO_LOGO` en caché cuando una búsqueda online falla, evitando reintentos infinitos.
- ✅ **Sincronización de Hilos**: `mActiveDataOps` se actualiza dentro de `runOnUiThread` para evitar condiciones de carrera.
- ✅ **Menú DEV Logos Online**: Añadido switch `SwitchCompat` en los diálogos de ingeniería de ambos motores (MT8163 / K706).
- ✅ **Recurso `cloud.png`**: Añadido a `res/drawable` para futura UI de streaming.
- ✅ **Radio-Browser Estabilizado**: Endpoint cambiado de `de1` a `at1.api.radio-browser.info`.
- ✅ **Bug Fix SharedPreferences**: `downloadAndSaveLogo()` leía `pref_logos_online` del archivo `RadioStationNames` en vez de `RadioPresets`.
- ✅ **Eliminación de Redundancia**: Quitada doble llamada a `updateMetrics()` en `EngineeringModeDialog`.
- ✅ **Fix XML**: Corregido `</LinearLayout>` faltante en `dialog_engineering_mode.xml` y añadido `xmlns:app` en `dialog_k706_engineering.xml`.

## Hallazgos y Observaciones (Auditoría Marzo 2026)

### ⚠️ Infraestructura y Deuda Técnica
- **Inconsistencia de Documentación**: `REFAC_README.md` menciona motores teóricos (`MtkEngine`, `StandardEngine`) que no existen. Los motores reales son **K706**, **MT8163** y **QS6**.
- **Callback AIDL Legacy**: En `MainActivity` persiste un `IRadioCallBack.Stub` parcial que solo maneja el código 110 (Debug RDS). Es un residuo que debe migrarse al sistema unificado de `RadioEngineCallback`.

### ⚠️ MainActivity y Encapsulamiento
- **Tamaño de Clase**: A pesar de los nuevos managers, `MainActivity` (+2700 líneas) aún retiene lógica crítica que debería ser delegada:
    - Gestión de archivos `.fav` (Import/Export).
    - Lógica pesada de UI en `updateFrequencyDisplay`.
    - Clases internas de escaneo (`ScannedStation`, `StationAdapter`).
- **Exposición de Campos**: Exceso de campos `public` (`mEngine`, `mPresetManager`, etc.).

## Roadmap (Próximos Pasos)

### Fase 1: Optimización de RDS y Logos (Prioridad Alta)
- [x] Implementar caché de logos con caché negativa para evitar búsquedas constantes.
- [x] Sistema de logos online centralizado (Supabase) con PI Code / Freq / Country.
- [ ] Mejorar el algoritmo de parsing de RDS RT para manejar caracteres especiales.
- [ ] Añadir soporte para logos de emisoras en alta resolución (256x256).

### Fase 2: Modularización y Limpieza (Prioridad Media)
- [x] **NightModeManager**: Encapsular la lógica de horario y cambio de skin automático.
- [x] **HistoryManager**: Centralizar la persistencia y gestión de emisoras recientes.
- [x] **PresetManager**: Extraer lógica de presets de `MainActivity`.
- [x] **Android Auto Manager**: Soporte para MediaSession y Google Assistant.
- [x] **Audio Focus Recovery**: Sistema de autocuración para Spotify y llamadas.
- [ ] **Limpieza de Arquitectura**: 
    - [ ] Sincronizar `REFAC_README.md` con motores reales.
    - [ ] Migrar callback AIDL legacy (mCallback) a `RadioEngineCallback`.
- [ ] **Refactorización MainActivity**:
    - [ ] Delegar Import/Export de Favoritos a `HistoryManager`.
    - [ ] Mover lógica de escaneo a `ScanManager`.
    - [ ] Encapsular campos públicos mediante Getters/Setters.

### Fase 3: Soporte Global y Hardware (Prioridad Media)
- [ ] Añadir selector de Región (USA, EU, JP, OIRT) para ajustar pasos de frecuencia y de-énfasis.
- [ ] Implementar soporte para Dongles DAB+ USB externos.
- [ ] Soporte para mandos al volante (SWC) mediante el broker de RadioEngine.

### Fase 4: Experiencia de Usuario (Prioridad Baja)
- [ ] **Diseño V3 Gold**: Refinamiento estético del Layout 3 con animaciones fluidas.
- [x] **Botón Power Off**: Implementado en los layouts con lógica de cierre seguro.
- [ ] **Hardware Hacking Dashboard**: Integrar herramientas externas para depuración de MCU.
- [ ] Modo "Visualizador" con espectro de audio.

### Fase 5: Limpieza Técnica y Refinamiento
- [x] **Arquitectura de Managers**: Implementados `PlaybackManager`, `DeviceManager` y `RDSManager`.
- [x] **RDS PS Dinámico**: Auto-dimensionado de texto en ambos layouts.
- [x] **UX Pulida**: Icono PowerOff con tintado dinámico y eliminación de placeholders RDS.
- [x] **Directorio `domain/` eliminado**: Limpieza estructural completada.

### Fase 6: Ideas de Expansión y Servidores
- [x] **Servidor de Logos Centralizado (Supabase)**: API REST con búsqueda por PI Code / Frecuencia / RDS Name / País.
    - [x] Sincronización asíncrona para descargar logos no disponibles localmente.
    - [x] Contribución comunitaria automática al servidor central.
    - [x] Toggle en menú DEV para activar/desactivar el sistema.
- [ ] **Radio Híbrida (FM + Streaming)**: Escuchar emisoras por Internet cuando la señal FM sea débil.
    - [ ] Vincular URLs de streaming a la base de datos de PI Codes/Nombres RDS.
    - [ ] Conmutación inteligente (Seamless switching) entre FM y Stream según calidad de señal.
    - [ ] Recurso `cloud.png` ya añadido para futura UI de streaming.

---
*OpenRadioFM development roadmap - Actualizado 4 Marzo 2026*
