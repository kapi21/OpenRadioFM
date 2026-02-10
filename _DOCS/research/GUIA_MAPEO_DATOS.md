# Guía de Mapeo de Datos: Gala a OpenRadioFM

Este documento detalla cómo se integran los datos externos en el modelo de `OpenRadioFM`.

## 1. Mapeo de campos

| Campo JSON | Clase Java | Lógica |
| :--- | :--- | :--- |
| `name` | `RadioStation.name` | Directo |
| `frequency` | `RadioStation.freqKHz` | `Float * 1000` |
| `genre` | `RadioStation.pty` | Directo |
| `logo_url` | `RadioStation.logoUrl` | URL remota |

## 2. Jerarquía de Información (Prioridades)
1. **Nombre Personalizado**: Guardado en `SharedPreferences` por el usuario.
2. **RDS Real**: El nombre que emite la radio por el hardware.
3. **Catálogo Predefinido**: Datos de `stations_es.json`.

---
*Documento generado durante la fase de integración v4.5.*
