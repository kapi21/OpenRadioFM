# Manual de Utilizare - OpenRadioFM v.5.0.0-Beta Stability Edition

Bun venit la **OpenRadioFM v.5.0.0-Beta Stability Edition**, evoluția definitivă a radioului FM pentru unitățile Android (Head Units). Această versiune introduce capacități de streaming în cloud și gestionare a logo-urilor, optimizate pentru stabilitate și performanță maximă.

---

## 1. Interfață și Navigare

### 1.1 Moduri de Ecran (Layout-uri)
OpenRadioFM dispune de două design-uri principale:
- **V2 (Clasic Vertical):** Optimizat pentru ecrane de tip tabletă sau verticale.
- **V3 (Premium Orizontal):** Design panoramic ideal pentru bord, cu pictograme mari și efect **"Glass Mode"**.
- **Cum se schimbă:** Apăsați lung butonul **LOC/DX** pentru a comuta. Aplicația se va reporni automat.

### 1.2 Navigarea Favoritelor (Hardware)
- **Favorite:** Butoanele centrale permit saltul între stațiile salvate. Compatibil cu comenzile de pe volan (K706/MT8163).
- **Căutare (Seek):** Butoanele exterioare efectuează căutarea automată a semnalului.

---

## 2. Personalizare Premium (Meniu Secret)

Apăsați lung butonul de **Setări (EQ)** pentru a accesa:

### 2.1 Culorile Temei și Modul de Noapte
Alegeți din 10 scheme de culori. În **Modul de Noapte**, va fi aplicată culoarea "Night Blue" pentru a îmbunătăți vizibilitatea pe timp de noapte și a reduce oboseala ochilor.

### 2.2 Mod Fundal (Glass Mode)
1. **Negru Pur:** Contrast maxim.
2. **Imagine background.png:** Încărcați o imagine personalizată din `/sdcard/RadioLogos/background.png`.
3. **Logo Dinamic (Glass Mode):** Fundalul este generat automat din logo-ul stației.

---

## 3. Logo-uri și Streaming Online [Nou în v4.7 Beta]

### 3.1 Server de Logouri (Beta)
Aplicația poate descărca automat logouri de pe serverul nostru Supabase.
- **Resetare Cache:** Dacă un logo este incorect sau doriți să forțați o reîncărcare, apăsați lung pictograma **Cloud**. Va apărea mesajul *"Cache post șters"* și informațiile vor fi resetate.
- **IMPORTANT:** În prezent, catalogul de logouri este axat în principal pe **Spania**, dar datorită Crowdsourcing-ului se extinde în fiecare zi.
- Activare în *Setări Premium > Logouri Online*.

### 3.2 Streaming Online (Beta)
- **Funcționalitate:** Permite ascultarea postului prin internet dacă semnalul FM este slab.
- **Stare:** Această funcție este în faza de **testare**. Catalogul de streaming este momentan axat pe posturile din **Spania**.
- **Hardware:** Motorul MT8163 a fost optimizat pentru a preveni blocările la comutarea între FM și Streaming.

### 3.3 Contribuție la Comunitate (Crowdsourcing)
- **Cum să ajuți:** Activați opțiunea **"Contribuie la Comunitate"** în Setările Premium.
- **Cum funcționează:** Când acordați un post cu RDS stabil, aplicația va trimite anonim frecvența și codul PI la server, astfel încât alți utilizatori să poată beneficia de logouri HD în zona dumneavoastră.

---

## 4. Gestionarea Favoritelor

### 4.1 Salvare și Încărcare (.fav)
Utilizați butonul cu **Dischetă (💾)** pentru a exporta sau importa lista de favorite. Acest lucru permite copii de rezervă sau mutarea setărilor între dispozitive.

---

## 5. Configurație Hardware

Dacă întâmpinați probleme audio sau de acord, selectați motorul din *Setări Hardware*:
- **HCN (K706):** Pentru unități Vento/HCN.
- **Eonon/Topway (MT8163):** Optimizat pentru a preveni blocările în v4.7.
- **QS6:** Pentru unități Nanis/NWD.

---
**NOTĂ:** Aceasta este o versiune **BETA**. Unele funcții de server și streaming sunt în fază de testare constantă.
*Dezvoltat cu ❤️ de Jimmy80 pentru comunitatea Android Head Unit - v.5.0.0-Beta Stability Edition*
