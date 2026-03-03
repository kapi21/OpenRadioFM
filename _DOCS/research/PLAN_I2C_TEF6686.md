## 🛑 Bloqueo Actual y Bypass Hardware (Marzo 2026)

Tras analizar el firmware del MCU y confirmar que versiones recientes (Nov/Dic 2025) incluyen intentos de "reducir la tasa de error del RDS" y "arreglar la detección de SNR", se evidencia que el procesador intermediario (ST MCU) es incapaz de leer correctamente el bus de alta velocidad del chip de radio TEF6686.

**El Problema**:
El MCU actúa como cuello de botella. Recibe la ráfaga de datos RDS (Bloques A, B, C, D) del TEF6686, pero su firmware descarta paquetes válidos o los corrompe antes de enviarlos por puerto serie (`0xB0`-`0xBF`) a Android. Es por esto que obtenemos el PS Name fragmentado y nunca recibimos el Radio Text (`0xB7`) limpio.

**La Solución: Hardware Hacking (Bypass I2C)**
Dado que el problema es de hardware/firmware cerrado, la única forma de obtener los datos puros (incluyendo el ansiado PI Code) es "espiar" la comunicación entre el MCU y el chip de radio TEF6686.

**Plan de Ejecución:**
1. **Identificar Pines**: Localizar el chip NXP TEF6686 en la placa base de la radio.
2. **Conexión Lógica**: Conectar un Analizador Lógico (24MHz) a los pines SCL y SDA del bus I2C que une el TEF6686 con el MCU.
3. **Captura**: Sintonizar una emisora fuerte y capturar el tráfico.
4. **Decodificación**: Analizar las tramas I2C buscando los comandos de configuración del TEF6686 y sus respuestas RDS para entender exactamente qué datos crudos está ignorando el MCU.
