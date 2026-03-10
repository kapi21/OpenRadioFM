# Корисничко упутство - OpenRadioFM v.4.8 Cloud_Server

Добродошли у **OpenRadioFM v.4.8 Cloud_Server**, врхунску еволуцију ФМ радиа за Андроид мултимедије. Ова верзија уводи могућности стримовања у облаку и управљања логотипима, оптимизоване за максималну стабилност и перформансе.

---

## 1. Интерфејс и навигација

### 1.1 Изглед екрана
OpenRadioFM има два главна дизајна:
- **V2 (Класични вертикални):** Оптимизован за екране у стилу таблета или вертикалне екране.
- **V3 (Премијум хоризонтални):** Дизајн за широк екран идеалан за командне табле, са великим иконама и ефектом **"Glass Mode"**.
- **Како променити:** Дуго притисните дугме **LOC/DX** за пребацивање. Апликација ће се аутоматски поново покренути.

### 1.2 Навигација кроз омиљене станице (Хардвер)
- **Омиљене:** Централна дугмад прескачу између ваших сачуваних станица. Компатибилно са контролама на волану (K706/MT8163).
- **Претрага (Seek):** Спољна дугмад врше аутоматску претрагу сигнала.

---

## 2. Премијум прилагођавање (Тајни мени)

Дуго притисните дугме **Подешавања (EQ)** да бисте приступили:

### 2.1 Боје теме и ноћни режим
Изаберите неку од 10 шема боја. У **Ноћном режиму**, биће примењена "Night Blue" боја како би се побољшала ноћна видљивост и смањило напрезање очију.

### 2.2 Режим позадине (Glass Mode)
1. **Чиста црна:** Максимални контраст.
2. **background.png слика:** Учитајте сопствену слику са `/sdcard/RadioLogos/background.png`.
3. **Динамични лого (Glass Mode):** Позадина се аутоматски генерише на основу логотипа станице.

---

## 3. Онлајн логотипи и стримовање [Ново у v4.7 Beta]

### 3.1 Server logotipa (Beta)
Aplikacija može automatski preuzeti logotipe sa našeg Supabase servera.
- **Resetovanje keša:** Ako je logo neispravan ili želite da iznudite ponovno učitavanje, dugo pritisnite ikonu **Cloud**. Pojaviće se poruka *"Кеш станице обрисан"* i informacije će biti resetovane.
- **VAŽNO:** Trenutno je katalog logotipa prvenstveno fokusiran na **Španiju**, ali se zahvaljujući Crowdsourcing-u širi svakodnevno.
- Aktivirajte u *Premium podešavanja > Onlajn logotipi*.

### 3.2 Onlajn striming (Beta)
- **Funkcionalnost:** Slušajte stanice putem interneta ako je FM signal slab.
- **Status:** Ova funkcija je u fazi **testiranja**. Katalog striminga je trenutno fokusiran na stanice iz **Španije**.
- **Hardver:** MT8163 motor je optimizovan da spreči zamrzavanje prilikom prebacivanja između FM-a i striminga.

### 3.3 Doprinos zajednici (Crowdsourcing)
- **Kako pomoći:** Aktivirajte opciju **"Doprinesi zajednici"** u Premium podešavanjima.
- **Kako funkcioniše:** Kada podesite stanicu sa stabilnim RDS-om, aplikacija će anonimno poslati frekvenciju i PI kod serveru kako bi drugi korisnici u vašem regionu mogli da koriste HD logotipe.

---

## 4. Управљање омиљеним станицама

### 4.1 Чување и учитавање (.fav)
Користите дугме **Флопи диск (💾)** за извоз или увоз ваше листе омиљених станица. Ово омогућава прављење резервних копија или пребацивање подешавања између уређаја.

---

## 5. Подешавање хардвера

Ако имате проблема са звуком или подешавањем станица, изаберите свој мотор у *Хардверским подешавањима*:
- **HCN (K706):** За Vento/HCN уређаје.
- **Eonon/Topway (MT8163):** Оптимизован да спречи блокирање у верзији v4.7.
- **QS6:** За Nanis/NWD уређаје.

---
**НАПОМЕНА:** Ово је **БЕТА** верзија. Неке функције сервера и стримовања су под сталним тестирањем.
*Развијено са ❤️ од стране Jimmy80 за заједницу Андроид мултимедија - v.4.8 Cloud_Server*
