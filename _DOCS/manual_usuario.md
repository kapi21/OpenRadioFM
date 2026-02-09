# Manual de Usuario - OpenRadioFM v4.0 (Global Edition)

Bienvenido a **OpenRadioFM v4.0**, la evolución definitiva de la radio FM para unidades Android (Head Units). Esta versión "Global Edition" está optimizada para ofrecer la máxima estabilidad, una calidad visual impecable y una integración total con el hardware del vehículo.

---

## 1. Interfaz y Navegación

### 1.1 Modos de Pantalla (Layouts)
OpenRadioFM cuenta con dos diseños principales que puedes intercambiar:
- **V2 (Clásico Vertical):** Optimizado para pantallas tipo tablet o verticales. *Ahora con estabilidad total en los textos RDS.*
- **V3 (Premium Horizontal):** Diseño panorámico ideal para el salpicadero, con iconos de gran formato y el nuevo **"Glass Mode"** de fondo.
- **Cómo cambiar:** Mantén pulsado el botón **LOC/DX** para alternar entre los diseños. La app se reiniciará automáticamente.

### 1.2 Indicador de Calidad de Señal [Novedad v4.0]
El icono de antena (`level_signal.png`) ahora es inteligente y cambia de color según la recepción real:
- 🟢 **Verde:** Señal excelente (Estéreo detectado y RDS sincronizado).
- 🟡 **Amarillo:** Señal media (Solo Estéreo o solo RDS disponible).
- 🔴 **Rojo:** Señal pobre o nula.

---

## 2. Personalización Premium (Menú Secreto)

Mantén pulsado el botón de **Configuración (EQ)** para acceder al centro de personalización:

### 2.1 Colores del Tema
Elige entre 10 esquemas de color que se aplican a los bordes y acentos. En **Modo Noche**, la app forzará el color "Night Blue" para reducir la fatiga visual.

### 2.2 Modo de Fondo (Glass Mode)
1. **Negro Puro:** Máximo contraste.
2. **Imagen background.png:** Carga tu imagen desde `/sdcard/RadioLogos/background.png`.
3. **Logo Dinámico (Glass Mode):** El fondo se genera automáticamente a partir del logo de la emisora, creando un efecto de cristal esmerilado muy elegante. En V3, este efecto funciona incluso si decides ocultar el logo central.

---

## 3. Gestión de Logos y Nombres

### 3.1 Logos de Emisoras (Hybrid Logic)
La app busca logos de tres formas:
1. **Local:** En `/sdcard/RadioLogos/frecuencia.png` (ej. `94.1.png`).
2. **API Online:** Si está activado en ajustes, descarga logos automáticamente.
3. **Prioridad:** El logo local siempre tiene prioridad sobre el online.

### 3.2 Logo de Marca de Coche
En Layout V3, puedes mostrar tu marca en el lateral derecho:
- Ubicación: `/sdcard/RadioLogos/car_logo.png`

---

## 4. Gestión de Favoritos (Save/Load)

### 4.1 Guardar y Cargar
Usa el botón del **Disquete (💾)** para abrir el gestor de archivos `.fav`. Esto te permite mover tus emisoras favoritas entre diferentes radios o hacer copias de seguridad antes de resetear el equipo.

---

## 5. Configuración de Hardware

### 5.1 Motor de Radio (Radio Engine)
Si tu radio no sintoniza correctamente, ve a **Ajustes de Hardware** y selecciona tu motor:
- **HCN:** Para la mayoría de radios Eonon, Xtrons y similares.
- **MTK:** Para placas Mediatek estándar.
- **TS/SYU:** Para unidades TopWay o Joying.

---

## 6. Solución de Problemas

- **Los botones se mueven de sitio:** Esto ha sido corregido en la v4.0. Las cajas de RDS ahora son fijas para evitar saltos visuales.
- **El fondo dinámico no carga:** Asegúrate de tener activada la opción en el menú Premium y que la emisora tenga un logo (local o remoto).
- **No se ve el color azul en modo noche:** Verifica que el "Modo Noche Automático" esté activado en las opciones de la app.

---
*Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit - v4.0 Final*
