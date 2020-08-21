name := "tweet_batchProcessing"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.0.0-preview2",
  "org.apache.spark" %% "spark-sql" % "3.0.0-preview2",
  "org.apache.spark" %% "spark-avro" % "3.0.0-preview2",
  "org.mongodb.spark" %% "mongo-spark-connector" % "3.0.0"
)