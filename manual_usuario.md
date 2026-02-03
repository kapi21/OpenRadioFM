# Manual de Usuario - OpenRadioFM v3.0 (The Car Experience)

Bienvenido a OpenRadioFM v3.0, la evolución definitiva de la radio FM para unidades Android (Head Units). Esta versión está optimizada tanto para uso vertical como para pantallas panorámicas (1024x600) con un nuevo diseño horizontal.

---

## 1. Interfaz y Navegación

### 1.1 Modos de Pantalla (Layouts)
OpenRadioFM ahora cuenta con dos diseños principales:
- **V2 (Vertical/Clásico):** Optimizado para pantallas tipo tablet o verticales. *Ahora también soporta fondos dinámicos.*
- **V3 (Horizontal/Premium) [Novedad]:** Diseño panorámico ideal para el salpicadero, con iconos más grandes, logos en formato galería e indicadores de banda gráficos.
- **Cómo cambiar:** Mantén pulsado el botón **LOC/DX** para alternar entre el diseño horizontal y el vertical. La app se reiniciará automáticamente con el nuevo aspecto.

### 1.2 Panel de Control Inferior (V3)
- **Settings (EQ):** Pulsación corta para el ecualizador. **Pulsación larga** para el Menú de Personalización Premium.
- **Band:** Cambia entre FM1, FM2 y FM3.
- **LOC/DX:** Cambia la sensibilidad (Local/Larga Distancia). *Larga pulsación para cambiar de layout.*
- **Scan:** Escaneo automático de emisoras.
- **Mute:** Silencio instantáneo.
- **GPS [Novedad]:** Pulsación corta para abrir tu aplicación de navegación favorita (Maps, Waze, etc.).
  - *Menú Oculto:* Haz **5 clics rápidos** sobre el icono de GPS para abrir el menú técnico de fábrica.

### 1.3 Control de Frecuencia y RDS
- **Flecha Izquierda (<):** Bajar frecuencia (-0.05 MHz). Pulsación larga para búsqueda automática hacia abajo.
- **Flecha Derecha (>):** Subir frecuencia (+0.05 MHz). Pulsación larga para búsqueda automática hacia arriba.
- **RDS:** Visualización dinámica de nombre de emisora y texto informativo (RadioText) con desplazamiento suave.

---

## 2. Personalización Premium (Menú Secreto)

Mantén pulsado el botón de **Configuración (EQ)** para acceder al centro de personalización:

### 2.1 Colores del Tema
Elige entre 10 esquemas de color (Naranja, Azul, Rojo, Cyan, etc.) que se aplican a los bordes y acentos de la interfaz.

### 2.2 Tipografía (Fuentes)
Cambia el aspecto de todos los textos. Disponibles:
- **System:** Por defecto de Android.
- **Bebas:** Estilo industrial/robótico.
- **Digital:** Estilo LED clásico de radio antigua.
- **Inter:** Moderna y minimalista (alta legibilidad).
- **Orbitron:** Estilo futurista y tecnológico.

### 2.3 Modo de Fondo
Gestiona cómo se ve el fondo de tu radio:
1. **Negro Puro:** Ideal para pantallas OLED/AMOLED o conducción nocturna.
2. **Imagen background.png:** Carga tu imagen desde `/sdcard/RadioLogos/background.png`.
3. **Logo Dinámico [Novedad]:** El fondo de la radio cambia automáticamente al logo de la emisora sintonizada, aplicado con un elegante efecto de desenfoque (blur).

---

## 3. Gestión de Logos y Nombres

### 3.2 Logo de Marca de Coche (Car Logo) [Novedad v3.0]
En el diseño horizontal (V3), puedes mostrar el logo de tu coche en el lateral derecho:
- **Ubicación:** `/sdcard/RadioLogos/car_logo.png`
- **Requisito:** La imagen debe llamarse exactamente `car_logo.png`. Se recomienda un fondo transparente para mejor estética.

---

## 4. Solución de Problemas

- **El GPS no se abre:** Asegúrate de tener una aplicación de mapas instalada y configurada como predeterminada en Android.
- **No cambia el fondo dinámico:** Verifica que la emisora sintonizada tenga un logo asignado. Si no hay logo, el fondo volverá a negro.
- **Los logos desaparecen al reiniciar:** En la v3.0 se ha añadido integración con el escáner de medios de Android para asegurar que los logos persistan siempre.
- **Botones SEEK invertidos:** En esta versión v3.0 se ha corregido la orientación para que coincida con la mayoría de mandos al volante.
- **Punto decimal en frecuencia:** Ahora se fuerza el uso de punto (ej. 108.0) para una visualización más limpia en todos los sistemas.

---
*Desarrollado por Jimmy80 - v3.0 (Febrero 2026)*
