# Manual de Usuario - OpenRadioFM v.5.0.0-Beta "Stability Edition"
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

## 3. Logos y Streaming Online [Novedad v4.7 Beta]

### 3.1 Sistema de Logos (Prioridad Local)
La app busca los logos en este orden de prioridad:
1. **Carpeta Local**: Busca en `/sdcard/RadioLogos/` archivos con nombre `Frecuencia_Nombre.png`.
2. **Servidor Online**: Si no existe localmente, descarga desde **Supabase** (Comunidad) o servidores web.
- **Reset de Caché:** Mantén pulsado el icono de la **Nubecita Cloud** para borrar el logo actual y forzar una nueva descarga.
- **Manuales**: Puedes añadir tus propios logos simplemente copiándolos a la carpeta `RadioLogos` con el nombre de la frecuencia (ej: `96900.png`).

### 3.2 Streaming Online (Beta)
- **Funcionalidad:** Permite escuchar la emisora vía internet si la señal FM es débil.
- **Estado:** Esta función está en fase de **pruebas**. El catálogo de streaming está centrado actualmente en emisoras de **España**.
- **Hardware:** Se ha optimizado el motor MT8163 para evitar bloqueos al conmutar entre FM y Streaming.

### 3.3 Contribución a la Comunidad (Crowdsourcing)
- **Cómo ayudar:** Activa la opción *"Contribuir a la Comunidad"* en Ajustes Premium. 
- **Funcionamiento:** Al sintonizar una emisora con RDS estable, la app enviará de forma anónima la frecuencia y el código PI al servidor para que otros usuarios se beneficien de los logos HD en tu zona.

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
