# Roadmap técnico – OpenRadioFM (solo FM)

Este documento recoge las tareas pendientes para mejorar la app de radio FM, pensando tanto en dispositivos con root como sin root, pero siempre usando solo radio FM (sin radio por internet).

---

## 1. Modos de funcionamiento (solo FM)

- [ ] Crear un flag o enum para diferenciar:
  - `MODO_FM_COMPLETO`: dispositivo con root + servicios especiales del coche.
  - `MODO_FM_BASICO`: dispositivo sin root (solo frecuencia + logos en SD y nombres manuales).
- [ ] En el arranque de la app:
  - [ ] Detectar si existe el servicio `com.hcn.autoradio` (motor de radio del coche).
  - [ ] Comprobar si se puede usar `su` (root) sin errores.
  - [ ] Si todo está disponible → activar `MODO_FM_COMPLETO`.
  - [ ] Si no hay root o el servicio falla → activar `MODO_FM_BASICO`.

---

## 2. Comportamiento en MODO_FM_BASICO (sin root)

- [ ] Desactivar `RootRDSSource`:
  - [ ] No intentar leer `/data/data/com.hcn.autoradio/...` si no hay root.
  - [ ] Hacer que las llamadas a `getRdsName()` devuelvan valores seguros (null/vacío) sin lanzar excepciones.
- [ ] Proteger el uso de APIs internas (`android.radio.RadioPlayer`, etc.):
  - [ ] Solo usarlas si las clases existen (try/catch, comprobación reflección).
- [ ] Mostrar siempre:
  - [ ] Frecuencia actual.
  - [ ] Logos desde `/sdcard/RadioLogos` (esto funciona sin root).
- [ ] Aceptar que en este modo no habrá nombres RDS automáticos por fichero:
  - [ ] Usar nombres personalizados del usuario (ver siguiente sección).

---

## 3. Nombres y logos personalizados por frecuencia

### 3.1. Logos en SD (mantener soporte actual)

- [ ] Mantener el sistema de logos:
  - [ ] `/sdcard/RadioLogos/{frecuencia}.png` (por ejemplo, `87500.png`).
  - [ ] `/sdcard/RadioLogos/{frecuencia_corta}.png` (por ejemplo, `8750.png`).
- [ ] Mejorar logs de depuración:
  - [ ] Mensajes claros cuando un logo existe o no existe.

### 3.2. Nombres personalizados escritos por el usuario

- [ ] Diseñar un esquema de `SharedPreferences`:
  - [ ] Clave tipo `NAME_87500` → valor `"Mi Radio Favorita"`.
- [ ] Lógica de prioridad para mostrar el nombre de la emisora:
  - [ ] 1º: Nombre personalizado del usuario (si existe).
  - [ ] 2º: Nombre RDS obtenido por root (solo en `MODO_FM_COMPLETO`).
  - [ ] 3º: Si no hay nombre, mostrar solo la frecuencia.
- [ ] Interfaz para editar el nombre:
  - [ ] Al mantener pulsado un preset:
    - [ ] Mostrar un diálogo con un campo de texto: “Nombre de la emisora”.
    - [ ] Guardar en `SharedPreferences` el nombre ligado a la frecuencia actual.
    - [ ] Actualizar el texto del preset con el nombre nuevo.
  - [ ] (Opcional) Botón “Editar nombre” para la emisora actual, aunque no sea preset.

---

## 4. Estabilidad y limpieza de recursos (solo FM)

- [ ] Revisar `MainActivity.onDestroy()`:
  - [ ] Cancelar el `Timer` que hace el sondeo periódico de estado.
  - [ ] Desregistrar el callback `mCallback` del servicio `IRadioServiceAPI`.
  - [ ] Llamar a `unbindService(mConnection)` para soltar el contexto de la Activity.
  - [ ] Llamar a `mHiddenPlayer.release()` para evitar fugas por el listener RDS.
  - [ ] Llamar a `mRepository.shutdown()` para cerrar:
    - [ ] El proceso root de `RootRDSSource`.
    - [ ] Hilos en segundo plano del repositorio.
- [ ] Comentar en español, de forma sencilla, qué se libera y por qué, para recordar que esto evita fugas de memoria.

---

## 5. Uso seguro de root

- [ ] En `MODO_FM_COMPLETO`:
  - [ ] Asegurar que `RootRDSSource.getRdsName()`:
    - [ ] Solo se llama desde hilos de fondo (nunca en el hilo de interfaz).
    - [ ] Maneja errores de root sin romper la app (try/catch + logs claros).
- [ ] En `MODO_FM_BASICO`:
  - [ ] No ejecutar `su` ni abrir el XML de RDS interno.
  - [ ] No depender de RDS por fichero; mostrar:
    - [ ] Frecuencia.
    - [ ] Logos de SD.
    - [ ] Nombres personalizados si el usuario los introduce.

---

## 6. Modernización visual (manteniendo solo FM)

- [ ] Revisar y modernizar `activity_main.xml`:
  - [ ] Usar `ConstraintLayout` para organizar mejor los elementos.
  - [ ] Mantener la frecuencia grande y clara como elemento principal.
  - [ ] Mejorar los botones (iconos, estados `pressed/selected`, sombras suaves, etc.).
- [ ] Revisar tipografía:
  - [ ] Decidir si seguir con `Orbitron` o combinarla con otra fuente para textos más largos.
- [ ] Mejorar indicadores (TA/AF/TP, LOC/DX, banda):
  - [ ] Colores con buen contraste.
  - [ ] Claridad de estados “activo/inactivo”.
- [ ] Mensaje para usuarios sin root:
  - [ ] Mostrar una nota breve (por ejemplo, en primera ejecución o en ajustes):
    - “Modo FM básico: sin root no se pueden leer algunos datos RDS avanzados. Puedes poner nombres manuales a tus emisoras.”

---

## 7. Limpieza y documentación del código

- [ ] Añadir comentarios sencillos en español en:
  - [ ] `MainActivity`
  - [ ] `RadioRepository`
  - [ ] `RootRDSSource`
  - [ ] `HiddenRadioPlayer`
- [ ] Explicar para cada clase:
  - [ ] Para qué sirve.
  - [ ] Si trabaja en hilo de interfaz (UI) o en hilos de fondo.
  - [ ] Qué recursos abre (servicios, procesos root, listeners) y dónde se cierran.

---

## 8. Próximos Pasos - v5.0 "Engineering & Performance"

- [x] **Tablero Técnico (Engineering Mode)**:
  - [x] Implementar visualización de métricas de calidad (SNR/RSSI inferidos).
  - [x] Acceso mediante 5 clics en icono GPS (rebuild del menú técnico).
- [x] **RDS PTY 2.0**:
  - [x] Mapeo completo de tipos de programa (Music, News, Sports).
  - [x] Implementar iconos categóricos dinámicos para el PTY.
- [ ] **Optimización de Ciclo de Vida**:
  - [ ] Refinar `unbindService` y sondeo RDS para reducir consumo en viajes largos.
  - [ ] Mejora de persistencia de logos remotos en caché.

---
*Roadmap actualizado: Febrero 2026 – Enfocado en v5.0*