# Handoff y Roadmap de Desarrollo (V16.0)

Este documento sirve como guía para futuros desarrolladores y establece los próximos pasos para OpenRadioFM tras la refactorización profesional.

## Estado Actual (Handoff)

- **Núcleo**: La radio está completamente desacoplada del hardware mediante la interfaz `RadioEngine`.
- **Detección**: `RadioServiceController` gestiona la lógica de conexión automática y manual.
- **Android Auto**: Implementado mediante `MediaSession` y `RadioMediaService`.
- **UI**: Soporta 9 idiomas y 3 layouts (V1, V2, V3).
- **RDS**: Implementado mediante un sistema de callbacks asínconos.
- **Modo Nocturno**: `NightModeManager` gestiona detección horaria/sistema y colores azul noche.
- **Historial**: `HistoryManager` centraliza emisoras recientes y export/import de favoritos.

### Puntos de Atención
- El motor **K706** requiere permisos de Root para interactuar con la consola `/dev/ttyMT1`.
- El motor **MT8163** utiliza una combinación de AIDL (`IRadioServiceAPI`) y reflexión para el RDS oculto.
- **MediaSession**: El sistema de medios de Android Auto se desconecta explícitamente en `onDestroy` para liberar recursos.
- **Investigación K706 (Marzo 2026)**: El motor está instrumentado con prefijos `🔬 [RESEARCH]` en el Logcat para cazar PI Codes y la fuerza de señal real (RSSI).

## Hallazgos y Observaciones (Auditoría Marzo 2026)

### ⚠️ Infraestructura y Deuda Técnica
- **Paquete `domain/` vacío**: El directorio existe pero no contiene lógica. Debe eliminarse o usarse para las interfaces del motor.
- **Inconsistencia de Documentación**: `REFAC_README.md` menciona motores teóricos (`MtkEngine`, `StandardEngine`) que no existen. Los motores reales son **K706**, **MT8163** y **QS6**.
- **Callback AIDL Legacy**: En `MainActivity` (L394) persiste un `IRadioCallBack.Stub` parcial que solo maneja el código 110 (Debug RDS). Es un residuo que debe migrarse al sistema unificado de `RadioEngineCallback`.

### ⚠️ MainActivity y Encapsulamiento
- **Tamaño de Clase**: A pesar de los nuevos managers, `MainActivity` (+2700 líneas) aún retiene lógica crítica que debería ser delegada:
    - Gestión de archivos `.fav` (Import/Export).
    - Lógica pesada de UI en `updateFrequencyDisplay`.
    - Clases internas de escaneo (`ScannedStation`, `StationAdapter`).
- **Exposición de Campos**: Exceso de campos `public` (`mEngine`, `mPresetManager`, etc.). Se requiere una migración a `private` con Getters/Setters o visibilidad de paquete para mejorar el encapsulamiento.

## Roadmap (Próximos Pasos)


### Fase 1: Optimización de RDS y Logos (Prioridad Alta)
- [ ] Implementar caché de logos RDS PS local para evitar búsquedas constantes.
- [ ] Mejorar el algoritmo de parsing de RDS RT para manejar caracteres especiales.
- [ ] Añadir soporte para logos de emisoras en alta resolución (256x256).

### Fase 2: Modularización y Limpieza (Prioridad Media)
- [x] **NightModeManager**: Encapsular la lógica de horario y cambio de skin automático.
- [x] **HistoryManager**: Centralizar la persistencia y gestión de emisoras recientes.
- [x] **PresetManager**: Extraer lógica de presets de `MainActivity`.
- [x] **Android Auto Manager**: Soporte para MediaSession y Google Assistant.
- [x] **Audio Focus Recovery**: Sistema de autocuración para Spotify y llamadas.
- [ ] **Limpieza de Arquitectura**: 
    - [ ] Eliminar paquete `domain/` vacío.
    - [ ] Sincronizar `REFAC_README.md` con motores reales.
    - [ ] Migrar callback AIDL legacy (mCallback) a `RadioEngineCallback`.
- [ ] **Refactorización MainActivity**:
    - [ ] Delegar Import/Export de Favoritos a `HistoryManager`.
    - [ ] Mover lógica de escaneo a `ScanManager`.
    - [ ] Encapsular campos públicos mediante Getters/Setters.
- [ ] **AudioManager**: Punto único para Mute, EQ de hardware y foco de audio.
- [ ] **WeatherManager**: Integrar API de clima y geolocalización asíncrona.


### Fase 3: Soporte Global y Hardware (Prioridad Media)
- [ ] Añadir selector de Región (USA, EU, JP, OIRT) para ajustar pasos de frecuencia y de-énfasis.
- [ ] Implementar soporte para Dongles DAB+ USB externos.
- [ ] Soporte para mandos al volante (SWC) mediante el broker de RadioEngine.

### Fase 4: Experiencia de Usuario (Prioridad Baja)
- [ ] **Diseño V3 Gold**: Refinamiento estético del Layout 3 con animaciones fluidas.
- [x] **Botón Power Off**: Implementado el botón `power_off.png` en los layouts con lógica de cierre seguro de la aplicación.
- [ ] **Hardware Hacking Dashboard**: Analizar integración de herramientas externas (Analizador Lógico, UART Console) para depuración profunda de MCU.
- [ ] Modo "Visualizador" con espectro de audio (vía AudioLoopback si es posible).

### Fase 5: Limpieza Técnica y Refinamiento (V5.7)
- [x] **Arquitectura de Managers**: Implementados `PlaybackManager`, `DeviceManager` y `RDSManager`.
- [x] **RDS PS Dinámico**: Implementado auto-dimensionado de texto en ambos layouts para nombres largos.
- [x] **UX Pulida**: Icono PowerOff con tintado dinámico y eliminación de placeholders RDS residuales.
- [x] **Directorio `domain/` eliminado**: ✅ Limpieza estructural completada.

### Fase 6: Ideas de Expansión y Servidores (Nueva)
- [ ] **Servidor de Logos Centralizado**: Crear una API/Backend para servir logos de emisoras basados en PI Code / Frecuencia / Localización.
    - [ ] Sincronización asíncrona para descargar logos que no estén en la base de datos local.
    - [ ] Contribución comunitaria para mantener la base de datos de logos actualizada.
- [ ] **Modo "Visualizador"**: Espectro de audio dinámico basado en la salida de audio de la head unit.

---
*OpenRadioFM development roadmap - Actualizado Mar 2026*

