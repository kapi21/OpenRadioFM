# Handoff — OpenRadioFM v5.7 (Junsun V7 / MT8768)

Documento de continuidad y cierre de release.

- **Rama activa:** `main`.
- **Tag de versión:** `v5.7`.
- **Versión de la app:** `5.7` (VersionCode `45`).
- **Binario Release:** [`_RELEASES/OpenRadioFM — v5.7.apk`](./_RELEASES/OpenRadioFM%20%E2%80%94%20v5.7.apk) (~20.5 MB; origen: build `app-release test 5.7.apk` del 2026-11-07).
- **Web oficial:** [https://kapi21.github.io/OpenRadioFM/](https://kapi21.github.io/OpenRadioFM/).
- **Fecha de cierre:** 2026-11-14 (vía verde tras validación V7 + regresión MT8163).

---

## 1. Qué entra en v5.7

### 1.1 Soporte Junsun V7 / MT8768 (parche Csaba)
- `HcnApiFactory.java`: detección contrato AIDL v25 (MT8163) vs v31 (MT8768); override `pref_hcn_contract` (0=auto, 25, 31).
- `HcnV31Api.java`: proxy Binder para el contrato de 31 comandos.
- Integración vía `HcnApiFactory.wrap()` en `MT8163Engine`, `MT8163TunerAdapter`, `RadioServiceController`.

### 1.2 Regresión MT8163
- Ruta v25 permanece operativa; verificada en hardware real MT8163 antes del cierre.

### 1.3 Branding / i18n
- Motor: `Topway / Eonon / Junsun V7 (MT8163/8768)` en 13 idiomas.

---

## 2. Comandos útiles

### Instalación Release:
```powershell
adb install -r "_RELEASES\OpenRadioFM — v5.7.apk"
```

### Recompilación Release:
```powershell
.\gradlew assembleRelease
```

---

## 3. Créditos
- Parche y validación en Junsun V7 (MT8768): **Csaba**.
