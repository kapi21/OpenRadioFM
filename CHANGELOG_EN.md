# Changelog (English)

Spanish version: [`CHANGELOG.md`](CHANGELOG.md)

---

## [5.0.12 (Beta)] - 2026-03-27
*Versioning: replace the “Stable” label with “Beta” in visible naming and active docs until release quality is consolidated.*

### Changed
- **Branding / versioning**: `versionCode 24`, `versionName 5.0.12 (Beta)`; `app_name_internal` **v5.0.12 Beta** (no functional changes vs 5.0.11).

---

## [5.0.11 (Stable)] - 2026-03-28
*K706: online streaming matches MCU routing; simple layout on non-sw720dp; engineering menus with experimental AutoScan toggle.*

### Fixed
- **K706 — FM audible while “streaming”**: starting a stream called `stopStreamInternal(false)`, which still scheduled `switchToFmAudio()` ~150ms later and forced `SetChannel(2)` after `SetChannel(4)`. FM recovery after stop now runs only on explicit user stop (`stopStream()`), not on internal cleanup before a new start (`OnlineStreamManager`).

### Added
- **Engineering menus** (MT8163/MTK8259, K706, QS6): *AutoScan mode* switch (`pref_dev_autoscan_enabled`, default off); when on, the main scan button uses `ScanManager.toggleAutoScan` instead of the “under study” toast. `DevAutoscanToggleHelper`, `MainActivity.applyDevAutoScanButtonState()`.

### Changed
- **Simple layout** (`layout/activity_simple_radio.xml`, not `sw720dp`): slightly smaller cloud / seek / mute icons and `match_parent` button row to avoid overflow with newer icon packs.
- **Version**: `versionCode 23`, `versionName 5.0.11 (Stable)`; `app_name_internal` **v5.0.11 Stable**.

---

## [5.0.10 (Stable)] - 2026-03-27
*K706: steering-wheel keys in background via accessibility service; MediaSession/FGS behavior when the app is not in foreground.*

### Added
- **K706 / accessibility**: `FactoryRadioHijackerService` intercepts steering `KeyEvent` when the launcher or another app is in front (OEM FM gets MCU/QuickFish; OpenRadioFM does not) and forwards `ACTION_MEDIA_BUTTON` to `RadioMediaService`; `accessibility_service_config.xml` uses `requestFilterKeyEvents` and no package filter for global key capture.
- **Preferences**: `pref_a11y_forward_media_keys` (default on) to disable forwarding if undesired.

### Changed
- **MainActivity**: `sMainActivityStarted` / `sK706WheelBridgeActive` so forwarding only runs on K706 with the app in background.
- **RadioMediaService**: on OEM audio loss on K706, keep session playing and FGS when applicable; `onStartCommand` avoids dropping FGS in that case; `ACTION_FORCE_PLAY` from `MainActivity.onStop` when not muted and not streaming.
- **RadioServiceController**: `isK706Mode()` for the above.
- **Strings**: accessibility service description (EN/ES) aligned with media key capture.
- **Version**: `versionCode 22`, `versionName 5.0.10 (Stable)`; `app_name_internal` **v5.0.10 Stable**.

---

## [5.0.9 (Stable)] - 2026-03-27
*MT8163/HCN: MediaSession handoff when stopping online streaming; more robust steering-wheel routing in background.*

### Added
- **MT8163**: `ACTION_MT8163_FM_HANDOFF` / `ACTION_MT8163_FM_HANDOFF_COMPLETE` in `RadioMediaService` to drop session state before FM reconnect (fewer OEM force-stops); `OnlineStreamManager` triggers handoff and shows a localized toast when streaming stops (hint to fully restart the app if FM misbehaves).
- **Media / steering**: explicit `setMediaButtonReceiver` to `MediaButtonBootstrapReceiver`; `ACTION_FAST_FORWARD` / `ACTION_REWIND` mapped like NEXT/PREV; unified `handleSteeringSkip` with cold-start queueing.

### Changed
- **MT8163Engine / RadioServiceController / MainActivity**: window blocking HCN bind after streaming, deferred `requestPlayAudio`, reconnect coordinated with session handoff.
- **Strings**: `mt8163_stream_stopped_restart_hint` in all locales; removed QS6 firmware notice strings/dialog; `app_name_internal` **v5.0.9 Stable**; `versionCode 21`, `versionName 5.0.9 (Stable)`.

---

## [5.0.8 (Stable)] - 2026-03-25
*Stable release: launcher/logo quality, engineering menu access and MTK8259 mixer toggle, cloud UX when offline, layout fixes.*

### Added
- **MTK8259/8667**: engineering menu toggle *v5.0 mixer compatibility* (`pref_mtk8259_v5_stream_mixer_compat`): legacy path using only `CloseRadioCh` / `OpenRadioCh` for FM vs streaming mix testing (vs. current path with `EnterMode` / explicit mute).

### Changed
- **Engineering menu**: open with **long-press GPS** on all engines (replaces 5 taps in ≤3 s). Dialog: scroll hint, visible scrollbar, `[ DEV_TOGGLES_HW ]` section, `MODE` log shows real `FmMode` + engine (removed hardcoded `MT8163_DIAGNOSTIC_CLONE`).
- **Cloud icon** (online logos enabled): **dimmed** when offline (~0.38 alpha); when online, previous behavior (blink on Supabase activity).
- **Launcher / UI**: adaptive icon with dedicated foreground layers; `ic_app_logo` in UI; high-quality mipmaps.
- **Layout 3 / presets**: cloud vs clock overlap reduced; preset logo refresh when retuning.
- **Branding & versioning**: `app_name` fixed to **OpenRadioFM** (launcher, all locales); `app_name_internal` keeps internal visible version text; `versionCode 20`, `versionName 5.0.8 (Stable)`.

---

## [5.0.7 (Stable)] - 2026-03-24
*Stable release: app label, About dialog, and docs aligned with `5.0.7 (Stable)`.*

### Fixed
- **K706 — online radio shows UI but FM keeps playing**: while the stream was buffering (and on other `setMute(false)` paths), `PlaybackManager` called `enforceAudioRecovery()` → `SetChannel(2)`, undoing the Android media route (`SetChannel(4)`) used by ExoPlayer. Recovery to FM is now skipped when the engine reports active streaming; `refreshRadioStatus` treats **loading** as streaming (not only `isPlaying`); `K706RadioManager` skips `enforceAudioChannelRecovery` and channel heartbeat while `mIsOnlineStreamingActive`.

### Changed
- **RadioEngine**: default `isOnlineStreamingActive()`; implemented on K706, MT8163, QS6.
- **Layout 2**: smaller cloud icon; preset + cloud right-aligned in `boxIcons` with reserved slots (`Space` + `INVISIBLE` for preset slot).
- **Launcher branding**: `app_name` / `app_name_internal` set to **v5.0.7 Stable** (all locales).
- **App versioning**: `versionCode 19`, `versionName 5.0.7 (Stable)`.

---

## [5.0.6 (K706 cold start + QS6 parity)] - 2026-03-24

### Fixed
- **K706 — first launch after head-unit reboot**: startup tuning no longer relies only on `getCurrentFreq() <= 0` (the engine could report a frequency different from the saved one without applying `tune`). Logic is now aligned with QS6: compare against `pref_last_freq`, reinforce tuning ~1.4s after init, 87.5/87.6 persistence guards, and optional bootstrap pref sanitize (`pref_k706_bootstrap_sanitized`).
- **K706RadioManager**: initial `mCurrentFreq` corrected from **8750** to **87500** (same OpenRadioFM ×1000 scale as the rest of the MCU → UI path).

### Changed
- **App versioning**: `versionCode 17`, `versionName 5.0.6 (K706 cold start + QS6 parity)`.

---

## [5.0.5 (Hardening Phase 7 + QS6 startup)] - 2026-03-24
*Merge of `hardening/phase-7-stability` into `main`: QS6 startup stability and playback tweak.*

### Fixed
- **QS6 / NWD — startup on last station**: hardened against OEM callbacks that re-inject 87.5/87.6 MHz or the first native preset after unexpected auto-scan; explicit-band `tuneWithBand`, re-assert after stopping scan, protection of app-requested tuning and restore from a recent stable frequency; `SRC=` log tags for origin tracing.
- **QS6 / NWD — persistence**: do not persist 87.x as last station unless the user explicitly tuned there; startup and power-off guards so late callbacks do not overwrite `pref_last_freq` / `pref_last_band`; one-time sanitize of corrupted bootstrap preferences.
- **Playback**: **idempotent** media button receiver registration to avoid duplicate handlers when the activity resumes.

### Changed
- **App versioning**: `versionCode 16`, `versionName 5.0.5 (Hardening Phase 7 + QS6 startup)`.

---

## [5.0.4 (QS NWD Advance + K706 Fixed)] - 2026-03-23
*Progress + fixed build (not a major release).*

### Fixed
- **QS6 / NWD — station transition**: reduced transient carry-over of previous station PS/logo when switching frequency or presets quickly.
- **QS6 / NWD — presets**: improved per-slot logo/text consistency by rejecting stale async callbacks.
- **QS6 / NWD — PTY**: added PTY persistence and per-frequency fallback to avoid showing "No PTY" when HAL events were received.
- **QS6 / NWD — startup**: spontaneous startup auto-scan is blocked while manual autoscan button behavior remains intact.
- **QS6 / NWD — last frequency**: on cold start the tuner retunes to saved `pref_last_freq` when it differs from the engine’s local state (previously `getCurrentFreq()` was never ≤0 so the last station was not restored).
- **K706 / controls**: steering wheel/media fixes are preserved and stabilized for both foreground and background paths.

### Added
- **Hardware Settings**: new steering `NEXT/PREV` mode selector:
  - `Seek (stations)`
  - `Preset (memories)`
- **UI**:
  - **👏 Acknowledgements** section in Premium Customization.
  - clickable `Radio Android España` text opening a QR dialog.
  - one-tap `dialog_credits` access from the About logo.

### Changed
- **App versioning**: `versionCode 15`, `versionName 5.0.4 (QS NWD Advance + K706 Fixed)`.
- **README**: updated with bilingual `v5.0.4` summary and history entry.
- **Hardening / stability pass**: safer lifecycle/resource cleanup in `RadioMediaService` and `RadioServiceController`; non-blocking Supabase activity indicator flow in `RadioRepository`; concurrency-safe cache/pending tracking; reduced network log verbosity (`DEBUG=BASIC`, `RELEASE=NONE`).

---

## [5.0.2 fixed (QS6 / NWD)] - 2026-03-22
*Fix build — not a full release announcement.*

### Fixed
- **QS6 / NWD**: when the app goes to the background it stops fighting the system music player for audio focus (`releaseAudioFocusOnlyForBackground`); the built-in music player plays reliably again.
- **Auto-scan**: scan button stays in sync with the real tuner state (`ScanManager` + `MainActivity`).

### Added
- **QS6 Engineering**: Technical Matrix dialog (GPS ×5 easter egg).
- **Docs / assets**: `INTELIGENCIA_QS_NWD.md`, V5 images under `docs/img/`.

### Changed
- **QS6Engine**, **RDSManager**, logos (`RadioRepository`, `SupabaseLogoSource`, `LogoManager`).

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
