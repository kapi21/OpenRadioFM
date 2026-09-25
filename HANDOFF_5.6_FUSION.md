# Handoff — OpenRadioFM 5.6 FUSION Edition

Documento de continuidad y cierre de release para OpenRadioFM v5.6 FUSION.

- **Rama activa:** `main` (sincronizada con `origin/main`).
- **Tag de versión:** `v5.6-fusion` (publicado en GitHub).
- **Versión de la app:** `5.6 FUSION` (VersionCode `44`).
- **Binario Release:** [`_RELEASES/OpenRadioFM — v5.6 FUSION.apk`](./_RELEASES/OpenRadioFM%20%E2%80%94%20v5.6%20FUSION.apk) (20.5 MB).
- **Web oficial:** [https://kapi21.github.io/OpenRadioFM/](https://kapi21.github.io/OpenRadioFM/) (desplegada en GitHub Pages).
- **Fecha de cierre:** 2026-09-25.

---

## 1. Resumen de Hitos y Cambios Implementados

### 1.1 Fusión Conectividad Offline / Online
- **Selector Inicial:** Diálogo en primer arranque/actualización (`dialog_connectivity_mode_notice.xml`) para seleccionar cómodamente entre modo 100% Offline (sin datos, logos locales) o modo Online (nube, logos dinámicos y streaming en vivo).
- **Persistencia Determinista:** `pref_offline_mode` almacenado y respetado sin sobreescrituras forzadas.
- **Streaming Desacoplado:** La radio por internet funciona de forma autónoma sin depender de tener activos los logotipos.

### 1.2 Mandos al Volante en Segundo Plano (QS6 / NWD / Topway)
- **Broadcast Interceptor OEM:** Implementada escucha activa de `com.nwd.action.ACTION_KEY_VALUE` y `ACTION_TEST_KEY` en `RadioMediaService`.
- **Mapeo de Teclas Panel/MCU:** Enrutamiento directo a `handleSteeringSkip` y `handleWidgetPresetSkip`.
- **Retención de AudioFocus:** Mantenimiento de foco continuo en segundo plano para evitar que el sistema o el launcher reasignen los controles a la app de radio OEM.
- **Validación en hardware real:** Verificado funcionamiento fluido con launchers de terceros (p. ej. Agama Car Launcher). *Nota técnica:* El launcher de serie de algunas ROMs QS6 enlaza sus teclas exclusivamente al widget oficial si está presente en el escritorio.

### 1.3 Internacionalización Completa (13 Idiomas)
- Sincronización y paridad al 100% de todas las cadenas de texto (`ES`, `EN`, `DE`, `FR`, `HU`, `IT`, `JA`, `PT`, `RO`, `RU`, `SR`, `UK`, `ZH`).
- Actualizada la descripción de accesibilidad HiHack explicando el soporte de teclas de volante en K706 y QS6.

### 1.4 Web y Documentación Oficial
- Portadas y metadatos actualizados en 8 idiomas bajo `docs/` para GitHub Pages.
- `README.md` actualizado con badges v5.6 FUSION y reorganización cronológica del historial de versiones (v5.6, v5.5, v5.2.2, v5.2.1, v5.2.0, v5.1.0).

---

## 2. Comandos Útiles

### Instalación rápida de Release:
```powershell
adb install -r "_RELEASES\OpenRadioFM — v5.6 FUSION.apk"
```

### Recompilación Release:
```powershell
.\gradlew assembleRelease
```
