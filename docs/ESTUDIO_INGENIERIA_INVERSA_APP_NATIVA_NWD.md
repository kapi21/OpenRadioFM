# Estudio de ingeniería inversa — app nativa NWD (radio QS6)

> **Documento único de inteligencia (consolidado A–D + implementación):** [**`INTELIGENCIA_QS_NWD.md`**](INTELIGENCIA_QS_NWD.md)

Este archivo conserva el **plan por fases**, checklist y plantillas. El detalle técnico reunido en un solo sitio está en **`INTELIGENCIA_QS_NWD.md`**; los archivos por fase siguen como **anexos** ampliados.

**Fase A (mapa del sistema):** [`NWD_FASE_A_MAPA_SISTEMA.md`](NWD_FASE_A_MAPA_SISTEMA.md).  
**Fase B (audio y fuente):** [`NWD_FASE_B_AUDIO_Y_FUENTE.md`](NWD_FASE_B_AUDIO_Y_FUENTE.md).  
**Fase C (IPC AIDL):** [`NWD_FASE_C_IPC_AIDL.md`](NWD_FASE_C_IPC_AIDL.md).  
**Fase D (RDS y Settings):** [`NWD_FASE_D_RDS_Y_SETTINGS.md`](NWD_FASE_D_RDS_Y_SETTINGS.md).

> **Ámbito legal:** solo sobre firmware/APKs de **tu propio hardware** o copias extraídas con derecho a analizarlas; el objetivo es **interoperabilidad** (accesibilidad radio de terceros), no redistribuir código propietario.

---

## 1. Objetivos del estudio

| Objetivo | Para qué sirve en OpenRadioFM |
|----------|-------------------------------|
| Contrato exacto de **broadcasts** (`ACTION_*`, extras, orden) | `QS6Engine`: `ACTION_CHANGE_SOURCE`, `ACTION_APP_IN_OUT`, salida ARM |
| **AIDL** `RadioFeature` / `RadioCallback` | Ya copiados en `app/src/main/aidl/com/nwd/radio/service/` — validar versiones |
| **Settings.System** (`nwd_radio_*`) | Shadow motor, normalización PS/frecuencia |
| **AudioFocus / AudioAttributes** del OEM | Misma política que `com.nwd.setting.service` |
| **SourceMgr** y `KernelUtils` | Quién recibe implicit broadcasts y en qué orden |
| Variantes **Sprd** vs **AW** (`SprdRadioManager`, `AWRadioManager`) | Misma lógica `extra_app_id` u otras ramas |
| **JNI / `.so`** si aplica | Parámetros HAL, nodos `/dev/*` referenciados en strings |

---

## 2. Material que ya tienes (referencia local)

Ruta base sugerida (fuera del repo OpenRadioFM, mismo árbol `K706_RE`):

| Recurso | Ubicación típica |
|---------|------------------|
| Descompilado **kernel / setting** (SourceMgr, KernelUtils, `ApplicationList.xml`) | `K706_RE/QS NWD/tools/nwd_kernel_service_decompiled/` |
| Descompilado **radio service** (AIDL, `SprdRadioManager`, `RadioService`) | `K706_RE/QS NWD/tools/nwd_radio_service_decompiled/` |
| APK radio original (referencia) | `K706_RE/Radio_Original.apk` |
| Logcat Bengal / unidad | `K706_RE/QS NWD/*.logcat` |
| Informe motor integrado en app | `OpenRadioFM/docs/INFORME_MOTOR_QS6_NWD.md` |

OpenRadioFM ya incluye **stubs AIDL** y tipos Java bajo `com.nwd.radio.service` para compilar; el estudio inverso confirma que coinciden con el firmware de tu unidad.

---

## 3. Cadena de herramientas recomendada

1. **Extracción desde la unidad (ADB)**  
   ```bash
   adb shell pm path com.nwd.radio
   adb shell pm path com.nwd.radio.service
   adb shell pm path com.nwd.setting.service   # o el paquete que corresponda al “kernel”
   adb pull …/base.apk
   ```
   Anotar **versionName / versionCode** (`aapt dump badging` o pantalla Ajustes).

2. **Apktool** — recursos, `AndroidManifest.xml` legible, `smali/`.  
   Útil para receivers, intent-filters y strings en XML.

3. **JADX** (o JADX-GUI) — lectura en Java aproximada.  
   Priorizar paquetes: `com.nwd.radio.*`, `com.nwd.kernel.*`, `com.nwd.setting.*`.

4. **Logcat correlacionado**  
   Filtrar por tags propios del OEM y por `ActivityManager`, `MediaFocusControl`, `BroadcastQueue`.  
   Cruzar: *acción usuario* → *broadcast* → *método en smali/Java*.

5. **Opcional: Ghidra / IDA** — solo si hay `.so` con lógica crítica no visible en Java.

6. **Frida** (avanzado) — traza de métodos en dispositivo rooteado; solo si lo necesitas y el entorno lo permite.

---

## 4. Plan de trabajo por fases

### Fase A — Mapa del sistema
- [x] Listar paquetes `com.nwd.*` y rol (descompilado + **captura ADB** en `NWD_FASE_A_MAPA_SISTEMA.md` §A.0).
- [x] Manifest / diagrama emisor–receptor de acciones críticas → ver **`docs/NWD_FASE_A_MAPA_SISTEMA.md`**.
- [x] Localizar `ApplicationList.xml` — **AppID 8 = `com.nwd.radio`**, Source audio — documentado en el mismo archivo.

### Fase B — Audio y fuente
- [x] `ACTION_CHANGE_SOURCE` + `SOURCE_RADIO`/`SOURCE_ANDROID` (**byte** `0x04` / `0x00`) → **`NWD_FASE_B_AUDIO_Y_FUENTE.md`** §B.1–B.2.
- [x] `ACTION_APP_IN_OUT`: `KernelUtils.appStart`, `SourceMgr$3` (default `extra_app_id=4`, debounce launcher), `SprdRadioManager$1` / `AWRadioManager$1` (`==8` → `InitFM`) → §B.3–B.4.
- [x] Extras (`extra_source_id`, `extra_app_*`, salida `EXIT_ARM_FM_RAIDO`) y **orden** abrir/cerrar → §B.5–B.6.

### Fase C — IPC radio
- [x] AIDL vs `TRANSACTION_*` en `RadioFeature$Stub` / `RadioCallback$Stub` → **`NWD_FASE_C_IPC_AIDL.md`** §C.2.
- [x] Secuencia tras `bindService` (`registCallback`, `setRadioBackServiceOn`, `INTRO`, `getRadioState`, retry) → §C.3.
- [x] Hilos: handshake en `Thread` en cliente; callbacks IPC en binder → UI vía `Handler` → §C.5.

### Fase C+ — Cobertura API (sin segundo firmware)
- [x] Inventario `RadioFeature` usado vs no usado en **`QS6Engine`** → **`NWD_FASE_C_IPC_AIDL.md`** §C.9.
- [x] `RadioCallback`: callbacks con lógica/UI vs overrides vacíos → §C.9.
- [ ] Comparación `TRANSACTION_*` entre firmwares (cuando haya un segundo APK/build).

### Fase D — RDS y Settings
- [x] Formato e interpretación de `nwd_radio_current_freq` / `nwd_radio_current_ps_data` (int + string, hex PS, heurística freq) → **`NWD_FASE_D_RDS_Y_SETTINGS.md`** §D.1–D.3.
- [x] Broadcasts `ACTION_SEND_RADIO_FREQUENCE_NEW`, `ACTION_SEND_RADIO_RDS_RT` (extras, `RECEIVER_EXPORTED`, convivencia AIDL) → §D.5–D.7.
- [x] Escritura OEM: `RadioProtocalUtil.responseCurrentFrequency` + `SettingTableKey.writeDataToTable` → `Settings.System` (condición `mcu_current_source == 4`) → **`NWD_FASE_D_RDS_Y_SETTINGS.md`** §D.4 **NWD-D001**.

### Fase E — Regresión por firmware
- [ ] Tabla: versión MCU / build → diferencias en acciones o extras (un commit o anexo en este doc).

---

## 5. Plantilla de hallazgo (copiar en cada ítem)

```
ID: NWD-XXX
Fecha:
Firmware / APK: (versionCode, ruta APK)
Archivo / clase: (ej. SprdRadioManager$1.smali)
Hecho: (qué hace el código)
Contrato: (acción, extras con tipos)
Efecto si falla: (ej. sin InitFM → silencio)
OpenRadioFM: (archivo/línea o PR que lo implementa)
```

---

## 6. Hallazgos ya integrados (resumen)

Ver también tabla en **`INTELIGENCIA_QS_NWD.md`** §12.

| Tema | Dónde está en código OpenRadioFM |
|------|----------------------------------|
| `ACTION_APP_IN_OUT` con `extra_app_id = 8` | `QS6Engine` — ver informe `INFORME_MOTOR_QS6_NWD.md` |
| Salida ARM `ACTION_EXIT_ARM_FM_RAIDO` (typo OEM) | `QS6Engine` |
| Coalescencia / throttle arranque | `QS6Engine` (`MIN_FORCE_SOURCE_BROADCAST_MS`, etc.) |
| PS hex Settings + filtro “frecuencia como PS” | `QS6Engine.normalizeNwdPsDisplay` / helpers — ver **`NWD_FASE_D_RDS_Y_SETTINGS.md`** |
| Banda coercida + Settings `freq` → kHz | `QS6Engine` — §D.2 |
| Shadow: broadcasts freq/PS/RT + `ContentObserver` | `QS6Engine.setupShadowMotor` — **`NWD_FASE_D_RDS_Y_SETTINGS.md`** §D.5–D.7 |
| Quién escribe `nwd_radio_*` en Settings | OEM: `RadioProtocalUtil.responseCurrentFrequency` → `SettingTableKey` (§D.4 NWD-D001) |
| TP real (bit 0x8) + `nwd_radio_rds_enable` | `QS6Engine.refreshTpTrafficProgramFromNwd` — **`INTELIGENCIA_QS_NWD.md`** §8 |
| Banda FM1–FM3 + `nwd_radio_current_band` | `QS6Engine` `coerceQs6BandForDisplay`, shadow, `pushUiFromCurrentFrequencyAidl` — §9 inteligencia |

Al descubrir **nuevo** comportamiento, añadir fila a la tabla anterior y una entrada con la plantilla §5.

---

## 7. Siguiente acción concreta (orden sugerido)

1. Abrir en **JADX** el APK más reciente de **`com.nwd.radio.service`** de **tu** unidad (no solo copias antiguas).
2. Ir a `SprdRadioManager$1` (y equivalente AW) y **volcar a este doc** la tabla de `extra_app_id` → ramas.
3. Repetir para `KernelUtils.appStart` / `SourceMgr` (referencias ya visibles en `nwd_kernel_service_decompiled`).
4. Añadir sección **“Diferencias vs OpenRadioFM actual”** cuando encuentres divergencias.

---

## 8. Enlace con ramas Git

Los cambios probados en unidad QS6 pueden vivir en la rama **`QS_NWD`**. Este estudio es **documentación viva**: puedes versionarlo en la misma rama o en `main` cuando hagas merge del informe, sin incluir binarios OEM en el repo.

---

*Documento: plan de estudio — OpenRadioFM / NWD.*
