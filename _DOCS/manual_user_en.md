# User Manual - OpenRadioFM v4.0 (Professional Edition)

Welcome to **OpenRadioFM v4.0**, the ultimate evolution of FM radio for Android Head Units. This "Professional Edition" is optimized for maximum stability, impeccable visual quality, and seamless vehicle hardware integration.

---

## 1. Interface & Navigation

### 1.1 Screen Layouts
OpenRadioFM features two main designs that you can toggle:
- **V2 (Classic Vertical):** Optimized for tablet-style or vertical screens. *Now with total stability in RDS text display.*
- **V3 (Premium Horizontal):** Widescreen design ideal for car dashboards, featuring large-format icons and the new **"Glass Mode"** background.
- **How to switch:** Long press the **LOC/DX** button to toggle between designs. The app will automatically restart.

### 1.2 Signal Quality Indicator [New v4.0]
The antenna icon (`level_signal.png`) is now intelligent and changes color based on actual reception quality:
- 🟢 **Green:** Excellent signal (Stereo detected and RDS synced).
- 🟡 **Yellow:** Medium signal (Stereo only or RDS only available).
- 🔴 **Red:** Poor or no signal.

---

## 2. Premium Customization (Secret Menu)

Long press the **Settings (EQ)** button to access the customization center:

### 2.1 Theme Colors
Choose from 10 color schemes applied to borders and accents. In **Night Mode**, the app will force the "Night Blue" color to reduce visual fatigue.

### 2.2 Background Mode (Glass Mode)
1. **Pure Black:** Maximum contrast.
2. **background.png Image:** Load your custom image from `/sdcard/RadioLogos/background.png`.
3. **Dynamic Logo (Glass Mode):** The background is automatically generated from the station logo, creating an elegant frosted glass effect. In V3, this effect works even if you choose to hide the central logo.

---

## 3. Logo & Name Management

### 3.1 Station Logos (Hybrid Logic)
The app looks for logos in three ways:
1. **Local:** In `/sdcard/RadioLogos/frequency.png` (e.g., `94.1.png`).
2. **Online API:** If enabled in settings, downloads logos automatically.
3. **Priority:** Local logos always take priority over online ones.

### 3.2 Car Brand Logo
In Layout V3, you can display your brand on the right side:
- Location: `/sdcard/RadioLogos/car_logo.png`

---

## 4. Favorites Management (Save/Load)

### 4.1 Save and Load
Use the **Floppy Disk (💾)** button to open the `.fav` file manager. This allows you to move your favorite stations between different radios or perform backups before resetting the device.

---

## 5. Hardware Configuration

### 5.1 Radio Engine
If your radio doesn't tune correctly, go to **Hardware Settings** and select your engine:
- **HCN:** For most Eonon, Xtrons, and similar units.
- **MTK:** For standard Mediatek boards.
- **TS/SYU:** For TopWay or Joying units.

---

## 6. Troubleshooting

- **Buttons move around:** This has been fixed in v4.0. RDS boxes are now fixed-size to prevent visual jumping.
- **Dynamic background not loading:** Ensure the option is enabled in the Premium menu and that the station has a logo (local or remote).
- **Night mode blue color not visible:** Verify that "Automatic Night Mode" is enabled in the app options.

---
*Developed with ❤️ by Jimmy80 for the Android Head Unit community - v4.0 Final*
