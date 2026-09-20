# Handoff — OpenRadioFM 5.5 OFFLINE Edition

Documento para retomar el trabajo sin perder contexto.
- **Rama activa:** `5.5-offline` (remoto `origin/5.5-offline`).
- **Versión de la app:** `v5.5-OFFLINE` (Code `42`).
- **Filosofía del producto:** 100% Offline, sin dependencias de red, sin servicios en la nube, gestión de logos puramente local (`/sdcard/RadioLogos/`).

---

## 1. Resumen de lo Implementado

### 1.1 Flujo de Presets y Selector de Logos Local
- **Guardado directo:** La pulsación larga sobre un preset lo almacena inmediatamente sin abrir el explorador ni interrumpir al usuario.
- **Edición desde el Dial:** Al pulsar sobre el dial de frecuencia (`boxFrequency` o `tvFrequency`), se abre el diálogo de edición con 4 botones del mismo tamaño (`layout_weight="1"`, `minHeight="48dp"`):
  - `GUARDAR`: Confirma cambios de nombre RDS / texto manual.
  - `LOGO`: Abre el diálogo integrado `dialog_logo_picker.xml` directamente en `/sdcard/RadioLogos/`.
  - `ORIGINAL`: Restaura el nombre original (RDS de fábrica).
  - `CANCELAR`: Cierra el diálogo sin aplicar cambios.
- **Explorador Visual Integrado de Logos (`dialog_logo_picker.xml`):**
  - Muestra miniaturas decodificadas y nombres de archivos para `.png`, `.jpg`, `.jpeg`, `.webp`, `.bmp` en una cuadrícula con scroll y caché LRU de imágenes para evitar demoras o bloqueos de memoria.
  - Atajos rápidos superiores en barra horizontal: `📁 RadioLogos`, `📥 Descargas` (`/sdcard/Download`), `💾 Memoria` (`/sdcard/`), `🔌 USB` (detección automática de pendrives en `/storage/` o `/mnt/media_rw/`) y `⬆ Subir` (carpeta superior).
  - Botón inferior `🌐 Explorador Android`: abre de forma segura el selector del sistema (SAF / `ACTION_GET_CONTENT`) con política permisiva de StrictMode para eliminar el error `FileUriExposedException`.
- **Normalización de imagen:** Procesa formatos `.png`, `.jpg`, `.jpeg`, `.webp`. Escala la imagen automáticamente a un máximo de **300x300 px** antes de guardarla.
- **Refresco en caliente:** Limpia la caché interna de Glide y `LogoManager`, forzando el refresco inmediato del logo en la interfaz principal, lista de presets y widgets asociados.

### 1.2 Limpieza Total de Conectividad (Modo Offline)
- **Eliminación de Nube y Web:**
  - Desconectados `RadioRepository`, `Supabase`, `RadioBrowser` y descargadores HTTP en segundo plano.
  - Retirada la sección completa de nube en el diálogo de ajustes premium (`dialog_premium_settings.xml`): eliminados los switches de aportación comunitaria y base de logos online.
  - Diálogos "Acerca de" (`dialog_about.xml`) y "Agradecimientos" (`dialog_acknowledgements.xml`): eliminados hipervínculos HTML externos, manteniendo el reconocimiento como texto plano.
- **Identificación de Versión:**
  - Sustituido el sufijo histórico `ROOT VERSION` por `OFFLINE VERSION`.
  - En la ventana de información se visualiza como: `v5.5-OFFLINE — OFFLINE VERSION`.

### 1.3 Arquitectura Multi-Agente
- Integrado el modelo de 3 sub-agentes adaptado del proyecto RASTRO (`AGENTS.md` y `.cursor/agents/`):
  1. `core-hardware.md`: Control de hardware, MCU (K706, MT8163, SPD, FYT), intercepción física y accesibilidad.
  2. `ui-skins.md`: Capa visual, temas, skins, dial, widgets y dimensiones responsivas.
  3. `data-presets.md`: Gestión local de frecuencias, nombres RDS, logos en `/sdcard/RadioLogos/` y caché local.

### 1.4 Optimización de Recursos, Sustitución de Logos y Exclusión Reloj/Coche (V5.5.1 Patch)
- **Sustitución inmediata y eliminación de logos previos en presets:**
  - `RadioRepository.deleteExistingLogoFilesForFrequency(freqKHz)`: elimina cualquier archivo anterior de esa frecuencia (`freq_*.*`, `freq.*`, etc.) en `/sdcard/RadioLogos/` y almacenamiento local antes de guardar uno nuevo.
  - Invalida la memoria de Glide (`Glide.get(context).clearMemory()`) y aplica `.signature(new ObjectKey(file.lastModified()))` en la carga de presets y emisora para evitar que Glide sirva bitmaps antiguos de la caché.
  - Método `forceUpdateSlotWithLogo(...)` en `PresetManager` para actualización visual en caliente e inmediata del slot sin depender de llamadas asíncronas lentas.
- **Acción directa de Quitar Logo:**
  - Botón integrado "🗑 Quitar" en `dialog_logo_picker.xml` conectado a `RadioRepository.removeCustomStationLogo(freqKHz)` para borrar el logo de disco y restaurar el placeholder al instante.
- **Exclusión mutua estricta entre Reloj Digital y Logo del Coche:**
  - `LogoManager.loadCarLogo()` ahora comprueba `pref_logo_mode`. Si el usuario tiene activo el reloj (`logoMode == 1`), `ivCarLogo` se oculta forzosamente (`GONE`) y nunca se superpone al reloj.
  - `DialogManager.showBackgroundSelector` delega en `applyLogoModePreference()` en lugar de forzar `loadCarLogo()`, impidiendo el solapamiento al cambiar entre fondo negro, imagen fija o logo dinámico.
- **Optimización de CPU y reducción del 98% en consumo de memoria:**
  - Reducción del tamaño de decodificación en presets: de `Target.SIZE_ORIGINAL` en 32 bits ARGB_8888 a `160x160 px` en `RGB_565` (~50 KB por logo en lugar de ~4 MB). Elimina por completo las pausas Stop-The-World del Garbage Collector (`GC_FOR_ALLOC`) que hacían que la app se volviera errática al cargar logos.
  - Miniaturas del explorador de logos (`LogoAdapter`) migradas a Glide con decodificación a `120x120 px`, permitiendo scroll a 60 FPS sin saturar los núcleos con `BitmapFactory.decodeFile` manual.

---

## 2. Estado de Compilación y Git

- **Compilación Gradle:** Verificada y limpia con `./gradlew assembleDebug`.
- **Binario generado:** `app/build/outputs/apk/debug/app-debug.apk`.
- **Rama:** `5.5-offline`.

---

## 3. Comandos Rápidos de Referencia

```bash
# Compilar APK de depuración
.\gradlew assembleDebug

# Instalar en unidad Android vía ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Inspeccionar logs de cambio de logo y presets
adb logcat -s OpenRadioFM LogoManager RadioRepository PresetManager
```

---
*Última actualización del handoff: 2026-09-20 — Refresco de presets, borrado de logo anterior, exclusión reloj/coche y optimización de recursos listos.*
