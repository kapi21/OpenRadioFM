# User Manual - OpenRadioFM

Welcome to OpenRadioFM, an advanced FM radio application designed specifically for Android Head Units, featuring hardware control support and customization options.

---

## 1. Main Interface

The interface is designed to be clear, legible, and easy to use while driving.

### 1.1 Left Panel: Presets
Here your 6 favorite saved stations are displayed.
- **Short Press:** Tunes to the saved station.
- **Long Press:** Saves the current frequency to that memory slot.
- **Icons:** Displays the station logo if available.

### 1.2 Center Panel: Information
- **Frequency:** Displays current frequency with 2-decimal precision (e.g., `100.00`). Text size auto-adjusts to be as large as possible.
- **RDS Name:** Displays the station name (e.g., "LOS40") received via radio signal.
- **RDS Text:** Additional information (song title, news) in marquee format.
- **Seek Buttons (Arrows):**
  - **Short Press:** Fine manual frequency adjustment (+/- 0.05 MHz).
  - **Long Press:** Automatically seeks the next station with good signal.
- **BAND Button:** Switches between bands (FM1, FM2, FM3, AM1, AM2).
- **SCAN Button:** Scans and briefly plays all available stations.

### 1.3 Right Panel: Controls & Logos
- **Main Logo:** Displays the current station logo in large size.
- **LOC/DX:** Toggles reception sensitivity (Local for city, DX for long distance).
- **Mute:** Instantly mutes audio.
- **EQ (Equalizer):** Opens system audio settings.
- **TEST:** Multi-function button for development tests.

---

## 2. Customization (Skins)

Match the radio with your car's interior!

### How to change the color (Skin)
1. Press the **TEST** button (or Settings if available).
2. Select "Change Theme" in the menu (if it appears) or wait for future updates for direct access.
   *(Note: In current v8.5 version, the selector is activated via a specific mechanism under development)*

**Available Colors:**
- 🟠 **Orange (Original)**
- 🔘 **Classic Gray** (Neutral style)
- 🔵 **Blue** (Modern style)
- 🟢 **Green** (Retro style)
- 🟣 **Purple** (Neon style)
- 🔴 **Red** (Sport style)
- 🟡 **Yellow** (Warm style)
- ❄️ **Cyan** (Ice style)
- 🌸 **Pink** (Chic style)
- ⚪ **White** (Minimalist style)

Selecting a color instantly changes all application borders and buttons.

---

## 3. Logo & Name Management

### Station Logos
OpenRadioFM attempts to display each station's logo automatically.
1. **Auto Load:** Searches for logos in internal database or internet if connected.
2. **Smart Cache:** Once downloaded, logos are saved in memory and disk to appear instantly next time, even without internet.

**Logo Location:** `/sdcard/RadioLogos/`
You can manually add your own logos by copying `.png` images to this folder named with the frequency.
*Example:* For 100.0 MHz, save image as `10000.png` or `100000.png`.

### Custom Names
If the RDS name is incorrect or you want to set your own:
1. Long press the **Name Text** (STATION) or the **Main Logo**.
2. A dialog box will appear.
3. Type the desired name and press "Save".
4. To revert to the original name (RDS), press "Restore Original".

---

## 4. Operating Modes

The app automatically detects your device capabilities:

- **Full Mode (Root + Service):**
  - Requires rooted device and `com.hcn.autoradio` service.
  - Full functional RDS, real names, and direct radio chip control.
  
- **Basic Mode:**
  - For standard devices without root.
  - Basic tuning and logo functions.
  - Station names must be entered manually.

---

## 5. Troubleshooting

**Frequency doesn't change:**
- Ensure you are not in "Scan" mode. Press Scan again to stop.

**Logos not showing:**
- Verify internet connection on first run.
- Check storage permissions are granted.

**App crashes on start:**
- If you recently updated, try clearing app data in Android Settings.

---
*Developed by OpenRadioFM Team - v8.5 (January 2026)*
