# User Manual - OpenRadioFM v6.0 (Stability & UI Tuning)

Welcome to OpenRadioFM v3.0, the ultimate evolution of FM radio for Android Head Units. This version is optimized for both vertical use and widescreen displays (1024x600) with a brand-new horizontal layout.

---

## 1. Interface & Navigation

### 1.1 Screen Layouts
OpenRadioFM now features two main designs:
- **V2 (Vertical/Classic):** Optimized for tablet-style or vertical screens. *Now also supports dynamic backgrounds.*
- **V3 (Horizontal/Premium) [New]:** Widescreen design ideal for car dashboards, featuring larger icons, gallery-style logos, and graphical band indicators.
- **How to switch:** Long press the **LOC/DX** button to toggle between horizontal and vertical layouts. The app will automatically restart with the new look.

### 1.2 Bottom Control Panel (V3)
- **Settings (EQ):** Short press for system equalizer. **Long press** for the Premium Customization Menu.
- **Band:** Switch between FM1, FM2, and FM3.
- **LOC/DX:** Change reception sensitivity (Local/Long Distance). *Long press to change layout.*
- **Scan:** Automatic station scanning.
- **Mute:** Instant silence.
- **GPS [New]:** Short press to open your favorite navigation app (Maps, Waze, etc.).
  - *Hidden Menu:* Fast-click the GPS icon **5 times** to open the factory technical menu.

### 1.3 Frequency & RDS Control
- **Left Arrow (<):** Decrease frequency. **Short press:** Manual step (-0.05 MHz). **Long press:** Automatic seek down.
- **Right Arrow (>):** Increase frequency. **Short press:** Manual step (+0.05 MHz). **Long press:** Automatic seek up.
- **Fluid Tuning (Drag) [New v4.3]:** You can drag your finger laterally over the frequency box for ultra-precise manual adjustment. Every ~30 pixels of movement equals one frequency step.
- **RDS:** Dynamic display of station name and informative text (RadioText) with smooth scrolling.

---

## 2. Premium Customization (Secret Menu)

Long press the **Settings (EQ)** button to access the customization center:

### 2.1 Theme Colors
Choose from 10 color schemes (Orange, Blue, Red, Cyan, etc.) applied to UI borders and accents.

### 2.2 Typography (Fonts)
Change the look of all texts. Available options:
- **System:** Android default.
- **Bebas:** Industrial/Robotic style.
- **Digital:** Classic LED style of vintage radios.
- **Inter:** Modern and minimalist (high readability).
- **Orbitron:** Futuristic and high-tech style.

### 2.3 Background Mode
Manage how your radio's background looks:
1. **Pure Black:** Ideal for OLED/AMOLED screens or night driving.
2. **background.png Image:** Load your custom image from `/sdcard/RadioLogos/background.png`.
3. **Dynamic Logo [New]:** The radio background automatically changes to the current station's logo, applied with an elegant blur effect.

---

## 3. Logo & Name Management

### 3.2 Car Brand Logo [New v3.0]
In the horizontal layout (V3), you can display your car's logo on the right side:
- **Location:** `/sdcard/RadioLogos/car_logo.png`
- **Requirement:** The image must be named exactly `car_logo.png`. A transparent background is recommended for better aesthetics.

---

## 4. Favorites Management (Save/Load) [New v4.0]

You can now backup your favorite stations and share them between devices.

### 4.1 Save Favorites (Backup)
1. Press the **💾 (Floppy Disk)** button in the right column (V2) or from the options menu.
2. Select **"Save Favorites"**.
3. A `.fav` file will be created in the `RadioLogos` folder with the current date and time.

### 4.2 Load Favorites (Restore)
1. Press the **💾** button.
2. Select **"Load Favorites"**.
3. Choose the file from the list. Stations will update instantly.

---

## 5. Advanced Configuration

### 5.1 Language Selector [New v4.0]
You can change the app language independently of the system:
1. Long press **EQ** to open the Premium Menu.
2. Go to **"App Options"** > **"🌍 App Language"**.
3. Select Español, English, or Русский. The app will restart to apply changes.

### 5.2 Premium Night Mode
Night mode now not only dims the screen but applies a "Night Blue" visual theme to:
- FM band icons and MHz labels.
- Control button borders.
- Frequency and info texts.

---

## 4. Troubleshooting

- **GPS doesn't open:** Ensure you have a maps app installed and set as default in Android.
- **Dynamic background not changing:** Check if the current station has an assigned logo. If no logo is found, the background defaults to black.
- **Logos disappear after reboot:** Version 3.0 adds integration with the Android Media Scanner to ensure logos always persist.
- **SEEK buttons inverted:** In this v3.0 version, orientation has been fixed to match most steering wheel controls.
- **Decimal point in frequency:** The frequency display now forces a dot separator (e.g., 108.0) for a cleaner look across all systems.

---
*Developed by Jimmy80 - v6.0 (February 2026)*
