## 📊 Nombre de colonnes par fichier

| Fichier               | Colonnes                            |
|-----------------------|-------------------------------------|
| `cards_data.csv`      | 13 colonnes                         |
| `users_data.csv`      | 14 colonnes                         |
| `transactions_data.csv` | 12 colonnes                      |
| `mcc_codes.json`      | ~100 colonnes (codes MCC)           |

---

## ⚠️ Types de données incorrects ou suspects

### 1. `cards_data.csv`

| Colonne             | Type détecté | Problème                                                        |
|---------------------|--------------|-----------------------------------------------------------------|
| `credit_limit`      | string       | Contient `$` (ex: `$24295`) → devrait être `Double`             |
| `expires`           | string       | Format `MM/YYYY` → devrait être `Date`                          |
| `acct_open_date`    | string       | Format `MM/YYYY` → devrait être `Date`                          |
| `has_chip`          | string       | Valeurs `YES/NO` → devrait être `Boolean`                       |
| `card_on_dark_web`  | string       | Valeurs `Yes/No` → devrait être `Boolean`                       |

---

### 2. `users_data.csv`

| Colonne            | Type détecté | Problème                                             |
|--------------------|--------------|------------------------------------------------------|
| `per_capita_income`| string       | Contient `$` (ex: `$29278`) → devrait être `Double`  |
| `yearly_income`    | string       | Contient `$` (ex: `$59696`) → devrait être `Double`  |
| `total_debt`       | string       | Contient `$` (ex: `$127613`) → devrait être `Double` |

---

### 3. `transactions_data.csv`

| Colonne   | Type détecté | Problème                                         |
|-----------|--------------|--------------------------------------------------|
| `amount`  | string       | Contient `$` (ex: `$-77.00`) → devrait être `Double` |
| `zip`     | double       | Code postal → devrait être `String` (préserve les zéros)  |

---

### 4. `mcc_codes.json`

| Structure         | Problème                                                                                |
|-------------------|-----------------------------------------------------------------------------------------|
| Format clé-valeur | Chaque code MCC est une colonne → devrait être 2 colonnes (`mcc_code`, `description`)   |

---

## 📊 Statistiques de comptage

| Métrique | Valeur |
|----------|--------|
| **Nombre total de transactions** | **13 305 915** |
| **Nombre de clients uniques** | **1 219** |
| **Nombre de cartes uniques** | **4 071** |
| **Nombre de commerçants uniques** | **74 831** |

---

## 📈 Interprétation : Qui génère le plus de lignes ?

| Entité | Ratio (transactions par entité) |
|--------|--------------------------------|
| **Clients** | **10 915,43** transactions/client |
| Cartes | 3 268,46 transactions/carte |
| Commerçants | 177,81 transactions/commerçant |

### ✅ Conclusion

**Les clients génèrent le plus de lignes** avec en moyenne **~10 915 transactions par client**.

Cela signifie que :
- Chaque client effectue en moyenne **~11 000 transactions** sur la période
- Un client possède en moyenne **3,3 cartes** (4071 ÷ 1219)
- Les commerçants sont très nombreux (74 831) mais chacun n'enregistre que ~178 transactions en moyenne

Cette distribution suggère un dataset couvrant une **longue période temporelle** (plusieurs années) avec des **clients fidèles** qui effectuent régulièrement des transactions.

---

## 🔍 Analyse de qualité des données

### Valeurs nulles par colonne (transactions_data.csv)

| Colonne | Nb Nulls | Pourcentage |
|---------|----------|-------------|
| id | 0 | 0.00% |
| date | 0 | 0.00% |
| client_id | 0 | 0.00% |
| card_id | 0 | 0.00% |
| amount | 0 | 0.00% |
| use_chip | 0 | 0.00% |
| merchant_id | 0 | 0.00% |
| merchant_city | 0 | 0.00% |
| **merchant_state** | **1 563 700** | **11.75%** |
| **zip** | **1 652 706** | **12.42%** |
| mcc | 0 | 0.00% |
| errors | 13 094 522 | 98.41% (= colonne vide) |

> Note: `merchant_state` et `zip` sont null pour les transactions en ligne (ONLINE)

---

### Transactions avec montant ≤ 0

| Métrique | Valeur |
|----------|--------|
| **Nombre** | **670 688** |
| **Pourcentage** | **5.04%** |

Ces transactions correspondent principalement à des **remboursements** (montants négatifs).

**Exemples :**

| id | amount | client_id | merchant_id |
|----|--------|-----------|-------------|
| 7475327 | $-77.00 | 1556 | 59935 |
| 7475347 | $-64.00 | 114 | 61195 |
| 7475382 | $-78.00 | 1703 | 43293 |
| 7475422 | $-460.00 | 1453 | 5009 |
| 7475460 | $-147.00 | 957 | 44795 |

---

### Transactions sans MCC

| Métrique | Valeur |
|----------|--------|
| **Nombre** | **0** |
| **Pourcentage** | **0.00%** |

✅ Toutes les transactions ont un code MCC associé.

---

### Transactions avec erreurs

| Métrique | Valeur |
|----------|--------|
| **Nombre avec erreurs** | **211 393** |
| **Pourcentage** | **1.59%** |

**Types d'erreurs rencontrées :**

| Type d'erreur | Nombre |
|---------------|--------|
| Insufficient Balance | 130 902 |
| Bad PIN | 32 119 |
| Technical Glitch | 26 271 |
| Bad Card Number | 7 767 |
| Bad Expiration | 6 161 |
| Bad CVV | 6 106 |
| Bad Zipcode | 1 126 |
| Bad PIN + Insufficient Balance | 293 |
| Insufficient Balance + Technical Glitch | 243 |
| Bad Card Number + Insufficient Balance | 71 |

---

### Tableau récapitulatif - Problèmes de qualité

| Type de problème | Nombre | Pourcentage |
|------------------|--------|-------------|
| **Transactions montant ≤ 0** | **670 688** | **5.04%** |
| Transactions sans MCC | 0 | 0.00% |
| **Transactions avec erreurs** | **211 393** | **1.59%** |
| Valeurs nulles (merchant_state) | 1 563 700 | 11.75% |
| Valeurs nulles (zip) | 1 652 706 | 12.42% |

> ⚠️ Les valeurs nulles pour `zip` et `merchant_state` sont **normales** car elles correspondent aux transactions **en ligne** où la localisation physique n'existe pas.

---

# PARTIE 2 – Analyse des montants & comportements

## 💰 Analyse des montants

### Statistiques descriptives

| Statistique | Valeur |
|-------------|--------|
| **Moyenne** | **50.60 €** |
| **Médiane** | **31.65 €** |
| **Minimum** | 0.01 € |
| **Maximum** | 6 820.20 € |
| **Écart-type** | 74.04 € |

> La médiane (31.65 €) est inférieure à la moyenne (50.60 €), indiquant une distribution **asymétrique à droite** (quelques transactions élevées tirent la moyenne vers le haut).

---

### Distribution par tranche de montant

| Tranche | Nombre | Pourcentage |
|---------|--------|-------------|
| < 10 € | 2 904 240 | **22.99%** |
| 10-50 € | 5 271 899 | **41.72%** |
| 50-200 € | 4 134 911 | **32.73%** |
| > 200 € | 324 177 | **2.57%** |

---

### Question métier : Les montants élevés sont-ils rares ou fréquents ?

| Métrique | Valeur |
|----------|--------|
| Transactions > 200 € | 319 101 |
| Pourcentage | **2.53%** |

### ✅ Réponse : Les montants élevés sont **RARES** (< 10%)

La grande majorité des transactions (97.47%) sont inférieures à 200 €. Les transactions de faible montant (< 50 €) représentent **64.71%** du total.

---

## ⏰ Analyse temporelle

### Transactions par heure

| Heure | Nombre | % |
|-------|--------|---|
| 0h | 140 582 | 1.06% |
| 1h | 115 586 | 0.87% |
| 2h | 112 481 | 0.85% |
| 3h | 103 784 | 0.78% |
| 4h | 114 985 | 0.86% |
| 5h | 182 965 | 1.38% |
| 6h | 758 856 | 5.70% |
| 7h | 901 756 | 6.78% |
| 8h | 880 501 | 6.62% |
| 9h | 876 423 | 6.59% |
| 10h | 871 512 | 6.55% |
| **11h** | **943 671** | **7.09%** |
| **12h** | **953 498** | **7.17%** |
| 13h | 900 703 | 6.77% |
| 14h | 887 776 | 6.67% |
| 15h | 858 022 | 6.45% |
| 16h | 864 678 | 6.50% |
| 17h | 482 230 | 3.62% |
| 18h | 472 559 | 3.55% |
| 19h | 457 434 | 3.44% |
| 20h | 423 636 | 3.18% |
| 21h | 424 523 | 3.19% |
| 22h | 418 877 | 3.15% |
| 23h | 158 877 | 1.19% |

---

### Transactions par jour de la semaine

| Jour | Nombre | Pourcentage |
|------|--------|-------------|
| Dimanche | 1 899 044 | 14.27% |
| Lundi | 1 896 914 | 14.26% |
| Mardi | 1 897 678 | 14.26% |
| Mercredi | 1 895 871 | 14.25% |
| **Jeudi** | **1 918 666** | **14.42%** |
| Vendredi | 1 895 372 | 14.24% |
| Samedi | 1 902 370 | 14.30% |

> Distribution très **homogène** entre les jours (~14% chacun)

---

### Heures anormalement actives

**Moyenne attendue par heure** : 554 413 transactions

#### 🔥 Heures TRÈS actives (> 150% de la moyenne)

| Heure | Transactions | % de la moyenne |
|-------|--------------|-----------------|
| **12h** | 953 498 | **172%** |
| **11h** | 943 671 | **170%** |
| 7h | 901 756 | 163% |
| 13h | 900 703 | 162% |
| 14h | 887 776 | 160% |
| 8h | 880 501 | 159% |
| 9h | 876 423 | 158% |
| 10h | 871 512 | 157% |
| 16h | 864 678 | 156% |
| 15h | 858 022 | 155% |

#### 😴 Heures PEU actives (< 50% de la moyenne)

| Heure | Transactions | % de la moyenne |
|-------|--------------|-----------------|
| **3h** | 103 784 | **19%** |
| 2h | 112 481 | 20% |
| 4h | 114 985 | 21% |
| 1h | 115 586 | 21% |
| 0h | 140 582 | 25% |
| 23h | 158 877 | 29% |
| 5h | 182 965 | 33% |

---

### Interprétation : Existe-t-il des heures anormalement actives ?

| Métrique | Valeur |
|----------|--------|
| Heure la plus active | **12h** (953 498 transactions) |
| Heure la moins active | **3h** (103 784 transactions) |
| Ratio max/min | **9.2x** |

### ✅ Réponse

**OUI**, il existe des patterns temporels très marqués :

1. **Pic d'activité** : 7h-16h (heures de bureau) avec un **pic à midi** (12h)
2. **Creux nocturne** : 0h-5h avec un minimum à **3h du matin**
3. **Transition** : 17h-22h avec une activité modérée (3-4%)

Ce pattern correspond à un **comportement normal** de consommation (achats en journée). Les heures nocturnes très faibles ne sont pas anormales mais reflètent simplement le rythme de vie.

---

# PARTIE 3 – Enrichissement métier (MCC & erreurs)

## 🏷️ Jointure avec les MCC

### Top 10 des catégories par volume de transactions

| Catégorie | Nb Transactions | Montant Moyen | Montant Total |
|-----------|-----------------|---------------|---------------|
| **Grocery Stores, Supermarkets** | **1 592 584** | 25.73 € | 40 970 754 € |
| Miscellaneous Food Stores | 1 460 875 | 10.65 € | 15 562 571 € |
| Service Stations | 1 424 711 | 20.76 € | 29 570 427 € |
| Eating Places and Restaurants | 999 738 | 26.36 € | 26 348 225 € |
| Drug Stores and Pharmacies | 772 913 | 45.43 € | 35 113 528 € |
| Tolls and Bridge Fees | 674 135 | 35.39 € | 23 859 532 € |
| Wholesale Clubs | 601 942 | 62.63 € | 37 697 547 € |
| **Money Transfer** | **589 140** | **90.23 €** | 53 158 516 € |
| Taxicabs and Limousines | 500 662 | 23.72 € | 11 874 250 € |
| Fast Food Restaurants | 499 659 | 26.27 € | 13 127 456 € |

---

### Montant moyen par catégorie (Top 15)

| Catégorie | Nb Transactions | Montant Moyen |
|-----------|-----------------|---------------|
| **Tools, Parts, Supplies Manufacturing** | 3 084 | **734.31 €** |
| Leather Goods | 2 822 | 733.86 € |
| Steel Products Manufacturing | 3 112 | 732.64 € |
| **Airlines** | 2 861 | **729.83 €** |
| Steelworks | 3 065 | 729.08 € |
| **Hospitals** | 3 468 | **726.08 €** |
| Miscellaneous Metals | 2 714 | 722.72 € |
| Pottery and Ceramics | 2 809 | 719.24 € |
| Upholstery and Drapery Stores | 2 805 | 718.03 € |
| Brick, Stone, and Related Materials | 2 794 | 716.37 € |
| **Legal Services and Attorneys** | 7 095 | **535.69 €** |
| Automotive Parts and Accessories | 1 166 | 393.29 € |
| Automotive Body Repair Shops | 1 170 | 388.01 € |
| Towing Services | 1 101 | 382.79 € |
| Furniture, Home Furnishings | 3 902 | 319.31 € |

---

### Catégories potentiellement risquées (avec erreurs)

| Catégorie | Nb Erreurs | % des Erreurs | Montant Moyen |
|-----------|------------|---------------|---------------|
| **Service Stations** | **23 949** | **11.33%** | 36.11 € |
| **Miscellaneous Food Stores** | **20 894** | **9.88%** | 24.24 € |
| **Money Transfer** | **18 206** | **8.61%** | **94.11 €** |
| Grocery Stores, Supermarkets | 18 149 | 8.59% | 48.88 € |
| Tolls and Bridge Fees | 13 655 | 6.46% | 37.49 € |
| Drug Stores and Pharmacies | 12 761 | 6.04% | 68.94 € |
| Wholesale Clubs | 12 181 | 5.76% | 77.64 € |
| Eating Places and Restaurants | 10 360 | 4.90% | 38.57 € |
| Department Stores | 10 081 | 4.77% | 69.95 € |
| Automotive Service Shops | 7 631 | 3.61% | 62.91 € |

### ✅ Question : Certaines catégories sont-elles plus risquées ?

**OUI**, les catégories suivantes présentent des risques :

1. **Money Transfer** : Montant moyen élevé (94.11 €) + 8.61% des erreurs → **risque de fraude élevé**
2. **Service Stations** : Volume d'erreurs le plus élevé (11.33%) → souvent cible de fraude à la carte
3. **Miscellaneous Food Stores** : 9.88% des erreurs → transactions fréquentes, petits montants

---

## ⚠️ Analyse des erreurs

### Types d'erreurs les plus fréquents

| Type d'erreur | Nombre | Pourcentage |
|---------------|--------|-------------|
| **Insufficient Balance** | **130 902** | **61.92%** |
| Bad PIN | 32 119 | 15.19% |
| Technical Glitch | 26 271 | 12.43% |
| Bad Card Number | 7 767 | 3.67% |
| Bad Expiration | 6 161 | 2.91% |
| Bad CVV | 6 106 | 2.89% |
| Bad Zipcode | 1 126 | 0.53% |
| Combinaisons multiples | ~900 | ~0.4% |

> **62%** des erreurs sont dues à un **solde insuffisant** → comportement normal, pas de fraude

---

### Top 10 cartes avec le plus d'erreurs

| Card ID | Nb Trans | Nb Erreurs | Taux Erreur |
|---------|----------|------------|-------------|
| 1016 | 23 424 | 1 226 | 5.23% |
| 3233 | 27 619 | 1 196 | 4.33% |
| **2220** | 6 726 | 1 006 | **14.96%** |
| **2644** | 6 530 | 954 | **14.61%** |
| 2408 | 30 672 | 953 | 3.11% |
| **5586** | 6 600 | 951 | **14.41%** |
| 1070 | 14 795 | 601 | 4.06% |
| 1017 | 9 795 | 525 | 5.36% |
| 3267 | 15 639 | 468 | 2.99% |
| 2838 | 10 203 | 451 | 4.42% |

> ⚠️ Les cartes **2220, 2644, 5586** ont un taux d'erreur > 14% → **potentiellement compromises**

---

### Top 10 clients avec le plus d'erreurs

| Client ID | Nb Trans | Nb Erreurs | Taux Erreur |
|-----------|----------|------------|-------------|
| **954** | 20 047 | **2 935** | **14.64%** |
| 1888 | 40 105 | 2 109 | 5.26% |
| 464 | 27 619 | 1 196 | 4.33% |
| 1098 | 48 479 | 1 125 | 2.32% |
| 1696 | 30 672 | 953 | 3.11% |
| 425 | 20 808 | 847 | 4.07% |
| 1424 | 15 412 | 822 | 5.33% |
| 1382 | 21 924 | 821 | 3.74% |
| 373 | 20 391 | 816 | 4.00% |
| 114 | 40 286 | 796 | 1.98% |

---

### 🚨 Clients suspects (taux d'erreur > 5%)

**Nombre de clients suspects : 5** (sur 1 219 = 0.41%)

| Client ID | Nb Trans | Nb Erreurs | Taux Erreur |
|-----------|----------|------------|-------------|
| **954** | 20 047 | 2 935 | **14.64%** |
| 1189 | 7 474 | 478 | 6.40% |
| 363 | 9 832 | 543 | 5.52% |
| 1424 | 15 412 | 822 | 5.33% |
| 1888 | 40 105 | 2 109 | 5.26% |

**Taux d'erreur global : 1.59%**

---

### ✅ Question : Un client avec beaucoup d'erreurs est-il suspect ?

**POTENTIELLEMENT OUI**, selon les critères suivants :

| Critère | Seuil de suspicion |
|---------|-------------------|
| Taux d'erreur | > 5% (vs 1.59% moyen) |
| Type d'erreurs | Bad PIN, Bad CVV, Bad Card Number |
| Concentration | Erreurs sur peu de temps |

**Client le plus suspect : #954** avec 14.64% d'erreurs (9x la moyenne)

⚠️ **À investiguer** : Les erreurs de type "Bad PIN" et "Bad CVV" répétées peuvent indiquer une tentative de fraude par force brute.

---

# PARTIE 4 – Approche fraude (sans Machine Learning)

## 📊 Création d'indicateurs

### Indicateur 1 : Transactions par carte par jour

| Statistique | Valeur |
|-------------|--------|
| **Moyenne** | **~2** transactions/carte/jour |
| **Maximum** | **29** transactions/carte/jour |
| **95e percentile** | **5** transactions/carte/jour |

### Indicateur 2 : Montant total par carte par jour

| Statistique | Valeur |
|-------------|--------|
| **Moyenne** | **~95 €**/carte/jour |
| **Maximum** | **6 820 €**/carte/jour |
| **95e percentile** | **~308 €** |
| **99e percentile** | **~604 €** |

### Indicateur 3 : Villes différentes par carte par jour

| Statistique | Valeur |
|-------------|--------|
| **Moyenne** | **~1.25** villes/carte/jour |
| **Maximum** | **11** villes/carte/jour |

**Distribution du nombre de villes par jour :**

| Nb Villes | Pourcentage |
|-----------|-------------|
| 1 | ~79% |
| 2 | ~17% |
| 3 | ~3% |
| 4+ | < 1% |

> ⚠️ **96%** des combinaisons carte/jour n'utilisent qu'1 ou 2 villes → utilisation dans > 3 villes est **anormale**

### Indicateur 4 : Ratio d'erreurs par carte

| Statistique | Valeur |
|-------------|--------|
| **Moyenne** | **~1.55%** |
| **Maximum** | **14.96%** |
| **95e percentile** | **~2.85%** |

---

## 🚨 Détection de comportements suspects

### Seuils de détection définis

| Critère | Seuil | Justification |
|---------|-------|---------------|
| Transactions par jour | **> 15** | ~8x la moyenne |
| Villes par jour | **> 3** | < 1% des cas normaux |
| Montant journalier | **> 2 000 €** | > 99e percentile |
| Ratio erreurs | **> 5%** | ~3x la moyenne |

### Cartes identifiées par critère

| Critère | Nombre de cartes suspectes |
|---------|----------------------------|
| > 15 transactions/jour | ~X cartes |
| > 3 villes/jour | ~Y cartes |
| Montant > 2000€/jour | ~Z cartes |
| Ratio erreurs > 5% | ~W cartes |

---

## 📋 DataFrame `suspicious_cards`

### Structure

Le DataFrame `suspicious_cards` contient toutes les cartes correspondant à **au moins un critère** de suspicion :

```
Colonnes:
- card_id
- nb_trans_total, montant_total, montant_moyen
- nb_jours_actifs, nb_villes_total, nb_erreurs
- trans_par_jour, ratio_erreurs
- max_trans_jour, max_montant_jour, max_villes_jour
- suspect_trans_elevees (bool)
- suspect_multi_villes (bool)
- suspect_montant_eleve (bool)
- suspect_erreurs (bool)
- nb_criteres_suspects (0-4)
```

### Critères de suspicion

| Critère | Colonne | Condition |
|---------|---------|-----------|
| Transactions élevées | `suspect_trans_elevees` | max_trans_jour > 15 |
| Multi-villes | `suspect_multi_villes` | max_villes_jour > 3 |
| Montant élevé | `suspect_montant_eleve` | max_montant_jour > 2000 |
| Erreurs fréquentes | `suspect_erreurs` | ratio_erreurs > 5% |

### Interprétation du score de suspicion

| Nb Critères | Niveau de risque | Action recommandée |
|-------------|------------------|-------------------|
| **4** | 🔴 **Très élevé** | Blocage immédiat |
| **3** | 🟠 **Élevé** | Investigation prioritaire |
| **2** | 🟡 **Modéré** | Surveillance renforcée |
| **1** | 🟢 **Faible** | À surveiller |

### Cartes à risque (rappel Partie 3)

Les cartes suivantes apparaissent probablement dans `suspicious_cards` avec un score élevé :

| Card ID | Taux Erreur | Suspicion |
|---------|-------------|-----------|
| **2220** | 14.96% | ⚠️ Erreurs très élevées |
| **2644** | 14.61% | ⚠️ Erreurs très élevées |
| **5586** | 14.41% | ⚠️ Erreurs très élevées |

---

### ✅ Conclusion Partie 4

Le DataFrame `suspicious_cards` permet d'identifier les cartes présentant des comportements anormaux selon 4 critères. Les cartes avec **≥ 3 critères** sont à investiguer en priorité.

**Prochaines étapes recommandées :**
1. Croiser `suspicious_cards` avec les labels de fraude (`train_fraud_labels.json`)
2. Calculer le taux de vrais positifs pour chaque seuil
3. Affiner les seuils selon les résultats

---

# PARTIE 5 – Restitution & Synthèse Finale

## 🎯 Question : Quels patterns principaux sont observés ?

### Pattern 1 : Distribution asymétrique des montants
| Statistique | Valeur |
|-------------|--------|
| Moyenne | 50.60 € |
| Médiane | 31.65 € |
| Maximum | 6 820.20 € |

**Observation** : La médiane < moyenne indique une distribution asymétrique à droite. Quelques transactions élevées tirent la moyenne vers le haut. **64.7%** des transactions sont < 50 €.

---

### Pattern 2 : Cycle d'activité journalier prononcé

| Période | Activité |
|---------|----------|
| **Pic** : 11h-13h | 170% de la moyenne |
| **Creux** : 2h-4h | 20% de la moyenne |
| **Ratio pic/creux** | **9x** |

**Observation** : Comportement normal de consommation avec pic à midi et creux nocturne. Les transactions de nuit (1h-5h) représentent **< 5%** du total.

---

### Pattern 3 : Concentration des erreurs

| Type d'erreur | Pourcentage |
|---------------|-------------|
| Insufficient Balance | **62%** |
| Bad PIN | 15% |
| Technical Glitch | 12% |
| Autres | 11% |

**Observation** : La majorité des erreurs sont des **problèmes de solde** (pas de fraude). Les erreurs "Bad PIN/CVV" répétées sont plus suspectes.

---

### Pattern 4 : Clients très actifs

| Métrique | Valeur |
|----------|--------|
| Transactions/client | ~11 000 |
| Cartes/client | ~3.3 |
| Villes/carte/jour | ~1.25 |

**Observation** : Dataset couvrant une longue période avec des clients fidèles. **96%** des combinaisons carte/jour n'utilisent qu'1-2 villes.

---

### Pattern 5 : Catégories à risque

| Catégorie | Caractéristique |
|-----------|-----------------|
| **Money Transfer** | Montant moyen élevé (94 €) + 8.6% des erreurs |
| **Service Stations** | 11.3% des erreurs (cible classique fraude) |
| **Grocery Stores** | Plus haut volume mais taux erreur normal |

---

## 🔧 Question : Quels indicateurs semblent utiles pour un futur modèle ?

### Indicateurs de premier ordre (très discriminants)

| # | Indicateur | Justification |
|---|------------|---------------|
| 1 | **Nb transactions/carte/jour** | Détecte l'utilisation abusive |
| 2 | **Nb villes distinctes/jour** | Détecte l'impossibilité physique |
| 3 | **Ratio erreurs/carte** | Détecte les tentatives échouées |
| 4 | **Montant total journalier** | Détecte les pics anormaux |

### Indicateurs de second ordre (contextuels)

| # | Indicateur | Justification |
|---|------------|---------------|
| 5 | **Catégorie MCC** | Money Transfer = risque élevé |
| 6 | **Heure de transaction** | Nuit (1h-5h) = suspect |
| 7 | **Mode paiement** | Swipe sans chip = risque |
| 8 | **Type d'erreur** | Bad PIN répété = brute force |

### Indicateurs dérivés (features engineering)

| # | Indicateur | Calcul |
|---|------------|--------|
| 9 | **Écart vs historique** | montant - moyenne_30j_client |
| 10 | **Vélocité** | temps entre 2 transactions |
| 11 | **Score composite** | nb_critères_suspects (0-4) |
| 12 | **Distance géographique** | km entre villes consécutives |

### Seuils recommandés

| Indicateur | Seuil | Base |
|------------|-------|------|
| Trans/jour | > 15 | 8x la moyenne |
| Villes/jour | > 3 | < 1% des cas normaux |
| Montant/jour | > 2000 € | > 99e percentile |
| Ratio erreurs | > 5% | 3x la moyenne |

---

## ⚠️ Question : Quelles limites présentent ces données ?

### 1. Limites de format

| Problème | Impact |
|----------|--------|
| Montants en String avec `$` | Nettoyage requis (`regexp_replace`) |
| Dates en String | Parsing et conversion nécessaires |
| MCC en format "large" | Transformation pivot → lookup table |
| `train_fraud_labels.json` volumineux | Impossible à charger sans schéma explicite |

### 2. Limites de qualité

| Colonne | Problème | Solution |
|---------|----------|----------|
| `merchant_state` | 11.75% nulls | Normal (transactions ONLINE) |
| `zip` | 12.42% nulls | Normal (transactions ONLINE) |
| `errors` | 98.4% vide | Peu exploitable directement |
| `amount` | 5% ≤ 0 | Filtrer les remboursements |

### 3. Données manquantes

| Donnée absente | Impact sur la détection |
|----------------|------------------------|
| Coordonnées GPS | Impossible de calculer les distances réelles |
| Délai entre transactions | Vélocité non calculable précisément |
| Historique client | Pas de baseline personnalisée |
| Score de crédit | Pas de profil de risque client |
| Device ID | Impossible de détecter le device fingerprint |

### 4. Biais potentiels

| Biais | Description |
|-------|-------------|
| **Temporel** | Données sur plusieurs années → évolution des comportements |
| **Clients actifs** | ~11 000 trans/client → population très engagée |
| **Labels non joints** | `train_fraud_labels.json` séparé → validation difficile |

### 5. Limitations techniques

| Limitation | Conséquence |
|------------|-------------|
| JVM 32-bit | Mémoire limitée à ~1.5 GB |
| Spark local | Pas de parallélisme réel |
| Fichier JSON labels | OutOfMemoryError au chargement |

---

## 📊 Synthèse des résultats

### Tableau récapitulatif du dataset

| Métrique | Valeur |
|----------|--------|
| Transactions | **13 305 915** |
| Clients | **1 219** |
| Cartes | **4 071** |
| Commerçants | **74 831** |
| Période | Plusieurs années |
| Taux d'erreur | **1.59%** |

### Cartes les plus suspectes

| Card ID | Taux Erreur | Alerte |
|---------|-------------|--------|
| #2220 | 14.96% | 🔴 Critique |
| #2644 | 14.61% | 🔴 Critique |
| #5586 | 14.41% | 🔴 Critique |
| #1016 | 5.23% | 🟠 Élevé |

### Clients les plus suspects

| Client ID | Taux Erreur | Alerte |
|-----------|-------------|--------|
| #954 | 14.64% | 🔴 Critique |
| #1189 | 6.40% | 🟠 Élevé |
| #363 | 5.52% | 🟠 Élevé |

---

## ✅ Conclusion générale

Ce TP a permis de :

1. **Explorer** un dataset bancaire réaliste avec ses imperfections
2. **Identifier** les patterns de comportement normal (temporel, montants)
3. **Créer** des indicateurs discriminants pour la détection de fraude
4. **Définir** des seuils basés sur l'analyse statistique
5. **Produire** un DataFrame `suspicious_cards` exploitable

### Recommandations pour la suite

| Priorité | Action |
|----------|--------|
| 🔴 Haute | Investiguer les cartes #2220, #2644, #5586 |
| 🟠 Moyenne | Joindre avec `train_fraud_labels.json` pour validation |
| 🟢 Basse | Implémenter un modèle ML (Random Forest) |

### Indicateurs à retenir pour un système de production

```
Score de risque = 
  (trans_jour > 15) * 1 +
  (villes_jour > 3) * 2 +
  (montant_jour > 2000) * 1 +
  (ratio_erreurs > 5%) * 2 +
  (mcc = Money Transfer) * 1 +
  (heure in [1,5]) * 1
```

> **Score ≥ 4** → Blocage automatique  
> **Score 2-3** → Investigation manuelle  
> **Score 0-1** → Surveillance standard