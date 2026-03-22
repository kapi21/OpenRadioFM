# Estudio de ingeniería inversa — radio OEM K706 (`com.android.fmradio.ext`)

> **Copia versionada en el repositorio** (`docs/ESTUDIO_INGENIERIA_INVERSA_K706_RADIO_OEM.md`). Los artefactos pesados de descompilación (**smali**, APK original, `framework.jar`) suelen guardarse **fuera del repo**, p. ej. `…\K706_RE\K706_RE\` (Radio_Decompiled / framework_decompiled).

**Fuente de análisis:** carpeta de trabajo `K706_RE`  
**Fecha de análisis:** 2026-03-21  
**Herramienta:** APK descompilado con **apktool 2.9.3** (smali + `AndroidManifest.xml` + recursos).

---

## 1. Inventario del directorio

| Elemento | Descripción |
|----------|-------------|
| **`Radio_Decompiled/`** | APK **`Radio_Original.apk`** → radio OEM **QuickFish / AOSP FM extendido**. Contiene la app de usuario relevante para K706. |
| **`framework_decompiled/`** | **`framework.jar`** (minSdk 28) descompilado — **framework Android / OEM** genérico, no la app radio en sí. Útil solo como referencia de APIs del sistema en la misma build. |

**Conclusión:** El núcleo del estudio útil para interoperar con OpenRadioFM está en **`Radio_Decompiled`**.

---

## 2. Identidad de la aplicación radio (OEM)

| Campo | Valor |
|--------|--------|
| **Paquete manifiesto** | `com.android.fmradio.ext` |
| **sharedUserId** | `android.uid.system` (**app de sistema**; firma plataforma) |
| **minSdk / targetSdk** | 23 / 29 |
| **Application** | `com.android.fmradio.FMRadioApplication` |
| **Actividad principal** | `com.android.fmradio.FmMainActivity` (`singleTask`) |
| **Servicio FM** | `com.android.fmradio.FmService` — acción `com.android.fmradio.IFmRadioService` |
| **Provider** | `com.android.fmradio.database.FmProvider` — authority `com.android.fmradio.ext` |

### Permisos destacables

- `ACCESS_FM_RADIO`, `MODIFY_AUDIO_ROUTING`, `MODIFY_AUDIO_SETTINGS`
- `READ_PHONE_STATE` (declarado; ver §5)

---

## 3. Arquitectura software (capas)

```
FmMainActivity / vistas
        ↓
    FmService (AudioManager, AudioFocus, MediaSession, routing)
        ↓
    FmManagerSelect
        ↓
    TunerManagerForExt  →  com.qf.clientsdk.QFTunerManager (singleton)
        ↓
    Broadcom stack (AIDL): com.broadcom.fm.fmreceiver.IFmReceiverService / IFmReceiverCallback
```

- **`com.qf.*`**: SDK **QuickFish** embebido (vehículo, tuner, skins `com.qf.skin.lib`, IPC CAN `com.qf.ipcsdk`).
- **`TunerManagerForExt`**: puente entre UI/servicio FM y `QFTunerManager` (`onTune`, `onBand`, `onSeek`, RDS AF/TA, presets, etc.).
- **`FmManagerSelect`**: fachada delgada; delega casi todo en `TunerManagerForExt`.

Esto **coincide en espíritu** con lo que OpenRadioFM ya explora vía reflexión (`McuManager`, `SetChannel`, Broadcom/QF), pero aquí está la **secuencia oficial** empaquetada por el OEM.

---

## 4. AudioFocus y manejo de “interrupciones” (llamadas, otras apps)

### 4.1 Petición de foco

En `FmService.requestAudioFocus()`:

- `AudioManager.requestAudioFocus(listener, stream, focusGain)`
- **Stream = 3** → `AudioManager.STREAM_MUSIC`
- **focusGain = 1** → `AUDIOFOCUS_GAIN`

Es el patrón clásico del **FM de AOSP / Media** (no `USAGE_MEDIA` moderno vía `AudioAttributes` en este binario).

### 4.2 Respuesta a cambios de foco (`updateAudioFocus`)

Lógica resumida desde smali (constantes de foco estándar):

| Código | Constante típica | Comportamiento observado |
|--------|-------------------|---------------------------|
| **1** | `AUDIOFOCUS_GAIN` | Restaura altavoz si aplica (`setForceUse`), puede relanzar `handlePowerUp` si había pérdida transitoria; **`setMute(false)`**. |
| **-1** | `AUDIOFOCUS_LOSS` | `mPausedByTransientLossOfFocus = false`; **`handlePowerDown()`**; **`forceToHeadsetMode()`**. |
| **-2** | `AUDIOFOCUS_LOSS_TRANSIENT` | Si FM estaba en POWER_UP → **`mPausedByTransientLossOfFocus = true`**; **`handlePowerDown()`**; **`forceToHeadsetMode()`**. |
| **-3** | `AUDIOFOCUS_LOSS_CAN_DUCK` | **`setMute(true)`** (duck / atenuar). |

La radio OEM **depende del framework** para pausar/bajar FM cuando entra el dialer u otra app con prioridad. No aparece en el paquete `com.android.fmradio` un **`TelephonyManager` / `PhoneStateListener`** explícito.

**Implicación:** En unidades donde el **MCU mezcla FM en hardware** y Android **no** envía `AUDIOFOCUS_LOSS` fiable a apps no-sistema, la app OEM **sí** es `android.uid.system` y puede recibir un tratamiento distinto en el stack de audio. Las apps usuario (OpenRadioFM) suelen necesitar **workarounds** (MCU `setMute` / `SetChannel`, telefonía explícita), como ya documentáis en `K706RadioManager`.

---

## 5. `READ_PHONE_STATE` sin uso evidente en smali FM

Búsqueda orientativa en `Radio_Decompiled/smali` bajo `com/android/fmradio/**`: **no hay referencias** a `TelephonyManager`, `PhoneStateListener`, `CALL_STATE_*`, etc.

El permiso puede ser:

- heredado de plantilla AOSP y **no usado** en esta variante, o  
- usado en código **ofuscado fuera de `com.android.fmradio`** (poco probable en este APK), o  
- reservado para otra función (no localizada en el análisis rápido).

**Contraste:** OpenRadioFM en K706 añade **`PhoneStateListener`** explícitamente para silenciar cuando el foco Android no basta — coherente con el vacío de telefonía en la app OEM **en el código FM visible**.

---

## 6. `framework_decompiled` (framework.jar)

- Descompilación masiva (smali, smali_classes2/3).
- Sirve para **cruzar nombres de clases / servicios del sistema** de la misma ROM, no para el flujo FM de usuario.
- **No sustituye** el análisis de `Radio_Original.apk` para tuner/UI.

---

## 7. Relación con OpenRadioFM (K706)

| Tema | App OEM (`com.android.fmradio.ext`) | OpenRadioFM (`K706RadioManager` / `K706Engine`) |
|------|--------------------------------------|--------------------------------------------------|
| Identidad | UID sistema | App usuario |
| Tuner | `QFTunerManager` + `TunerManagerForExt` | Reflexión + AIDL propietarios donde aplica |
| Interrupciones | **AudioFocus** + `handlePowerDown` / mute / headset | AudioFocus **+** `PhoneStateListener` **+** `SetChannel(4)` MCU |
| Objetivo | Ser la app oficial en la ROM | Reemplazar UI y lógica manteniendo hardware |

**Qué aporta este estudio a OpenRadioFM**

1. Confirmar **orden conceptual**: servicio FM central (`FmService`) → foco → mute / power routes.  
2. Ver **stream y tipos de pérdida de foco** que el OEM espera (`LOSS` vs `TRANSIENT` vs `CAN_DUCK`).  
3. Validar que el stack **QF + Broadcom** es el camino “oficial” en paralelo a vuestros hooks MCU.  
4. Reforzar la hipótesis de que **sin UID sistema**, puede faltar el mismo comportamiento de audio; por eso **telefonía + MCU** en OpenRadioFM no es redundante, es **defensivo**.

---

## 8. Próximos pasos sugeridos (ingeniería)

1. ~~**Extraer** (jadx) `com.android.fmradio.*` y `com.qf.clientsdk.*`~~ — **Cubierto en §11** vía smali; opcional instalar **jadx** para Java legible: `jadx -d out_jadx --select-package com.android.fmradio Radio_Original.apk` (y otro paso para `com.qf.clientsdk`).  
2. Comparar intents de **§11.2** con logcat en el coche.  
3. Mantener **`framework.jar`** solo si necesitáis una clase concreta del framework de esa ROM (no hace falta para el día a día FM).  
4. Opcional: tabla de **frecuencias / bandas** desde `RadioConfigData`, `FmBandInfo`, recursos `res/values*`.

---

## 9. Referencias de archivos clave (Radio_Decompiled)

- `AndroidManifest.xml` — paquete, componentes, permisos.  
- `smali/com/android/fmradio/FmService.smali` — AudioFocus, `updateAudioFocus`, `requestAudioFocus`, `setMute`.  
- `smali/com/android/fmradio/FmManagerSelect.smali` — delegación a `TunerManagerForExt`.  
- `smali/com/android/fmradio/TunerManagerForExt.smali` — llamadas a `QFTunerManager`.  
- `smali/com/qf/clientsdk/QFTunerManager.smali` — API del tuner OEM.  
- `smali/com/broadcom/fm/fmreceiver/*.smali` — contratos AIDL Broadcom FM.

---

## 10. Documentación relacionada en este repo

- [Compatibilidad de hardware](HW_COMPATIBILITY.md) — notas generales SoC / integración.  
- [Inteligencia QS NWD](INTELIGENCIA_QS_NWD.md) — plataforma Qualcomm/NWD (distinta de K706, misma idea de motores).  
- [Comparativa K706 OpenRadioFM vs OEM](COMPARATIVA_K706_OPENRADIO_VS_OEM.md) — tablas motor vs `FmService`/QF.

---

## 11. Análisis detallado — `FmService` y `QFTunerManager` (smali, paso 2)

> **Nota:** El análisis siguiente se extrajo de **`Radio_Decompiled/smali`**. Puedes **validar** con Java legible usando **jadx** local (ver §11.8).

### 11.8 jadx local (Windows)

**Ubicación habitual del proyecto:** `C:\@MIS PROYECTOS\K706_RE\K706_RE\jadx\` (carpeta `bin\jadx.bat` / `jadx-gui.bat`).

**Script en el repo** (desde la raíz de OpenRadioFM):

```text
scripts\k706_jadx_decompile.bat [ruta\Radio_Original.apk]
```

- Por defecto busca el APK en `K706_RE\Radio_Original.apk` (hermano de `OpenRadioFM`). Si tu APK tiene otro nombre/ruta, pásala como argumento o define **`K706_RADIO_APK`**.
- Salida por defecto: **`K706_RE\jadx_out_radio_oem\`** (sobrescribible con **`K706_JADX_OUT`**).
- Solo decompila paquetes **`com.android.fmradio`** y **`com.qf.clientsdk`** (ajusta el `.bat` si necesitas `com.qf.skin`, etc.).

**Comando manual equivalente:**

```text
"C:\@MIS PROYECTOS\K706_RE\K706_RE\jadx\bin\jadx.bat" -d jadx_out --select-package com.android.fmradio --select-package com.qf.clientsdk Radio_Original.apk
```

**GUI:** ejecutar `jadx-gui.bat` y abrir el mismo APK.

**Java:** jadx reciente requiere **JDK 11 o superior**. Si ves `UnsupportedClassVersionError` ... `55.0`, Windows sigue usando **Java 8** aunque tengas el 17 instalado.

- **Solución rápida** (en la misma ventana CMD, antes del `.bat`):

  ```bat
  set "J706_JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
  ```

  (Sustituye la ruta por la carpeta real de tu JDK 17: suele estar en `C:\Program Files\Eclipse Adoptium\` o `C:\Program Files\Java\`.)

- O bien pon **`JAVA_HOME`** del sistema al JDK 17 y **arriba** en el `Path` el `%JAVA_HOME%\bin`.

El script `k706_jadx_decompile.bat` **lee `J706_JAVA_HOME`** y fuerza ese JDK antes de llamar a jadx.

### 11.1 `handlePowerDown()`

1. Log `--->>handlePowerDown()`.
2. **`powerDown()`** (ver §11.3).
3. **`notifyActivityStateChanged(Bundle)`** con `callback_flag = 10` (`0x0A`) para avisar a la actividad.

### 11.2 `powerDown()` (núcleo al perder foco / apagar)

Orden exacto en smali:

1. Si `mPowerStatus == POWER_DOWN` → `return true` (idempotente).
2. **`setMute(true)`**
3. **`setRds(false)`**
4. **`enableFmAudio(false)`** → internamente **`releaseAudioPatch()`** + **`stopRender()`**
5. **`FmManagerSelect.powerDown()`** (delega al tuner QF / Broadcom).
6. Si OK → `mPowerStatus = POWER_DOWN`.

**Contraste OpenRadioFM:** `K706RadioManager` hace mute, RDS según implementación, **MCU `SetChannel(4)`**, telefonía, etc. La OEM **no** muestra `PhoneState`; apaga el camino FM por **stack FM + audio** (`powerDown` del manager).

### 11.3 `forceToHeadsetMode()`

- Solo actúa si **`mIsSpeakerUsed`** y **`isHeadSetIn()`**.
- Log `forceToHeadsetMode`.
- **`FmUtils.setIsSpeakerModeOnFocusLost(context, true)`** — guarda preferencia para restaurar modo altavoz cuando vuelva el foco (flujo coherente con `requestAudioFocus` / `updateAudioFocus` GAIN).

No mueve solo el “balance”; es **estado de UI/preferencia** para no quedar forzado a altavoz tras una pérdida de foco.

### 11.4 `createAudioPatch()` / `releaseAudioPatch()` / `enableFmAudio`

- En este APK, **`createAudioPatch()`** asigna un **`new Object()`** a `mAudioPatch` y **devuelve `-1`** (código de error típico). No invoca `AudioManager.createAudioPatch(...)` en el smali visible — puede ser **build recortada**, **ofuscación** o lógica real en otra rama/clase cargada dinámicamente.
- **`releaseAudioPatch()`** pone `mAudioPatch = null` y anula `mAudioSource` / `mAudioSink` (`AudioDevicePort`).
- **`enableFmAudio(true)`**: si `POWER_UP` y hay foco, lista patches con **`AudioManager.listAudioPatches`**, comprueba **`isPatchMixerToEarphone`**, y decide entre **`stopRender` / `createAudioPatch` / `startRender`** (fallback si “falla” el patch).

Para ingeniería práctica en OpenRadioFM: seguir confiando en **MCU + foco** como en `K706RadioManager`; el “patch” OEM aquí no aporta una API reutilizable clara desde app no-sistema.

### 11.5 Inventario de acciones / intents (`FmService` + receptor)

**Registradas en `registerFmBroadcastReceiver()`:**

| Acción | Uso |
|--------|-----|
| `com.android.music.musicservicecommand` | Con extra `command` — ver receptor |
| `android.intent.action.ACTION_SHUTDOWN` | Apagado |
| `android.intent.action.SCREEN_ON` | `setRdsAsync(true)` |
| `android.intent.action.SCREEN_OFF` | `setRdsAsync(false)` |
| `android.media.VOLUME_CHANGED_ACTION` | Ajuste volumen |

**`FmServiceBroadcastReceiver` — casos:**

- `com.android.music.musicservicecommand` + `command == "pause"` → limpia handler, **`FmService` teardown interno (`access$200`)**, **`stopSelf()`** del servicio.
- `ACTION_SHUTDOWN` → similar limpieza + teardown (sin `stopSelf` en el tramo mostrado; seguir smali si hace falta el detalle).
- `SCREEN_ON` / `SCREEN_OFF` → RDS async on/off.

**Constantes de clase `FmService` (comandos internos / extras):**  
`fmradio.seek.previous`, `fmradio.seek.next`, `fmradio.turnoff`, `fmradio.enter`, `fmradio.exit`, `fmradio.decrease`, `fmradio.increase`, `CMDPAUSE` = `"pause"`, `SOUND_POWER_DOWN_MSG` = `com.android.music.musicservicecommand`.

**Otras apps / UI:**

- `com.android.fmradio.favorite_changed` — favoritos.
- `content://com.android.fmradio.ext/station` — provider.

### 11.6 API pública relevante de `com.qf.clientsdk.QFTunerManager` (singleton)

Métodos observados en smali (para cruzar con reflexión en OpenRadioFM):

`getInstance`, `autoScan`, `mute`, `unMute`, `isMuted`, `onBand`, `onFine`, `onLoc`, `onNext`, `onPre`, `onPresetSave`, `onPresetSelect`, `onRadioArea`, `onSeek`, `onTune`, `openAFSwitcher`, `openPTYSwitcher`, `openRDSSwitcher`, `openREGSwitcher`, `openTASwitcher`, `setRdsAFSwitch`, `setRdsPtyType`, `setRdsSwitch`, `setRdsTASwitch`, `setPresetList`, `setTunerTool`, `removeTunerTool`, `setTunerScanning`, `isTunerScanning`, `stopScan`, `requestSendCurrentFrequencyRdsInfo`, `requestSendTunerData`, `requestSetRadioAntennaSupply`, `reportRds_TA_PlayState`, `tuneExt`, `getTunerPresenter`, `getServer` (privado, `ITuner`), `sendCmds`.

### 11.7 Comparación rápida con `K706RadioManager` (OpenRadioFM)

| Aspecto | OEM `FmService` | OpenRadioFM |
|---------|-----------------|-------------|
| Pérdida de foco fuerte / transitoria | `handlePowerDown` + `forceToHeadsetMode` | `onAudioFocusChange` + mute + `SetChannel(4)` |
| Apagado FM lógico | `powerDown()` → mute, RDS off, audio off, `FmManagerSelect.powerDown()` | Equivalente parcial + MCU |
| Llamada telefónica | *No en código FM*; depende del sistema enviando foco | `PhoneStateListener` explícito |
| Audio patch HAL | Lista patches / placeholder `Object` | No replicado; no necesario si MCU cubre |

---

*Mantener sincronizado con el resumen en la carpeta de trabajo `K706_RE` si se amplía el análisis.*
