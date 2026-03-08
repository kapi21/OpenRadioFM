# Manuale Utente - OpenRadioFM v.4.8 Cloud_Server

Benvenuti in **OpenRadioFM v.4.8 Cloud_Server**, l'evoluzione definitiva della radio FM per unità Android (Head Units). Questa versione introduce funzionalità di streaming e gestione dei loghi nel cloud, ottimizzate per la massima stabilità e prestazioni.

---

## 1. Interfaccia e Navigazione

### 1.1 Layout dello Schermo
OpenRadioFM dispone di due design principali:
- **V2 (Classico Verticale):** Ottimizzato per schermi tipo tablet o verticali.
- **V3 (Premium Orizzontale):** Design panoramico ideale per il cruscotto, con icone di grande formato ed effetto **"Glass Mode"**.
- **Come cambiare:** Premere a lungo il tasto **LOC/DX** per alternare. L'app si riavvierà automaticamente.

### 1.2 Navigazione Preferiti (Hardware)
- **Preferiti:** I pulsanti centrali permettono di saltare tra le stazioni memorizzate. Compatibile con i comandi al volante (K706/MT8163).
- **Ricerca (Seek):** I pulsanti esterni eseguono la ricerca automatica del segnale.

---

## 2. Personalizzazione Premium (Menu Segreto)

Premere a lungo il tasto **Impostazioni (EQ)** per accedere:

### 2.1 Colori del Tema e Modalità Notte
Scegli tra 10 schemi di colori. In **Modalità Notte**, verrà applicato il colore **"Night Blue"** per migliorare la visibilità notturna e ridurre l'affaticamento degli occhi.

### 2.2 Modalità Sfondo (Glass Mode)
1. **Nero Puro:** Massimo contrasto.
2. **Immagine background.png:** Carica un'immagine personalizzata da `/sdcard/RadioLogos/background.png`.
3. **Logo Dinamico (Glass Mode):** Lo sfondo viene generato automaticamente dal logo della stazione.

---

## 3. Loghi e Streaming Online [Novità v4.7 Beta]

### 3.1 Server dei Loghi (Beta)
L'app può scaricare automaticamente i loghi dal nostro server Supabase.
- **Reset della Cache:** Se un logo è errato o vuoi forzare un ricaricamento, premi a lungo l'icona della **Nuvola Cloud**. Apparirà il messaggio *"Cache della stazione cancellata"* e le informazioni verranno resettate.
- **IMPORTANTE:** Attualmente il catalogo dei loghi è focalizzato principalmente sulla **Spagna**, ma grazie al Crowdsourcing si espande ogni giorno.
- Può essere attivato in *Impostazioni Premium > Loghi Online*.

### 3.2 Streaming Online (Beta)
- **Funzionalità:** Permette di ascoltare la stazione via internet se il segnale FM è debole.
- **Stato:** Questa funzione è in fase di **test**. Il catalogo dello streaming è attualmente focalizzato sulle stazioni in **Spagna**.
- **Hardware:** Il motore MT8163 è stato ottimizzato per evitare blocchi durante la commutazione tra FM e Streaming.

### 3.3 Contributo alla Comunità (Crowdsourcing)
- **Come aiutare:** Attiva l'opzione *"Contribuisci alla Comunità"* nelle Impostazioni Premium.
- **Funzionamento:** Quando sintonizzi una stazione con RDS stabile, l'app invierà in modo anonimo la frequenza e il codice PI al server in modo che altri utenti possano beneficiave dei loghi HD nella tua zona.

---

## 4. Gestione dei Preferiti

### 4.1 Salva e Carica (.fav)
Usa il pulsante del **Dischetto (💾)** per esportare o importare la tua lista di preferiti. Questo permette backup o di spostare le tue impostazioni tra dispositivi.

---

## 5. Configurazione Hardware

Se riscontri problemi di audio o sintonizzazione, seleziona il tuo motore in *Impostazioni Hardware*:
- **HCN (K706):** Per unità Vento/HCN.
- **Eonon/Topway (MT8163):** Ottimizzato per evitare blocchi nella v4.7.
- **QS6:** Per unità Nanis/NWD.

---
**AVVISO:** Questa è una versione **BETA**. Alcune funzioni del server e dello streaming sono oggetto di test costanti.
*Sviluppato con ❤️ da Jimmy80 per la comunità Android Head Unit - v.4.8 Cloud_Server*
