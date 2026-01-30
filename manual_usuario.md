# Manual de Usuario - OpenRadioFM v2.0b

Bienvenido a OpenRadioFM, una aplicación de radio FM avanzada diseñada específicamente para unidades principales de Android (Head Units) con soporte para control de hardware y personalización.



---

## 1. Interfaz Principal

La interfaz ha sido diseñada para ser clara, legible y fácil de usar mientras se conduce.

### 1.1 Panel Izquierdo: Memorias (Presets)
Aquí se muestran tus 12 emisoras favoritas guardadas (desliza verticalmente para ver todas).
- **Pulsación Corta:** Sintoniza la emisora guardada.
- **Pulsación Larga:** Guarda la frecuencia actual en esa memoria.
- **Iconos:** Muestra el logo de la emisora si está disponible.

### 1.2 Panel Central: Información
- **Frecuencia:** Muestra la frecuencia actual con precisión de 2 decimales (ej. `100.00`). El tamaño del texto se ajusta automáticamente para ser lo más grande posible.
- **Nombre RDS:** Muestra el nombre de la emisora (ej. "LOS40") recibido por la señal de radio.
- **Texto RDS:** Información adicional (título de canción, noticias) en formato marquesina.
- **Botones de Búsqueda (Flechas):**
  - **Pulsación Corta:** Ajuste fino manual de frecuencia (+/- 0.05 MHz).
  - **Pulsación Larga:** Busca automáticamente la siguiente emisora con buena señal (Seek).
- **Botón BAND:** Cambia entre las bandas (FM1, FM2, FM3, AM1, AM2).
- **Botón SCAN:** Escanea y reproduce brevemente todas las emisoras disponibles.

### 1.3 Panel Derecho: Controles y Logos
- **Logo Principal:** Muestra el logo de la emisora actual en gran tamaño.
- **LOC/DX:** Alterna la sensibilidad de recepción (Local para ciudad, DX para larga distancia).
- **Mute:** Silencia el audio instantáneamente.
- **Botón EQ (Configuración):** Ajustes de audio (abre el ecualizador del sistema).
  - **Pulsación Larga:** Abre el menú de **Selección de Skins** (Temas de color).
- **Botón TEST:** Menú de pruebas interno. fábrica (5 clicks para activar).

---

## 2. Personalización (Skins)

¡Haz que la radio combine con el interior de tu coche!

### Cómo cambiar el color (Skin)
### Cómo cambiar el color (Skin)
1. Mantén pulsado el botón **EQ / Ajustes** (icono de ecualizador).
2. Aparecerá el selector de temas.
3. Elige tu color favorito y la interfaz se actualizará al instante.

**Colores Disponibles:**
- 🟠 **Naranja (Original)**
- 🔘 **Clásico Gris** (Estilo neutro)
- 🔵 **Azul** (Estilo moderno)
- 🟢 **Verde** (Estilo retro)
- 🟣 **Púrpura** (Estilo neón)
- 🔴 **Rojo** (Estilo deportivo)
- 🟡 **Amarillo** (Estilo cálido)
- ❄️ **Cyan** (Estilo hielo)
- 🌸 **Rosa** (Estilo chic)
- ⚪ **Blanco** (Estilo minimalista)

Al seleccionar un color, todos los bordes y botones cambiarán. Los botones ahora son **transparentes** para permitir ver el fondo personalizado.

### Fondos Personalizados
Puedes poner tu propia imagen de fondo:
1. Copia tu imagen `background.jpg` o `background.png` en la carpeta `/sdcard/RadioLogos/`.
2. Reinicia la aplicación.
3. Tu imagen aparecerá como fondo a través de la interfaz transparente.

---

## 3. Gestión de Logos y Nombres

### Logos de Emisoras
OpenRadioFM intenta mostrar el logo de cada emisora automáticamente.
1. **Carga Automática:** Busca logos en su base de datos interna o internet si hay conexión.
2. **Caché Inteligente:** Una vez descargado un logo, se guarda en memoria y en disco para que aparezca instantáneamente la próxima vez, incluso sin internet.

**Ubicación de Logos:** `/sdcard/RadioLogos/`
*Nota: La carpeta se crea automáticamente al iniciar la app. Si no existe, puedes crearla manualmente.*
Puedes añadir tus propios logos manualmente copiando imágenes `.png` en esa carpeta con el nombre de la frecuencia.
*Ejemplo:* Para 100.0 MHz, guarda la imagen como `10000.png` o `100000.png`.

## 4. Personalización (Skins)
Mantén pulsado el botón de **Configuración (EQ)** para abrir el selector de temas.
Puedes elegir entre:
- Clásico (Gris)
- Naranja
- Azul
- Verde
- Púrpura
- Rojo
- Amarillo
- Cyan
- Rosa
- Blanco

El cambio se aplica instantáneamente a toda la interfaz, incluyendo las memorias.

### Nombres Personalizados
Si el nombre RDS no es correcto o quieres poner uno propio:
1. Mantén pulsado el **Texto del Nombre** (STATION) o el **Logo Principal**.
2. Aparecerá un cuadro de diálogo.
3. Escribe el nombre deseado (ej. "ROCK FM") y pulsa "Guardar".
4. La app buscará automáticamente un logo con ese nombre (ej. `96900_ROCKFM.png`).
5. Esto permite tener logos diferentes para la misma frecuencia en distintas ciudades (ideal para usuarios sin Root).

---

## 4. Modos de Funcionamiento

La aplicación detecta automáticamente las capacidades de tu dispositivo:

- **Modo Completo (Root + Servicio):**
  - Requiere dispositivo con Root y el servicio `com.hcn.autoradio`.
  - Funcionalidad completa de RDS, nombres reales y control directo del chip de radio.
  
- **Modo Básico:**
  - Para dispositivos estándar sin root.
  - Funciones básicas de sintonización y logos.
  - Los nombres de emisoras deben introducirse manualmente.

---

## 5. Solución de Problemas

**La frecuencia no cambia:**
- Asegúrate de que no estás en modo "Scan". Pulsa Scan de nuevo para detener.

**No se ven los logos:**
- Verifica que tienes conexión a internet la primera vez.
- Verifica que tienes permisos de almacenamiento concedidos.

**La app se cierra al abrir:**
- Si has actualizado recientemente, prueba a borrar los datos de la aplicación en Ajustes de Android.

---
*Desarrollado por el equipo OpenRadioFM - v2.0 (Enero 2026)*
