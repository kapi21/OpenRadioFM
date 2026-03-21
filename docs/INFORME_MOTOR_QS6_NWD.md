# Informe: motor de radio QS6 / NWD (OpenRadioFM)

**Rama Git:** `QS_NWD`  
**Ámbito:** Integración con `com.nwd.radio.service` (AIDL), audio FM en unidades **QS6** (stack NWD / SourceMgr), RDS y “shadow motor” por broadcasts + `Settings.System`.

Este documento resume lo conseguido en el motor **QS6Engine** y piezas conectadas. **No forma parte de `main`** hasta que se decida merge tras revisión.

---

## 1. Objetivo

Hacer que OpenRadioFM funcione de forma fiable en head units **QS6** que exponen la radio FM vía el servicio OEM **NWD**:

- Sintonía y RDS vía **IPC AIDL** (`RadioFeature` / `RadioCallback`).
- **Audio FM audible** (ruta DSP / ARM alineada con la app de radio del fabricante).
- **RDS** (PS/RT) legible en UI, sin basura hexadecimal ni confundir frecuencia con nombre de emisora.
- Comportamiento estable ante **doble arranque** (p. ej. `MainActivity` + `RadioMediaService`), pérdidas de **AudioFocus** y recuperación.

---

## 2. Problema crítico resuelto: FM sin sonido

### Síntoma
RDS y frecuencia correctos en logs, pero **silencio** en salida de audio.

### Causa raíz (ingeniería inversa, carpeta `QS NWD/tools`)
En el stack OEM (p. ej. `SprdRadioManager` / `AWRadioManager`), la secuencia que inicializa la ruta de audio ARM (`InitFM` / envío equivalente a `SendArmFmMediaPlay`) depende del broadcast:

`com.nwd.action.ACTION_APP_IN_OUT`

con **`extra_app_id = 8`** (ID de la app de radio NWD en `ApplicationList.xml`). Si el `extra` no coincide, el valor por defecto puede ser otro (p. ej. launcher) y **no se ejecuta `InitFM()`**.

### Solución en código
En **`QS6Engine`**, al entrar en modo radio FM:

- Envío de `ACTION_APP_IN_OUT` alineado con el OEM: `extra_app_id=8`, `extra_app_operation=1`, `extra_app_event=0`, más extras documentados en el firmware (`extra_app_in_out`, `extra_app_reset`, etc.).
- Al salir: broadcast documentado con el typo del firmware: **`com.nwd.android.ACTION_EXIT_ARM_FM_RAIDO`** para la ruta de salida ARM.

Tras esto, el audio FM **sí se escucha** en unidad real.

---

## 3. AudioFocus y ruta de audio

- Petición de **AudioFocus** con atributos equivalentes a los que usa el sistema NWD (**`USAGE_MEDIA` / `CONTENT_TYPE_MUSIC`**), en hilo principal cuando aplica.
- Tras concesión: **`AudioManager.setMode(MODE_NORMAL)`** y parámetros HAL donde corresponde (`setParameters` / `fm_radio_on=…` según versión).
- **`ACTION_CHANGE_SOURCE`** hacia **`SOURCE_RADIO`** mediante broadcast **implícito** (`sendSourceSystemBroadcast`) para que `com.nwd.setting.service` / SourceMgr reciban el cambio de fuente.
- Reacción a **`AUDIOFOCUS_LOSS`**: re-intento diferido (“reclaim”) para no quedar mudos tras competencia con otras apps.

---

## 4. Arranque, doble inicio y “salto” de audio

### Síntoma
Al abrir la app, un **segundo pico** de inicialización repetía `ACTION_CHANGE_SOURCE` + `ACTION_APP_IN_OUT` en ~1,5–1,7 s, provocando un **segundo `InitFM()`** y un salto/pitido.

### Mitigación
- **Coalescencia** de broadcasts forzados de fuente: `MIN_FORCE_SOURCE_BROADCAST_MS` ampliado (p. ej. **2400 ms**) para que el camino de recuperación no duplique la secuencia completa pegada al primer arranque.
- **Throttle** de `ACTION_APP_IN_OUT` (enter) en la misma ventana, con reset al salir de FM, para evitar doble `InitFM` si otro camino vuelve a disparar el broadcast.

---

## 5. AIDL: servicio NWD y handshake

- Bind a `com.nwd.radio.service/.RadioService`.
- Registro de **`RadioCallback`** y activación de modo background del servicio (`setRadioBackServiceOn`, intro/handshake según versión del plan).
- Logs de **`getRadioState()`** tras el handshake para diagnóstico.
- Manejo de **caída del servicio** (`linkToDeath`, re-bind / limpieza).

---

## 6. “Shadow motor” (redundancia NWD)

Paralelamente al AIDL:

- **Broadcasts** NWD (`ACTION_SEND_RADIO_FREQUENCE_NEW`, RDS RT, etc.).
- **ContentObserver** sobre `Settings.System`: `nwd_radio_current_freq`, `nwd_radio_current_ps_data`.

Sirve como respaldo cuando el AIDL va retrasado o la UI del sistema actualiza Settings.

### Ajustes importantes
- **PS en Settings** a menudo viene como **cadena hexadecimal** (8 bytes RDS → 16 caracteres hex). Sin decodificar, la UI mostraba `434f5045…` o ceros.
  - **Normalización**: decodificación hex cuando el patrón es el del OEM; filtrado de todo ceros; texto imprimible y espacios RDS.
- **Falso PS numérico**: el firmware a veces pone la **frecuencia** como “PS” (`87600`, `8760`, etc.). Se **rechaza** como nombre RDS.
- **Hex de 8 caracteres solo 0–9**: no se trata como PS hex real (evita falsos positivos tipo `87600000`).
- **Frecuencia en Settings**: heurística antigua (`< 3000 → ×10`) rompía casos como **5310** (MW). Nueva función **`nwdSystemSettingFreqToKhz`**: FM en décimas, FM en kHz, MW y corrección ×10 erróneo en rango intermedio.
- El shadow **ya no llama a `onBandChanged`**: la banda la fija el **AIDL** con **`coerceQs6BandForDisplay`** (el HAL a veces reporta bandas 1/2/4 en frecuencias que son FM 65–120 MHz).

---

## 7. Archivos principales tocados

| Área | Archivo(s) |
|------|-------------|
| Motor QS6 / NWD | `app/.../data/source/QS6Engine.java` |
| Selección de motor / arranque | `app/.../ui/main/RadioServiceController.java` |
| UI / diálogo QS6 (strings) | `MainActivity.java`, `values/strings.xml`, `values-en/strings.xml` |
| Sesión / foco en servicio | `RadioMediaService.java` |
| Callbacks compuestos (si aplica) | `CompositeRadioEngineCallback.java` |
| Otros motores / README | `K706Engine.java`, `README.md` (ajustes menores si constan en el commit) |
| Web estática docs | `docs/*/index.html` (si van en el mismo commit) |

---

## 8. Cómo probar en unidad QS6

1. Instalar APK de la rama `QS_NWD`.
2. Arranque en frío: comprobar **audio FM** sin necesidad de abrir la app OEM.
3. Cambiar emisora: PS/RT coherentes; no debe mostrarse **87600** como nombre si hay **ROCK FM** (u otro PS real).
4. Banda: no debe “bailar” entre FM/MW por el shadow; en FM debe estabilizarse en **banda 0** salvo MW real.
5. Pérdida de foco (navegación, asistente): recuperación sin quedar mudo indefinidamente.

---

## 9. Limitaciones conocidas

- **`DeadObjectException`** u otros errores en procesos OEM pueden aparecer en logcat; no siempre son atribuibles a OpenRadioFM.
- RDS RT con **scroll** del encoder sigue llegando en fragmentos; la limpieza de espacios ayuda pero no sustituye a un debounce opcional futuro.
- Comportamiento exacto puede variar con **versión de firmware** NWD; esta rama está validada contra el escenario descrito en las sesiones de desarrollo (marzo 2026).

---

## 10. Próximos pasos sugeridos (fuera de este informe)

- Merge a `main` tras revisión y CI.
- Opcional: debounce de RT en QS6; tests instrumentados mock AIDL.
- Mantener documentación al día si el OEM cambia acciones o extras.

---

*Documento generado para la rama **QS_NWD** — OpenRadioFM.*
