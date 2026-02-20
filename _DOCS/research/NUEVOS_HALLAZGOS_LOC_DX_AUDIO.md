### 5.5 Comandos de LOC/DX y Salida (Descubiertos vía logcat 20 Feb 2026)

Tras analizar los comandos UART en `radio_debug_v72.log` de la aplicación nativa (`com.android.fmradio.ext`), se ha descubierto el verdadero funcionamiento de LOC/DX y la secuencia de salida:

#### Botón LOC / DX
El botón LOC/DX en la aplicación nativa **no usa el sub-comando `0x0A`**. Sorprendentemente, **comparte el sub-comando `0x07`** (que originalmente mapeamos solo a "Change Band").

El comportamiento observado es:
1. Manda `[A0 07 00 00]` -> Activa modo **DX** (Distance)
2. MCU responde con info actualizada en el flag del `0xB0`
3. Manda `[A0 07 01 00]` -> Activa modo **LOC** (Local)
4. MCU responde con info actualizada en el flag del `0xB0` 

    > [!IMPORTANT]
    > Esto significa que el comando `0x07` acepta el modo de sensibilidad de radio como parámetro *opcional* o alternativo en función del byte `Param 1`. Si el bit 0 de `Param 1` de `0x07` es 0 es DX, si es 1 es LOC. Queda por investigar cómo difiere esto funcionalmente del uso de `0x07` para cambiar de banda (seguramente haya una lógica de offset o un bit específico no trackeado previamente).

#### Secuencia de Apagado (Audio)
La aplicación original implementa un teardown riguroso para asegurar que la salida de audio vuelve al sistema multimedia normal cuando la aplicación de radio se cierra o va al fondo.  

```java
// Secuencia capturada del FmService.java nativo:
powerDown();
setMute(true);
setVolume(0);
enableFmAudio(false);
stopRender();
RPC_SetChannel(4); // Audio channel change to MPU (media_type), volume 12
MSGID_FM_EXIT
closeDevice();
```
El log UART correspondiente al cambio de audio de FM a MPU (Android) es:
1. `call RPC_SetChannel(4) from 4141`
2. `APP2MCU - writeToUartNormal : [ff fd fe 04 01 84 04 0c 8d ff ]` (`0x84` parece ser el comando de control de volumen/mixer de canal).
