# User Manual - OpenRadioFM v.5.0.0-Beta "Stability Edition"
Welcome to **OpenRadioFM v.5.0.0**, the edition focused on extreme hardware stability and visual refinement. This version introduces the new **Light Mode (White Skin)** and critical protections for MediaTek engines.

---

## 1. Interface & Navigation

### 1.1 Screen Layouts
OpenRadioFM features two main designs:
- **V2 (Classic Vertical):** Optimized for tablet-style or vertical screens.
- **V3 (Premium Horizontal):** Widescreen design ideal for dashboards, with large icons and **"Glass Mode"** effect.
- **New: White Skin (Light Mode):** High visibility theme for daytime driving.
- **How to switch Layout:** Long press the **LOC/DX** button to toggle between V2 and V3.
- **How to switch Theme:** Long press the **Settings (EQ)** button and select "Select Theme".

### 1.2 Favorites Navigation (Hardware)
- **Favorites:** Central buttons skip between your saved stations. Compatible with steering wheel controls (K706/MT8163).
- **Seek:** Outer buttons perform automatic signal search.

---

## 2. Premium Customization (Secret Menu)

Long press the **Settings (EQ)** button to access:

### 2.1 Theme Colors & Night Mode
Choose from 10 color schemes. In **Night Mode**, "Night Blue" will be applied to improve night visibility and reduce eye strain.

### 2.2 Background Mode (Glass Mode)
1. **Pure Black:** Maximum contrast.
2. **background.png Image:** Load a custom image from `/sdcard/RadioLogos/background.png`.
3. **Dynamic Logo (Glass Mode):** The background is automatically generated from the station logo.

---

## 3. Local Logos & Offline Mode [OpenRadioFM 5.5 OFFLINE]

### 3.1 Built-in Visual Logo Explorer
- **Direct Access:** Tap the central frequency dial and press the **LOGO** button to open the built-in explorer.
- **Navigation Shortcuts:**
  - `📁 RadioLogos`: Direct access to your local logos folder (`/sdcard/RadioLogos/`).
  - `📥 Downloads`: Navigate straight to `/sdcard/Download/`.
  - `💾 Storage`: Internal system storage.
  - `🔌 USB`: Instant detection of connected USB flash drives.
  - `⬆ Up`: Navigate up to parent directory.
- **Supported Formats:** `.png`, `.jpg`, `.jpeg`, `.webp`, and `.bmp` with automatic scaling up to 300x300 px for optimal sharpness and low memory overhead.
- **System Explorer (Android):** Alternative button to launch the native Android file picker safely.

### 3.2 Non-Destructive Logo Removal
- Tapping **🗑 Remove** inside the logo explorer **unassigns** the logo from the active preset or station, but **never deletes any file from your storage or USB drive**.
- The preset slot immediately restores the station frequency number and RDS name without flickering to `---`.

### 3.3 100% Offline Mode (Zero Dependencies)
- All cloud server connections, background sync workers, and telemetry have been removed, ensuring fast cold starts, zero battery drain, and no residual network icons in the top bar.

---

## 4. Favorites Management

### 4.1 Save and Load (.fav)
Use the **Floppy Disk (💾)** button to export or import your favorites list. This allows for backups or moving settings between devices.

---

## 5. Hardware Configuration

If you experience audio or tuning issues, select your engine in *Hardware Settings*:
- **HCN (K706):** For Vento/HCN units.
- **MediaTek 8259 / 8667:** New engine with improved stability and AM band protection.
- **Topway / Eonon (MT8163):** Optimized to prevent duplicate instances and freezes.
- **QS6:** For Nanis/NWD units.
- **SAFETY NOTICE**: In MTK engines, the AM band is forced to prevent hardware freezes. Do not attempt to disable it.

---
**NOTICE:** This is a **Stability BETA** version. Some server and streaming functions are under constant testing.
*Developed with ❤️ by Jimmy80 for the Android Head Unit community - v.5.0.0-Beta "Stability Edition"*
