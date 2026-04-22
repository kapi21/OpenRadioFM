# OpenRadioFM — K706 Root Hijack (Magisk)

## Qué hace
- En cada arranque, **deshabilita** la FM OEM real: `com.android.fmradio.ext`
- Al desinstalar el módulo, **restaura** el paquete (`pm enable`).

## Instalación
1) Comprime la carpeta `magisk/K706_Root/` como ZIP manteniendo esta estructura:

```
module.prop
service.sh
uninstall.sh
```

2) Instala el ZIP desde Magisk → Modules.
3) Reinicia la unidad.

## Verificación (ADB)
```sh
adb connect 192.168.1.98:9876
adb -s 192.168.1.98:9876 shell su -c "pm disable-user --user 0 com.android.fmradio.ext"
```

## Rollback
- Desinstala el módulo desde Magisk y reinicia.

