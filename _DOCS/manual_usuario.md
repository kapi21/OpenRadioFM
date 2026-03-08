# Manual de Usuario - OpenRadioFM v.4.8 Cloud_Server

Bienvenido a **OpenRadioFM v.4.8 Cloud_Server**, la evolución definitiva de la radio FM para unidades Android (Head Units). Esta versión introduce capacidades de streaming y gestión de logos en la nube, optimizada para máxima estabilidad y rendimiento.

---

## 1. Interfaz y Navegación

### 1.1 Modos de Pantalla (Layouts)
OpenRadioFM cuenta con dos diseños principales:
- **V2 (Clásico Vertical):** Optimizado para pantallas tipo tablet o verticales.
- **V3 (Premium Horizontal):** Diseño panorámico ideal para el salpicadero, con iconos de gran formato y efecto **"Glass Mode"**.
- **Cómo cambiar:** Mantén pulsado el botón **LOC/DX** para alternar. La app se reiniciará automáticamente.

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

### 3.1 Servidor de Logos (Beta)
La app puede descargar logos automáticamente desde nuestro servidor Supabase.
- **Reset de Caché:** Si un logo es incorrecto o quieres forzar la recarga, mantén pulsado el icono de la **Nubecita Cloud**. Aparecerá el mensaje *"Caché de emisora borrada"* y se reajustará la información.
- **IMPORTANTE:** Actualmente el catálogo de logos está enfocado principalmente a **España**, pero gracias al Crowdsourcing se expande cada día.
- Se puede activar en *Ajustes Premium > Logos en Línea*.

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
- **Eonon/Topway (MT8163):** Optimizado para evitar congelaciones en v4.7.
- **QS6:** Para unidades Nanis/NWD.

---
**AVISO:** Esta es una versión **BETA**. Algunas funciones de servidor y streaming están bajo pruebas constantes.
*Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit - v.4.8 Cloud_Server*
