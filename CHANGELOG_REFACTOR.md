# CHANGELOG - Refactorización de Arquitectura Modular (OpenRadioFM)

## v22.5 - "Total Independence"
**Fecha:** 12 de Abril, 2026

### 🔄 Resumen de Cambios
Se ha completado la migración de la lógica de hardware a un patrón de **Adaptador de Sintonizador (TunerAdapter)**, desacoplando totalmente la aplicación de los servicios AIDL nativos específicos de cada fabricante.

### 🛠️ Motores Actualizados
1.  **QS6 (Nowada)**:
    *   Migración a `NWDTunerAdapter`.
    *   Sincronización con el SDK oficial de Nowada (Callbacks corregidos).
    *   Implementación de comandos de redundancia (Shadow Intents) para Mute y Scan.
2.  **MT8163 (HCN)**:
    *   Migración a `MT8163TunerAdapter`.
    *   Corrección de firmas de métodos de sintonización.
3.  **MTK8259/8667 (TopWay)**:
    *   Migración a `TopwayTunerAdapter` (Doble vínculo ITsCommon + ITsSpeechRadio).
    *   Sincronización con la API real de TS (TurnBandAndFq, seekUp/Dn).
4.  **K706 & Jancar**:
    *   Sincronización con la nueva interfaz `RadioEngine` (implementación de `setBand`).

### 📐 Arquitectura
*   **RadioEngine Interface**: Evolucionada para incluir `setBand(int)` y estandarizar el retorno de `requestPlayAudio()` a booleano.
*   **RadioServiceController**: Centralización de la instanciación de motores mediante constructores limpios.
*   **RadioMediaService**: Eliminación de casquillos de código específicos (casting de motores en callbacks).

### ✅ Estado Final
*   **Compilación**: Exitosa (APK generada).
*   **Dependencias**: Reducidas.
*   **Mantenibilidad**: Alta (Añadir un nuevo chip ahora solo requiere crear un nuevo `TunerAdapter`).
