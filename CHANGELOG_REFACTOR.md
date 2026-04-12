# Changelog - Refactorización de Arquitectura (Abril 2026)

## [1.0.0-refactor] - 2026-04-12

### Añadido
- **Arquitectura Modular**: Introducción de un sistema de Coordinadores y Mediadores para reducir la complejidad de `MainActivity`.
- **UiViewMediator**: Nueva clase que centraliza todas las referencias a vistas (`findViewById`), permitiendo un acceso limpio y seguro desde cualquier componente.
- **HardwareKeyCoordinator**: Gestor dedicado para eventos de teclas físicas y mandos al volante (Steering Wheel Controls).
- **LifecycleCoordinator**: Delegación de los métodos del ciclo de vida de Android (`onResume`, `onPause`, `onDestroy`, etc.) para una gestión más limpia de recursos.
- **EngineCallbackCoordinator**: Aislamiento de los callbacks del motor de radio, desacoplando la lógica de hardware de la interfaz.
- **SkinCoordinator**: Centralización de la lógica de aplicación de temas y skins visuales.
- **StatusRefreshCoordinator**: Orquestador de las tareas de refresco de estado (hardware, UI, RDS).
- **FrequencyStateManager**: Gestión refinada del estado de las frecuencias para evitar parpadeos visuales durante el zapping.

### Cambios
- **MainActivity**: Reducción masiva del tamaño del archivo. La actividad ahora actúa exclusivamente como orquestador de alto nivel.
- **Orden de Arranque**: Reestructuración del método `onCreate` para garantizar que todos los componentes se inicialicen antes de su primer uso, eliminando condiciones de carrera.
- **ScanManager**: Actualizado para comunicarse con el nuevo `EngineCallbackCoordinator`.

### Corregido
- **NullPointerException (NPE)**: Corregidos múltiples crashes durante el arranque relacionados con el manejo de la barra de estado en unidades OEM (Android 11+).
- **Estabilidad de UI**: Blindaje en la vinculación de vistas para prevenir cierres inesperados durante transiciones rápidas de layout.
- **Codificación**: Limpieza de caracteres corruptos en comentarios y cadenas de texto.
