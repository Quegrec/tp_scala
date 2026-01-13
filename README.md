# 🔍 TP Spark - Analyse de Fraude Bancaire

## 📋 Description

Ce projet implémente une analyse complète de données bancaires pour la détection de fraude, utilisant **Apache Spark** et **Scala**.

---

## 🗂️ Structure du Projet

```
tp-Spark/
├── data/                      # Données du TP
│   ├── transactions_data.csv  # Transactions bancaires (13M lignes)
│   ├── cards_data.csv         # Informations cartes
│   ├── users_data.csv         # Informations clients
│   ├── mcc_codes.json         # Codes catégories marchands
│   └── train_fraud_labels.json # Labels de fraude (référence)
│
├── Partie1_Exploration.scala  # Exploration et qualité des données
├── Partie2_Montants.scala     # Analyse des montants et temporelle
├── Partie3_MCC_Erreurs.scala  # Enrichissement MCC et erreurs
├── Partie4_Fraude.scala       # Indicateurs de fraude
│
├── answer.md                  # Réponses aux questions du TP
├── build.sbt                  # Configuration SBT
├── .jvmopts                   # Options JVM
└── README.md                  # Ce fichier
```

---

## ⚙️ Prérequis

- **Java JDK 8** (version 32-bit ou 64-bit)
- **SBT** (Scala Build Tool) ≥ 1.9
- **Scala 2.13.x**
- **Apache Spark 3.5.0** (téléchargé automatiquement par SBT)

### Vérifier l'installation

```bash
java -version    # Doit afficher 1.8.x
sbt --version    # Doit afficher 1.9+ 
```

---

## 🚀 Exécution

### 1. Cloner/Ouvrir le projet

```bash
cd tp-Spark
```

### 2. Compiler le projet

```bash
sbt compile
```

### 3. Exécuter chaque partie

```bash
# Partie 1 - Exploration des données
sbt "runMain Partie1_Exploration"

# Partie 2 - Analyse des montants et temporelle
sbt "runMain Partie2_Montants"

# Partie 3 - Enrichissement MCC et analyse des erreurs
sbt "runMain Partie3_MCC_Erreurs"

# Partie 4 - Indicateurs de fraude et détection
sbt "runMain Partie4_Fraude"

# Partie 5 - Synthèse finale
sbt "runMain Partie5_Synthese"
```

### 4. Exécuter tout d'un coup

```bash
# Exécuter toutes les parties séquentiellement
sbt "runMain Partie1_Exploration" && sbt "runMain Partie2_Montants" && sbt "runMain Partie3_MCC_Erreurs" && sbt "runMain Partie4_Fraude" && sbt "runMain Partie5_Synthese"
```

---

## ⚠️ Résolution des Problèmes

### Erreur "OutOfMemoryError"

Le fichier `.jvmopts` configure la mémoire JVM. Pour une JVM 32-bit :

```
-Xmx1500m
-Xms768m
```

Pour une JVM 64-bit, vous pouvez augmenter :

```
-Xmx4g
-Xms1g
```

### Erreur "Could not reserve enough space"

Réduisez les valeurs dans `.jvmopts` :

```
-Xmx900m
-Xms256m
```

### Erreur "sbt server already booting"

Fermez les processus Java existants :

```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill -f java
```

### Warning "HADOOP_HOME unset"

Ce warning est normal et n'affecte pas l'exécution. Spark fonctionne sans Hadoop pour le mode local.

---

## 📊 Contenu des Parties

### Partie 1 - Exploration
- Chargement des fichiers CSV/JSON
- Affichage des schémas
- Comptage des lignes
- Identification des problèmes de types

### Partie 2 - Montants & Temporel
- Statistiques descriptives (moyenne, médiane, min, max)
- Distribution par tranches (< 10€, 10-50€, 50-200€, > 200€)
- Analyse par heure et jour de la semaine
- Identification des heures anormales

### Partie 3 - MCC & Erreurs
- Jointure avec les codes MCC
- Top catégories par volume et montant moyen
- Analyse des types d'erreurs
- Taux d'erreur par carte et client

### Partie 4 - Détection de Fraude
- Création d'indicateurs :
  - Transactions par carte/jour
  - Montant total par carte/jour
  - Villes différentes par carte
  - Ratio d'erreurs par carte
- Définition des seuils de détection
- Génération du DataFrame `suspicious_cards`

### Partie 5 - Synthèse
- Patterns principaux observés
- Indicateurs utiles pour un modèle ML
- Limites des données
- Recommandations

---

## 📝 Réponses aux Questions

Toutes les réponses aux questions du TP sont documentées dans le fichier **`answer.md`**.

---

## 🛠️ Technologies Utilisées

| Technologie | Version | Usage |
|-------------|---------|-------|
| Scala | 2.13.12 | Langage principal |
| Apache Spark | 3.5.0 | Traitement distribué |
| Spark SQL | 3.5.0 | Requêtes DataFrame |
| SBT | 1.12.0 | Build tool |

---

## 📈 Résultats Clés

| Métrique | Valeur |
|----------|--------|
| Transactions analysées | 13 305 915 |
| Clients uniques | 1 219 |
| Cartes uniques | 4 071 |
| Taux d'erreur global | 1.59% |
| Montant médian | 31.65 € |
| Cartes suspectes identifiées | ~100-200 |

---

## 👤 Auteur

**[Nom de l'étudiant]**  
M1 TL - Janvier 2026

---

## 📄 Licence

Projet académique - Usage éducatif uniquement.
