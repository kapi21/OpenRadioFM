# Plan de Refinamiento Estético y Organización

## Objetivos
1. Mover la documentación de la IA al proyecto para fácil acceso.
2. Hacer que los menús respeten la tipografía elegida por el usuario.
3. Unificar el estilo premium (negro traslúcido) en el historial.

## Cambios Propuestos

### Documentación
- Crear `_DOCS/brain_records/` en la raíz del proyecto.
- Mover/Copiar los archivos `.md` de la carpeta de artefactos de la IA a esta nueva ubicación.

### [DialogManager.java](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/DialogManager.java)
- Crear un método `applySystemFontToView(View view)` que recorra los elementos y aplique la `Typeface` obtenida desde `MainActivity` (que a su vez la obtiene de `ThemeManager`).
- Integrar esta llamada en `applyPremiumListStyle` y otros diálogos personalizados.

### Estilo de Historial
- Localizar el diálogo que muestra las emisoras escaneadas o el historial de búsqueda.
- Asegurar que el fondo sea `@drawable/bg_submenu_box` (para diálogos pequeños) o `@drawable/bg_glass_card_premium` (para pantallas completas).

## Verificación
- Abrir Ajustes Premium y cambiar fuente -> Verificar que el propio menú de ajustes cambia su fuente al instante o tras reabrir.
- Abrir Historial -> Verificar fondo traslúcido.
