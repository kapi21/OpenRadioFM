# OpenRadioFM 📻

[![Version](https://img.shields.io/badge/version-v5.0.16-green.svg)]()
[![Branch](https://img.shields.io/badge/branch-main-informational.svg)]()

[![License](https://img.shields.io/badge/license-Apache_2.0-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android_7.1+-orange.svg)]()
[![Hardware](https://img.shields.io/badge/hardware-MT8163_|_K706_|_QS6_|_MTK8259-purple.svg)]()

**Aplicación de radio FM premium para Android Head Units**, con soporte activo para **K706**, **MT8163 (Junsun V1 Pro)** y las plataformas **MTK 8227L / 8259 / 8667**.  
Interfaz Glassmorphism, RDS completo (PS, RT, PTY, AF, TA, TP), y personalización avanzada de logos y temas.

<div align="center">
  <img src="docs/img/app_icon.png" width="150" alt="OpenRadioFM Logo">
  <br>
  <p align="center"><b>【 GALERÍA DE CAPTURAS 】</b></p>
  <img src="docs/img/screenshot1.png" width="45%" alt="Layout V2 (v4.7)">
  <img src="docs/img/screenshot2.png" width="45%" alt="Layout V3 (v4.7)">
  <br><br>
  <p align="center"><b>【 NUEVO LAYOUT V3 - Stability Beta 5.0 】</b></p>
  <img src="docs/img/v18_layout1.png" width="45%" alt="Simple Layout Night Mode">
  <img src="docs/img/v18_layout2.png" width="45%" alt="Standard Layout Premium">
  <br><br>
  <p align="center"><b>【 PRESENTACIÓN V5 - ESTABILIDAD BETA 】</b></p>
  <img src="docs/img/v5 español.png" width="90%" alt="Presentación V5">
  <br><br>
  <p align="center"><b>【 GUÍA DEL SISTEMA DE LOGOS V5 】</b></p>
  <img src="docs/img/Instrucciones logos español v5.png" width="90%" alt="Guía de Logos V5">
</div>

---

## 🎯 Funciones Principales

| Función | MT8163 / Junsun | K706 | MTK8259/8667 |
|---|:---:|:---:|:---:|
| Sintonización FM | ✅ | ✅ | ✅ |
| Seek / AutoScan | ✅ | ✅ | ✅ |
| RDS PS (nombre) | ✅ | ✅ | ✅ |
| RDS RT (información) | ✅ | ✅ | ✅ |
| AF / TA / TP | ✅ | ✅ | ✅ |
| DX / Local | ✅ | ✅ | ✅ |
| Stereo / Mono | ✅ | ✅ | ✅ |
| Layouts (V2 / V3) | ✅ | ✅ | ✅ |
| Temas / Night Mode | ✅ | ✅ | ✅ |
| Logos HD (Supabase) | ✅ | ✅ | ✅ |
| Soporte Android Auto | ✅ | ✅ | ✅ |
| Streaming Online | ✅ | ✅ | ✅ |

**5.0.16 “Setup & Stability Hotfix” (abril 2026):** estabilidad de streaming en MT8163 (liberación de `ExoPlayer` coordinada), logos en V3/QS6 sin quedarse “pegados”, volante en segundo plano también en **QS6**, metadatos/notificaciones más coherentes (sin `notify()` si no hay permiso), `getStationInfo` desde UI en segundo plano, recordatorio **HiHack** tras reinicio, **widget** mejorado (logo/banda/seek/mute y mejor UX en launchers sin resize), **backups** de estado (opciones `.ors` o copia completa `.orzip` con `RadioLogos/`, progreso + cancelar + sugerencia de reinicio), e **icono launcher** estilo OEM (`drawable-nodpi-v4`) para evitar pixelado en QS6/NWD. **Pendiente validar en hardware real K706 y MT8163.**

---

## 🏗️ Arquitectura

```mermaid
graph TB
    subgraph "UI Layer"
        MA[MainActivity]
        LCN[Layout Controllers]
        STM[Standard/V3 Layouts]
        SLM[Simple Layout]
    end

    subgraph "Domain Managers"
        PM[PlaybackManager]
        SM[ScanManager]
        HM[HistoryManager]
        PRM[PresetManager]
        RDS[RDSManager]
    end

    subgraph "System & UI Logic"
        TM[ThemeManager]
        NM[NightModeManager]
        LM[LogoManager]
        DM[DialogManager]
    end

    subgraph "Hardware Abstraction"
        RSC[RadioServiceController]
        REI[RadioEngine Interface]
        DEV[DeviceManager]
        HWM[HardwareManager]
    end

    subgraph "Radio Engines"
        K706[K706Engine]
        MT[MT8163Engine]
        QS[QS6Engine]
        MTK[MTK8259_8667Engine]
    end

    MA --> LCN
    LCN --> STM
    LCN --> SLM
    MA --> PM
    MA --> SM
    MA --> HM
    PM --> RSC
    RSC --> REI
    REI --> K706
    REI --> MT
    REI --> QS
    REI --> MTK
    
    style MA fill:#1a1a2e,stroke:#e94560,color:#fff
    style PM fill:#16213e,stroke:#0f3460,color:#fff
    style RSC fill:#16213e,stroke:#0f3460,color:#fff
```

---

## 📱 Hardware Compatible

| Dispositivo | Chip | Motor | Colaborador |
|---|---|---|---|
| JUNSUN V1 Pro / Topway | MediaTek MT8163 | `FM_MT8163` | ✅ |
| Radio K706 / HCN / Vento | K706 + MCU | `FM_K706` | ✅ |
| MTK 8227L / 8259 / 8667 | MediaTek | `FM_MT8259` | 🤝 Csaba Edition |
| Radio NWD G5 | Qualcomm | `FM_QS6` | 🛠️ Beta (rama **QS_NWD**) |
| Otros Android Head Units | Varía | `FM_BASICO` | ⚠️ Solo UI |

---

## 🛠️ Compilación

### Requisitos
- Android Studio Hedgehog+ o SDK 34
- JDK 17
- Gradle 9.x (incluido en wrapper)

### Supabase (obligatorio para compilar)
Desde **v5.0.15**, la URL y la clave **anon** de Supabase no van en el código: añádelas a `local.properties` en la raíz (junto a `sdk.dir`) o exporta `SUPABASE_URL` y `SUPABASE_ANON_KEY`. Plantilla: `local.properties.example`. Guía CI: [`docs/CI_SUPABASE.md`](docs/CI_SUPABASE.md).

### Build
```bash
# Clonar
git clone https://github.com/kapi21/OpenRadioFM.git
cd OpenRadioFM

# Compilar
./gradlew assembleDebug

# APK generado en:
# app/build/outputs/apk/debug/app-debug.apk
```

### Importar dataset Radio-Browser a Supabase (admin)
Para poblar la tabla secundaria `stations_radiobrowser` desde un snapshot como `radiobrowser_stations_20260116_234403.json` (solo países seleccionados), usa el script:

```bash
pip install requests
set SUPABASE_URL=https://<project-ref>.supabase.co
set SUPABASE_SERVICE_ROLE_KEY=<service_role_key>
python tools/import_radiobrowser_to_supabase.py --json radiobrowser_stations_20260116_234403.json
```

### Instalación en Head Unit
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚠️ Problemas Conocidos (Abril 2026)
- **Verificación v5.0.16**: los cambios de la **5.0.16** aún deben probarse en **K706** y **MT8163** en condiciones reales (streaming, volante, logos, notificaciones).
- **Audio Focus (K706)**: ✅ Resuelto. Centralización de foco en RadioManager para evitar cortes por conmutación de canal MCU (Channel 2/4).
- **Volante en segundo plano (K706)**: En muchas ROM, con el **launcher u otra app al frente**, las teclas del volante llegan como `KeyEvent` al foco y la **radio OEM** sigue recibiendo mandos por **MCU/QuickFish**. OpenRadioFM no puede usar ese canal sin integración OEM; a partir de **v5.0.10** el servicio de accesibilidad **Factory Radio Hijacker** puede **capturar y reenviar** esas teclas a la app (activar el servicio en Ajustes → Accesibilidad). Opcional (avanzado): en SharedPreferences **RadioPresets**, `pref_a11y_forward_media_keys=false` desactiva el reenvío (por defecto está activo).
- **Seek por Hardware (K706)**: Interactúa con el volumen en algunos firmwares (pendiente investigación MCU).
- **Layout V2**: Algunos iconos pueden tener áreas de pulsación solapadas.
- **AutoScan (Smart v2)**: ⏸️ **En estudio** por defecto. Desde **v5.0.11**, en los **menús de ingeniería** (pulsación larga en GPS) puedes activar *Modo AutoScan* para usar el botón de escaneo en la UI principal de forma experimental (`pref_dev_autoscan_enabled` en **RadioPresets**).
- **Audio QS6 (Qualcomm / NWD)**: ⚠️ Comportamiento dependiente del firmware; se sigue probando **cambio de fuente** (`ACTION_CHANGE_SOURCE`), foco y rutas de recuperación. Reporta modelo + build si falla el audio tras sintonizar.

---

## 📖 Documentación

- [Manual de Usuario (Español)](_DOCS/manual_usuario.md)
- [User Manual (English)](_DOCS/manual_user_en.md)
- [Руководство пользователя (Русский)](_DOCS/manual_user_ru.md)
- [Compatibilidad de Hardware](docs/HW_COMPATIBILITY.md)
- [Compilación / CI — credenciales Supabase](docs/CI_SUPABASE.md)
- [Ingeniería inversa — radio OEM K706 (`com.android.fmradio.ext`)](docs/ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md)
- [Comparativa motor K706 OpenRadioFM vs radio OEM](docs/COMPARATIVA_K706_OPENRADIO_VS_OEM.md)
- [Inteligencia QS NWD (Qualcomm)](docs/INTELIGENCIA_QS_NWD.md) — incluye **§15 Roadmap motor QS6**
- [Changelog](CHANGELOG.md) · [Changelog (English)](CHANGELOG_EN.md) · [Changelog (Русский)](CHANGELOG_RU.md)
- [Depurado en tres fases](depurado%20en%20tres%20fases.md)
- [Roadmap](_DOCS/roadmap.md)

---

## 🤝 Cómo Contribuir

- **Issues**: Si encuentras un bug o comportamiento raro en tu radio (modelo, firmware, chip, versión de app), abre un *Issue* detallando:
  - Modelo exacto de la head unit (HCN, K706, QS6, MTK8259, etc.).
  - Versión de OpenRadioFM (por ejemplo `v4.9.3_Stable`).
  - Pasos para reproducir el problema y, si es posible, logs o fotos/vídeos.
- **Pull Requests**: Se aceptan mejoras de código, refactors y nuevas traducciones:
  - Intenta mantener el estilo actual (Java para el core; Kotlin bienvenido para piezas nuevas).
  - Acompaña los cambios con una breve explicación en el PR y, si afecta a la UX, con capturas.
  - Para cambios grandes (refactor de motores, arquitectura, etc.), es recomendable comentar primero en un Issue para alinear el enfoque.

---

## 📜 Historial de Versiones

### v5.0.16 (Abril 2026) — `main` — “Setup & Stability Hotfix”
- **ES:** **OpenRadioFM v5.0.16** — “Setup & Stability Hotfix”: streaming MT8163, logos V3/QS6, volante QS6 en segundo plano, MediaSession/notificaciones, `RadioRepository` en UI, HiHack tras reinicio, **widget** mejorado (logo/banda/seek/mute; PS→info; compatibilidad con launchers sin resize), **backups** de estado (opciones `.ors` y copia completa `.orzip` con logos + progreso/cancelar), e **icono launcher** OEM (`drawable-nodpi-v4`) para evitar pixelado. **Pendiente verificación en K706 y MT8163.** Ver `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.16** — “Setup & Stability Hotfix”: MT8163 streaming, V3/QS6 logos, QS6 background steering, MediaSession/notifications, UI-thread `getStationInfo`, HiHack reboot reminder, **widget** improvements (logo/band/seek/mute; PS→info; non-resizable launcher UX), **app-state backups** (options `.ors` and full `.orzip` with logos + progress/cancel), and **OEM-style launcher icon** (`drawable-nodpi-v4`) to avoid pixelation. **K706 + MT8163 verification still pending.** See `CHANGELOG_EN.md`.

### v5.0.15 (Abril 2026) — `main`
- **ES:** **OpenRadioFM v5.0.15** — cierre de línea 5.0.15: widget, AutoScan/i18n/diálogos (ver `CHANGELOG.md`); contribución Supabase más estricta; credenciales Supabase en compilación (`local.properties`); layout V2 presets.
- **EN:** **OpenRadioFM v5.0.15** — release notes: widget, AutoScan/i18n/dialogs (see `CHANGELOG_EN.md`); stricter Supabase community contribution; build-time Supabase credentials; V2 preset column spacing.

### v5.0.14 **Beta** (Marzo 2026) — `main`
- **ES:** **OpenRadioFM v5.0.14 Beta** — indicador de banda (FM1/FM2/FM3/AM1/AM2) y unidad (MHz/kHz) como texto para respetar la tipografía elegida; selector “🔢 Números presets” con estilo por defecto o Tabler (assets). Detalle en `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.14 Beta** — band indicator (FM1/FM2/FM3/AM1/AM2) and unit (MHz/kHz) rendered as text to respect the selected font; “🔢 Preset numbers” selector with Default or Tabler style (assets). See `CHANGELOG_EN.md`.

### v5.0.12 **Beta** (Marzo 2026) — `main`
- **ES:** **OpenRadioFM v5.0.12 Beta** — misma base que 5.0.11; se deja de usar la etiqueta *Stable* en `versionName` / `app_name_internal` y se publica como **Beta** hasta considerar la línea suficientemente madura.
- **EN:** **OpenRadioFM v5.0.12 Beta** — same codebase as 5.0.11; *Stable* is dropped from `versionName` / `app_name_internal` in favor of **Beta** until the line is mature enough.

### v5.0.11 (Marzo 2026) — `main`

- **ES:** **OpenRadioFM v5.0.11** — K706: corrección streaming online (sin `switchToFm` retardado al reiniciar stream); layout simple en pantallas no `sw720dp` (iconos más compactos); menús ingeniería con interruptor AutoScan experimental; seek/preset en segundo plano (v5.0.10 accesibilidad) validado. Detalle en `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.11** — K706 online streaming fix; simple layout tweaks on non-`sw720dp`; engineering AutoScan toggle; background seek/preset (v5.0.10 accessibility) confirmed. See `CHANGELOG_EN.md`.

### v5.0.10 **Stable** (Marzo 2026) — `main`

- **ES:** **OpenRadioFM v5.0.10 Stable** — K706: volante en segundo plano mediante servicio de accesibilidad (captura de `KeyEvent` y reenvío a `RadioMediaService`); sesión de medios y FGS alineados cuando la app no está al frente; `ACTION_FORCE_PLAY` desde `MainActivity` al pasar a segundo plano si aplica. Detalle en `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.10 Stable** — K706: background steering via accessibility (key capture → `RadioMediaService`); MediaSession/FGS tweaks; `ACTION_FORCE_PLAY` from `MainActivity` when going to background when applicable. See `CHANGELOG_EN.md`.

### v5.0.9 **Stable** (Marzo 2026) — `main`

- **ES:** MT8163/HCN: handoff de MediaSession al cerrar streaming online; mandos de volante y sesión más robustos en segundo plano. Ver `CHANGELOG.md`.
- **EN:** MT8163/HCN: MediaSession handoff when stopping streaming; improved steering/session in background. See `CHANGELOG_EN.md`.

### v5.0.8 **Stable** (Marzo 2026) — `main`

- **ES:** **OpenRadioFM v5.0.8 Stable** — icono launcher / logo in-app en máxima calidad (adaptativo + `ic_app_logo`); menú ingeniería con **pulsación larga en GPS** (antes 5 toques); interruptor opcional **MTK8259 mixer v5.0** (Close/OpenRadioCh) para pruebas de mezcla FM/streaming; icono nube **atenuado sin internet**; ajustes layout 3 (reloj / nube) y presets; pista de scroll en el diálogo de ingeniería. Detalle en `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.8 Stable** — launcher / in-app logo quality, engineering menu via **long-press GPS**, optional **MTK8259 v5.0 mixer** toggle, cloud icon dimmed when offline, layout 3 and preset fixes, engineering dialog scroll hints. See `CHANGELOG_EN.md`.

### v5.0.7 **Stable** (Marzo 2026) — `main`

- **ES:** Cierre de la línea 5.0.x con nombre de app **OpenRadioFM v5.0.7 Stable**, streaming K706 sin “robo” de canal FM, y ajustes de iconos (nube / preset) en layout estándar. Incluye todo lo acumulado en builds 5.0.5–5.0.7 (QS6 arranque, K706 frío, streaming). Detalle en `CHANGELOG.md`.
- **EN:** **OpenRadioFM v5.0.7 Stable** branding, K706 online streaming vs FM channel fix, and layout 2 icon strip tweaks. See `CHANGELOG_EN.md`.

### v5.0.6 **"K706 cold start + QS6 parity"** (Marzo 2026) — `main`

- **ES:** K706: restauración de última frecuencia al **primer arranque tras reinicio de unidad** (misma familia de guardas que QS6), y corrección de escala inicial de frecuencia en `K706RadioManager`. Ver `CHANGELOG.md`.
- **EN:** K706: restore last station on **first launch after head-unit reboot** (same guard family as QS6), plus initial frequency scale fix in `K706RadioManager`. See `CHANGELOG_EN.md`.

### v5.0.5 **"Hardening Phase 7 + QS6 startup"** (Marzo 2026) — `main`

- **ES:** Endurecimiento QS6 frente a rebotes del firmware (87.5/87.6, primer preset tras scan), persistencia coherente de última emisora/banda, saneo opcional de prefs bootstrap, y receptor de medios idempotente. Ver `CHANGELOG.md`.
- **EN:** QS6 hardening against OEM frequency bounce (87.5/87.6, first preset after scan), consistent last-station/band persistence, optional bootstrap pref sanitize, and idempotent media receiver. See `CHANGELOG_EN.md`.

### v5.0.4 **"QS NWD Advance + K706 Fixed"** (Marzo 2026) — rama `QS_NWD`

- **ES:** Build de avance centrado en **consistencia QS6/NWD** (transición de frecuencia, logos en presets, PTY, auto-scan de arranque) y consolidación de **fixes K706** (controles de volante/media). Incluye mejoras de UI: Agradecimientos con QR, créditos accesibles desde Acerca y modo opcional NEXT/PREV (seek o presets).
- **EN:** Progress build focused on **QS6/NWD consistency** (frequency transition behavior, preset logos, PTY, startup auto-scan guard) while consolidating **K706 fixes** (steering wheel/media controls). Also includes UI improvements: Acknowledgements with QR, credits from About, and optional NEXT/PREV mode (seek or presets).
- **QS6 — última frecuencia:** en arranque en frío se vuelve a sintonizar `pref_last_freq` cuando difiere del estado local del motor (el motor QS6 no reporta frecuencia “0”, así que antes no se aplicaba la última emisora guardada).
- **K706 — audio al arranque:** recuperación de audio retardada (+450 ms / +1,5 s) y mute unificado vía `PlaybackManager` en layouts simple/minimal para evitar FM silenciada hasta pulsar mute.

### v5.0.2 **fixed** (Marzo 2026) — rama `QS_NWD`
*Corrección acumulada; **no** es un anuncio de release completo (sí sirve como APK de prueba / “fixed build”).*

| 🇪🇸 **Español** | 🇬🇧 **English** |
|---|---|
| Mejor comportamiento al usar la **radio** junto con el **reproductor de música del coche**: al cambiar de pantalla, la música del sistema vuelve a funcionar con normalidad. El **escaneo automático** muestra mejor si la radio está buscando emisoras. Incluye otros **ajustes de estabilidad** en unidades NWD (Qualcomm). | **Better behaviour** when switching between this **FM app** and the **car’s built-in music player** — music plays reliably again. The **auto-scan** control better matches what the tuner is doing. Includes other **stability tweaks** for NWD (Qualcomm) head units. |

### Rama `QS_NWD` — notas técnicas (desarrollo)
*Build orientado a radio **NWD** (`com.nwd.radio.service`).*

- **UI AutoScan**: botón de escaneo sincronizado con el HAL (`onScanStatusChanged`, `onResume`).
- **QS6 / motor**: ajustes en `QS6Engine`, ceda de audio en segundo plano (`releaseAudioFocusOnlyForBackground`), RDS y documentación `docs/INTELIGENCIA_QS_NWD.md`.
- **Ingeniería QS6**: diálogo *Technical Matrix* (pulsación larga en GPS en modo QS6).
- **Logos / datos**: `RadioRepository`, Supabase, `RDSManager` / logos en hardware real.
- **Assets**: guías V5 en `docs/img/`.

### v5.0.1 "K706 hotfix" (22 de Marzo 2026)
*Build de corrección; no sustituye el anuncio de release 5.0.0 — útil para subir **APK debug/firmado** a GitHub como asset “fixed” / pre-release.*

- **K706 — Llamadas entrantes / salientes**:
  - Solicitud en tiempo de ejecución de **`READ_PHONE_STATE`** (Android 6+); sin permiso el sistema no notifica llamadas y la FM podía seguir sonando.
  - Tras colgar: restauración reforzada (**`SetChannel(2)`**, audio, **`requestPlayAudio()`**).
- **K706 / volante y sesión multimedia**:
  - **NEXT / PREVIOUS** del volante y de **MediaSession** / notificación avanzan o retroceden **frecuencia (seek)**, no memorias.
- **Documentación**: comparativa OEM K706, estudio radio OEM, script jadx opcional (`docs/`).

### v5.0.0 "Stability Beta" (21 de Marzo 2026)
- **Hardware Stability & Contributor Support**:
    - **Csaba Edition**: Integración oficial de soporte para MTK 8227L / 8259 / 8667 de la mano de Csaba.
    - **Junsun V1 Pro**: Optimización del motor MT8163 para dispositivos Junsun.
    - **Protección de Red (SST)**: Parche crítico para `NoClassDefFoundError` en Retrofit/Supabase.
    - **Protección AM**: Eliminada la opción de desactivar AM para evitar bloqueos del motor en Topway.
    - **Precisión de Sintonía**: Implementado cálculo manual de pasos de frecuencia en MTK para sintonización exacta.
    - **Instancia Única**: Corregida la duplicidad del motor MT8163 en reconexiones del servicio AIDL.
- **Personalización Premium**:
    - **Light Mode**: Nuevo skin "White" completamente funcional con visibilidad optimizada.
    - **Transparencia Real**: Sincronización de opacidad en todas las tarjetas y layouts.
- **Core Polish**:
    - Fix de crash por recursividad de iconos en el lanzador.
    - Mitigación de parpadeo (flicker) en el texto RDS.

### v4.9.5 "Audio Focus & Sequential Presets" (19 de Marzo 2026) - STABLE
- **Audio Focus K706 (Fix Crítico)**:
    - Rediseñada la lógica de `onAudioFocusChange` para respetar la pérdida permanente de foco.
    - El canal MCU (Channel 4) ahora se libera correctamente al perder el foco, permitiendo que otros reproductores (Spotify, etc.) suenen sin interferencias.
    - Corregido el bucle de auto-recuperación agresiva.
- **Navegación Secuencial de Presets**:
    - Los botones de Favorito Siguiente/Anterior ahora recorren los slots (1-18) de forma numérica.
    - Salto automático de slots vacíos y comportamiento circular por banda.

### v4.9.4 "Streaming & Stability Polish" (19 de Marzo 2026) - STABLE
- **Streaming Online Pro (Icecast & M3U)**:
    - **Resolución de Playlists**: Añadido soporte automático para ficheros `.m3u` y `.pls`. La app ahora extrae la URL real del stream en segundo plano, permitiendo sintonizar emisoras que antes daban error de formato.
    - **Compatibilidad Icecast SSL**: Forzado automático de protocolo **HTTP** en puertos no estándar (8xxx/9xxx). Esto soluciona los fallos de conexión por certificados desactualizados en hardware antiguo.
    - **Mime Type Forcing**: Detección mejorada de streams MPEG (`audio/mpeg`) para URLs que terminan en `/stream`.
- **Fix de Audio Duplicado**:
    - **Protección de Ciclo de Vida**: Corregido bug donde la radio FM se reactivaba al volver a la app mientras el streaming LIVE seguía sonando.
    - **Mute Guard**: El sistema de recuperación de audio ahora respeta el estado del streaming activo para no desmutear la radio física innecesariamente.
- **Estabilidad de Instancia**:
    - **SingleTask Launch**: Configurado `launchMode="singleTask"` en el manifiesto para evitar que algunos launchers OEM creen múltiples instancias de la app al volver desde el escritorio.

### v4.9.3 "Hijacker & Layout Refinement" (15 de Marzo 2026) - STABLE
- **Intent Hijacking (Interceptación de Botón Físico)**:
    - Implementación de `FactoryRadioHijackerService` (Servicio de Accesibilidad) para interceptar el botón RADIO físico en unidades K706 (y similares), permitiendo que OpenRadioFM se abra automáticamente sobre la app de fábrica sin necesidad de Root.
- **Refinamiento Estético Layout 3 (V3)**:
    - **Clean UI**: Los boxes genéricos de control (RDS, bandas, búsqueda) son ahora completamente planos (sin bordes) para un aspecto más moderno.
    - **Skins Focalizados**: Los colores de los Skins (Night, Classic, Orange, etc.) ahora se aplican exclusivamente a las cajas de favoritos (P1-P12).
    - **Neutralidad de Frecuencia**: El recuadro de frecuencia conserva un borde neutro transparente (`bg_glass_card_classic`) independiente del skin seleccionado.
- **Motor MTK8259 Polish**:
    - Optimización del método `forceUnmute()` eliminando el comando redundante de Mute para mejorar la estabilidad del canal de audio al arrancar.
- **Branding**:
    - Reducción del nombre en el lanzador a simplemente "OpenRadioFM" para mayor limpieza visual en el escritorio de la radio.

### v4.9.0 "Community Logo Edition" (14 de Marzo 2026) - STABLE
- **Enfoque Comunitario**: Nueva edición centrada en la base de datos de logos compartidos por la comunidad.
- **Refinamiento de UI Individual**:
    - **Reubicación de Favorito**: El icono de favorito se ha trasladado a la barra superior en ambos layouts principales para una visualización más limpia.
    - **Optimización de Espacio**: Reajustado el tamaño de los iconos de estado y nube (120dp) para garantizar la visibilidad del indicador de favorito.
    - **Señal MT8163**: Corregido bug donde el icono de señal quedaba blanco; ahora se colorea en tiempo real mediante el bucle asíncrono (verde en estéreo, amarillo en mono).
- **Core Stability & Motores**:
    - **Auditoría MainActivity**: Saneamiento de código obsoleto, reemplazo de `NetworkInfo` por `NetworkCapabilities`, mejoras de null-safety en streaming online y eliminación de callbacks duplicados.
    - **MTK8259 Engine Polish**:
        - Fix crítico en `onManualUp`/`onManualDown` que forzaba el salto entre presets de fábrica en lugar de buscar frecuencias (calculado manualmente a intervalos de 100KHz y 9KHz).
        - Desacoplamiento total de `AudioManager` para la lógica de Mute, delegando exclusivamente al SoC `TsCommon` según hardware specs.
        - Fix de Crash al cambiar Layout provocado por callbacks `DeadObject` del RDS polling.
- **Shadow Motor QS6 (Redundancia)**:
    - **Broadcast Monitoring**: Nueva lógica para capturar frecuencias y RDS PS directamente desde el bus del sistema (`ACTION_SEND_RADIO_FREQUENCE_NEW`).
    - **Settings Observer**: Monitorización de `nwd_radio_current_freq` para refrescar la UI independientemente de fallos en el servicio AIDL.
- **Saneamiento Android 13+**: Registro de receptores con flag `RECEIVER_EXPORTED`.
- **Estabilidad General**: Mejoras en la persistencia de datos y eliminación de parpadeos en las actualizaciones de RDS.

### v21.1 "QS6 Shadow Motor & Android 13" (14 de Marzo 2026)
- **Implementación del Shadow Motor (Redundancia QS6)**:
    - **Broadcast Monitoring**: Nueva lógica para capturar frecuencias y RDS PS directamente desde el bus del sistema (`ACTION_SEND_RADIO_FREQUENCE_NEW`), evitando parálisis si el servicio AIDL de Qualcomm colapsa.
    - **Settings Observer**: Monitorización en tiempo real de `nwd_radio_current_freq` para refrescar la UI independientemente de los callbacks remotos.
    - **Comandos Redundantes**: Los cambios de frecuencia ahora se envían también vía Intent por si el proxy AIDL está bloqueado.
- **Saneamiento Android 13+ (API 33)**: 
    - Corregido el regitro de receptores añadiendo el flag obligatorio `RECEIVER_EXPORTED` para permitir la comunicación entre apps de sistema y la radio.
- **Optimización de Rendimiento**:
    - **Anti-Lag**: Implementado filtrado de actualizaciones de frecuencia para evitar el parpadeo de la UI durante cambios rápidos por parte del hardware.
    - **Persistence Refinement**: Corregida la duplicidad de registros del Shadow Motor en los ciclos de vida de la Activity.
- **Estado QS6**: Estabilidad total en UI, sintonía y RDS. El desarrollo de sonido queda en pausa temporal tras la reversión de las pruebas inestables v21.2-v21.5.

### v20.0 Alpha "QS6 Robustness & MTK Volume" (13 de Marzo 2026)
- **Robustez Crítica Motor QS6 (NWD Platform)**:
    - **AIDL Resilience**: Implementación de un wrapper `performAidlCall` que captura `DeadObjectException` y relanza la vinculación del servicio automáticamente.
    - **Auto-Rebind Loop**: Sistema de 3 reintentos de conexión forzada ante fallos críticos del proceso remoto del sistema.
    - **Estabilización de Arranque**: Introducción de un retardo táctico de 500ms en `onEngineReady` para evitar colisiones con el kernel durante la carga del layout.
    - **Persistence Fix**: Gestión inteligente de `unbindService` para mantener el audio fluyendo durante la recreación de la Activity (cambios de orientación).
- **Corrección de Volumen MTK 8259**: 
    - Implementación de `setVolumeControlStream` en `MainActivity` para forzar el control del stream de música por defecto.
    - Integración de gestión de **AudioFocus** en `MTK8259_8667RadioManager`.
    - Solucionado el bug de "doble pulsación" necesaria para mostrar la barra de volumen del sistema.
- **Estabilización de RDS (Feedback Csaba)**:
    - Eliminado el parpadeo del RDS Text en el motor MTK mediante la optimización del ciclo de refresco de texto.
    - Actualizada la interfaz de hardware `ITsCommon.aidl` con soporte para `GetAudioSessionId` y `GetRadioSessionId`.
- **Depuración de Layouts & Clics**:
    - **UI Clearance**: Reajuste de guidelines en el Layout Estándar y V3 (frecuencia al 48%) para evitar el solapamiento físico con botones de control.
    - **Fix de Interrupción**: Restaurados los clics inmediatos en botones AF/TA/TP que anteriormente quedaban bloqueados por el TextView de frecuencia.
    - **Instant Logo Clear**: Los logos se limpian instantáneamente al iniciar una sintonía, eliminando el efecto de "logo pegado" del hardware lento.

### v19.5 "Cloud ID & Premium Layout Refinement" (12 de Marzo 2026)
- **Identificación Única (Cloud Sync)**:
    - **Hardware ID Mapping**: Implementada la utilidad `DeviceMetadataUtils` para asignar un ID persistente a cada unidad (basado en Serial de HW para K706/MT8163 o Android ID/UUID).
    - **Supabase Device Tracking**: La base de datos ahora registra el `device_id` en cada reporte de estación para mejorar la moderación de logos y métricas.
- **Refinamiento Estético Premium**:
    - **Layout V3 Transparency**: El contenedor de RDS RT ahora es 100% transparente, logrando un diseño más limpio integrado en el fondo.
    - **Geometría de Interfaz**: Reducción del tamaño del icono de señal (44dp) y ajuste de altura de la frecuencia en V3 para evitar solapamientos.
- **Estabilización de Motores**:
    - **MT8163 Full Restore**: Recuperada la funcionalidad de RDS RT y nombres de emisora tras la regresión de la sesión anterior.
    - **MTK8259 Polish**: Corregido el intercambio de datos entre PTY y Texto. Se ha fijado el icono de Mute a un estado estático profesional (`radio_mute_n`).
    - **K706 Band Fix**: Resuelto el problema visual donde las bandas AM se mostraban con iconos de FM.

### v4.8.5 "UI Stability & Anti-Flicker Pro" (11 de Marzo 2026)
- **Eliminación de Parpadeo (Fases 2.5 - 2.7)**:
    - **Master Guard de Hilos**: Implementado un sistema de bloqueo reactivo que reduce la creación de hilos en un 90% cuando la radio está estática.
    - **Estabilización SPRD/Unisoc**: Eliminado el jitter periódico de 5s mediante la desactivación del heartbeat forzado y optimización del compositor de hardware.
    - **Notificaciones Inteligentes**: El servicio de medios ahora utiliza guardas de estado para evitar la inundación de notificaciones redundantes en milisegundos.
- **Limpieza de IPC**: 
    - Silenciado de broadcasts del sistema denegados para reducir el ruido en el bus de datos y liberar recursos del hilo principal.
    - Aplicación de guardas visuales estrictas (`IfChanged`) en fondos, filtros de color y recursos de imagen.
- **Depuración de Crashing**:
    - Corregido error crítico de índice fuera de rango al activar el Mute/Unmute desde la notificación.

### v4.8.4 "V5.0 Phase 1: The Core Decoupling" (11 de Marzo 2026)
- **Desacoplamiento Masivo (V5.0 Fase 1)**:
    - **ScanManager**: Extracción completa de la lógica de búsqueda de emisoras de `MainActivity`. Centralización de estados de escaneo y diálogos manuales.
    - **HistoryManager Pro**: Sistema unificado de persistencia con soporte para 5 bandas de FM y 2 de AM, con capacidad ampliada hasta 20 presets por banda.
- **Refinamiento de Mute & Hardware**:
    - **Aislamiento de Polling**: La sincronización forzada de mute con Android ahora solo actúa en motores MTK (Csaba Fix), garantizando la estabilidad total del hardware K706 original.
    - **Mute Visual Fix**: Iconos de mute garantizados para sincronizarse instantáneamente al recuperar el audio desde cualquier fuente externa.
- **Smart Logo & RDS Polish**:
    - **Búsqueda por Nombres Cortos**: Reducción del filtro de seguridad a 2 caracteres para permitir la identificación de emisoras como "RR".
    - **Invalidación de Caché Inteligente**: Al editar un nombre manualmente, la app ahora borra la caché visual y dispara una búsqueda inmediata en Supabase con el nuevo nombre.
    - **RDS PTY Pro (MTK)**: Mejora en `RDSManager` para interpretar correctamente textos de categoría de programa en unidades Topway.

### v4.8.3 "The Cloud Robustness & Server Sync" (11 de Marzo 2026)
- **Capa de Datos de Supabase (Database)**: 
    - **Alineación de Columnas**: Sincronización total con el esquema de base de datos (`logo_url`, `hw_model`).
    - **Tipos de Datos**: Migración del campo frecuencia a `String` (MHz) para coherencia con reportes RDS.
    - **Deduplicación Automática**: Implementada lógica de servidor (PostgreSQL) para evitar emisoras repetidas mediante restricciones `UNIQUE` e `ilike`.
- **Supabase Storage (Cloud Logos)**:
    - **Path Fix**: Corrección del error 400 mediante la desactivación del encoding de Retrofit en las rutas del bucket (soporte para subcarpetas por país).
    - **RLS Policies**: Apertura de permisos para permitir subidas anónimas (`anon`) y lectura pública desde la app.
- **UI & UX Premium**:
    - **Status Indicator**: Punto de estado dinámico (`• Online` / `• Offline`) en ajustes de logos que verifica la conexión con el servidor en tiempo real.
    - **Limpieza de Menús**: Eliminación del botón de prueba manual tras estabilizar la sincronización automática.
- **Estabilidad de Red**: Integración de `HttpLoggingInterceptor` para depuración profesional de peticiones API.

### v4.8.2 "The Technical Matrix & MTK Polish" (10 de Marzo 2026)
- **Unificación del Menú de Ingeniería**: Rediseño masivo de los diálogos de diagnóstico para K706 y MTK8259, adoptando un estilo **"Technical Matrix"** unificado con secciones categorizadas por hardware, RDS y SO.
- **Correcciones Críticas MTK8259 (Feedback de Csaba)**: 
    - **RDS PTY/RT Separation**: Corregida la lógica de filtrado para evitar que el género de música (PTY) se muestre duplicado en el campo de Radio Text.
    - **Smart Mute Sync**: Implementada sincronización activa del icono de Mute. El botón se desmarca automáticamente si el usuario desmutea la radio mediante mandos al volante o el sistema Android.
    - **Hardware Mute Restore**: Restaurado el comando de silencio por HW (`TsCommon.Mute`) para garantizar fiabilidad en todas las variantes de unidades Topway.
- **Build & Quality**: 
    - Solucionados errores de parseo XML por caracteres ilegales (`<`, `>`) en etiquetas de botones.
    - Restaurado el método `updateSignalQuality` en el diálogo K706 para corregir fallos de compilación en `MainActivity`.
- **UI Refinements**: Eliminación definitiva de bordes y mejora de fondos en el **Simple Layout**.
- **Internacionalización**: Verificación y soporte completo para el idioma **Húngaro (Magyar)**.

### v4.8.1 "The Great Decoupling" (9 de Marzo 2026)
- **Refactorización Arquitectónica Total**: `MainActivity` ahora es ligera, delegando responsabilidades en:
    - `HardwareManager`: Gestión de comandos MCU y estado de hardware.
    - `ControlPanelManager`: Control de botones físicos y utilidades.
    - `StandardLayoutManager`: Gestión de los layouts tradicionales V1, V2 y V3.
    - `StationAdapter`: Lógica independiente para listas de estaciones.
- **Modo Noche Premium (Simple Layout)**: Implementación exhaustiva del **Azul Noche** (`#1A237E`) en todos los elementos visuales (frecuencia, RDS, botones, reloj e icono cloud).
- **Reparación de Sintaxis**: Solucionados errores de compilación críticos y referencias rotas entre managers y diálogos.
- **Estandarización de Servicios**: Actualizado el motor MTK8259 a `MainUI` para compatibilidad según requerimientos de Csaba.

### v4.8.0 "Advanced Cloud & UI Customization" (Marzo 2026)
- **Supabase Storage Integration**: Ahora la app sube los archivos de imagen locales (`.png`/`.jpg`) directamente al almacenamiento de Supabase, garantizando que otros usuarios puedan ver el logo compartido.
- **Identificación de Dispositivo**: Implementado `user_id` basado en el `ANDROID_ID` para trazar las contribuciones a la nube y evitar colisiones de datos.
- **Reloj Digital Premium**: Nueva opción en Layout 3 para alternar entre el logo del coche y un reloj digital de gran formato con auto-dimensionado.
- **Soporte MTK8259_8667 (Alfa)**: Iniciada la integración para unidades 8259/8667 con soporte para RDS y cambio de bandas mediante pulsación larga.
- **Reorganización Pro**: Limpieza masiva del repositorio. Logs y scripts movidos a carpetas locales excluidas de Git para un mantenimiento profesional.
- **UI Fix**: Implementación de `Barrier` en Layout 3 para evitar solapamientos entre el icono de nube y el nuevo reloj.

### v4.8 "Cloud Server Release" (Marzo 2026) - BETA
- **Audio Focus Inteligente (K706)**: Centralizada la gestión de foco en el hardware para evitar que el MCU apague la FM al detectar sonido de Android.
- **Internacionalización**: Añadidos **Francés (FR)**, **Alemán (DE)** y **Rumano (RO)**.
- **Fix de Frecuencia**: Eliminado el mensaje "Buscando..." en favoritos al cambiar de emisora.
- **Identidad v4.8**: Actualizados todos los nombres de versión a "Cloud_Server".
- **Recuperación de canal**: `MainActivity` ahora fuerza la restauración del canal FM al volver del segundo plano.

### v4.7.5 "Cloud Intelligence & UI Perfection" (Marzo 2026)
- **Crowdsourcing (Contribución Cloud)**: Los usuarios ahora pueden alimentar la base de datos de logos enviando RDS PS y PI Code automáticamente.
- **Cloud Cache Reset**: Pulsación larga en el icono de nube para invalidar la caché local y forzar la recarga de logos/streams.
- **Localización Completa**: Añadidos manuales y recursos para **Alemán (DE)**, **Italiano (IT)** y **Portugués (PT)**.
- **Fix de Modo Noche**: El icono de actividad de red ahora hereda correctamente el tinte azul noche.
- **Optimización RDS PTY**: Eliminado icono redundante para mejorar la legibilidad de categorías de radio.

### v18.5.1 "QS6 Tuning & AM Optimization" (Marzo 2026)
- **Optimización AM**: Deshabilitada la búsqueda de logos en Supabase para banda AM para evitar bloqueos y mejorar la fluidez.
- **QS6 Seek/Fine Fix**: Corregida la sintonización automática e incremental para que coincida con el paso del hardware.
- **Secuencia de Apagado QS6 (Beta)**: Implementación de la secuencia V18.4 con `ACTION_REQUEST_CHANGE_SOURCE` y desvinculación AIDL proactiva.
- **Limpieza de Recursos**: Mejora en el cierre de hilos de red en `RadioRepository` durante el apagado de la app.
- **Audio Focus**: Refinamiento en la gestión del foco para asegurar el silencio tras el cierre.

### v16.3.0 "Online Logos & Stability" (Marzo 2026)
- **Sistema de Logos Online (Supabase)**: Búsqueda centralizada de logos por PI Code, RDS Name y Frecuencia, con contribución comunitaria automática.
- **Caché Negativa**: Evita reintentos infinitos de búsqueda cuando un logo no existe en el servidor.
- **Menú DEV**: Toggle para activar/desactivar el sistema de logos online en ambos motores (MT8163 y K706).
- **Radio-Browser Estabilizado**: Endpoint migrado a `at1.api.radio-browser.info` para mayor fiabilidad.
- **Sincronización de Hilos**: Contador de actividad de red movido al UI Thread para evitar parpadeos permanentes del icono de datos.
- **Bug Fix**: Corregida lectura de preferencias en `downloadAndSaveLogo` (leía del archivo incorrecto).

### v5.7.0 "Dynamic PS Scaling" (Marzo 2026)
- **Auto-dimensionado de Texto**: La frecuencia/nombre de emisora ahora se escala dinámicamente en ambos layouts para evitar recortes.
- **Optimización de Espacio**: Reducción de márgenes laterales y ajuste de fuente mínima a 20sp para nombres extra largos.

### v5.6.0 "NighMode Polish" (Marzo 2026)
- **UI NightBlue**: El botón PowerOff ahora hereda el color azul noche en el modo nocturno.
- **PTY Agrandado**: Icono y texto de Tipo de Programa un 30% más grandes en Layout 2.
- **Clean UI**: Eliminación total de placeholders "Sin datos RDS RT" para una interfaz vacía más elegante.

### v5.5.0 "Manager & RDS Architecture" (Marzo 2026)
- **Refactorización V5.5**: Creación de `PlaybackManager` (Audio/Mute) y `DeviceManager` (Hardware/Power) para desacoplar `MainActivity`.
- **RDS PS Dinámico**: Sustitución inteligente de la frecuencia por el nombre de la emisora (RDS PS) o nombre personalizado.
- **Optimización Layout 2**: Reestructuración de PTY y alineación de iconos con mayores tamaños para mejorar la visibilidad.
- **Limpieza Estructural**: Eliminación de placeholders redundantes y directorios vacíos.

### v5.2.0 "Power & Focus Integration" (Marzo 2026)
- **Botón de Apagado**: Implementado en todos los layouts para un cierre seguro y liberación inmediata del canal de audio.
- **Recuperación de Audio Focus**: Sistema de autocuración mejorado para Spotify y llamadas.
- **Unificación de Interfaz**: Añadido `closeDevice()` a la capa de abstracción `RadioEngine`.

### v4.7.0 "Car & Audio Integration" (Marzo 2026)
- **Soporte Android Auto**: Integración completa mediante `MediaSession` y `MediaBrowserService`. Los favoritos ahora aparecen como una lista navegable en el coche.
- **Refactorización Core**: Modularización de `MainActivity` mediante `NightModeManager`, `HistoryManager` y `PresetManager`.
- **Media Control**: Comando de pausa/play mapeado al botón de Mute para controles remotos y volante.
- **Limpieza de Recursos**: Optimización drástica de `onDestroy` para evitar fugas de memoria en head units con poca RAM.

### v4.6.1 "Stable Integration" (Marzo 2026)
- **Navegación Hardware:** Control de emisoras favoritas directamente desde mandos al volante y botones de hardware (MCU K706 / Reflexión MT8163).
- **Easter Egg Hacker:** Nuevo diálogo de créditos premium con la imagen del desarrollador Jimmy80.
- **Barra de Estado Opcional:** Configuración para mostrar/ocultar la status bar de Android en el Layout 2.
- **Modo Noche Total:** Tintado unificado para todos los indicadores (ST, PTY, AF, TA, TP).


### v4.6 "Beta Integration" (Febrero 2026)
- **Dual Hardware:** Unificación de motores MT8163 y K706 en una sola APK.
- **Soporte QS6 G5:** Integración inicial para plataformas NWD G5.
- **RDS AF/TA/TP:** Funcional en ambos motores con iconos interactivos.
- **Localización:** Añadidos idiomas Rumano, Ucraniano y Serbio.
- **DX/Local:** Toggle funcional para K706 con feedback visual.
- **Layout V3:** Corrección del desplazamiento causado por el icono Stereo.
- **Fuente Formula 1:** Integración tipográfica premium para la frecuencia.
- **Bug Fix:** Corrección de la frecuencia "00.0" en MT8163 durante el arranque.

<details>
<summary>Versiones anteriores</summary>

### v4.5.1 "Server Test"
- Indicador de señal dinámico (verde/amarillo/rojo)
- Layout 3 refinado con Glass Mode
- PTY multilingüe

### v4.0 "Global Edition"
- Multi-Hardware Engine (HCN, MTK, TS, SYU)
- Night Mode con programación horaria

### v3.0 "The Car Experience"
- Rediseño completo de interfaz horizontal (V3)
- Glassmorphism y menús premium

### v2.0b
- Interfaz Glassmorphism y fondos personalizados

### v1.0b
- Versión inicial con sintonización básica y logos
</details>

---

## 📄 Licencia

```
Copyright 2025-2026 Jimmy80

Licensed under the Apache License, Version 2.0.
See LICENSE file for details.
```

---

**Desarrollado con ❤️ por Jimmy80 para la comunidad Android Head Unit.**
