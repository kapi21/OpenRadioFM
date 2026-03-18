# Corrección Total del Retraso en el Tintado del Modo Noche

El problema es que [setImageResource()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2668-2676) de Android **borra los `colorFilter` activos**, y hay múltiples llamadas asíncronas ([handleFrequencyChange](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2452-2537), [onMuteStateChanged](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#1064-1087), [updateDataActivityUI](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#471-530)) que cambian imágenes DESPUÉS de que [applyNightModeColors()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/NightModeManager.java#102-210) ha tintado todo. Esos elementos pierden el tinte azul y no se recuperan hasta el re-tintado periódico (throttleado a 30 segundos).

## Proposed Changes

### Helpers y Funciones Estáticas

---

#### [MODIFY] [MainActivity.java](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java)

**Cambio 1**: [setImageResourceIfChanged()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2668-2676) (línea 2668) — Preservar tinte noche tras cambiar imagen.

```diff
 public static void setImageResourceIfChanged(android.widget.ImageView iv, int resId) {
     if (iv == null) return;
     Object current = iv.getTag(R.id.tag_image_res);
     if (current == null || (int)current != resId) {
         iv.setImageResource(resId);
         iv.setTag(R.id.tag_image_res, resId);
+        // setImageResource borra colorFilter; re-aplicar si había uno activo
+        Object savedFilter = iv.getTag(R.id.tag_color_filter);
+        if (savedFilter instanceof Integer) {
+            iv.setColorFilter((Integer) savedFilter, android.graphics.PorterDuff.Mode.SRC_IN);
+        }
     }
 }
```

> [!IMPORTANT]
> Esta es la corrección **más impactante**. El tag `R.id.tag_color_filter` ya se usa en [setColorFilterIfChanged()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2651-2667) para trackear el color activo. Al restaurarlo tras [setImageResource()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2668-2676), los botones/iconos conservarán su tinte noche automáticamente sin importar qué método asíncrono cambie su imagen.

**Cambio 2**: `NIGHT_MODE_CHECK_INTERVAL_MS` (línea 197) — Reducir de 30s a 5s.

```diff
-private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 30_000;
+private static final long NIGHT_MODE_CHECK_INTERVAL_MS = 5_000;
```

**Cambio 3**: [applySkin()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2022-2053) (línea 2026) — Mover [applyNightModeColors()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/NightModeManager.java#102-210) como paso FINAL definitivo.

```diff
 public void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
     if (mThemeManager != null) mThemeManager.applySkin(skin);
 
     boolean isNight = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE);
     boolean isClear = (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.CLEAR);
 
     if (mUiController != null) {
         mUiController.applySkin(isNight);
-    } else if (...) { ... }
+    } else if (mIsSimpleLayout && mSimpleLayoutManager != null) {
+        mSimpleLayoutManager.applyColors(isNight);
+    }
 
     applyClearButtonIconTint(isClear && !isNight);
-    // Shared Clock Visibility Color...
+
+    // Paso FINAL: aplicar/resetear colores noche sobre TODOS los elementos
+    // DESPUÉS de que ThemeManager haya puesto fondos y UiController parciales
+    if (isNight) {
+        if (mNightModeManager != null) mNightModeManager.applyNightModeColors(mLastFreq);
+    } else {
+        if (mNightModeManager != null) mNightModeManager.resetNightModeColors(mLastFreq);
+    }
+
+    // Shared Clock Visibility Color (ya cubierto por NightModeManager, pero safe)
+    if (tvDigitalClock != null) {
+        ...
+    }
 }
```

**Cambio 4**: Eliminar el [SkinAppliedListener](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/theme/ThemeManager.java#49-52) en [onCreate()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#987-1268) (línea 1222) para evitar la doble aplicación desordenada.

```diff
-mThemeManager.setSkinAppliedListener(skin -> {
-    if (skin == com.example.openradiofm.ui.theme.ThemeManager.Skin.NIGHT_MODE) {
-        if (mNightModeManager != null) mNightModeManager.applyNightModeColors(mLastFreq);
-    } else {
-        if (mNightModeManager != null) mNightModeManager.resetNightModeColors(mLastFreq);
-    }
-});
```

**Cambio 5**: [onMuteStateChanged()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#1064-1087) (línea ~1077) — Preservar tinte noche tras cambiar icono de mute.

```diff
 if (isMuted && !isMTK) {
     btnMute.setImageResource(R.drawable.radio_mute_p);
 } else {
     btnMute.setImageResource(R.drawable.radio_mute_n);
 }
+// Preservar tinte noche si activo
+Object savedFilter = btnMute.getTag(R.id.tag_color_filter);
+if (savedFilter instanceof Integer) {
+    btnMute.setColorFilter((Integer) savedFilter, android.graphics.PorterDuff.Mode.SRC_IN);
+}
```

---

### Layout Controllers

---

#### [MODIFY] [MainLayoutController.java](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainLayoutController.java)

**Cambio 6**: [updateBandIndicator()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainLayoutController.java#153-167) (línea 153) — Re-aplicar tinte noche tras [setImageResource()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2668-2676).

```diff
 public void updateBandIndicator(int band) {
     if (ivBandIndicator == null) return;
     int drawId;
     switch (band) { ... }
     ivBandIndicator.setImageResource(drawId);
+    // Preservar tinte noche si está activo
+    boolean isNight = (mActivity.mThemeManager != null
+            && mActivity.mThemeManager.getActiveSkin() == ThemeManager.Skin.NIGHT_MODE);
+    if (isNight) {
+        int nightBlue = mActivity.getResources().getColor(R.color.night_blue_primary, null);
+        ivBandIndicator.setColorFilter(nightBlue, android.graphics.PorterDuff.Mode.SRC_IN);
+    } else {
+        ivBandIndicator.clearColorFilter();
+    }
 }
```

---

### ThemeManager (Limpieza)

---

#### [MODIFY] [ThemeManager.java](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/theme/ThemeManager.java)

**Cambio 7**: Eliminar la notificación del listener desde [applySkin()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2022-2053) (ya que la lógica se mueve a `MainActivity.applySkin()` directamente).

```diff
-    // Notificar al listener (Night Mode colors, etc.)
-    if (mListener != null) {
-        mListener.onSkinApplied(skin);
-    }
```

> [!NOTE]
> El listener sigue existiendo en la clase por si se necesita en el futuro, pero ya no se invoca desde [applySkin()](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/java/com/example/openradiofm/ui/main/MainActivity.java#2022-2053). Toda la lógica de night/day colors ahora vive explícitamente en `MainActivity.applySkin()` como paso final.

## Verification Plan

### Manual Verification (usuario)
1. **Compilar y desplegar** la app en el dispositivo
2. **Ciclar skins rápidamente** (click en logo/reloj) pasando por CLASSIC → ORANGE → BLUE → ... → NIGHT_MODE
3. **Verificar que AL LLEGAR a Night Mode**, todos los elementos se tintan de azul noche **instantáneamente**:
   - ✅ Textos (frecuencia, RDS, PTY, reloj)
   - ✅ Botones de control (seek, fav, band, mute, settings, etc.)
   - ✅ Iconos RDS (AF, TA, TP, stereo)
   - ✅ Indicador de banda FM1/FM2/etc.
   - ✅ Presets P1-P18 (textos y logos)
4. **Pulsar Mute mientras en Night Mode** → verificar que el icono cambia pero MANTIENE el tinte azul
5. **Cambiar de frecuencia/banda mientras en Night Mode** → verificar que el indicador de banda NO pierde el tinte azul
6. **Esperar 10-15 segundos en Night Mode** → verificar que nada "parpadea" o pierde tinte
