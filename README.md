# OpenRadioFM 📻 - The Car Experience

**OpenRadioFM** es una aplicación de radio premium diseñada específicamente para unidades Android automotrices (Head Units), con un enfoque en hardware basado en **MT8163** (HCN/AutoRadio). Ofrece una interfaz extremadamente pulida con efectos de Glassmorphism, personalización avanzada y una experiencia optimizada para la conducción.

<div align="center">
  <img src="docs/img/app_icon.png" width="200" alt="OpenRadioFM Logo">
  <br>
  <img src="docs/img/screenshot1.png" width="45%" alt="OpenRadioFM Interface 1">
  <img src="docs/img/screenshot2.png" width="45%" alt="OpenRadioFM Interface 2">
</div>

## ✨ Novedades en v3.0
- **Layout Panorama (V3):** Nuevo diseño horizontal nativo para pantallas de 1024x600 con iconos maximizados.
- **Personalización Premium:** Centro de ajustes estéticos (pulsación larga en EQ) con 10 colores de acento.
- **Gestor de Tipografías:** 5 fuentes integradas, incluyendo la nueva *Orbitron* y *Digital*.
- **Fondo Dinámico Universal:** Efecto de desenfoque (blur) basado en el logo de la emisora sintonizada.
- **Integración Vehicular:** Botón de acceso rápido a GPS y soporte para logo de marca de coche personalizado.

## 🛠️ Funciones Principales
- **Binding de Hardware:** Integración directa con el servicio `com.hcn.autoradio`.
- **Hybrid Logos:** Carga inteligente de logos desde API y fallback local.
- **RDS Estable:** Visualización dinámica de texto informativo sin saltos.
- **Control Intuitivo:** Lógica de búsqueda (SEEK) corregida y compatible con mandos al volante.
- **Multilingüe:** Soporte completo en Español, Inglés y Ruso.

## 📖 Documentación
- [Manual de Usuario (Español)](manual_usuario.md)
- [User Manual (English)](manual_user_en.md)
- [Руководство пользователя (Русский)](manual_user_ru.md)

## 📜 Historial de Versiones

### v5.0 "Hardware Precision" (Febrero 2026)
- **MT8163 Consys Optimization:** Forzado de modo estéreo por reflexión para mayor sensibilidad.
- **Indicador de Calidad Compuesto:** Nuevo algoritmo que estima la señal basado en RDS Lock y Stereo.
- **RDS Real-Time:** Paso de sondeo (polling) a eventos en tiempo real para nombres RDS instantáneos.
- **V3 Expanded:** Barra inferior ampliada a 8 botones con accesos a Ajustes Android y Favoritos.
- **Modo Noche Avanzado:** Tintado azul noche para favoritos y frecuencia dinámica.
- **Historial de Emisoras:** Corrección de la persistencia de las últimas 15 emisoras.

### v4.0 "Global Edition" (Febrero 2026)
- **Internacionalización:** Traducción completa a Español, Inglés y Ruso con selector manual.
- **Gestión de Favoritos:** Sistema de Guardar/Cargar listas de emisoras en archivos `.fav`.
- **Modo Nocturno Premium:** Tinting inteligente de iconos y bordes en azul nocturno.
- **Layout V2 Refinado:** Alineación perfecta y nuevos botones de acceso rápido.

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
