# Investigación: GalaOnlineMusic APK

Este documento resume las recomendaciones estratégicas derivadas del análisis de `GalaOnlineMusic.apk` para su aplicación en el proyecto `OpenRadioFM`.

## 1. Conclusiones Globales
El análisis ha revelado que el APK original es una excelente referencia no solo por sus datos, sino por su simplicidad técnica. La arquitectura de su API permite una integración rápida, aunque requiere precauciones de estabilidad.

## 2. Recursos Aprovechables para OpenRadioFM

### A. Catálogo de Emisoras y Países
*   **Alcance**: 98 países con miles de emisoras activas.
*   **Metadatos**: Incluye nombres, géneros (`title`), eslóganes (`artist`) y frecuencias.
*   **Recomendación**: Usado para generar `stations_es.json`.

### B. Sistema de Logos (Imagen Corporativa)
*   **Activos**: Enlaces directos a logos en alta calidad alojados en el servidor.
*   **Implementación**: Carga dinámica usando Glide apuntando a `http://116.203.217.114/radio/uploads/...`.

### C. Lógica de "Ahora Suena" (Metadatos)
*   **Descubrimiento**: El servidor NO entrega la canción actual por API. Se extrae del stream de audio (ICY Metadata).

## 3. Ideas de Mejora para la Interfaz (UX)
*   **Ciclo de Temas**: Sistema de cambio de color de acento disparado por toque en el logo.
*   **Limpieza de RDS**: Filtro para eliminar etiquetas técnicas y ruido en los metadatos.

---
*Documento generado durante la fase de integración v4.5.*
