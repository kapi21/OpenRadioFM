# Informe K706: mandos multimedia en segundo plano y estabilidad

Fecha: 2026-04-17  
Proyecto: OpenRadioFM  
Motor afectado: **K706 (QuickFish)**

## Objetivo

Conseguir que **los mandos multimedia OEM** (volante/launcher/widget OEM) y los controles estándar de Android
controlen OpenRadioFM **en segundo plano**, con comportamiento de app multimedia (sesión de medios, AudioFocus y
servicio en foreground cuando procede), sin depender de servicios de accesibilidad.

## Problemas observados

- **Los KeyEvents de medios no llegaban a la app** cuando el launcher OEM estaba al frente; el sistema entregaba los
  eventos al launcher o a la app de radio de fábrica.
- Bajo estrés (ráfagas de NEXT/PREV/SEEK), la radio podía quedar **pillada** por:
  - trabajo repetitivo de recuperación de audio (recovery) encadenado tras cada pulsación,
  - spam de excepciones relacionadas con Broadcom en ROMs sin soporte.
- Tras pulsar **PAUSE**, podía quedar muteada y luego “muerta”: los mandos ya no reactivaban la reproducción.

## Solución implementada (resumen técnico)

### 1) Bridge OEM de KeyEvents (K706) vía `util_service`

En `RadioMediaService` se añadió un bridge que registra un listener en el servicio OEM `util_service` para recibir
`KeyEvent` (KEYCODE_MEDIA_*). Esto permite que el servicio procese los mandos incluso cuando la app está en segundo plano.

- El listener se registra cuando el engine está listo.
- Se implementaron `hashCode()`, `equals()` y `toString()` en el proxy para evitar fallos internos del manager OEM.
- Se añadió un `unregister` en `onDestroy()` para evitar fugas.

### 2) Ajuste de “PAUSE no mata controles”

Tras `PAUSE` se mantiene `mUserPaused=true` para modelar la intención del usuario, pero el bridge OEM:

- **sigue aceptando PLAY / PLAY_PAUSE** para “revivir” la sesión,
- bloquea el resto (NEXT/PREV/SEEK) mientras el usuario está pausado.

Resultado: la radio puede pausarse y reanudarse desde mandos OEM sin quedar en estado “sin control”.

### 3) Anti-estrés: limitación de ráfagas de teclas

El bridge OEM aplica un throttle por `keyCode` (ventana de ~180 ms) para evitar que pulsaciones repetidas o botones
mantenidos saturen el servicio con operaciones pesadas (notificación, foreground, llamadas a motor).

### 4) Evitar recovery agresivo tras SEEK/NEXT/PREV

Se introdujo un flujo “audible sin recovery” para K706:

- Tras SEEK/NEXT/PREV, se actualiza estado de reproducción y se desmutea **sin** disparar secuencias completas de
  recuperación que podían pisar la frecuencia o causar inestabilidad.
- En K706 se evita explícitamente pasar por `PlaybackManager.setMute(false)` en este contexto, porque esa ruta puede
  forzar recovery agresivo en el motor.

### 5) Broadcom: desactivar reintentos si la ROM no soporta FmProxy

En `K706RadioManager` se detecta `ClassNotFoundException` de `com.broadcom.bt.app.fm.FmProxy` y se marca el override
como **no soportado** para esa ROM:

- se evita reintentar en cada recovery,
- se elimina spam de logs/excepciones bajo estrés.

### 6) Limpieza de recursos (estabilidad)

En `K706RadioManager` se reforzó el cierre/limpieza:

- desregistro del `PhoneStateListener`,
- unbind seguro del binding Broadcom (si llegara a realizarse),
- evitar fugas por múltiples binds.

## Archivos principales tocados

- `app/src/main/java/com/example/openradiofm/service/RadioMediaService.java`
- `app/src/main/java/com/example/openradiofm/data/source/K706RadioManager.java`
- `app/src/main/java/com/example/openradiofm/data/source/K706Engine.java`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/openradiofm/receiver/OemRadioWidgetReceiver.java`

## Resultado

- Mandos OEM (volante/launcher/widget) **controlan la radio en segundo plano** de forma consistente.
- `PAUSE` + `PLAY` desde mandos OEM **funciona** (no se queda bloqueado).
- Bajo estrés, el sistema se mantiene estable (menos recovery repetitivo + sin spam Broadcom + throttle de eventos).

