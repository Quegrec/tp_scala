import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Partie1_Exploration {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Partie 1 - Exploration des données")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    val dataPath = "data/"

    println("=" * 60)
    println("PARTIE 1 - EXPLORATION DES DONNÉES")
    println("=" * 60)

    // Chargement des fichiers
    println("\nChargement des données...")
    val cardsDF = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath + "cards_data.csv")
    val usersDF = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath + "users_data.csv")
    val transactionsDF = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath + "transactions_data.csv")
    val mccCodesDF = spark.read.option("multiLine", "true").json(dataPath + "mcc_codes.json")

    // Schémas
    println("\n--- CARDS DATA ---")
    cardsDF.printSchema()
    cardsDF.show(5, truncate = false)

    println("\n--- USERS DATA ---")
    usersDF.printSchema()
    usersDF.show(5, truncate = false)

    println("\n--- TRANSACTIONS DATA ---")
    transactionsDF.printSchema()
    transactionsDF.show(5, truncate = false)

    // Résumé des colonnes
    println("\n" + "=" * 60)
    println("RÉSUMÉ DES COLONNES")
    println("=" * 60)
    println(s"Cards:        ${cardsDF.columns.length} colonnes")
    println(s"Users:        ${usersDF.columns.length} colonnes")
    println(s"Transactions: ${transactionsDF.columns.length} colonnes")
    println(s"MCC Codes:    ${mccCodesDF.columns.length} colonnes")

    // Statistiques de comptage
    println("\n" + "=" * 60)
    println("STATISTIQUES DE COMPTAGE")
    println("=" * 60)
    
    val nbTransactions = transactionsDF.count()
    val nbClientsUniques = transactionsDF.select("client_id").distinct().count()
    val nbCartesUniques = transactionsDF.select("card_id").distinct().count()
    val nbCommercants = transactionsDF.select("merchant_id").distinct().count()
    
    println(s"\nNombre total de transactions : $nbTransactions")
    println(s"Nombre de clients uniques    : $nbClientsUniques")
    println(s"Nombre de cartes uniques     : $nbCartesUniques")
    println(s"Nombre de commerçants uniques: $nbCommercants")
    
    // Ratios
    val ratioParClient = nbTransactions.toDouble / nbClientsUniques
    val ratioParCarte = nbTransactions.toDouble / nbCartesUniques
    val ratioParCommercant = nbTransactions.toDouble / nbCommercants
    
    println(f"\nRatio transactions/client    : $ratioParClient%.2f")
    println(f"Ratio transactions/carte     : $ratioParCarte%.2f")
    println(f"Ratio transactions/commerçant: $ratioParCommercant%.2f")

    // Analyse qualité
    println("\n" + "=" * 60)
    println("ANALYSE QUALITÉ DES DONNÉES")
    println("=" * 60)
    
    println("\n--- Valeurs nulles par colonne ---")
    transactionsDF.columns.foreach { colName =>
      val nbNulls = transactionsDF.filter(col(colName).isNull || col(colName) === "").count()
      val pct = (nbNulls.toDouble / nbTransactions) * 100
      if (nbNulls > 0) println(f"$colName%-20s : $nbNulls%10d ($pct%.2f%%)")
    }
    
    // Transactions avec montant <= 0
    val transactionsClean = transactionsDF.withColumn("amount_clean", regexp_replace(col("amount"), "\\$", "").cast("double"))
    val nbMontantNegatif = transactionsClean.filter(col("amount_clean") <= 0).count()
    println(f"\nTransactions montant <= 0: $nbMontantNegatif (${(nbMontantNegatif.toDouble / nbTransactions) * 100}%.2f%%)")
    
    // Transactions avec erreurs
    val nbAvecErreurs = transactionsDF.filter(col("errors").isNotNull && col("errors") =!= "").count()
    println(f"Transactions avec erreurs: $nbAvecErreurs (${(nbAvecErreurs.toDouble / nbTransactions) * 100}%.2f%%)")

    spark.stop()
    println("\n✅ Partie 1 terminée!")
  }
}
