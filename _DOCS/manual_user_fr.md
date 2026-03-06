# Manuel de l'Utilisateur - OpenRadioFM v.4.7 Beta Server

Bienvenue sur **OpenRadioFM v.4.7 Beta Server**, l'évolution ultime de la radio FM pour les unités Android (Head Units). Cette version introduit des capacités de streaming sur le cloud et de gestion de logos, optimisées pour une stabilité et des performances maximales.

---

## 1. Interface et Navigation

### 1.1 Modes d'Écran (Layouts)
OpenRadioFM propose deux designs principaux :
- **V2 (Classique Vertical) :** Optimisé pour les écrans de type tablette ou verticaux.
- **V3 (Premium Horizontal) :** Design panoramique idéal pour le tableau de bord, avec de grandes icônes et un effet **"Glass Mode"**.
- **Comment changer :** Appuyez longuement sur le bouton **LOC/DX** pour basculer. L'application redémarrera automatiquement.

### 1.2 Navigation des Favoris (Matériel)
- **Favoris :** Les boutons centraux permettent de basculer entre vos stations enregistrées. Compatible avec les commandes au volant (K706/MT8163).
- **Recherche (Seek) :** Les boutons extérieurs effectuent une recherche automatique de signal.

---

## 2. Personnalisation Premium (Menu Secret)

Appuyez longuement sur le bouton de **Configuration (EQ)** pour y accéder :

### 2.1 Couleurs du Thème et Mode Nuit
Choisissez parmi 10 schémas de couleurs. En **Mode Nuit**, la couleur "Night Blue" sera appliquée pour améliorer la visibilité nocturne et réduire la fatigue oculaire.

### 2.2 Mode Arrière-plan (Glass Mode)
1. **Noir Pur :** Contraste maximal.
2. **Image background.png :** Chargez une image personnalisée depuis `/sdcard/RadioLogos/background.png`.
3. **Logo Dynamique (Glass Mode) :** L'arrière-plan est généré automatiquement à partir du logo de la station.

---

## 3. Logos et Streaming en Ligne [Nouveauté v4.7 Beta]

### 3.1 Serveur de Logos (Beta)
L'application peut télécharger automatiquement des logos depuis notre serveur Supabase.
- **Réinitialisation du Cache :** Si un logo est incorrect ou si vous souhaitez forcer un rechargement, maintenez enfoncé l'icône du **Nuage Cloud**. Le message *"Cache de la station effacé"* apparaîtra et les informations seront réinitialisées.
- **IMPORTANT :** Actuellement, le catalogue de logos est principalement axé sur l'**Espagne**, mais grâce au Crowdsourcing, il s'étend chaque jour.
- Activer dans *Paramètres Premium > Logos en Ligne*.

### 3.2 Streaming en Ligne (Beta)
- **Fonctionnalité :** Permet d'écouter la station via internet si le signal FM est faible.
- **État :** Cette fonction est en phase de **test**. Le catalogue de streaming est actuellement axé sur les stations en **Espagne**.
- **Matériel :** Le moteur MT8163 a été optimisé pour éviter les blocages lors de la commutation entre FM et Streaming.

### 3.3 Contribution à la Communauté (Crowdsourcing)
- **Comment aider :** Activez l'option **"Contribuer à la Communauté"** dans les Paramètres Premium.
- **Fonctionnement :** Lors de la syntonisation d'une station avec un RDS stable, l'application enverra de manière anonyme la fréquence et le code PI au serveur afin que d'autres utilisateurs puissent bénéficier des logos HD dans votre zone.

---

## 4. Gestion des Favoris

### 4.1 Enregistrer et Charger (.fav)
Utilisez le bouton de la **Disquette (💾)** pour exporter ou importer votre liste de favoris. Cela permet de sauvegarder ou de déplacer vos réglages entre appareils.

---

## 5. Configuration Matérielle

Si vous rencontrez des problèmes audio ou de réglage, sélectionnez votre moteur dans *Paramètres Matériels* :
- **HCN (K706) :** Pour les unités Vento/HCN.
- **Eonon/Topway (MT8163) :** Optimisé pour éviter les gels en v4.7.
- **QS6 :** Pour les unités Nanis/NWD.

---
**AVIS :** Ceci est une version **BETA**. Certaines fonctions de serveur et de streaming sont en phase de test constant.
*Développé avec ❤️ par Jimmy80 pour la communauté Android Head Unit - v.4.7 Beta Server*
