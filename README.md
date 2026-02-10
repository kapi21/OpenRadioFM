# OpenRadioFM 📻 v4.5.1 Server Test - The Car Experience

**OpenRadioFM** es una aplicación de radio premium diseñada específicamente para unidades Android automotrices (Head Units), con un enfoque en hardware basado en **MT8163** (HCN/AutoRadio). Ofrece una interfaz extremadamente pulida con efectos de Glassmorphism, personalización avanzada y una experiencia optimizada para la conducción.

<div align="center">
  <img src="docs/img/app_icon.png" width="200" alt="OpenRadioFM Logo">
  <br>
  <img src="docs/img/screenshot1.png" width="45%" alt="OpenRadioFM Interface 1">
  <img src="docs/img/screenshot2.png" width="45%" alt="OpenRadioFM Interface 2">
</div>

## ✨ Novedades en v4.5.1 "Server Test"
- **Indicador de Señal Dinámico:** El icono de cobertura ahora cambia de color (Verde/Amarillo/Rojo) según la calidad real de la sintonización (Estéreo + RDS).
- **Layout 3 (Horizontal) Refinado:** Fondo dinámico "Glass Mode" que se adapta al logo de la emisora incluso con el logo central oculto.
- **Estabilidad Total:** Eliminados los saltos de interfaz en Layout 2 mediante el uso de placeholders fijos para el texto RDS.
- **Persistencia Inteligente:** La app ahora recuerda tus favoritos, el tema seleccionado y el estado de la señal tras cualquier cambio de vista.
- **Modo Noche Configurable:** Programación horaria automática para el cambio de tema, ajustable desde la configuración premium.
- **PTY Multilingüe:** Indicadores de tipo de programa (Noticias, Pop, Rock...) totalmente localizados en ES/EN/RU.
- **Gestión de Bandas:** Favoritos independientes para FM1, FM2 y FM3.

## 🛠️ Funciones Principales
- **Binding de Hardware:** Integración directa con el servicio de radio del sistema (HCN, MTK, SYU, TS).
- **Hybrid Logos:** Carga inteligente de logos desde API y fallback local en `/sdcard/RadioLogos`.
- **RDS Estable:** Visualización dinámica de texto informativo sin saltos ni parpadeos.
- **Control Multifuncional:** Toque corto para pasos manuales, pulsación larga para búsqueda automática.
- **Multilingüe:** Soporte completo en Español, Inglés y Ruso.

## 📖 Documentación
- [Manual de Usuario (Español)](_DOCS/manual_usuario.md)
- [User Manual (English)](_DOCS/manual_user_en.md)
- [Руководство пользователя (Русский)](_DOCS/manual_user_ru.md)

## 📜 Historial de Versiones

### v4.5.1 "Server Test" (Febrero 2026)
- **Localización Completa:** PTY traducido a 3 idiomas y textos ajustados.
- **UI Refinada:** Iconos de MHz y PTY aumentados para legibilidad extrema.
- **Night Schedule:** Horario de modo noche personalizable en ajustes.
- **Band Isolation:** Corrección crítica para favoritos independientes por banda.

### v4.0.0 "Global Edition" (Febrero 2026)
- **Signal Quality Tinting:** Nuevo algoritmo visual para el estado de recepción.
- **Layout 2 Stability:** Fijación de elementos RDS para evitar desplazamientos visuales.
- **Layout 3 Premium Blur:** Fondos dinámicos independientes de la visibilidad del logo central.
- **Night Mode persistence:** Tintado azul noche garantizado en todos los diales y etiquetas.
- **Multi-Hardware Engine:** Selector manual de motor de radio (HCN, MTK, TS, SYU).
- **Activity State Fixes:** Persistencia total de favoritos y configuración visual.

### v3.0 "The Car Experience" (Febrero 2026)
- Salto a versión estable con rediseño completo de la interfaz horizontal (V3).
- Implementación de menús premium y personalización en tiempo real.
- Soporte para Car Brand Logo personalizado.

### v2.0b (Enero 2026)
- Introducción de la interfaz Glassmorphism y fondos personalizados.

### v1.0b (Diciembre 2025)
- Versión inicial con soporte básico para sintonización y logos.

---
**Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit.**
