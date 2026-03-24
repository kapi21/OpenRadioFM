## [5.0.7 (K706 streaming vs FM channel)] - 2026-03-24

### Fixed
- **K706 — Radio Online sin audio / sigue sonando FM**: durante la carga del stream (y en otros `setMute(false)`), `PlaybackManager` llamaba a `enforceAudioRecovery()` → `SetChannel(2)` y anulaba el paso a canal Android (`SetChannel(4)`) que usa ExoPlayer. Se omite el recovery forzado a FM si el motor indica streaming activo; `refreshRadioStatus` trata como streaming también el estado **loading** (no solo `isPlaying`); `K706RadioManager` no ejecuta `enforceAudioChannelRecovery` ni el heartbeat de canal mientras `mIsOnlineStreamingActive`.

### Changed
- **RadioEngine**: método por defecto `isOnlineStreamingActive()`; implementado en K706, MT8163 y QS6.
- **Versionado app**: `versionCode 18`, `versionName 5.0.7 (K706 streaming vs FM channel)`.

---

## [5.0.6 (K706 cold start + QS6 parity)] - 2026-03-24

### Fixed
- **K706 — primera apertura tras reinicio de unidad**: el arranque ya no depende solo de `getCurrentFreq() <= 0` (el motor reportaba frecuencia distinta de la guardada sin forzar `tune`). Se alinea la lógica con QS6: comparar con `pref_last_freq`, reforzar sintonía ~1,4 s después del init, guardas de persistencia 87.5/87.6 y saneo opcional de prefs bootstrap (`pref_k706_bootstrap_sanitized`).
- **K706RadioManager**: valor inicial de `mCurrentFreq` corregido de **8750** a **87500** (misma escala OpenRadioFM ×1000 que el resto del pipeline MCU → UI).

### Changed
- **Versionado app**: `versionCode 17`, `versionName 5.0.6 (K706 cold start + QS6 parity)`.

---

## [5.0.5 (Hardening Phase 7 + QS6 startup)] - 2026-03-24
*Merge de `hardening/phase-7-stability` en `main`: estabilidad QS6 al arranque y ajuste de reproducción.*

### Fixed
- **QS6 / NWD — arranque en última emisora**: refuerzo frente a callbacks del stack OEM que reinyectan 87.5/87.6 MHz o el primer preset nativo tras auto-scan; `tuneWithBand` con banda explícita, re-afirmación tras cortar scan, protección de sintonía solicitada y restauración desde frecuencia estable reciente; logs `SRC=` para trazar origen.
- **QS6 / NWD — persistencia**: no guardar 87.x como última emisora salvo sintonía explícita del usuario; guardas en arranque y al apagado (`prepareForPowerOff`) para que callbacks tardíos no pisen `pref_last_freq` / `pref_last_band`; saneo único de preferencias bootstrap contaminadas.
- **Reproducción**: registro del receptor de medios **idempotente** para evitar duplicados al reanudar la actividad.

### Changed
- **Versionado app**: `versionCode 16`, `versionName 5.0.5 (Hardening Phase 7 + QS6 startup)`.

---

## [5.0.4 (QS NWD Advance + K706 Fixed)] - 2026-03-23
*Build de avance y corrección (fixed build, no release mayor).*

### Fixed
- **QS6 / NWD — transición de emisora**: mitigado arrastre temporal de PS/logo de la emisora anterior al cambiar rápido de frecuencia o preset.
- **QS6 / NWD — presets**: reforzada la coherencia de logos y textos por slot para descartar callbacks asíncronos obsoletos.
- **QS6 / NWD — PTY**: persistencia y fallback de PTY por frecuencia para evitar mostrar "Sin PTY" cuando el evento sí llegó desde el HAL.
- **QS6 / NWD — arranque**: bloqueo de auto-scan espontáneo al iniciar (manteniendo autoscan manual por botón).
- **QS6 / NWD — última frecuencia**: al abrir la app en frío se sintoniza de nuevo `pref_last_freq` si difiere del estado local del motor (antes `getCurrentFreq()` nunca era ≤0 y no se restauraba la emisora guardada).
- **K706 / controles**: se mantienen y consolidan los fixes de mandos volante/media para comportamiento estable en foreground/background.

### Added
- **Ajustes de Hardware**: nuevo selector para mandos de volante `NEXT/PREV`:
  - `Seek (emisoras)`
  - `Preset (memorias)`
- **UI**:
  - sección **👏 Agradecimientos** en Personalización Premium.
  - texto pulsable `Radio Android España` con diálogo QR.
  - acceso a `dialog_credits` con 1 toque sobre el logo en **Acerca de OpenRadioFM**.

### Changed
- **Versionado app**: `versionCode 15`, `versionName 5.0.4 (QS NWD Advance + K706 Fixed)`.
- **README**: actualizado historial y resumen bilingüe de la build `v5.0.4`.
- **Hardening / estabilidad**: limpieza más segura de recursos/ciclo de vida en `RadioMediaService` y `RadioServiceController`; indicador de actividad Supabase no bloqueante en `RadioRepository`; cachés y tracking pendiente reforzados para concurrencia; menor verbosidad de logs de red (`DEBUG=BASIC`, `RELEASE=NONE`).

---

## [5.0.2 fixed (QS6 / NWD)] - 2026-03-22
*Build de corrección (no release completo).*

### Fixed
- **QS6 / NWD**: al pasar la app a segundo plano se libera la competencia por audio con el reproductor del sistema (`releaseAudioFocusOnlyForBackground`); el reproductor nativo deja de cortarse.
- **AutoScan**: `ScanManager` + `MainActivity` — botón alineado con el estado real del escaneo.

### Added
- **QS6 Engineering**: diálogo *Technical Matrix* (easter egg GPS ×5).
- **Docs / assets**: `INTELIGENCIA_QS_NWD.md`, imágenes V5 en `docs/img/`.

### Changed
- **QS6Engine**, **RDSManager**, logos (`RadioRepository`, `SupabaseLogoSource`, `LogoManager`).

---

## [5.0.1 (K706 hotfix)] - 2026-03-22
### Fixed
- **K706 — Llamadas**: `READ_PHONE_STATE` en runtime; `PhoneStateListener` registrado solo con permiso; al colgar se llama a `requestPlayAudio()` para recuperar FM.
- **K706 — Volante / MediaSession**: `MEDIA_NEXT` / `PREVIOUS` y `RadioMediaService` (`skip` / cola) usan **seek** (frecuencia), no siguiente/anterior preset.

### Added
- **Docs**: `COMPARATIVA_K706_OPENRADIO_VS_OEM.md`, ampliaciones al estudio OEM; script opcional `scripts/k706_jadx_decompile.bat`.

---

## [5.0.0 (Stability Beta)] - 2026-03-21
### Added
- **Estabilidad MTK8259 (Crítico)**:
    - Nuevo sistema de paso de frecuencia (manual) para evitar errores de sintonía en Topway.
    - Eliminada la opción "Desactivar AM" en Ajustes Premium para prevenir bucles infinitos en el hardware.
    - Sincronización de Mute delegada exclusivamente a `TsCommon` para mayor fiabilidad.
- **Modo Claro (White Skin)**: Implementación completa del skin "White" con visibilidad garantizada en todos los elementos de UI y fuentes.
- **Transparencia Uniforme**: Aplicada opacidad consistente en todas las tarjetas de Layout 2 y Classic Skin.
- **Engineering Mode v2**: Mejoras en el terminal de log "Matrix Style" y comprobación de permisos Root.

### Fixed
- **MT8163 Initialization**: Corregido bug de instancia duplicada en `onServiceConnected` mediante el método `updateService`.
- **Iconos Recursores**: Solucionado el crash de "Recursive reference" en el icono del lanzador.
- **RDS Flickering**: Optimizado el buffer de texto RDS para evitar parpadeos en unidades MTK.
- **Supabase Stability**: Reemplazado `catch (Exception)` por `catch (Throwable)` en procesos de red para evitar cierres por `NoClassDefFoundError` (ClassValue) en Android 9.
- **Service Recovery**: Mejorada la recuperación del servicio nativo tras un cierre inesperado del proceso remoto.

## [4.9.5] - 2026-03-19
### Added
- **Engineering Mode Dashboard**: Hidden diagnostic menu (GPS button x5) features:
    - **RF Telemetry**: Signal Quality (SQI), Stereo/Mono, LOC/DX, Estimated RSSI.
    - **RDS Inspector**: PI, PTY, PS, AF List, Sync Status.
    - **Asset Diagnostics**: Real-time checks for `RadioLogos` folder, custom backgrounds, and car logos.
    - **System Info**: Device Model, Board ID, and **Root Access** verification (SU Check).
    - **Interactive Tuner**: Manual stepping controls (`<` / `>`) with real-time signal feedback.
    - **Live Terminal Log**: "Matrix Style" scrolling console showing kernel events (TUNED, RDS_UPDATE, STEREO_LOCK).
    - **Data Management**: Tools to Factory Reset favorites and clear station history.
- **Categorical PTY Icons**: Visual indicators for station categories (Music, News, Sport, Talk, Jazz, Classical, etc.) based on RDS PTY codes.

## [4.0.0] - 2026-02-09
### Added
- **Dynamic Backgrounds (Global Edition)**: Entirely refactored the dynamic blur engine for Layout 3. Background now updates even when central logo is hidden, providing a premium translucent glass effect.
- **Signal Level Quality Tinting**: Implemented dynamic coloring for the signal icon (`ivSignalLevel`) based on reception quality:
    - **Green**: Excellent (Stereo + RDS Lock).
    - **Yellow**: Medium (Stereo or RDS).
    - **Red**: Poor (No Lock).
- **Expanded Layout 3 (Horizontal)**: Control bar expanded to 8 buttons, integrated Android settings, and improved sidebar for panoriamic displays.
- **Save/Load Favorites**: Advanced backup system using `.fav` files in `RadioLogos` folder.

### Fixed
- **Layout 2 Stability**: Prevented layout shifting by ensuring RDS boxes (`tvRdsName`, `tvRdsInfo`) stay visible as transparent placeholders when empty.
- **Night Mode Consistency**: Consolidated all color-tinting logic. Now Frequency, Band, Signal, and RDS consistently use **Night Blue** in Night Mode.
- **Activity State Persistence**: Implemented `onSaveInstanceState` to preserve favorites and signal status across layout switches.
- **Icon Alignment**: Standardized `fitStart` and `fitEnd` across all layouts to eliminate internal drawable padding issues.
- **Language Selector**: Fixed automatic restart when switching between Spanish, English, and Russian.

### Changed
- **Icon Sizing**: Increased padding for secondary control buttons (GPS, Settings) to 28dp for better visual weight in Layout 2.
- **PTY Refinement**: Temporarily simplified PTY display logic to prioritize stability in low-signal environments.

---

## [3.0] - 2026-02-08
### Added
- Initial implementation of Layout 3 (Premium Horizontal).
- Support for Car Brand Logo customization.

Actualización centrada en la internacionalización, gestión de contenidos y refinamiento visual.

### 🌍 Internacionalización
*   **Soporte Multiidioma:** Traducción completa de la interfaz a Español, Inglés y Ruso.
*   **Selector Manual:** Nuevo selector de idioma en el menú Premium que permite forzar el idioma independientemente del sistema.
*   **Reinicio Dinámico:** La aplicación aplica el nuevo idioma instantáneamente reiniciando la actividad.

### 💾 Gestión de Favoritos
*   **Save/Load System:** Nueva funcionalidad para guardar y cargar tus listas de emisoras favoritas.
*   **Formato .fav (JSON):** Los archivos se guardan en `/sdcard/RadioLogos` y son fáciles de compartir o respaldar.
*   **Contenido Guardado:** Frecuencia, número de preset, nombre personalizado y timestamp.

### 🎨 Mejoras Visuales (V2 & V3)
*   **Layout V2 Refinado:** Corrección total de la alineación en la columna derecha. Nuevos botones de acceso rápido.
*   **Modo Nocturno Premium:** Los iconos de banda (FM) y etiquetas (MHz) ahora reciben un tinte azul nocturno ("Night Blue") en modo noche, junto con los bordes de los botones.
*   **Iconos Optimizados:** Reducción de tamaño (padding 18dp) para una estética más limpia.

### 🛠️ Otras Mejoras
*   **Botones Extra (V2):**
    *   **Settings:** Acceso directo a los ajustes de Android.
    *   **Favoritos:** Acceso directo al diálogo de Guardar/Cargar.
*   **Gestos (Beta):** Marcados como característica experimental en el menú.
*   **Correcciones:** 
    *   Solucionado error de compilación con referencias a variables antiguas.
    *   Corregidos formats strings con múltiples argumentos.

---


## v3.0 "The Car Experience" (Febrero 2026)

Esta es la actualización más ambiciosa hasta la fecha, transformando OpenRadioFM en una experiencia premium diseñada específicamente para el entorno del automóvil.

### 🚀 Novedades Principales
*   **Nuevo Layout Horizontal (V3):** Diseñado para pantallas de 1024x600. Iconos maximizados, visualización de frecuencia optimizada y logos tipo galería.
*   **Logo Marca Coche (V3):** Soporte para logo de marca personalizado en `/sdcard/RadioLogos/car_logo.png`.
*   **Indicadores de Banda Gráficos (V3):** Sustitución del texto FM1/FM2 por iconos dinámicos (`radio_fm1.png`, etc.) y nuevo icono de botón `BAND`.
*   **Menú de Personalización Premium:** Todo un centro de ajustes estéticos al alcance de una pulsación larga en el botón EQ/Settings.
*   **Capa de Fondo Dinámico (Universal):** El fondo de la aplicación (tanto en V2 como en V3) ahora puede cambiar dinámicamente al logo de la emisora sintonizada (con efecto Blur/Desenfoque).
*   **Gestor de Tipografías:** Soporte para 5 fuentes distintas (System, Bebas, Digital, Inter y la nueva **Orbitron**) integradas directamente en la app.
*   **Acceso Rápido GPS:** Nuevo botón dedicado para lanzar tu navegador GPS favorito (Maps, Waze, etc.) directamente desde la radio.

### 🛠️ Mejoras Técnicas & UI
*   **Visibilidad de Diálogos:** Nuevo fondo difuminado (70% dim) en todos los ajustes para mejorar el contraste sobre la interfaz.
*   **Coloreado de Bordes:** Optimizado según el layout (completo en V2, solo memorias en V3).
*   **Legibilidad de Memorias:** Aumento de tamaño de fuente a **19sp** en el layout horizontal.
*   **Separador Decimal:** Forzado a punto (**108.0**) para una visualización uniforme.
*   **Maximización de Controles:** Eliminación de márgenes (padding) en la fila inferior para facilitar la pulsación táctil conduciendo.
*   **Optimización 1024x600:** Reajuste de pesos en el diseño horizontal para evitar recortes en pantallas de baja resolución vertical.
*   **Refresco en Tiempo Real:** Los cambios de fondo y tipografía se aplican instantáneamente sin necesidad de reiniciar.
*   **Internacionalización:** Soporte completo de idiomas para Español, Inglés y Ruso en todos los menús.

### 🐛 Bugs Corregidos
*   **Persistencia de Logos:** Solucionado el problema que hacía desaparecer los logos tras un reinicio mediante integración forzada con **MediaScanner**.
*   **Lógica SEEK:** Corregida la dirección de los botones de búsqueda (Izquierda=Bajar, Derecha=Subir) para ser intuitiva y compatible con mandos al volante.
*   **Persistencia de Fondo:** Corregido el error que impedía aplicar el modo de fondo seleccionado desde el menú.
*   **Pantalla Completa:** Ajuste en el manejo de la barra de estado de Android en modo horizontal.
*   **Menú TEST:** El menú técnico de fábrica ahora está oculto tras 5 clics en el icono de GPS.

---

## v2.0b (Enero 2026)
*   **Expansión de Memorias:** De 6 a 12 presets por banda.
*   **Personalización de Fondos:** Soporte para `background.jpg/png`.
*   **Interfaz Glassmorphism:** Nuevos botones semitransparentes.
