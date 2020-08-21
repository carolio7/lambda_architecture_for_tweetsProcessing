import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
 * @author carolio7
 */
object App {

  // je crée mon spark session
  val spark: SparkSession = SparkSession
    .builder()
    .master("local[*]")
    .appName("twitter-batchProcess")
    .getOrCreate()

  def main(args: Array[String]): Unit = {
    // Je définis l'addresse de HDFS
    val addr_hdfs = "hdfs://localhost:8000"

    // Lecture du flux kafka
    val df = spark.read
      .format("avro")
      .load("hdfs://localhost:8000/data/twitter/full/date=2020-08-19/heure=17")

    // Mettre chaque élement de  la liste en Rows
    val exploded_df = df.selectExpr("explode(entities.hashtags)")
    exploded_df.persist()

    // Grouper, compter et ordonner les hashtags selon leur nombre
    val htag_df = exploded_df.select("col.text")
      .withColumnRenamed("text", "hashtags")
      .groupBy("hashtags").count()
      .orderBy(col("count").desc)
      .limit(10)

    htag_df.show(true)

    htag_df.write.format("mongo")
      .mode("append")
      .option("uri","mongodb://localhost:27017")
      .option("database","twitter")
      .option("collection","batchView")
      .save()

    println("Fin de l'ecriture!!!")
  }
}
