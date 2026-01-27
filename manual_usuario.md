# Manual de Usuario - OpenRadioFM

Bienvenido a OpenRadioFM, una aplicación de radio FM avanzada diseñada específicamente para unidades principales de Android (Head Units) con soporte para control de hardware y personalización.



---

## 1. Interfaz Principal

La interfaz ha sido diseñada para ser clara, legible y fácil de usar mientras se conduce.

### 1.1 Panel Izquierdo: Memorias (Presets)
Aquí se muestran tus 6 emisoras favoritas guardadas.
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
- **EQ (Ecualizador):** Abre los ajustes de audio del sistema.
- **TEST:** Botón multifunción para pruebas de desarrollo.

---

## 2. Personalización (Skins)

¡Haz que la radio combine con el interior de tu coche!

### Cómo cambiar el color (Skin)
1. Pulsa el botón **TEST** (o Ajustes si está disponible).
2. Selecciona "Cambiar Tema" en el menú (si aparece) o espera a futuras actualizaciones para un acceso más directo.
   *(Nota: En la versión actual v8.5, el selector se activa mediante un mecanismo específico en desarrollo)*

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

Al seleccionar un color, todos los bordes y botones de la aplicación cambiarán instantáneamente.

---

## 3. Gestión de Logos y Nombres

### Logos de Emisoras
OpenRadioFM intenta mostrar el logo de cada emisora automáticamente.
1. **Carga Automática:** Busca logos en su base de datos interna o internet si hay conexión.
2. **Caché Inteligente:** Una vez descargado un logo, se guarda en memoria y en disco para que aparezca instantáneamente la próxima vez, incluso sin internet.

**Ubicación de Logos:** `/sdcard/RadioLogos/`
Puedes añadir tus propios logos manualmente copiando imágenes `.png` en esa carpeta con el nombre de la frecuencia.
*Ejemplo:* Para 100.0 MHz, guarda la imagen como `10000.png` o `100000.png`.

### Nombres Personalizados
Si el nombre RDS no es correcto o quieres poner uno propio:
1. Mantén pulsado el **Texto del Nombre** (STATION) o el **Logo Principal**.
2. Aparecerá un cuadro de diálogo.
3. Escribe el nombre deseado y pulsa "Guardar".
4. Para volver al nombre original (RDS), pulsa "Restaurar Original".

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
*Desarrollado por el equipo OpenRadioFM - v8.5 (Enero 2026)*
