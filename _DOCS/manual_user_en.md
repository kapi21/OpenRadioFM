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

## 3. Online Logos & Streaming [New in v4.7 Beta]

### 3.1 Logo System (Local Priority)
The app searches for logos in this priority order:
1. **Local Folder**: Looks in `/sdcard/RadioLogos/` for files named `Frequency_Name.png`.
2. **Online Server**: IF not found locally, downloads from **Supabase** (Community) or web servers.
- **Cache Reset:** Long press the **Cloud icon** to clear the current logo and force a new download.
- **Manuals**: You can add your own logos by simply copying them to the `RadioLogos` folder with the frequency name (e.g., `96900.png`).

### 3.2 Online Streaming (Beta)
- **Functionality:** Listen to stations via internet if the FM signal is weak.
- **Status:** This feature is in the **testing** phase. The streaming catalog is currently focused on stations from **Spain**.
- **Hardware:** The MT8163 engine has been optimized to prevent freezes when switching between FM and Streaming.

### 3.3 Community Contribution (Crowdsourcing)
- **How to help:** Enable **"Contribute to Community"** in Premium Settings.
- **How it works:** When tuning a station with stable RDS, the app will anonymously send frequency and PI code to the server so other users can benefit from HD logos in your area.

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
