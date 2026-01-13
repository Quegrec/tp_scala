import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Partie3_MCC_Erreurs {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Partie 3 - MCC et Erreurs")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    val dataPath = "data/"

    println("=" * 60)
    println("PARTIE 3 - ENRICHISSEMENT MCC & ERREURS")
    println("=" * 60)

    println("\nChargement des données...")
    val transactionsDF = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath + "transactions_data.csv")
    val mccCodesDF = spark.read.option("multiLine", "true").json(dataPath + "mcc_codes.json")
    val transactionsClean = transactionsDF.withColumn("amount_clean", regexp_replace(col("amount"), "\\$", "").cast("double"))

    // Transformation MCC
    val mccCols = mccCodesDF.columns
    val stackExpr = s"stack(${mccCols.length}, ${mccCols.map(c => s"'$c', `$c`").mkString(", ")}) as (mcc_code, merchant_category)"
    val mccLookupDF = mccCodesDF.selectExpr(stackExpr).withColumn("mcc_code", col("mcc_code").cast("int"))

    // Jointure
    val transactionsEnrichies = transactionsClean
      .join(mccLookupDF, transactionsClean("mcc") === mccLookupDF("mcc_code"), "left")
      .drop("mcc_code")

    // Top catégories
    println("\n--- Top 10 catégories par volume ---")
    transactionsEnrichies.groupBy("merchant_category")
      .agg(count("*").as("nb_trans"), round(avg("amount_clean"), 2).as("montant_moyen"))
      .orderBy(desc("nb_trans"))
      .show(10, truncate = false)

    // Catégories avec erreurs
    println("\n--- Catégories avec le plus d'erreurs ---")
    transactionsEnrichies.filter(col("errors").isNotNull && col("errors") =!= "")
      .groupBy("merchant_category")
      .agg(count("*").as("nb_erreurs"), round(avg("amount_clean"), 2).as("montant_moyen"))
      .orderBy(desc("nb_erreurs"))
      .show(10, truncate = false)

    // Types d'erreurs
    println("\n--- Types d'erreurs ---")
    transactionsDF.filter(col("errors").isNotNull && col("errors") =!= "")
      .groupBy("errors").count().orderBy(desc("count")).show(15, truncate = false)

    // Taux d'erreur par carte
    println("\n--- Top 10 cartes avec le plus d'erreurs ---")
    transactionsClean.groupBy("card_id")
      .agg(count("*").as("nb_total"),
           sum(when(col("errors").isNotNull && col("errors") =!= "", 1).otherwise(0)).as("nb_erreurs"))
      .withColumn("taux_erreur", round(col("nb_erreurs") / col("nb_total") * 100, 2))
      .filter(col("nb_erreurs") > 0)
      .orderBy(desc("taux_erreur"))
      .show(10)

    // Clients suspects
    println("\n--- Clients avec taux d'erreur > 5% ---")
    transactionsClean.groupBy("client_id")
      .agg(count("*").as("nb_total"),
           sum(when(col("errors").isNotNull && col("errors") =!= "", 1).otherwise(0)).as("nb_erreurs"))
      .withColumn("taux_erreur", round(col("nb_erreurs") / col("nb_total") * 100, 2))
      .filter(col("taux_erreur") > 5)
      .orderBy(desc("taux_erreur"))
      .show(10)

    spark.stop()
    println("\n✅ Partie 3 terminée!")
  }
}
