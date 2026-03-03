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

## Roadmap (Próximos Pasos)

### Fase 1: Optimización de RDS y Logos (Prioridad Alta)
- [ ] Implementar caché de logos RDS PS local para evitar búsquedas constantes.
- [ ] Mejorar el algoritmo de parsing de RDS RT para manejar caracteres especiales.
- [ ] Añadir soporte para logos de emisoras en alta resolución (256x256).

### Fase 2: Modularización - "Managers" Finalizados (Prioridad Media)
- [x] **NightModeManager**: Encapsular la lógica de horario y cambio de skin automático.
- [x] **HistoryManager**: Centralizar la persistencia y gestión de emisoras recientes.
- [x] **PresetManager**: Extraer lógica de presets de `MainActivity`.
- [x] **Android Auto Manager**: Soporte para MediaSession y Google Assistant.
- [ ] **ScanManager**: Extraer lógica de escaneo selectivo y adaptadores de `MainActivity`.
- [ ] **AudioManager**: Punto único para Mute, EQ de hardware y foco de audio.
- [ ] **WeatherManager**: Integrar API de clima y geolocalización asíncrona.

### Fase 3: Soporte Global y Hardware (Prioridad Media)
- [ ] Añadir selector de Región (USA, EU, JP, OIRT) para ajustar pasos de frecuencia y de-énfasis.
- [ ] Implementar soporte para Dongles DAB+ USB externos.
- [ ] Soporte para mandos al volante (SWC) mediante el broker de RadioEngine.

### Fase 4: Experiencia de Usuario (Prioridad Baja)
- [ ] **Diseño V3 Gold**: Refinamiento estético del Layout 3 con animaciones fluidas.
- [ ] **Botón Power Off**: Implementar el botón `power_off.png` en los layouts con lógica de cierre seguro de la aplicación.
- [ ] Modo "Visualizador" con espectro de audio (vía AudioLoopback si es posible).

---
*OpenRadioFM development roadmap - Mar 2026*

