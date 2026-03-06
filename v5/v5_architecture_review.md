# 🔍 Auditoría Técnica y Roadmap para OpenRadioFM V5.0

Tras analizar el código fuente actual (v4.7.5), he identificado los puntos fuertes del proyecto y las áreas clave que necesitan optimización para garantizar la escalabilidad y estabilidad en la próxima gran versión.

## 📊 Estado Actual (Diagnóstico)

### Puntos Fuertes ✅
1. **Abstracción de Hardware**: La interfaz `RadioEngine` es un gran acierto. Permite manejar K706, MT8163 y QS6 de forma agnóstica.
2. **Modularización Inicial**: `RadioRepository`, `ThemeManager`, `PtyManager` y `DialogManager` han aliviado la carga principal. Integrar datos locales y Supabase en un repositorio unificado fue una decisión excelente.
3. **Estabilidad**: El sistema de callbacks y `ExecutorService` funciona, resolviendo la mayoría de los ANRs (App Not Responding).

### Cuellos de Botella (Deuda Técnica) ❌
1. **El "Monstruo" `MainActivity`**: 
   - Con casi 2500 líneas, la `MainActivity` hace de todo: gestiona el ciclo de vida, pinta la UI, intercepta toques (gestos), maneja el Bluetooth, el Audio Focus y se suscribe directamente a los eventos del motor. Esto viola el principio de responsabilidad única (SOLID).
2. **Infierno de Callbacks (Callback Hell)**: 
   - Múltiples fuentes (`RadioEngine`, `RadioRepository`, `Supabase`) invaden la `MainActivity` con callbacks que exigen `runOnUiThread()`. Esto puede causar micro-lags visuales.
3. **Duplicación en Motores AIDL**:
   - `K706Engine`, `MT8163Engine` y `QS6Engine` repiten lógica de gestión de estado (ej: guardar si está en estéreo, banda actual) en lugar de delegarlo a una clase base.
4. **Acoplamiento al Ciclo de Vida de la UI**:
   - Falta un *Foreground Service* (Servicio en Primer Plano). Si el sistema del coche cierra la `MainActivity` por falta de memoria, se pierde el control de la radio (especialmente crítico ahora que hay streaming online).

---

## 🚀 Hoja de Ruta de Optimización (Hacia la V5.0)

Para la versión 5.0, el objetivo principal debe ser la **Arquitectura y el Rendimiento**, preparando el terreno para nuevas funciones como la integración con Android Auto.

### Fase 1: Arquitectura y Desacoplamiento (MVVM)
* **Objetivo:** Vaciar `MainActivity` y hacer la app reactiva.
* **Acciones:**
  1. **Migración a ViewModel/LiveData (o StateFlow en Kotlin)**: 
     - Crear un `RadioViewModel`. `MainActivity` solo debería "observar" el estado (Frecuencia, Nombre, Logo) y pintarlo, sin procesar lógica.
  2. **`MediaSessionControl`**: Extraer la lógica de volúmenes, mute, y AudioFocus a un gestor de audio independiente.
  3. **`PresetUIController`**: Sacar toda la lógica de los 12 botones de favoritos fuera de la Activity.

### Fase 2: Robustez del Motor (Service-Based Architecture)
* **Objetivo:** Que la radio nunca muera, aunque la pantalla esté apagada o en otra app.
* **Acciones:**
  1. **`RadioForegroundService`**: Mover la instanciación del `RadioEngine` a un Servicio persistente que muestre una notificación con controles multimedia (Play/Pause, Seek). Esto es obligatorio para que los controles del volante (SWC) y el launcher del coche (PiP) funcionen suavemente en Android 8+.
  2. **Clase `BaseRadioEngine`**: Factorizar el código repetido de K706, MT8163 y QS6.

### Fase 3: Modernización (Migración a Kotlin) progresiva
* **Objetivo:** Menos código, más seguro.
* **Acciones:**
  1. Kotlin permite reemplazar los callbacks anidados y la complejidad de `ExecutorService` por **Coroutines**, haciendo que las peticiones a red (Supabase) y el paso de RDS a la UI sean asíncronos pero se lean como código síncrono. (Reduciría el código de `RadioRepository` a un 50%).

### Fase 4: Nuevas Funcionalidades V5.0 ("The Android Auto Era")
Una vez limpia la casa, estas son las "Killer Features" que la arquitectura permitirá de forma sutil:
1. **Soporte Nativo Android Auto**: Usando `MediaBrowserServiceCompat` (que construiremos en la Fase 2), la app aparecerá mágicamente en la pantalla de Android Auto.
2. **Ecualizador DSP Avanzado**: Crear una vista gráfica de ecualizador interceptando el audio mix.
3. **Modo Híbrido Real**: Si entras en un túnel y se pierde el RDS/FM, el ViewModel detecta la caída de RSSI y cambia instantáneamente y sin cortes al stream de internet de la misma emisora.

---

## 🛠️ ¿Por dónde empezamos?
Te sugiero empezar por el **Desacoplamiento de MainActivity**. Extraer la gestión de atajos (Favoritos) o la gestión de Audio (`AudioFocus`/`Bluetooth`) a sus propias clases `Manager` puras, o si estás dispuesto a dar el salto a arquitecturas modernas de Android, introducir un **ViewModel**.

¿Qué dirección prefieres tomar para arrancar esta V5.0?
