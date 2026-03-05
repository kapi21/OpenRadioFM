# Informe de Daños y Estado de Bloqueo (5 Marzo 2026)

Este documento detalla los fallos críticos persistentes tras el intento de corrección de la V17.0 y sirve como punto de partida para la depuración profunda.

## 1. Radio Online: Silencio Absoluto
- **Estado**: BLOQUEADO.
- **Síntoma**: Al pulsar el icono de la nube (Streaming), la UI indica "Cargando" (Amarillo) y luego "Reproduciendo" (Cian), pero no sale audio por los altavoces.
- **Hallazgos Técnicos**:
    - Se eliminó el mute de `STREAM_MUSIC` en `PlaybackManager`, por lo que Android "cree" que está reproduciendo.
    - Se forzó `RPC_SetChannel(4)` (Canal Android) y `Mute(false)` en el MCU.
- **Sospecha**: El MCU de la K706 es extremadamente sensible al orden de comandos. Es posible que el `setMute(false)` deba ejecutarse *antes* o *varios ms después* del cambio de canal, o que el Android Audio Patch de la unidad esté bloqueado por un proceso fantasma del sintonizador nativo.

## 2. FM Radio: Seek vs Volumen
- **Estado**: FALLIDO / INESTABLE.
- **Síntoma**: Al pulsar "Seek Up/Down" (pulsación larga), a veces no ocurre nada o el sistema interpreta que se está subiendo/bajando el volumen en lugar de buscar frecuencias.
- **Hallazgos Técnicos**:
    - Se cambió el envío de bytes directos al MCU (`0xA0 0x01/0x02`) por el método de alto nivel `QFTunerManager.onSeek()`.
- **Sospecha**: 
    - Conflicto de IDs de botones en el Layout V3 que podrían estar capturando eventos del sistema.
    - El SDK `QFTunerManager` podría estar devolviendo un error silencioso si no tiene el foco de audio "real" según el sistema.
    - El hecho de que se mueva el volumen sugiere que el MCU está recibiendo códigos de tecla de volante (KeyEvents) en lugar de comandos de tuner.

## 3. Estabilidad de Layouts (V2 vs V3)
- **Estado**: PARCIALMENTE ARREGLADO.
- **Síntoma**: Se corrigió el crash `ClassCastException` del inicio, pero la respuesta táctil en el Layout 2 sigue siendo reportada como "difícil" o "no se deja pulsar".
- **Hallazgos Técnicos**: El `ivDataActivity` (nube) ahora es un `FrameLayout` que envuelve un `ImageView`. 
- **Sospecha**: Problema de Z-Order o márgenes en el XML que hacen que otros elementos transparentes (como el dial o las guías) solapen el área de pulsación.

---

## Recomendaciones para el Siguiente Turno
1. **Logcat Filtrado**: Ejecutar `adb logcat -s K706RadioManager QFApi mcu_services` para ver exactamente qué bytes salen al pulsar Seek.
2. **Delayed Execution**: Probar a meter un delay de 200ms entre el `returnAudioChannel()` y el `setMute(false)`.
3. **Layout Inspection**: Revisar en Android Studio el "Layout Inspector" para ver si hay un View invisible tapando el icono de la nube en V2.
