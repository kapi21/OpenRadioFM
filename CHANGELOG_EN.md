# Changelog (English)

Spanish version: [`CHANGELOG.md`](CHANGELOG.md)

---

## [5.0.1 (K706 hotfix)] - 2026-03-22
### Fixed
- **K706 — Phone calls**: Runtime **`READ_PHONE_STATE`**; **`PhoneStateListener`** registered only when permission is granted; on hang-up, **`requestPlayAudio()`** is called to restore FM.
- **K706 — Steering wheel / MediaSession**: **`MEDIA_NEXT`** / **`PREVIOUS`** and **`RadioMediaService`** (skip / pending queue) use **frequency seek**, not next/previous preset.

### Added
- **Docs**: `COMPARATIVA_K706_OPENRADIO_VS_OEM.md`, OEM radio study updates; optional script `scripts/k706_jadx_decompile.bat`.

---

## [5.0.0 (Stability Beta)] - 2026-03-21
### Added
- **MTK8259 stability (critical)**:
    - New manual frequency step system to avoid tuning errors on Topway.
    - Removed “Disable AM” option in Premium Settings to prevent infinite hardware loops.
    - Mute sync delegated exclusively to **`TsCommon`** for reliability.
- **Light mode (White skin)**: Full “White” skin with guaranteed visibility across UI and fonts.
- **Uniform transparency**: Consistent opacity on cards in Layout 2 and Classic skin.
- **Engineering Mode v2**: “Matrix style” log terminal and root permission checks.

### Fixed
- **MT8163 initialization**: Duplicate instance in `onServiceConnected` fixed via **`updateService`**.
- **Recursive icons**: Crash from recursive launcher icon reference fixed.
- **RDS flickering**: RDS text buffer tuned to reduce flicker on MTK head units.
- **Supabase stability**: Replaced `catch (Exception)` with `catch (Throwable)` in network paths to avoid `NoClassDefFoundError` (ClassValue) on Android 9.
- **Service recovery**: Better recovery after unexpected remote process death.

---

## [4.9.5] - 2026-03-19
### Added
- **Engineering Mode dashboard** (hidden: GPS ×5):
    - **RF telemetry**: SQI, stereo/mono, LOC/DX, estimated RSSI.
    - **RDS inspector**: PI, PTY, PS, AF list, sync status.
    - **Asset diagnostics**: `RadioLogos`, backgrounds, car logos.
    - **System info**: model, board, **root** check.
    - **Interactive tuner**: `<` / `>` with live signal feedback.
    - **Live terminal log**: kernel events (TUNED, RDS_UPDATE, STEREO_LOCK).
    - **Data Management**: factory reset favorites, clear history.
- **Categorical PTY icons**: Visual category hints from RDS PTY codes.

---

## [4.0.0] - 2026-02-09
### Added
- **Dynamic backgrounds (Global Edition)**: Layout 3 blur engine; background updates even when central logo is hidden.
- **Signal quality tinting**: Signal icon colors by reception (green / yellow / red).
- **Expanded Layout 3 (horizontal)**: 8-button bar, Android settings shortcut, sidebar for wide displays.
- **Save/Load favorites**: `.fav` backups in `RadioLogos`.

### Fixed
- **Layout 2 stability**: RDS placeholders stay visible when empty.
- **Night mode**: Unified “Night Blue” tint for frequency, band, signal, RDS.
- **State persistence**: `onSaveInstanceState` for favorites/signal across layout changes.
- **Icon alignment**: `fitStart` / `fitEnd` standardized.
- **Language selector**: Restart loop fixed when switching ES/EN/RU.

### Changed
- **Icon padding**: 28dp on secondary controls (GPS, Settings) in Layout 2.
- **PTY**: Simplified display for weak-signal stability.

---

## [3.0] - 2026-02-08
### Added
- Initial **Layout 3** (premium horizontal).
- **Car brand logo** customization.

### Internationalization
- Full UI in **Spanish, English, Russian**.
- Manual language selector in Premium menu.
- Instant apply via activity restart.

### Favorites
- Save/load lists; **`.fav` (JSON)** under `/sdcard/RadioLogos`.
- Stores frequency, preset slot, custom name, timestamp.

### Visual (V2 & V3)
- Layout V2 column alignment; quick-access buttons.
- **Night Blue** tint on band icons and MHz labels in night mode.
- Smaller icon padding (18dp) for cleaner look.

### Other
- **V2**: Settings shortcut; favorites save/load shortcut.
- **Gestures (beta)** marked experimental.
- Build fixes; multi-arg format strings corrected.

---

## v3.0 “The Car Experience” (February 2026)
Major update focused on in-car UX.

### Highlights
- **Layout 3 (horizontal)**: 1024×600-oriented; large icons, gallery-style logos.
- **Car logo**: `/sdcard/RadioLogos/car_logo.png`.
- **Band icons**: FM1/FM2 as graphics + `BAND` control.
- **Premium customization**: long-press EQ/Settings hub.
- **Dynamic ** blurred background from station logo (V2/V3).
- **Fonts**: System, Bebas, Digital, Inter, **Orbitron**.
- **GPS quick launch**: Maps, Waze, etc.

### Technical / UI
- Dialog dim (70%) for readability.
- Border tinting by layout.
- Preset text **19sp** in horizontal layout.
- Decimal **108.0** display.
- Full-width bottom row for driving.
- **1024×600** layout tweaks.
- **i18n**: ES / EN / RU everywhere.

### Bug fixes
- Logo persistence via **MediaScanner**.
- **SEEK** direction (left/right) aligned with wheel logic.
- Background mode persistence.
- Fullscreen / status bar in landscape.
- Factory test menu hidden behind GPS ×5.

---

## v2.0b (January 2026)
- Presets expanded **6 → 12** per band.
- Custom **`background.jpg/png`**.
- **Glassmorphism** buttons.
