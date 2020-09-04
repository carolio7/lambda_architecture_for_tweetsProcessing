import java.nio.file.{Files, Paths}
import java.util.UUID

import org.apache.avro.Schema
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.SchemaConverters
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger.ProcessingTime
import org.apache.spark.sql.types.{DataTypes, StructType}

import scala.concurrent.duration._
/**
 * @author carolio7
 */
object Connecteur {

  // je crée mon spark session
  val spark: SparkSession = SparkSession
    .builder()
    .master("local[*]")
    .appName("hdfs-kafka-connector")
    .getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {
    // Je définis l'addresse de HDFS
    val addr_hdfs = "hdfs://localhost:8000"
    val checkpointLocation: String = addr_hdfs + "/tmp/checkpoint-" + UUID.randomUUID.toString

    // On parse le schéma avro pour le convertir en Structype
    //Le dataframe obtenu sera calqué à l'image de notre schéma avro
    val avroSchema = new String(Files.readAllBytes(Paths.get("./schemaTweets.avsc")))
    val parser: Schema.Parser = new Schema.Parser()
    val avroSchemaParsed: Schema = parser.parse(avroSchema)
    val schemaType = SchemaConverters.toSqlType(avroSchemaParsed)
      .dataType.asInstanceOf[StructType]

    // Lecture du flux kafka
    val upstream = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "tweetsTopic")
      .load().selectExpr("CAST(value AS STRING)")

    // Conversion de value reçu de kafka en fonction du schéme avro,
    // filter les messages sans hashtags
    // Ajout de colonne jour et heure pour ranger les données dans HDFS
    val df = upstream
      .select(from_json(col("value"), schemaType).as("json")).select("json.*")
      .filter(size($"json.entities.hashtags")>0)
      .withColumn("timestamp", col("timestamp_ms").cast(DataTypes.LongType).$div(1000).cast(DataTypes.TimestampType))
      .withColumn("date", col("timestamp").cast(DataTypes.DateType))
      .withColumn("heure", hour(col("timestamp")))
      .drop("timestamp")

    // Ecriture des flux dans HDFS après un temps
    val downstream = df.writeStream
      .partitionBy("date","heure")
      .format("avro")
      .option("path", addr_hdfs + "/data/twitter/full")
      .outputMode("append")
      .trigger(ProcessingTime(300.seconds))
      .option("checkpointLocation", checkpointLocation)
      .start()

    println(downstream.lastProgress)
    downstream.awaitTermination()
    println("processus arreté !!! Au revoir.")
  }

}
