import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Partie4_Fraude {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Partie 4 - Détection Fraude")
      .master("local[1]")  // Un seul core pour économiser la mémoire
      .config("spark.sql.shuffle.partitions", "5")
      .config("spark.driver.memory", "512m")
      .config("spark.executor.memory", "512m")
      .config("spark.sql.adaptive.enabled", "false")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    val dataPath = "data/"

    println("=" * 60)
    println("PARTIE 4 - INDICATEURS DE FRAUDE")
    println("=" * 60)

    // Charger avec un échantillon pour éviter les problèmes de mémoire
    println("\nChargement des transactions (échantillon 30%)...")
    val transactionsRaw = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(dataPath + "transactions_data.csv")
      .sample(0.3)  // Échantillon 30% pour économiser la mémoire

    val transactionsDF = transactionsRaw
      .select("id", "card_id", "date", "amount", "merchant_city", "errors")
      .withColumn("amount_clean", regexp_replace(col("amount"), "\\$", "").cast("double"))
      .withColumn("date_jour", to_date(col("date")))
      .withColumn("has_error", when(col("errors").isNotNull && col("errors") =!= "", 1).otherwise(0))

    val nbEchantillon = transactionsDF.count()
    println(s"Transactions dans l'échantillon: $nbEchantillon")

    // ============================================
    // INDICATEUR 1: Transactions par carte par jour
    // ============================================
    println("\n--- Indicateur 1: Transactions par carte par jour ---")
    
    val transParCarteJour = transactionsDF
      .groupBy("card_id", "date_jour")
      .agg(count("*").as("nb_trans_jour"))

    val statsTransJour = transParCarteJour.agg(
      round(avg("nb_trans_jour"), 2).as("moyenne"),
      max("nb_trans_jour").as("maximum")
    ).collect()(0)
    
    println(s"Moyenne trans/carte/jour: ${statsTransJour.get(0)}")
    println(s"Maximum trans/carte/jour: ${statsTransJour.get(1)}")

    // ============================================
    // INDICATEUR 2: Montant total par carte par jour  
    // ============================================
    println("\n--- Indicateur 2: Montant par carte par jour ---")
    
    val montantParCarteJour = transactionsDF
      .filter(col("amount_clean") > 0)
      .groupBy("card_id", "date_jour")
      .agg(round(sum("amount_clean"), 2).as("montant_jour"))

    val statsMontantJour = montantParCarteJour.agg(
      round(avg("montant_jour"), 2).as("moyenne"),
      round(max("montant_jour"), 2).as("maximum")
    ).collect()(0)
    
    println(s"Moyenne montant/jour: ${statsMontantJour.get(0)} €")
    println(s"Maximum montant/jour: ${statsMontantJour.get(1)} €")

    // ============================================
    // INDICATEUR 3: Villes par carte (simplifié)
    // ============================================
    println("\n--- Indicateur 3: Villes différentes par carte ---")
    
    val villesParCarte = transactionsDF
      .filter(col("merchant_city").isNotNull && col("merchant_city") =!= "ONLINE")
      .groupBy("card_id")
      .agg(approx_count_distinct("merchant_city", 0.1).as("nb_villes"))

    val statsVilles = villesParCarte.agg(
      round(avg("nb_villes"), 2),
      max("nb_villes")
    ).collect()(0)
    
    println(s"Moyenne villes/carte: ${statsVilles.get(0)}")
    println(s"Maximum villes/carte: ${statsVilles.get(1)}")

    // ============================================
    // INDICATEUR 4: Ratio erreurs par carte
    // ============================================
    println("\n--- Indicateur 4: Ratio erreurs par carte ---")
    
    val erreursParCarte = transactionsDF
      .groupBy("card_id")
      .agg(
        count("*").as("nb_total"),
        sum("has_error").as("nb_erreurs")
      )
      .withColumn("ratio_erreurs", round(col("nb_erreurs") * 100.0 / col("nb_total"), 2))

    val statsErreurs = erreursParCarte.agg(
      round(avg("ratio_erreurs"), 2),
      round(max("ratio_erreurs"), 2)
    ).collect()(0)
    
    println(s"Moyenne ratio erreurs: ${statsErreurs.get(0)}%")
    println(s"Maximum ratio erreurs: ${statsErreurs.get(1)}%")

    // ============================================
    // DÉTECTION SUSPECTS
    // ============================================
    println("\n" + "=" * 60)
    println("DÉTECTION COMPORTEMENTS SUSPECTS")
    println("=" * 60)

    val seuilTransJour = 15
    val seuilMontantJour = 2000.0
    val seuilVilles = 5
    val seuilErreurs = 5.0

    println(s"\nSeuils appliqués:")
    println(s"  - Trans/jour    > $seuilTransJour")
    println(s"  - Montant/jour  > $seuilMontantJour €")
    println(s"  - Villes/carte  > $seuilVilles")
    println(s"  - Ratio erreurs > $seuilErreurs%")

    // Cartes avec trop de transactions/jour
    val cartesTransElevees = transParCarteJour
      .filter(col("nb_trans_jour") > seuilTransJour)
      .select("card_id").distinct()
    println(s"\nCartes > $seuilTransJour trans/jour: ${cartesTransElevees.count()}")

    // Cartes avec montant élevé/jour
    val cartesMontantEleve = montantParCarteJour
      .filter(col("montant_jour") > seuilMontantJour)
      .select("card_id").distinct()
    println(s"Cartes > $seuilMontantJour €/jour: ${cartesMontantEleve.count()}")

    // Cartes multi-villes
    val cartesMultiVilles = villesParCarte
      .filter(col("nb_villes") > seuilVilles)
      .select("card_id")
    println(s"Cartes > $seuilVilles villes: ${cartesMultiVilles.count()}")

    // Cartes avec erreurs élevées
    val cartesErreurs = erreursParCarte
      .filter(col("ratio_erreurs") > seuilErreurs)
      .select("card_id")
    println(s"Cartes > $seuilErreurs% erreurs: ${cartesErreurs.count()}")

    // ============================================
    // SUSPICIOUS CARDS - Union des critères
    // ============================================
    println("\n" + "=" * 60)
    println("SUSPICIOUS_CARDS")
    println("=" * 60)

    val suspiciousCards = cartesTransElevees
      .union(cartesMontantEleve)
      .union(cartesMultiVilles)
      .union(cartesErreurs)
      .distinct()

    val nbSuspects = suspiciousCards.count()
    val nbCartesTotal = transactionsDF.select("card_id").distinct().count()
    
    println(s"\nTotal cartes suspectes: $nbSuspects")
    println(s"Cartes dans l'échantillon: $nbCartesTotal")
    println(f"Pourcentage suspect: ${nbSuspects.toDouble/nbCartesTotal*100}%.2f%%")

    // Afficher quelques cartes suspectes
    println("\nExemples de cartes suspectes:")
    suspiciousCards.show(20)

    // Top transactions/jour
    println("Top 10 jours avec le plus de transactions:")
    transParCarteJour.orderBy(desc("nb_trans_jour")).show(10)

    // Top montants/jour
    println("Top 10 montants journaliers:")
    montantParCarteJour.orderBy(desc("montant_jour")).show(10)

    spark.stop()
    println("\n✅ Partie 4 terminée avec succès!")
  }
}
