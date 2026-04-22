# Changelog (English)

Spanish version: [`CHANGELOG.md`](CHANGELOG.md)

---

## [Unreleased] - MCU2

### K706 Root Edition (`K706_Root` branch, April 2026)
- **Magisk (`magisk/K706_Root/`)**: on each boot disables `com.android.fmradio.ext` and **patches** XML under `/data/data/*/shared_prefs` that reference the OEM shortcut (several component string variants; package/class split); `*.bak_orf` backups; `uninstall.sh` restores prefs and re-enables OEM.
- **Magisk — Windows build**: `magisk/build_k706_root_zip.bat` forces **LF** line endings before zipping; `.gitattributes` sets `eol=lf` under `magisk/`.
- **K706 / OEM widget**: `com.qf.radio.update_action` is also sent to the **resolved HOME package** (e.g. `com.android.launcher.gradient.black`), in addition to `movablecell` and `com.android.auto.autohome` (`K706Engine`, `WidgetBroadcastManager`).
- **`LauncherIntentUtils`**: moved to `com.example.openradiofm.util` for engine use.
- **Logos / Glide**: avoid crash on failed logo load by posting fallback onto the main `Handler` outside Glide’s `RequestListener` callback.
- **Docs**: `HANDOFF_K706_ROOT.md` (handoff, short roadmap, ADB). See also `K706_ROOT_CHECKLIST.md` and `magisk/K706_Root/README_K706_ROOT_MAGISK.md`.
- **Open**: Magisk App **unzip** error in some setups; **Level A** in-app root assistant per checklist.

### FYT / Teyes — OEM intent-based engine (April 2026)
- **`FYTOemEngine`**: new **FYT/OEM** engine (package `com.syu.radio`) with no root/AIDL: `tune` via deep‑link `radio://tune?freq=…` and **Prev/Next** via `startService` to `com.syu.broadcast.MyService` (`com.syu.radio.prevservice/nextservice`). Includes auto-detection in `RadioServiceController` (uses `sys.fyt.platform` when available).

### QS6 / NWD — cold start, rebind & stability (April 2026)
- **`QS6Engine`**: post‑reboot *warm-up rebind*: if no RX arrives within ~2.2s (callbacks/settings), retry `connect()` and force a `pollNwdSettingsAndFire()` so state is picked up without opening the OEM radio UI.
- **`NWDTunerAdapter`**: AIDL reconnect with **backoff**, explicit `startService()` warm-up (no UI), and `linkToDeath` so a dead binder (or callback registration failures) triggers an automatic re-bind.

### Launchers / MediaSession — initial metadata (April 2026)
- **`RadioMediaService`**: delayed **initial metadata publish** on startup (avoids “session is there but metadata=null” in launchers such as Agama) and expanded artwork-grant allowlist for common car launchers.

### UI — smoother seek/scan + station rename (April 2026)
- **`MainActivity` / `ScanManager`**: UI‑only “optimistic” frequency ticker while `seekUp/seekDown` and during scanning; automatically stops when the real tuned frequency arrives or scanning ends.
- **`DialogManager`**: new **Edit station name** dialog (save / restore original) with immediate UI+preset refresh and logo cache invalidation.

### Startup performance (April 2026)
- **`MainActivityBootstrap`**: defer heavy work (fonts, icon pack, `MediaSessionManager.connect`, media receiver registration, and late bootstrap phase) to the next UI tick to reduce “Skipped frames” on some head units.

### QS6 / NWD — RDS, System mirror & OEM loops (April 2026)
- **Field workaround**: if RDS/tuner state drifts from NWD firmware expectations, **confirmed approach** is to align RDS (and related OEM toggles) via the **head unit’s native radio settings** while app/KernelService paths mature. See `README.md` (*Known issues*) and `QS6_MCU_KERNELSERVICE_INFORME.md`.
- **`QS6Engine`**: during **slow AutoScan**, **AIDL** is preferred for `tune`/`seek` (`setAutoScanOemPreferred`, wired from `ScanManager`); outside AutoScan, **MCU-first** remains where applicable. Clears **RT** when frequency/band changes on AIDL callbacks. Optional `Settings.System` mirror guarded by `canWrite` (M+) with a **single warning** if write permission is missing.
- **`MainActivity`**: **~280 ms coalescing** of heavy `handleFrequencyChange` work to damp OEM callback/broadcast bursts (unstable frequency, stuck PS).
- **`WidgetBroadcastManager`**: **~320 ms coalescing** in `sendUpdate` against fast launcher/HAL re-injection.
- **`StatusRefreshCoordinator`**: **RDS reset**, logo clear, and **MHz UI refresh** run **before** enqueueing `getStationInfo` on the background executor (fixes racing the old PS back onto the frequency line after zapping).
- **`RadioRepository`**: **`pickBestSupabaseRow`** when the API returns multiple `ps_name` matches; **frequency+country fallback** if custom/PI/ps_name returned nothing; safer frequency parsing on Supabase rows (MHz vs kHz).
- **`MT8163Engine`**: post-streaming HCN reconnect goes through **`ACTION_MT8163_FM_HANDOFF`** plus delayed bind (**~550 ms**), waiting out the OEM block window when needed; avoids competing with `MainActivity` and ROM `forceStop` edge cases.
- **`MainActivity` / MT8163**: removed `mHcnPostStreamReconnectRunnable`; the post-streaming window is owned by the engine.
- **`AndroidManifest`**: optional `WRITE_SETTINGS` (with `tools:ignore`) for the QS6 `Settings.System` mirror.

### MT8163 / HCN — Agama launcher & MediaSession (April 2026)
- **Shared engine with `RadioMediaService`**: after the HCN bind in `MainActivity`, `MT8163Engine` is registered in `RadioServiceController`; the service calls `start()` only to deliver `onEngineReady` **without** a second `bindService` to `com.hcn.autoradio` (avoids force-stop on some OEM ROMs).
- **External controls**: seek / preset from launchers such as **Agama** (and the media session) get a non-null `mEngine` instead of being no-ops.
- **`MT8163Engine.getCallback()`**: composite callback with the UI (same pattern as K706/QS6).
- **`release(false)`**: calls `clearSharedLocalEngineIfSame` when tearing down the engine.
- **Limitation**: if the app has never been opened in that session, there is no shared engine yet; the widget/session stays inactive until that first bind (by design).

### K706 / Android Auto + Spotify + on-device diagnostics (Z-Link; follow-up TBD)
- **`RadioActivityFileLogger`**: stable file log (`commit` for filename and flag); periodic **`TICK`** heartbeat; optional **`logcat -d`** dump from K706 engineering UI. `LifecycleCoordinator` feeds **`uiResumed`** into ticks.
- **Engineering menus**: file-log toggle relies on **`onToggleChanged`** only (no duplicate `apply`). K706: logcat dump button + i18n key **`eng_dev_logcat_dump_button`**.
- **`K706RadioManager`**: **`LOSS_TRANSIENT`** treats AA voice as not GSM (`VOICE_SESSION_AA_OR_TTS`); playback reflection / `APLAY_*` / `AUDIO_FOCUS` logging; weak ref for TICK snapshots.
- **Focus vs `getCurrentFreq` race**: on real **`AUDIOFOCUS_LOSS`**, clear **`mIsAudioFocusHeld`** and **`mAllowImplicitFmRecoverFromPoll`** at the **start** of the `try` (before MCU work) so Binder threads do not “recover” with stale `held=true`.
- **Spotify / AA**: if channel is **4** and **`isMusicActive()`** or mux competition, do not force FM; optionally sync logical focus. **`getCurrentFreq`** polls **`checkAndRecoverAudio`** whenever **`mUserWantsFmAudio`** so FM can resume when external media stops without relying only on **`AUDIOFOCUS_GAIN`**. Heartbeat uses **`enforceAudioChannelRecovery()`** instead of naked **`SetChannel(2)`**.
- **`K706Engine.switchToFmAudio`**: calls **`requestPlayAudio()`** for a full FM sequence when returning from UI.
- **Still open (tracked for later)**: closer match to **QS6-style ducking** (AA prompts without harsh permanent LOSS); validate on K706+Z-Link with `RadioLogos` logs. **MT8163/QS6** remain a different stack (HAL/AIDL vs MCU mux).

### K706 / Android Auto & navigation voice (Zlink)
- **`K706RadioManager`**: “Glitch protect” that re-armed FM after `AUDIOFOCUS_LOSS` / `LOSS_TRANSIENT` is **skipped** when `AudioManager.isMusicActive()` (Maps TTS / another app is playing), matching OEM behavior (yield mux). **`mAutoRecoveryRunnable`** now calls `requestAudioFocus(false)` so it **does not** extend the 2.5s anti-LOSS window (which kept fighting guidance audio).
- **`RadioMediaService`**: on OEM broadcast **`EVENT_LOSS_TRANSIENT`**, **do not** call `refreshSteeringMediaSessionAndForeground()` (it forced PLAYING+FGS and focus while navigation wanted the channel).

### K706 / shared engine & AutoScan UI
- **`CompositeRadioEngineCallback`**: forwards **`onHwAutomationEvent`** to both delegates (hardware automation events are no longer dropped when UI + service share the engine).
- **K706 selective scan dialog**: on dismiss, restores the **previous** `RadioEngineCallback` (e.g. `Composite` UI + `RadioMediaService`) instead of only the coordinator.
- **AutoScan icon**: after slow preset overwrite scan, the MCU can emit spurious `onScanStatusChanged(true)` and restart rotation; UI now **filters** that for ~2.2s after slow autoscan completion (`adjustEngineScanningForAutoScanUi`), **`stopAutoScanAnimation`** is hardened (`animate().cancel()`, `clearAnimation()`, `rotation=0`), and **`LifecycleCoordinator.onResume`** uses the same rule.

### K706 / audio recovery
- **Fewer FM micro-dropouts (K706)**: when `RPC_GetChannel` already reports **FM (2)**, `enforceAudioChannelRecovery()` skips the full ritual that started with `setMute(true)` (it collided with `PlaybackManager.setMute(false)` → `enforceAudioRecovery`). `startFmAudioSequence(fast)` skips the leading mute when the channel is already 2. A real **4→2** route change still runs the full sequence when needed.
- **Future review**: if a click remains on **mux 4→2** after BT/QF stack events, consider **debouncing** or a **grace window** after `abandonCustomAudioFocus` before forcing `SetChannel(2)` (trade-off: slower recovery).

### UI / dynamic background
- **`ivDynamicBackground`**: **fitCenter** in default and `sw720dp` layouts; `LogoManager` decodes to screen size (cap **1600 px** on the long edge) and uses Glide **fitCenter** to avoid centerCrop-style clipping.

### Version (MCU2)
- `versionCode` **39**, `versionName` **5.1.7**.

---

## [5.2.0] - 2026-04-12
### QS6 (Nowada) Critical Stabilization
- **Master Mode (Independence)**: Direct writes to `Settings.System` (`nwd_radio_current_freq`, `ps_data`) to sync the MCU without needing the native app.
- **Clean Teardown**: `ACTION_EXIT_ARM_FM_RAIDO` signal implemented to force audio stop on app close.
- **Mute Fix (Stabilized)**: Resolved issue where Mute failed due to MCU interference. Silence is now guaranteed by forcing `SOURCE_ANDROID` and disabling the radio backup service in `Settings.System` (`KEY_NWD_RADIO_BACK_SERVICE_ON`), ensuring the FM audio channel remains closed until Unmute.
- **System Sync**: Immediate update of Nowada registry keys on source change, preventing the system from unexpectedly reverting the audio state.
- **Lifecycle Management**: `RadioMediaService.onDestroy` sync with `QS6Engine.release` to free hardware.

---

## [5.1.3] - 2026-04-12
*Patch on top of 5.1.2 (`versionCode` **35**, `versionName` **5.1.3**).*

### Added
- **Bar signal meter** (`pref_signal_meter_bars`): toggle under Premium settings → **Audio & screen**; on **Layout V2/V3** replaces the classic icon with **5 segments** in the frequency box. Colors follow the active skin (classic: white + alpha; night: `night_blue_primary`; day/CLEAR: black + alpha). `SignalBarsView` + `SignalMeterCoordinator`; full i18n.

### Fixed / Improved
- **Bar meter**: polling no longer applies the legacy yellow tint (it was misclassified as “red” → stuck at 1 bar); `applyModeVisibility` only seeds from stereo/RDS when level is still unset, so RSSI is not wiped on every skin reapply; **negative dBm** path when RSSI/SNR are negative; **QS6/NWD 0–5** OEM scale mapped directly (values 1–3 no longer collapse to one segment via the 0–15 formula); `legacyColorToLit` distinguishes amber/yellow from red.

### Docs
- Concept art: `docs/img/concept_signal_bars_main_ui.png`.

---

## [5.1.2] - 2026-04-11
*Patch on top of 5.1.1 (`versionCode` **34**, `versionName` **5.1.2**).*

### Fixed / Improved
- **Slow autoscan (overwrite / QS6)**: the seek loop is rescheduled when switching FM sub-bands (FM2/FM3); if frequency does not advance between ticks, `stepUp()` is used to unblock searches that could stall.
- **After slow autoscan**: tune to the **first available preset** (captured list or first slot with a frequency), with a short delay vs OEM callbacks; a short defer window avoids forcing `handleFrequencyChange` from the UI while the stack still reports band top (e.g. 108 MHz).
- **Layout 2**: **`boxAutoScan`** wrapper holds the glass card; the **`ImageButton`** is icon + ripple only — autoscan animation **spins the icon**, not the frame. `ThemeManager` applies the skin to the wrapper when present.

---

## [5.1.1] - 2026-04-11
*Patch on top of 5.1.0 (`versionCode` **33**, `versionName` **5.1.1**).*

### Added
- **Slovenia (SI)**: country in the in-app selector (i18n), `eslovenia/` folder for Storage logos, and Supabase `country_folder` mapping.
- **Widget diagnostics**: logging in `RadioWidgetActionReceiver` (tag **`ORF_WidgetRx`**) and traces in `RadioMediaService.handleWidgetSeek`.
- **Optional infinite preset strip**: screen settings toggle for **Layout V2/V3** (`pref_preset_scroll_loop`, activity `recreate` on change); `preset_loop_slot_v2/v3` + `InfinitePresetScrollHelper`.

### Changed
- **App versioning**: `versionCode 33`, `versionName 5.1.1`; `app_name_internal` **v5.1.1**.
- **BuildConfig**: public logos base URL at the `station-logos` bucket root (per-country subfolders).
- **Backup Studio (web)**: **SI** option for `pref_country_code` in `.ors`.
- **Day Mode**: unified **beige** background (`@color/day_mode_background` **#EDE4D3**) for window + `LogoManager`; `DAY_MODE` skin accent hex aligned.
- **Premium settings**: section titles plus **On/Off summaries** on switches.
- **Presets / steering**: NEXT/PREV in preset mode walks **slot order** (1…N) with wrap, aligned with the active screen.

### Fixed / Improved
- **Home-screen widget (e.g. QS6)**: `RadioMediaService` refreshes the **OpenRadioFM** widget (freq/band/PS) when tuning changes **without relying on `MainActivity`** (engine callbacks + delayed refresh if the OEM reports late).
- **Widget — logo**: **Glide on the main thread** and service refresh path that avoids dropping the bitmap when rebuilding `RemoteViews`.
- **Release build**: `SupabaseLogoSource` initializes `applicationContext` at the start of the async block before Storage upload (fixes unresolved `appContext`).
- **Preset loop strip**: loop clones match **skin**, text color, and logo tint of main slots; **`mLogoUiGeneration`** increments only when the **active** skin actually changes (prevents **dynamic background** from clearing on periodic auto-night checks).
- **Auto night mode**: skips redundant `applySkin` when the active skin already matches the target.

### Known / Pending
- **Widget**: station logo may appear briefly then fall back to the app icon; follow-up needed (Glide / `RemoteViews` re-render).
- **Layout 2 / stress tuning**: **`ivMainLogo`** may show the station logo briefly then revert to fallback/empty under rapid zapping and layout switches; **pending** analysis (`clearLogo` / `getStationInfo` / callback ordering).

---

## [5.1.0] - 2026-04-09 — “Backup Studio + Web Tools”
*Release focused on backup/import tooling and an installable web companion (`versionCode` **31**, `versionName` **5.1.0**).*

### Added
- **Web editor shortcut**: the **Save/Load Favorites** dialog now includes a **✏️** button that opens `https://kapi21.github.io/OpenRadioFM/editor/`.
- **Backup Studio (web/PWA)**: installable Android web app that can:
  - Create/edit/export **`.fav`** presets.
  - Generate/load **`.ors`** menu options via a guided form.
  - Generate/load full **`.orzip`** backups (ZIP with `state.json` + `RadioLogos/` images: station logos, `car_logo.png`, `background.*`) with automatic resize and previews.
  - Built-in **ES/EN manual** plus navigation buttons (back to main site + manual).
  - Per-field **(i)** tooltips explaining settings.
- **Backup Studio — expanded `.ors` fields**: logo provider, header car/clock, steering NEXT/PREV, scheduled night mode (checkbox enables start/end editing), night logo tinting, status bar, HD relief, save history, presets on the right (layout 2); clearer first-run wizard copy (language/country).
- **Supabase (community)**: when **no logo** is available but **RDS PS is stable** and passes the quality gate, the app may **upsert metadata-only** (PI/PS + frequency) to seed the database for later logo/stream enrichment; **FM only** (≥ 30 MHz), honors `pref_cloud_contrib` and `CloudContributionGuard`, with an in-memory **cooldown** to avoid repeated upserts.

### Changed
- **App versioning**: `versionCode 31`, `versionName 5.1.0`; `app_name_internal` **v5.1.0**.

### Fixed / Improved
- **Skins**: added **Day Mode** (bone/off-white background + black tinting) and improved **Night Mode** tint consistency (Layout 2/3).
- **Backgrounds (Layout 2/3)**: reduced `background.jpg/png` flicker/flash and forced immediate background refresh when entering/leaving Day Mode.
- **Logos**: hardened load/clear paths to avoid “carry-over” when zapping frequencies/presets (notably on QS6 and Layout 3).
- **Dynamic background (bgMode=2)**: retry loading when `ivDynamicBackground` ends up empty after zapping/cancelled loads (fixes “sometimes it doesn’t show”).
- **QS6 (NWD)**: **PowerOff** now performs a more robust stop/mute sequence to prevent OEM radio audio resuming in background.
- **QS6 (NWD)**: adjusted anti-bootstrap guard during SEEK/AutoScan to avoid getting stuck at 87.5/87.6 while scanning.
- **Steering wheel (QS6)**: “silent bridge” keeps media routing to OpenRadioFM in background without forcing audio playback.
- **Supabase**: connectivity check now uses `auth/v1/health` to avoid false “offline”.
- **UI**: cloud indicator softly blinks during online activity and uses a 200 ms alpha transition when dimming for no internet.
- **UI (RDS)**: brief “tick” flash when **RDS lock** is acquired (PS/PTY).
- **UI**: updated toast + web editor shortcut icons; improved per-skin tinting across layouts.

---

## [5.0.16] - 2026-04-03 — “Setup & Stability Hotfix”
*Hotfix on top of 5.0.15 (`versionCode` 29). **Still pending verification on real K706 and MT8163 hardware.***

### Added
- **HiHack / reboot**: `HihackBootReminderReceiver` + `HiHackBootReminder`, preferences, reminder on app launch, and toggle in premium settings; strings in all locales.
- **Backups (app state)**: long-press on **Save/Load Favorites** opens a **Backup** menu with export/import:
  - **Menu options only** to `.ors` (settings `pref_*` + `ThemePrefs`)
  - **Full backup** to `.orzip` (ZIP with `state.json` + `RadioLogos/` images)
  Includes progress + **Cancel**, and prompts the user to **restart** after restore so layout/theme changes apply.
- **Home-screen widget**: compact/expanded layouts, band label, extra actions (seek/mute) and PS tap for quick info; on launchers without resize the simple widget maps buttons back to **SEEK** (regression avoided).
- **Launcher icon (QS6/NWD)**: `android:icon` / `roundIcon` now point to `@drawable` with a high-res `drawable-nodpi-v4` fallback (matching the OEM radio approach).

### Fixed
- **Streaming / MT8163 (HCN)**: `OnlineStreamManager` marshals `stopStream()` / `release()` to the main looper; when deferred MT8163 handoff applies, `ExoPlayer.release` is postponed to avoid overlapping two streams.
- **Logo (Layout V3 / QS6)**: per-band logo cache, re-apply load when the URL is unchanged after `applyFallbackLogo`; `clearLogo()` on frequency changes to avoid a “stuck” logo or artifacts behind RDS PS.
- **Background steering (QS6)**: same bridge path as K706 (`sWheelMediaBridgeActive`, `onStop`, `ACTION_FORCE_PLAY`); `RadioMediaService` / FGS and `FactoryRadioHijackerService` cover QS6; `RadioServiceController.isSteeringWheelMediaBridgeMode()`.
- **RDS / MediaSession / notifications**: subtitle normalization (MHz) for metadata dedup in `MediaSessionManager`; `onRdsName` uses `%.1f MHz` when refreshing status; `RadioMediaService` skips `notify()` without notification permission or when notifications are disabled (**widget** still uses `AppWidgetManager` + prefs).
- **UI thread / logos**: if `RadioRepository.getStationInfo(..., callback != null)` is called on the UI thread, work runs on `logoExecutor` (`getStationInfoImpl`).
- **Logo quality**: avoid saving downloaded logos as 512×512 (pixelated when scaled up); decode/load using ARGB8888 and original size where applicable.

### Version
- `versionCode 29`, `versionName 5.0.16`; `app_name_internal` **v5.0.16**.

---

## [5.0.15] - 2026-04-03
*Release closing the 5.0.15 line (`versionCode` 28).*

### Added
- **Home-screen widget** (`AppWidget`): frequency, RDS PS, app icon, and previous/next preset; updates with `MainActivity`; controls via `RadioMediaService` + `RadioWidgetActionReceiver` (works on typical `com.android.launcher` setups; many automotive shells do not list third-party widgets).
- **Strings** for the widget and AutoScan dialog in all locales.
- **Internationalization**: expanded `strings.xml` (toasts, dialogs, AutoScan, scanning, history) with matching keys across `values` and `values-*`; helper scripts under `tools/` to sync/validate keys (`diff_strings.py`, `sync_toast_strings_locales.py`, block insert helpers, etc.).
- **Dialogs aligned with premium / AutoScan styling**: grid selector (`dialog_language_selector`), `.fav` file picker when loading favorites (`dialog_favorites_file_picker`), station history (`dialog_station_history`); dark frame, **active skin** card, user typography, red **Cancel** on lists; `item_fav_file_row` / `item_language` cells.
- **Localized layouts**: `dialog_save_load`, `dialog_credits`, `dialog_selective_scan`, and scan rows wired to `@string/`.
- **Build / Supabase**: credentials via `SUPABASE_URL` and `SUPABASE_ANON_KEY` in root `local.properties`, environment variables, or Gradle `-P` (no defaults committed). `BuildConfig` exposes URL, anon key, and public Storage base URL. Template `local.properties.example`; see [`docs/CI_SUPABASE.md`](docs/CI_SUPABASE.md).
- **Supabase community (data quality)**: centralized quality gate (`isAcceptableForCloudUpsert`, `sanitizePsForCloudUpsert`, PS rules); `CloudContributionGuard` — skip contribution while scanning or ~1.75s after a frequency change; PS must be stable ~4s before upload. `CloudContributionGuard.java`, `RadioRepository`, `MainActivity`, `SupabaseLogoSource`.

### Changed
- **Toasts & copy**: hard-coded strings replaced with `getString(R.string.*)` across `MainActivity`, `DialogManager`, `ScanManager`, `ControlPanelManager`, `HardwareManager`, `MinimalLayoutManager`, `StationAdapter`, and related classes.
- **RDS placeholders in scan lists**: `scan_rds_searching` / `selective_scan_waiting_rds` stay consistent with locale (no fixed Spanish mixed into other languages).
- **Slow AutoScan**: confirmation dialog matches premium menu styling (glass/active skin, typography, buttons).
- **AutoScan (FM)**: always starts at **87.5 MHz** to sweep the full band; auto-stops at **108 MHz** or on tuner wrap to the FM low end.
- **Jancar IVI / 8227L**: additional path through `com.jancar.radio.FmService` (`fmradio.freq.valid`, seek next/prev, power-off on teardown) when the real tuner does not follow `IRadio` alone.
- **Layout V3**: hide the small logo when the station logo is shown as background to avoid overlapping PS.
- **History dialog**: removed `applyPremiumListStyle` on the system list; replaced with a custom unified layout.
- **Engineering menus** (layouts): scroll / section tweaks for K706, QS6, and standard engineering dialogs.
- **Layout V2**: uniform horizontal spacing (`layout_v2_column_spacing`) between screen edge, preset column, and center guideline.
- **Version**: `versionCode 28`, `versionName 5.0.15`; `app_name_internal` **v5.0.15**.

---

## [5.0.14 (Beta)] - 2026-03-30
*Font-respecting indicators (band + unit), plus preset numbers in assets.*

### Added
- **Preset numbers**: new customization option **🔢 Preset numbers** with **Default** or **Tabler** style (assets `icons_numbers/number-1-small.svg` … `number-18-small.svg`).
- **HiHack (Accessibility)**: enabled/disabled status indicator inside premium settings, with a shortcut to the system accessibility settings.

### Changed
- **Band indicator**: `FM1/FM2/FM3/AM1/AM2` switched from icons to **autosized text** to respect the selected font, keeping the same reserved space in V2/V3 layouts (incl. `sw720dp`).
- **ST indicator**: stereo indicator switched from an icon to **“ST” text**, keeping the reserved slot and night-mode tinting behavior.
- **Sizing tweak**: slightly reduced band text autosize range for a better visual balance.
- **Unit**: `MHz/kHz` switched from icon to **autosized text** (changes with band), keeping the same reserved space in layouts.
- **Assets**: `icons_numbers` is packaged as `assets` via `sourceSets` in `app/build.gradle.kts`.
- **Version**: `versionCode 26`, `versionName 5.0.14 (Beta)`.

---

## [5.0.13 (Beta)] - 2026-03-29
*SVG icon packs (Material, Lucide, Remix, Font Awesome, Tabler), night/CLEAR/cloud tinting, V3 car logo & MT8163 LOC/DX sync.*

### Added
- **Icon packs** (assets + settings selector): `icons_google` (*_p3), `Icons_lucide` (*_p4), `icons_remix` (*_p5, including `power_off_p5.svg`), `icons_awesome` (*_p6), `icons_tabler` (*_p7; folder uses `ic_android_settings_p2.svg` / `power_off_p2.svg` for two assets). `IconPackManager`: SVG raster → bitmap + white silhouette for tinting; `isSvgTemplatePack()` for packs 2–6.
- **Strings**: `icon_pack_lucide`, `icon_pack_remix`, `icon_pack_awesome`, `icon_pack_tabler` (all locales).

### Fixed
- **MT8163 / icon packs**: LOC/DX button synced with `isDxLocal()` when AIDL callback 106 is missing (incl. post-click); Google/SVG icons no longer stuck when toggling.
- **Cloud icon**: red / yellow / night blue / black (CLEAR) without `applyClearButtonIconTint` overwriting streaming state; `updateDataActivityUI` idle CLEAR uses explicit black.

### Changed
- **Layout V3**: car logo (`ivCarLogo`) — same as digital clock (tap = cycle skin, long-press = night mode); `clickable`/`focusable` in XML.
- **Version**: `versionCode 25`, `versionName 5.0.13 (Beta)`; `app_name_internal` **v5.0.13 Beta**.

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
