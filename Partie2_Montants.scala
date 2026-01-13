import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Partie2_Montants {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Partie 2 - Analyse des montants")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    val dataPath = "data/"

    println("=" * 60)
    println("PARTIE 2 - ANALYSE DES MONTANTS & TEMPORELLE")
    println("=" * 60)

    println("\nChargement des transactions...")
    val transactionsDF = spark.read.option("header", "true").option("inferSchema", "true").csv(dataPath + "transactions_data.csv")
    val transactionsClean = transactionsDF.withColumn("amount_clean", regexp_replace(col("amount"), "\\$", "").cast("double"))
    val nbTransactions = transactionsDF.count()

    // Statistiques des montants
    println("\n--- Statistiques des montants ---")
    val montantsPositifs = transactionsClean.filter(col("amount_clean") > 0)
    
    val statsDF = montantsPositifs.agg(
      round(avg("amount_clean"), 2).as("moyenne"),
      round(min("amount_clean"), 2).as("minimum"),
      round(max("amount_clean"), 2).as("maximum"),
      round(stddev("amount_clean"), 2).as("ecart_type")
    ).first()
    
    val mediane = montantsPositifs.stat.approxQuantile("amount_clean", Array(0.5), 0.01)(0)
    
    println(f"Moyenne     : ${statsDF.getDouble(0)}%.2f €")
    println(f"Médiane     : $mediane%.2f €")
    println(f"Minimum     : ${statsDF.getDouble(1)}%.2f €")
    println(f"Maximum     : ${statsDF.getDouble(2)}%.2f €")
    println(f"Écart-type  : ${statsDF.getDouble(3)}%.2f €")

    // Distribution par tranche
    println("\n--- Distribution par tranche ---")
    val transactionsAvecTranche = montantsPositifs.withColumn("tranche",
      when(col("amount_clean") < 10, "< 10 €")
        .when(col("amount_clean") < 50, "10-50 €")
        .when(col("amount_clean") < 200, "50-200 €")
        .otherwise("> 200 €")
    )
    
    transactionsAvecTranche.groupBy("tranche").count()
      .withColumn("pourcentage", round(col("count") / montantsPositifs.count() * 100, 2))
      .orderBy(when(col("tranche") === "< 10 €", 1).when(col("tranche") === "10-50 €", 2)
        .when(col("tranche") === "50-200 €", 3).otherwise(4))
      .show()

    // Analyse temporelle
    println("\n--- Analyse temporelle ---")
    val transactionsTemporelles = transactionsDF
      .withColumn("heure", hour(col("date")))
      .withColumn("jour_semaine", dayofweek(col("date")))
      .withColumn("jour_nom", 
        when(col("jour_semaine") === 1, "Dimanche")
          .when(col("jour_semaine") === 2, "Lundi")
          .when(col("jour_semaine") === 3, "Mardi")
          .when(col("jour_semaine") === 4, "Mercredi")
          .when(col("jour_semaine") === 5, "Jeudi")
          .when(col("jour_semaine") === 6, "Vendredi")
          .otherwise("Samedi"))

    println("\nTransactions par heure:")
    transactionsTemporelles.groupBy("heure").count().orderBy("heure").show(24)

    println("Transactions par jour:")
    transactionsTemporelles.groupBy("jour_semaine", "jour_nom").count().orderBy("jour_semaine").show()

    spark.stop()
    println("\n✅ Partie 2 terminée!")
  }
}
