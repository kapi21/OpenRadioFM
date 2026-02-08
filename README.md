# OpenRadioFM 📻 - The Car Experience

**OpenRadioFM** es una aplicación de radio premium diseñada específicamente para unidades Android automotrices (Head Units), con un enfoque en hardware basado en **MT8163** (HCN/AutoRadio). Ofrece una interfaz extremadamente pulida con efectos de Glassmorphism, personalización avanzada y una experiencia optimizada para la conducción.

<div align="center">
  <img src="docs/img/app_icon.png" width="200" alt="OpenRadioFM Logo">
  <br>
  <img src="docs/img/screenshot1.png" width="45%" alt="OpenRadioFM Interface 1">
  <img src="docs/img/screenshot2.png" width="45%" alt="OpenRadioFM Interface 2">
</div>

## ✨ Novedades en v4.3
- **Compatibilidad de Hardware:** Soporte para motores de radio MTK, TS, HCN y Standard (Universal).
- **Gestión de Banda AM:** Soporte completo para kHz y pasos de 9kHz con opción de desactivar la banda.
- **Sintonización Fluida:** Nuevo gesto de arrastre (Drag) en frecuencia para ajuste manual preciso.
- **Alineación Premium (V3):** Padding y simetría total en la barra de controles inferior.

## 🛠️ Funciones Principales
- **Binding de Hardware:** Integración directa con el servicio de radio del sistema.
- **Hybrid Logos:** Carga inteligente de logos desde API y fallback local.
- **RDS Estable:** Visualización dinámica de texto informativo sin saltos.
- **Control Multifuncional:** Toque corto para pasos manuales, pulsación larga para búsqueda automática.
- **Multilingüe:** Soporte completo en Español, Inglés y Ruso.

## 📖 Documentación
- [Manual de Usuario (Español)](_DOCS/manual_usuario.md)
- [User Manual (English)](_DOCS/manual_user_en.md)
- [Руководство пользователя (Русский)](_DOCS/manual_user_ru.md)

## 📜 Historial de Versiones

### v4.3.0 "Hardware & Gestures" (Febrero 2026)
- **Universal Radio Engine:** Selector manual para forzar el motor de radio (HCN, MTK, TS, Standard).
- **Soporte AM:** Visualización en kHz y lógica de pasos de 9kHz para emisoras de onda media.
- **Smooth Tuning:** Implementación de gestos de deslizamiento lateral para sintonización manual.
- **Premium Icons:** Etiquetas MHz/kHz gráficas y presets expandidos a 15 con iconos específicos.

### v4.0 "Global Edition" (Febrero 2026)
- **MT8163 Consys Optimization:** Forzado de modo estéreo por reflexión para mayor sensibilidad.
- **Indicador de Calidad Compuesto:** Nuevo algoritmo que estima la señal basado en RDS Lock y Stereo.
- **RDS Real-Time:** Paso de sondeo (polling) a eventos en tiempo real para nombres RDS instantáneos.
- **Internacionalización:** Traducción completa a Español, Inglés y Ruso con selector manual.
- **Gestión de Favoritos:** Sistema de Guardar/Cargar listas de emisoras en archivos `.fav`.
- **Modo Nocturno Avanzado:** Tintado azul noche para favoritos y frecuencia dinámica.
- **V3 Expanded:** Barra inferior ampliada a 8 botones con accesos a Ajustes Android y Favoritos.
- **Layout V2 Refinado:** Alineación perfecta, scroll oculto y mejores márgenes.
- **Historial de Emisoras:** Corrección de la persistencia de las últimas 15 emisoras.

### v3.0 "The Car Experience" (Febrero 2026)
- Salto a versión estable con rediseño completo de la interfaz horizontal.
- Implementación de menús premium y personalización en tiempo real.
- Corrección de bugs de persistencia de logos y lógica de búsqueda.

### v2.0b (Enero 2026)
- Introducción de la interfaz Glassmorphism.
- Soporte para fondos personalizados y aumento a 12 presets.

### v1.0b (Diciembre 2025)
- Versión inicial con soporte básico para sintonización y logos.

---
**Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit.**
