# Manual de Usuario - OpenRadioFM v5.0.0 (Stability Beta)
Bienvenido a **OpenRadioFM v.5.0.0**, la edición centrada en la estabilidad extrema del hardware y refinamiento visual. Esta versión introduce el nuevo **Modo Claro (White Skin)** y protecciones críticas para motores MediaTek.

---

## 1. Interfaz y Navegación

### 1.1 Modos de Pantalla (Layouts)
OpenRadioFM cuenta con dos diseños principales:
- **V2 (Clásico Vertical):** Optimizado para pantallas tipo tablet o verticales.
- **V3 (Premium Horizontal):** Diseño panorámico ideal para el salpicadero, con iconos de gran formato y efecto **"Glass Mode"**.
- **Nuevo: White Skin (Modo Claro):** Tema de alta visibilidad para conducción diurna.
- **Cómo cambiar Layout:** Mantén pulsado el botón **LOC/DX** para alternar entre V2 y V3. 
- **Cómo cambiar Tema:** Mantén pulsado el botón de **Configuración (EQ)** y selecciona "Seleccionar Tema".

### 1.2 Navegación de Favoritos (Hardware)
- **Favoritos:** Los botones centrales permiten saltar entre tus emisoras memorizadas. Compatible con mandos de volante (K706/MT8163).
- **Búsqueda (Seek):** Los botones exteriores realizan la búsqueda automática de señal.

---

## 2. Personalización Premium (Menú Secreto)

Mantén pulsado el botón de **Configuración (EQ)** para acceder:

### 2.1 Colores del Tema y Modo Noche
Elige entre 10 esquemas de color. En **Modo Noche**, se aplicará el color **"Night Blue"** para mejorar la visibilidad nocturna y reducir la fatiga visual.

### 2.2 Modo de Fondo (Glass Mode)
1. **Negro Puro:** Máximo contraste.
2. **Imagen background.png:** Carga una imagen personalizada desde `/sdcard/RadioLogos/background.png`.
3. **Logo Dinámico (Glass Mode):** El fondo se genera automáticamente a partir del logo de la emisora.

---

## 3. Logos Locales y Modo Offline [OpenRadioFM 5.5 OFFLINE]

### 3.1 Explorador Visual de Logos
- **Acceso Directo:** Toca la frecuencia central (dial) y pulsa en el botón **LOGO** para abrir el explorador integrado.
- **Atajos de Navegación:**
  - `📁 RadioLogos`: Acceso directo a tu carpeta de logos (`/sdcard/RadioLogos/`).
  - `📥 Descargas`: Navega directamente a la carpeta de descargas (`/sdcard/Download/`).
  - `💾 Memoria`: Almacenamiento interno del sistema.
  - `🔌 USB`: Detección instantánea de unidades y pendrives USB conectados.
  - `⬆ Subir`: Vuelve a la carpeta superior.
- **Formatos Compatibles:** `.png`, `.jpg`, `.jpeg`, `.webp` y `.bmp` con ajuste automático a un máximo de 300x300 px para máxima nitidez y bajo consumo.
- **Explorador del Sistema (Android):** Botón alternativo para abrir el selector de archivos nativo de Android.

### 3.2 Quitar Logo sin Borrar Archivos
- Al pulsar el botón **🗑 Quitar** dentro del explorador, el logo se **desvincula** del preset y de la emisora activa, pero **nunca se borra de tu almacenamiento ni de tu USB**.
- El preset restaurará de inmediato su frecuencia o nombre de emisora sin mostrar guiones `---`.

### 3.3 Modo 100% Offline (Cero dependencias)
- En esta versión se han suprimido las conexiones a servidores en la nube, optimizando el arranque, ahorrando batería y garantizando máxima fluidez sin pausas del sistema ni iconos residuales de red en pantalla.

---

## 4. Gestión de Favoritos

### 4.1 Guardar y Cargar (.fav)
Usa el botón del **Disquete (💾)** para exportar o importar tu lista de favoritos. Esto permite copias de seguridad o mover tu configuración entre dispositivos.

---

## 5. Configuración de Hardware

Si experimentas problemas de audio o sintonización, selecciona tu motor en *Ajustes Hardware*:
- **HCN (K706):** Para unidades Vento/HCN.
- **MediaTek 8259 / 8667:** Nuevo motor con estabilidad mejorada y protección de banda AM.
- **Topway / Eonon (MT8163):** Optimizado para evitar duplicidad de instancias y bloqueos.
- **QS6:** Para unidades Nanis/NWD.
- **AVISO DE SEGURIDAD**: En motores MTK, la banda AM está forzada para evitar bloqueos del hardware. No intentes desactivarla. 

---
**AVISO:** Esta es una versión **BETA de Estabilidad**. Algunas funciones de servidor y streaming están bajo pruebas constantes.
*Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit - v.5.0.0-Beta "Stability Edition"*
