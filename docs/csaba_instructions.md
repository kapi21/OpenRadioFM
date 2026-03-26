# OpenRadioFM — MTK8259 Audio Streaming Test
### Instructions for Csaba / Instrucciones para Csaba

---

## 🇬🇧 ENGLISH

### What was fixed
A bug caused FM radio audio to mix with online streaming instead of being silenced. Three test strategies have been implemented, selectable from the Engineering Menu.

### How to test

1. **Install** the new APK on your MTK8259/8667 unit.
2. Open OpenRadioFM and **long-press the logo** (or use your usual gesture) to open the **Engineering Menu**.
3. Scroll down to the **`DEV_TOGGLES_HW`** section.
4. **Start with ALL MTK8259 switches OFF** — this tests the base fix (race condition corrected + 150ms delay before FM recovery).
5. Start an online stream and check if FM audio bleeds through.

### If FM still bleeds during streaming → Try strategies in order:

| Switch | Label in menu | What it does |
|--------|--------------|-------------|
| **[B]** | `MTK8259 [B]: Mute verified with retries` | Calls [Mute()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/data/source/MTK8259_8667Engine.java#220-224), waits 50ms, checks `IsMute()`, retries if needed. Best first option. |
| **[B]+[C]** | Also enable `MTK8259 [C]: Skip EnterMode()` | Removes the [EnterMode()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/data/source/MTK8259_8667RadioManager.java#77-90) call that may wake the HW mixer. Experimental. |
| **[A]** | `MTK8259 [A]: Legacy v5.0 (CloseRadioCh only)` | Minimal: only closes the FM channel, no mute. Try if [B] causes FM to stay silent after stopping stream. |

> ⚠️ **Only activate ONE strategy at a time.** Exception: [B] and [C] can be used together.

### What to report back
- Which strategy (A, B, C, B+C, or none) solved the issue
- Whether FM returns correctly when stream is stopped
- Any logcat output from tags: `MTK8259_8667RM`, [OnlineStreamManager](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/OnlineStreamManager.java#18-312)

---

## 🇪🇸 ESPAÑOL

### Qué se ha corregido
Un bug provocaba que el audio de la FM se mezclara con el streaming online en lugar de silenOciarse. Se han implementado tres estrategias de prueba, seleccionables desde el Menú de Ingeniería.

### Cómo probar

1. **Instala** la nueva APK en tu unidad MTK8259/8667.
2. Abre OpenRadioFM y **mantén pulsado el logo** (o el gesto habitual) para abrir el **Menú de Ingeniería**.
3. Baja hasta la sección **`DEV_TOGGLES_HW`**.
4. **Empieza con todos los switches MTK8259 en OFF** — esto prueba la corrección base (race condition arreglada + 150ms de delay antes de recuperar la FM).
5. Activa un stream online y comprueba si se escucha la FM por debajo.

### Si la FM sigue colándose durante el streaming → Prueba las estrategias en orden:

| Switch | Texto en el menú | Qué hace |
|--------|----------------|---------|
| **[B]** | `MTK8259 [B]: Mute verificado con reintentos` | Llama a [Mute()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/data/source/MTK8259_8667Engine.java#220-224), espera 50ms, comprueba `IsMute()`, reintenta si no cambió. Primera opción recomendada. |
| **[B]+[C]** | Activa también `MTK8259 [C]: Omitir EnterMode()` | Elimina la llamada [EnterMode()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/data/source/MTK8259_8667RadioManager.java#77-90) que puede despertar el mixer HW. Experimental. |
| **[A]** | `MTK8259 [A]: Legacy v5.0 (solo CloseRadioCh)` | Mínimo: solo cierra el canal FM, sin mute. Probar si con [B] la FM queda silenciada al parar el stream. |

> ⚠️ **Activa solo UNA estrategia a la vez.** Excepción: [B] y [C] pueden usarse juntos.

### Qué reportar
- Qué estrategia (A, B, C, B+C o ninguna) resolvió el problema
- Si la FM vuelve correctamente al parar el stream
- Cualquier log de `adb logcat` con los tags: `MTK8259_8667RM`, [OnlineStreamManager](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/OnlineStreamManager.java#18-312)
