# User Manual - OpenRadioFM v3.0 (The Car Experience)

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
- **Band:** Switch between FM1, FM2, FM3, AM1, and AM2.
- **LOC/DX:** Change reception sensitivity (Local/Long Distance). *Long press to change layout.*
- **Scan:** Automatic station scanning.
- **Mute:** Instant silence.
- **GPS [New]:** Short press to open your favorite navigation app (Maps, Waze, etc.).
  - *Hidden Menu:* Fast-click the GPS icon **5 times** to open the factory technical menu.

### 1.3 Frequency & RDS Control
- **Left Arrow (<):** Decrease frequency (-0.05 MHz). Long press for automatic seek down.
- **Right Arrow (>):** Increase frequency (+0.05 MHz). Long press for automatic seek up.
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

### Station Logos
- **Location:** `/sdcard/RadioLogos/`
- **Manual:** You can add your own logos in `.png` format named with the frequency (e.g., `96900.png`).
- **Custom Names:** Long press the station name on screen to rename it. The app will look for logos matching that name.

---

## 4. Troubleshooting

- **GPS doesn't open:** Ensure you have a maps app installed and set as default in Android.
- **Dynamic background not changing:** Check if the current station has an assigned logo. If no logo is found, the background defaults to black.
- **SEEK buttons inverted:** In this v3.0 version, orientation has been fixed to match most steering wheel controls.

---
*Developed by Jimmy80 - v3.0 (February 2026)*
