# Handoff — OpenRadioFM 5.5.0 Universal Edition

Documento de continuidad de sesión para reanudar el trabajo sin pérdida de contexto.

- **Rama activa:** `main` (fusionada con `5.5-offline`).
- **Versión de la app:** `5.5.0 Universal` (VersionCode `43`).
- **Binario generado:** `app/build/outputs/apk/debug/app-debug.apk` (Compilación limpia, `BUILD SUCCESSFUL in 3s`).
- **Fecha:** 2026-09-23.

---

## 1. Resumen de Cambios Implementados

### 1.1 Corrección del Bug de Luces de Coche (Hardware K706)
- **Fallo previo:** En `MainActivity.handleHwLightsAutomation`, al apagar las luces se intentaba leer la clave inexistente `pref_skin_v2` (devolviendo `0`), lo que correspondía al ordinal 0 de `Skin.values()` (`NIGHT_MODE`), bloqueando el retorno al modo día. Además, no se respaldaba el skin previo al encender las luces.
- **Solución:**
  - Al encender luces (`lightsOn = true`): Se almacena el skin actual en `ThemePrefs` bajo la clave `prev_skin_before_night` y se aplica `NIGHT_MODE`.
  - Al apagar luces (`lightsOn = false`): Se recupera el skin original desde `prev_skin_before_night` (o `CLASSIC` como fallback) y se restaura el tema diurno.

### 1.2 Unificación Universal en una Sola APK
- **Switch Maestro en Ajustes (`dialog_premium_settings.xml` y `DialogManager.java`):**
  - `"Modo Fuera de Línea (100% Local)"` (`pref_offline_mode`, activado por defecto).
  - Si está activado: Oculta opciones de nube, mantiene el icono `ivDataActivity` en `GONE` y bloquea cualquier llamada de red.
  - Si está desactivado: Despliega las opciones de Supabase, RadioBrowser, streaming y contribución comunitaria.
- **Repositorio Híbrido (`RadioRepository.java`):**
  - Método `isOfflineMode()` conectado a `pref_offline_mode`.
  - En modo offline: Búsqueda y guardado exclusivo en `/sdcard/RadioLogos/`.
  - En modo online: Recuperación de logos remotos y resolución de streaming.
  - Preservación del 98% de reducción de memoria (decodificación RGB_565 y máx 300x300 px).
- **Indicador de Nube (`StreamingUiCoordinator.java`):**
  - `updateDataActivityUi` responde dinámicamente a `pref_offline_mode`.

---

## 2. Plan de Pruebas de Mañana

1. **Instalación de APK:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Prueba 1: Luces en hardware K706:**
   - Encender luces del coche -> La app debe pasar a `NIGHT_MODE` (fondo oscuro / acento azul).
   - Apagar luces del coche -> La app debe restaurar inmediatamente el skin que tenía previamente (ej. `DAY_MODE` o `CLASSIC`) sin intervención manual.
3. **Prueba 2: Comportamiento Offline por defecto:**
   - Abrir Ajustes Premium.
   - Verificar que *"Modo Fuera de Línea"* está activado.
   - Verificar que no hay icono de nube visible ni tráfico de red.
4. **Prueba 3: Conmutación a Online:**
   - Desactivar *"Modo Fuera de Línea"* en Ajustes.
   - Verificar que se despliegan las opciones de Supabase y el estado de conexión.
   - Comprobar que el icono de nube se muestra al sintonizar con internet.
5. **Cierre:** Confirmar resultados para etiquetar versión release en Git.
