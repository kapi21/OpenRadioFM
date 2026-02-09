# Changelog

## [5.0.0-Beta] - In Progress
### Added
- **Engineering Mode Dashboard**: Hidden diagnostic menu (GPS button x5) features:
    - **RF Telemetry**: Signal Quality (SQI), Stereo/Mono, LOC/DX, Estimated RSSI.
    - **RDS Inspector**: PI, PTY, PS, AF List, Sync Status.
    - **Asset Diagnostics**: Real-time checks for `RadioLogos` folder, custom backgrounds, and car logos.
    - **System Info**: Device Model, Board ID, and **Root Access** verification (SU Check).
    - **Interactive Tuner**: Manual stepping controls (`<` / `>`) with real-time signal feedback.
    - **Live Terminal Log**: "Matrix Style" scrolling console showing kernel events (TUNED, RDS_UPDATE, STEREO_LOCK).
    - **Data Management**: Tools to Factory Reset favorites and clear station history.
- **Categorical PTY Icons**: Visual indicators for station categories (Music, News, Sport, Talk, Jazz, Classical, etc.) based on RDS PTY codes.

## [4.0.0] - 2026-02-09
### Added
- **Dynamic Backgrounds (Global Edition)**: Entirely refactored the dynamic blur engine for Layout 3. Background now updates even when central logo is hidden, providing a premium translucent glass effect.
- **Signal Level Quality Tinting**: Implemented dynamic coloring for the signal icon (`ivSignalLevel`) based on reception quality:
    - **Green**: Excellent (Stereo + RDS Lock).
    - **Yellow**: Medium (Stereo or RDS).
    - **Red**: Poor (No Lock).
- **Expanded Layout 3 (Horizontal)**: Control bar expanded to 8 buttons, integrated Android settings, and improved sidebar for panoriamic displays.
- **Save/Load Favorites**: Advanced backup system using `.fav` files in `RadioLogos` folder.

### Fixed
- **Layout 2 Stability**: Prevented layout shifting by ensuring RDS boxes (`tvRdsName`, `tvRdsInfo`) stay visible as transparent placeholders when empty.
- **Night Mode Consistency**: Consolidated all color-tinting logic. Now Frequency, Band, Signal, and RDS consistently use **Night Blue** in Night Mode.
- **Activity State Persistence**: Implemented `onSaveInstanceState` to preserve favorites and signal status across layout switches.
- **Icon Alignment**: Standardized `fitStart` and `fitEnd` across all layouts to eliminate internal drawable padding issues.
- **Language Selector**: Fixed automatic restart when switching between Spanish, English, and Russian.

### Changed
- **Icon Sizing**: Increased padding for secondary control buttons (GPS, Settings) to 28dp for better visual weight in Layout 2.
- **PTY Refinement**: Temporarily simplified PTY display logic to prioritize stability in low-signal environments.

---

## [3.0] - 2026-02-08
### Added
- Initial implementation of Layout 3 (Premium Horizontal).
- Support for Car Brand Logo customization.

Actualización centrada en la internacionalización, gestión de contenidos y refinamiento visual.

### 🌍 Internacionalización
*   **Soporte Multiidioma:** Traducción completa de la interfaz a Español, Inglés y Ruso.
*   **Selector Manual:** Nuevo selector de idioma en el menú Premium que permite forzar el idioma independientemente del sistema.
*   **Reinicio Dinámico:** La aplicación aplica el nuevo idioma instantáneamente reiniciando la actividad.

### 💾 Gestión de Favoritos
*   **Save/Load System:** Nueva funcionalidad para guardar y cargar tus listas de emisoras favoritas.
*   **Formato .fav (JSON):** Los archivos se guardan en `/sdcard/RadioLogos` y son fáciles de compartir o respaldar.
*   **Contenido Guardado:** Frecuencia, número de preset, nombre personalizado y timestamp.

### 🎨 Mejoras Visuales (V2 & V3)
*   **Layout V2 Refinado:** Corrección total de la alineación en la columna derecha. Nuevos botones de acceso rápido.
*   **Modo Nocturno Premium:** Los iconos de banda (FM) y etiquetas (MHz) ahora reciben un tinte azul nocturno ("Night Blue") en modo noche, junto con los bordes de los botones.
*   **Iconos Optimizados:** Reducción de tamaño (padding 18dp) para una estética más limpia.

### 🛠️ Otras Mejoras
*   **Botones Extra (V2):**
    *   **Settings:** Acceso directo a los ajustes de Android.
    *   **Favoritos:** Acceso directo al diálogo de Guardar/Cargar.
*   **Gestos (Beta):** Marcados como característica experimental en el menú.
*   **Correcciones:** 
    *   Solucionado error de compilación con referencias a variables antiguas.
    *   Corregidos formats strings con múltiples argumentos.

---


## v3.0 "The Car Experience" (Febrero 2026)

Esta es la actualización más ambiciosa hasta la fecha, transformando OpenRadioFM en una experiencia premium diseñada específicamente para el entorno del automóvil.

### 🚀 Novedades Principales
*   **Nuevo Layout Horizontal (V3):** Diseñado para pantallas de 1024x600. Iconos maximizados, visualización de frecuencia optimizada y logos tipo galería.
*   **Logo Marca Coche (V3):** Soporte para logo de marca personalizado en `/sdcard/RadioLogos/car_logo.png`.
*   **Indicadores de Banda Gráficos (V3):** Sustitución del texto FM1/FM2 por iconos dinámicos (`radio_fm1.png`, etc.) y nuevo icono de botón `BAND`.
*   **Menú de Personalización Premium:** Todo un centro de ajustes estéticos al alcance de una pulsación larga en el botón EQ/Settings.
*   **Capa de Fondo Dinámico (Universal):** El fondo de la aplicación (tanto en V2 como en V3) ahora puede cambiar dinámicamente al logo de la emisora sintonizada (con efecto Blur/Desenfoque).
*   **Gestor de Tipografías:** Soporte para 5 fuentes distintas (System, Bebas, Digital, Inter y la nueva **Orbitron**) integradas directamente en la app.
*   **Acceso Rápido GPS:** Nuevo botón dedicado para lanzar tu navegador GPS favorito (Maps, Waze, etc.) directamente desde la radio.

### 🛠️ Mejoras Técnicas & UI
*   **Visibilidad de Diálogos:** Nuevo fondo difuminado (70% dim) en todos los ajustes para mejorar el contraste sobre la interfaz.
*   **Coloreado de Bordes:** Optimizado según el layout (completo en V2, solo memorias en V3).
*   **Legibilidad de Memorias:** Aumento de tamaño de fuente a **19sp** en el layout horizontal.
*   **Separador Decimal:** Forzado a punto (**108.0**) para una visualización uniforme.
*   **Maximización de Controles:** Eliminación de márgenes (padding) en la fila inferior para facilitar la pulsación táctil conduciendo.
*   **Optimización 1024x600:** Reajuste de pesos en el diseño horizontal para evitar recortes en pantallas de baja resolución vertical.
*   **Refresco en Tiempo Real:** Los cambios de fondo y tipografía se aplican instantáneamente sin necesidad de reiniciar.
*   **Internacionalización:** Soporte completo de idiomas para Español, Inglés y Ruso en todos los menús.

### 🐛 Bugs Corregidos
*   **Persistencia de Logos:** Solucionado el problema que hacía desaparecer los logos tras un reinicio mediante integración forzada con **MediaScanner**.
*   **Lógica SEEK:** Corregida la dirección de los botones de búsqueda (Izquierda=Bajar, Derecha=Subir) para ser intuitiva y compatible con mandos al volante.
*   **Persistencia de Fondo:** Corregido el error que impedía aplicar el modo de fondo seleccionado desde el menú.
*   **Pantalla Completa:** Ajuste en el manejo de la barra de estado de Android en modo horizontal.
*   **Menú TEST:** El menú técnico de fábrica ahora está oculto tras 5 clics en el icono de GPS.

---

## v2.0b (Enero 2026)
*   **Expansión de Memorias:** De 6 a 12 presets por banda.
*   **Personalización de Fondos:** Soporte para `background.jpg/png`.
*   **Interfaz Glassmorphism:** Nuevos botones semitransparentes.
