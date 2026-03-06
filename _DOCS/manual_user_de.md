# Benutzerhandbuch - OpenRadioFM v.4.7 Beta Server

Willkommen bei **OpenRadioFM v.4.7 Beta Server**, der ultimativen Entwicklung des UKW-Radios für Android-Head-Units. Diese Version führt Cloud-Streaming- und Logo-Management-Funktionen ein, die auf maximale Stabilität und Leistung optimiert sind.

---

## 1. Benutzeroberfläche & Navigation

### 1.1 Bildschirm-Layouts
OpenRadioFM bietet zwei Hauptdesigns:
- **V2 (Klassisch Vertikal):** Optimiert für Tablet-Style- oder vertikale Bildschirme.
- **V3 (Premium Horizontal):** Widescreen-Design ideal für Armaturenbretter, mit großen Symbolen und **"Glass Mode"** Effekt.
- **Umschalten:** Drücken Sie lange auf die **LOC/DX**-Taste. Die App wird automatisch neu gestartet.

### 1.2 Favoriten-Navigation (Hardware)
- **Favoriten:** Mittlere Tasten springen zwischen Ihren gespeicherten Sendern. Kompatibel mit Lenkradsteuerungen (K706/MT8163).
- **Suche (Seek):** Äußere Tasten führen eine automatische Signalsuche durch.

---

## 2. Premium-Anpassung (Geheimmenü)

Halten Sie die Taste **Einstellungen (EQ)** gedrückt, um darauf zuzugreifen:

### 2.1 Themenfarben & Nachtmodus
Wählen Sie aus 10 Farbschemata. Im **Nachtmodus** wird "Night Blue" angewendet, um die Sicht bei Nacht zu verbessern und die Augen zu entlasten.

### 2.2 Hintergrundmodus (Glass Mode)
1. **Pures Schwarz:** Maximaler Kontrast.
2. **background.png Bild:** Laden Sie ein benutzerdefiniertes Bild von `/sdcard/RadioLogos/background.png`.
3. **Dynamisches Logo (Glass Mode):** Der Hintergrund wird automatisch aus dem Sendersymbol generiert.

---

## 3. Online-Logos & Streaming [Neu in v4.7 Beta]

### 3.1 Logo-Server (Beta)
Die App kann automatisch Logos von unserem Supabase-Server herunterladen.
- **Cache-Reset:** Wenn ein Logo falsch ist oder Sie ein Neuladen erzwingen möchten, drücken Sie lange auf das **Cloud-Symbol**. Die Meldung *"Sender-Cache gelöscht"* erscheint und die Informationen werden zurückgesetzt.
- **WICHTIG:** Derzeit konzentriert sich der Logo-Katalog hauptsächlich auf **Spanien**, aber dank Crowdsourcing wird er täglich erweitert.
- Aktivieren unter *Premium-Einstellungen > Online-Logos*.

### 3.2 Online-Streaming (Beta)
- **Funktionalität:** Hören Sie Sender über das Internet, wenn das UKW-Signal schwach ist.
- **Status:** Diese Funktion befindet sich in der **Testphase**. Der Streaming-Katalog konzentriert sich derzeit auf Sender aus **Spanien**.
- **Hardware:** Der MT8163-Motor wurde optimiert, um Einfrieren beim Umschalten zwischen UKW und Streaming zu verhindern.

### 3.3 Community-Beitrag (Crowdsourcing)
- **Wie Sie helfen:** Aktivieren Sie **"Zur Gemeinschaft beitragen"** in den Premium-Einstellungen.
- **Wie es funktioniert:** Wenn Sie einen Sender mit stabilem RDS einstellen, sendet die App anonym die Frequenz und den PI-Code an den Server, damit andere Benutzer in Ihrer Region von HD-Logos profitieren können.

---

## 4. Favoriten-Verwaltung

### 4.1 Speichern und Laden (.fav)
Verwenden Sie die **Disketten-Schaltfläche (💾)**, um Ihre Favoritenliste zu exportieren oder zu importieren. Dies ermöglicht Backups oder das Verschieben von Einstellungen zwischen Geräten.

---

## 5. Hardware-Konfiguration

Wenn Audio- oder Abstimmungsprobleme auftreten, wählen Sie Ihren Motor in den *Hardware-Einstellungen*:
- **HCN (K706):** Für Vento/HCN Einheiten.
- **Eonon/Topway (MT8163):** Optimiert, um Einfrieren in v4.7 zu verhindern.
- **QS6:** Für Nanis/NWD Einheiten.

---
**HINWEIS:** Dies ist eine **BETA**-Version. Einige Server- und Streaming-Funktionen werden ständig getestet.
*Entwickelt mit ❤️ von Jimmy80 für die Android Head Unit Community - v.4.7 Beta Server*
