import java.net.URI

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

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
    val port_hdfs = "hdfs://localhost:8000"
    // Exemple de chemin: "hdfs://localhost:8000/data/twitter/full/date=2020-08-19/heure=17"

    if (args.length == 1 && args(0).matches("date=(\\d{4}-\\d{2}-\\d{2})")) {
      val date_jour = "/data/twitter/full/" + args(0) //"/data/twitter/full/date=2020-08-24"
      val fs = org.apache.hadoop.fs.FileSystem
        .get(new URI(port_hdfs), spark.sparkContext.hadoopConfiguration)
      fs.listStatus(new Path(date_jour))
        .filter(_.isDirectory)
        .map(_.getPath)
        .foreach(unDir => traiterUneheureDonnee(unDir.toString))

      println("Fin de l'ecriture!!!")
    } else {
      println("dude, i need one parameter like this: \"date=YYYY-MM-dd\"")
    }

  }

  def traiterUneheureDonnee( hourPathInHdfs: String) : Unit = {
    // Exctraction de date et heure à partir du regex
    val regexDate = "date=(\\d{4}-\\d{2}-\\d{2})".r.unanchored
    val regexHeure = "heure=(\\d{2})".r.unanchored
    val regexDate(date) = hourPathInHdfs
    val regexHeure(heure) = hourPathInHdfs
    val heureDebut = heure.toInt
    val heureFin = heure.toInt + 1

    // Lecture des données à une heure donnée dans hdfs
    val df = spark.read
      .format("avro") // format avro
      .load(hourPathInHdfs)
      .filter("lang == 'en'")

    // Mettre chaque élement de  la liste en Rows
    val exploded_df = df.selectExpr("explode(entities.hashtags)")
    exploded_df.persist()

    // Grouper, compter et ordonner les hashtags selon leur nombre
    val htag_df = exploded_df.select("col.text")
      .withColumnRenamed("text", "hashtags")
      .groupBy("hashtags").count()
      .orderBy(col("count").desc)
      .limit(10)
      .withColumn("date", lit(date).cast(DateType))
      .withColumn("heureDebut", lit(heureDebut.toString + "h:00m:00s"))
      .withColumn("heureFin", lit(heureFin.toString + "h:00m:00s"))

    htag_df.show(true)

    htag_df.write.format("mongo") //com.mongodb.spark.sql.DefaultSource
      .mode("append")
      .option("uri","mongodb://localhost:27017")
      .option("database","twitter")
      .option("collection","batchView")
      .save()

    exploded_df.unpersist()
  }
}
