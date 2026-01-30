# Changelog - OpenRadioFM

## v2.0b (Enero 2026)

### ✨ Novedades Principales
*   **Expansión de Memorias:** De 6 a **12 presets por banda**. Nuevo diseño con desplazamiento vertical (Scroll).
*   **Personalización de Fondos:** Soporte para cargar imagenes propias (`background.jpg` o `.png`) desde la carpeta `/sdcard/RadioLogos/`.
*   **Gestión de Skins Mejorada:** El selector de temas se ha movido a una **pulsación larga** del botón `Settings (EQ)`.
*   **Interfaz Glassmorphism:** Nuevos botones con fondo semitransparente para mejor integración con fondos personalizados.

### 🛠️ Mejoras Técnicas
*   **Gestión de Logos:**
    *   Corrección de persistencia: Los logos ya no desaparecen al cambiar levemente de frecuencia (±0.05 MHz).
    *   Caché inteligente por banda (FM1/FM2/FM3).
*   **Sistema de Archivos:** Creación automática de la carpeta `RadioLogos` si no existe.
*   **Estabilidad:** Múltiples correcciones de bugs menores y limpieza de código.
*   **Compatibilidad:** Actualizado `versionCode` para garantizar la detección de actualizaciones.

### 🐛 Bugs Corregidos
*   Logo no reaparecía al volver a una frecuencia previamente sintonizada.
*   Miniaturas de presets no se actualizaban al cambiar el logo principal.
*   Botón TEST restaurado a su funcionalidad original (Menú oculto con 5 toques).
