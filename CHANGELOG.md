# Changelog - OpenRadioFM
    
## v4.3.0 "Hardware & Gestures" (Febrero 2026)

Actualización técnica centrada en la compatibilidad universal de hardware y la optimización de la experiencia táctil.

### ⚙️ Hardware & Compatibilidad
*   **Compatibilidad Universal:** Nuevo sistema de detección de servicios que soporta chips HCN, MTK (Mediatek), TS (TopWay) y Android Estándar.
*   **Selector de Motor de Radio:** Nueva opción en el menú Premium para forzar el motor de radio manualmente si el autodetector falla.
*   **Gestión de Banda AM:** Opción para habilitar/deshabilitar la banda AM. Si se desactiva, la app salta automáticamente las bandas AM al ciclar con el botón BAND.
*   **Frecuencias AM:** Soporte completo para visualización en kHz y pasos de 9kHz (Región Europa).

### 👆 Interacción & Gestos
*   **Sintonización Fluida (Drag):** Arrastre lateral en el cuadro de frecuencia para un ajuste manual preciso.
*   **Nueva Lógica de Botones:** 
    *   Click corto: Paso manual (0.05MHz / 9kHz).
    *   Pulsación larga: Búsqueda automática (Seek).
*   **Sensibilidad Mejorada:** Ajuste de los umbrales de swipe para una respuesta más natural en pantallas resistivas y capacitivas de coche.

### 🎨 Visual & UI (V3)
*   **Etiquetas Gráficas:** Sustitución de texto MHz/kHz por iconos de alta resolución.
*   **Alineación Premium:** Corregido el padding en los botones de "Ajustes Android" y "Guardar/Cargar" para una simetría total en la barra de control.
*   **Galería de Favoritos:** Ahora soporta hasta 15 presets con iconos premium numerados (`radio_icon_p01` a `p15`).

### 🛠️ Mejoras Técnicas
*   **Depuración de Código:** Eliminación de variables duplicadas y resolución de errores de compilación en lambdas.
*   **Build System:** Migración a Java 21 para el proceso de compilación.
*   **Git Hygiene:** Exclusión de la carpeta de recursos del repositorio para agilizar la sincronización.


Actualización visual y de estabilidad centrada en el refinamiento del diseño y la experiencia de usuario.

### 🎨 Visual & UI
*   **Nuevo Icono V4:** Renovado logo "Orange Waveform" aplicado globalmente (Launcher, Menús, About).
*   **Layout Vertical (V2) Perfeccionado:** Alineación matemática de botones y simetría total entre columna central y derecha.
*   **Glassmorphism Oscuro:** Nuevos fondos semistransparentes (70% dim) para todos los diálogos (Save/Load, About, Settings).

### 🌍 Funcionalidad
*   **Selector de Idioma Manual:** Corregido comportamiento. Ahora permite cambiar idioma independientemente del sistema y reinicia la app automáticamente.
*   **Correcciones Menores:** Ajustes de padding en botones y textos.


## v4.0 "Global Edition" (Febrero 2026)

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
