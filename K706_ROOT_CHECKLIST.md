# Checklist — K706 Root Edition (OpenRadioFM)

Este documento define el checklist de implementación para una edición “root” en **QF K706** cuyo objetivo es que OpenRadioFM sea la **radio principal** del dispositivo, usando **solo el motor K706**, y neutralizando por completo la radio OEM.

## 0) Objetivo y alcance

- [ ] **Alcance HW**: solo **QF K706** (ROM: última operativa).
- [ ] **Motor**: forzar que toda la ruta FM use **K706** (sin fallback a servicios OEM).
- [ ] **Anulación OEM**: neutralizar las apps/servicios de radio nativos detectados en la ROM:
  - [ ] `com.android.fmradio.ext` (priv-app + `FmService`)
- [ ] **Rollback**: el usuario debe poder **restaurar** el estado original (reenable) sin reflashear.

## 1) Descubrimiento por ADB (baseline por ROM)

- [ ] Guardar `ro.build.fingerprint` y `getprop` relevantes.
- [ ] Registrar paquete(s) de radio OEM presentes:
  - [ ] `pm list packages | grep -i radio` (o listado equivalente)
  - [ ] `cmd package dump com.android.fmradio.ext`
- [ ] Identificar activities/receivers/services OEM que arrancan radio en boot o por launcher.

## 2) Nivel A (sin tocar /system): APK normal + root (modo “hijack”)

### 2.1 RootOps (ejecución `su -c`)
- [ ] Detectar root de forma robusta (`su -c id`).
- [ ] Wrapper de comandos con timeout + stdout/stderr + códigos de salida.
- [ ] Log exportable (para soporte por ROM).

### 2.2 Neutralización OEM (reversible)
- [ ] Implementar “Aplicar modo root”:
  - [ ] `pm disable-user --user 0 com.android.fmradio.ext`
  - [ ] `am force-stop com.android.fmradio.ext`
- [ ] Implementar “Restaurar radio OEM”:
  - [ ] `pm enable com.android.fmradio.ext`
- [ ] Verificación post-acción (enabled/disabled) con `cmd package dump <pkg>`.

### 2.3 Persistencia (boot + auto-repair)
- [ ] `BOOT_COMPLETED` receiver: si modo root activo, re-aplicar neutralización.
- [ ] (Opcional) watchdog ligero (JobScheduler) para detectar re-habilitado por ROM.

### 2.4 “Ser la radio principal” (UX / sistema)
- [ ] `MediaSession` + notificación MediaStyle consistentes (play/pause, next/prev, scan).
- [ ] AudioFocus bien gestionado (especialmente en head units con otras fuentes).
- [ ] Acceso directo/Tile/atajos (según launcher OEM) para abrir OpenRadioFM.

## 3) Nivel B (modo sistema): módulo Magisk + (opcional) system/priv-app

### 3.1 Magisk module (recomendado)
- [ ] Crear módulo con:
  - [ ] `module.prop`
  - [ ] `post-fs-data.sh` (si aplica)
  - [ ] `service.sh` (aplicar disable/force-stop en boot)
- [ ] Añadir “uninstall” limpio (restaurar `pm enable`).
- [ ] (Opcional) auto-lanzar OpenRadioFM tras boot.

### 3.2 System/priv-app (avanzado)
- [ ] (Opcional) empaquetar APK como `system/priv-app/OpenRadioFM/OpenRadioFM.apk` (systemless con Magisk).
- [ ] Documentar limitación: permisos *signature/privileged* requieren firma ROM o `privapp-permissions` compatible.

## 4) K706 engine only: garantías técnicas

- [ ] Asegurar que en modo root:
  - [ ] no se usa `com.android.fmradio.IFmRadioService`
  - [ ] no se depende de intents/servicios OEM para sintonizar
- [ ] Recovery de tuner:
  - [ ] re-init si el OEM dejó el chip en estado inconsistente
  - [ ] restaurar banda/frecuencia/estado de mute coherentes

## 5) QA / Validación en hardware real (K706)

- [ ] Boot en frío: OpenRadioFM abre y la radio OEM no aparece.
- [ ] Controles volante / mandos multimedia: funcionan con OpenRadioFM.
- [ ] Convivencia con Spotify/AA/Z-Link: audio focus estable.
- [ ] Seek/Scan: sin “rebotes” ni bloqueos.
- [ ] Rollback: “Restaurar radio OEM” devuelve el dispositivo a estado original.

## 6) Entregables

- [ ] Pantalla “Modo Root” (asistente) con:
  - [ ] detectar root
  - [ ] aplicar / restaurar
  - [ ] verificación y logs
- [ ] Módulo Magisk (zip) con instrucciones de instalación/desinstalación.

---

## 7) Estado en repo (abril 2026, rama `K706_Root`)

- Módulo Magisk en `magisk/K706_Root/` operativo a nivel de scripts; generación de ZIP con LF en `magisk/build_k706_root_zip.bat`.
- `service.sh`: disable OEM + escaneo universal de `shared_prefs` para atajo RADIO; `uninstall.sh`: rollback.
- App: broadcasts QF al HOME actual; fix Glide en logos; interceptación `/customize/radio/*`.
- **Seguimiento:** ver [`HANDOFF_K706_ROOT.md`](HANDOFF_K706_ROOT.md) (v1.1: APK trampolín + overlay `system/`; **instalación del ZIP en unidad** pendiente de cerrar; Nivel A UI).

