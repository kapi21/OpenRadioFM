# Журнал изменений (Русский)

Испанская версия: [`CHANGELOG.md`](CHANGELOG.md)  
Английская версия: [`CHANGELOG_EN.md`](CHANGELOG_EN.md)

---

## [Unreleased]

- **K706 Root (`K706_Root`)**: модуль Magisk, патч ярлыков RADIO в `shared_prefs`, broadcast виджета на текущий HOME, см. [`CHANGELOG.md`](CHANGELOG.md) и [`HANDOFF_K706_ROOT.md`](HANDOFF_K706_ROOT.md).
- **QS6 / NWD (после перезагрузки)**: *warm‑up rebind* и автопереподключение AIDL с backoff в `NWDTunerAdapter` (в т.ч. `linkToDeath`), плюс ранний повторный `connect()`/poll в `QS6Engine`, чтобы подхватывать состояние без открытия OEM UI.
- **Launchers / MediaSession**: публикация **первичных метаданных** на старте `RadioMediaService` (исправляет “session есть, но metadata=null” в лаунчерах типа Agama) + расширение allowlist для выдачи доступа на artwork.
- **UI**: “плавный” тикер частоты во время `seek/scan` и диалог **редактирования имени станции** (с сохранением/восстановлением оригинала).
- **K706 / Android Auto, голос навигации (Zlink)**: см. [`CHANGELOG.md`](CHANGELOG.md) — уступка аудиофокуса при `isMusicActive`, `requestAudioFocus(false)` в auto-recovery, `RadioMediaService` без PLAYING+FGS на `LOSS_TRANSIENT`. Плюс пункты **5.1.6**. Версия **5.1.7** (`versionCode` **39**).

---

## [5.1.0] - 2026-04-07 — «Backup Studio + Web Tools»
*Релиз, сфокусированный на инструментах резервного копирования/импорта и установочном веб‑помощнике (`versionCode` 30).*

### Добавлено
- **Ярлык веб‑редактора**: в диалоге **Save/Load Favorites** добавлена кнопка **✏️**, открывающая `https://kapi21.github.io/OpenRadioFM/editor/`.
- **Backup Studio (web/PWA)**: устанавливаемое веб‑приложение для Android, которое умеет:
  - создавать/редактировать/экспортировать **`.fav`** (пресеты),
  - генерировать/загружать **`.ors`** (опции меню),
  - генерировать/загружать полные бэкапы **`.orzip`** (ZIP с `state.json` + изображениями `RadioLogos/`).

### Исправлено / Улучшено
- **Скины**: добавлен **Day Mode** (фон «костяной» + чёрные оттенки) и улучшена согласованность тонировки **Night Mode** (Layout 2/3).
- **Фон (Layout 2/3)**: уменьшено мерцание/«flash» `background.jpg/png`, добавлено немедленное обновление фона при входе/выходе из Day Mode.
- **Логотипы**: укреплены пути загрузки/очистки, чтобы избежать «перетекания» при частых переключениях частоты/пресетов (особенно QS6 и Layout 3).
- **QS6 (NWD)**: **PowerOff** более надёжно останавливает/глушит OEM‑радио, чтобы звук не возобновлялся в фоне.
- **Руль (QS6)**: «тихий мост» оставляет управление медиа‑кнопками в OpenRadioFM в фоне без принудительного запуска аудио.

### Версия
- `versionCode 30`, `versionName 5.1.0`; `app_name_internal` **v5.1.0**.

---

## [5.0.16] - 2026-04-03 — «Setup & Stability Hotfix»
*Хотфикс поверх 5.0.15 (`versionCode` 29). **Требуется проверка на реальном железе K706 и MT8163.***

### Добавлено
- **HiHack / перезагрузка системы**: `HihackBootReminderReceiver` + `HiHackBootReminder`, настройки, напоминание при запуске приложения и переключатель в премиум-настройках; строки во всех локалях.
- **Резервные копии (состояние приложения)**: длительное нажатие на **Save/Load Favorites** открывает меню **Backup** с экспортом/импортом:
  - **Только опции меню** в `.ors` (настройки `pref_*` + `ThemePrefs`)
  - **Полная копия** в `.orzip` (ZIP с `state.json` + изображениями `RadioLogos/`)
  Включает прогресс и кнопку **Cancel**, а после восстановления предлагает **перезапуск** для применения изменений темы/лейаута.
- **Виджет рабочего стола**: два лейаута (компактный/расширенный), индикатор диапазона, дополнительные действия (seek/mute) и быстрый инфо-экран по нажатию на PS; в лаунчерах без изменения размера кнопки снова работают как **SEEK** (регрессия устранена).
- **Иконка лаунчера (QS6/NWD)**: `android:icon` / `roundIcon` указывают на `@drawable` с fallback `drawable-nodpi-v4` (высокое качество) по аналогии с OEM-радио.

### Исправлено
- **Стриминг / MT8163 (HCN)**: `OnlineStreamManager` координирует `stopStream()` / `release()` через главный looper; при отложенном handoff на MT8163 `ExoPlayer.release` задерживается, чтобы избежать наложения двух потоков.
- **Логотип (Layout V3 / QS6)**: кэш логотипов по диапазонам, повторная загрузка когда URL не меняется после `applyFallbackLogo`; `clearLogo()` при смене частоты, чтобы логотип не «залипал» и не оставлял артефакты за RDS PS.
- **Руль в фоне (QS6)**: тот же мост, что и на K706 (`sWheelMediaBridgeActive`, `onStop`, `ACTION_FORCE_PLAY`); поддержка в `RadioMediaService`/FGS и `FactoryRadioHijackerService`; `RadioServiceController.isSteeringWheelMediaBridgeMode()`.
- **RDS / MediaSession / уведомления**: нормализация подзаголовка (MHz) для дедупликации метаданных в `MediaSessionManager`; `onRdsName` обновляет частоту в формате `%.1f MHz`; `RadioMediaService` не вызывает `notify()` без разрешения уведомлений (при этом **виджет** продолжает работать через `AppWidgetManager` + prefs).
- **UI поток / логотипы**: если `RadioRepository.getStationInfo(..., callback != null)` вызывается из UI-потока, работа выполняется в `logoExecutor` (`getStationInfoImpl`).
- **Качество логотипов**: предотвращено сохранение загруженных логотипов в 512×512 (пикселизация при масштабировании); загрузка/декодирование в ARGB8888 и с исходным размером там, где применимо.

### Версия
- `versionCode 29`, `versionName 5.0.16`; `app_name_internal` **v5.0.16**.

---

## [5.0.15] - 2026-04-03
*Релиз, закрывающий линию 5.0.15 (`versionCode` 28).*

Подробности смотрите в [`CHANGELOG.md`](CHANGELOG.md) и [`CHANGELOG_EN.md`](CHANGELOG_EN.md).

