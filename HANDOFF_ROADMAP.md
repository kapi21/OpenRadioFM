# Handoff y Roadmap de Desarrollo (V15.5)

Este documento sirve como guía para futuros desarrolladores y establece los próximos pasos para OpenRadioFM tras la refactorización profesional.

## Estado Actual (Handoff)

- **Núcleo**: La radio está completamente desacoplada del hardware mediante la interfaz `RadioEngine`.
- **Detección**: `RadioServiceController` gestiona la lógica de conexión automática y manual.
- **UI**: Soporta 9 idiomas y 3 layouts (V1, V2, V3).
- **RDS**: Implementado mediante un sistema de callbacks asíncronos.

### Puntos de Atención
- El motor **K706** requiere permisos de Root para interactuar con la consola `/dev/ttyMT1`.
- El motor **MT8163** utiliza una combinación de AIDL (`IRadioServiceAPI`) y reflexión para el RDS oculto.
- Se ha implementado un reset centralizado de RDS en `handleFrequencyChange` para evitar datos residuales al cambiar de emisora.

## Roadmap (Próximos Pasos)

### Fase 1: Optimización de RDS y Logos (Prioridad Alta)
- [ ] Implementar caché de logos RDS PS local para evitar búsquedas constantes.
- [ ] Mejorar el algoritmo de parsing de RDS RT para manejar caracteres especiales.
- [ ] Añadir soporte para logos de emisoras en alta resolución (256x256).

### Fase 2: Soporte Global y Hardware (Prioridad Media)
- [ ] Añadir selector de Región (USA, EU, JP, OIRT) para ajustar pasos de frecuencia y de-énfasis.
- [ ] Implementar soporte para Dongles DAB+ USB externos.
- [ ] Soporte para mandos al volante (SWC) mediante el broker de RadioEngine.

### Fase 3: Experiencia de Usuario (Prioridad Baja)
- [ ] **Diseño V3 Gold**: Refinamiento estético del Layout 3 con animaciones fluidas.
- [ ] Modo "Visualizador" con espectro de audio (vía AudioLoopback si es posible).
- [ ] Integración con servicios de carátulas de álbumes para emisoras que transmiten metadatos de canciones.

---
*OpenRadioFM development roadmap - Mar 2026*
