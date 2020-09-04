ThisBuild / useCoursier := false

name := "connecteurKafka2Hdfs"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.0.0",
  "org.apache.spark" %% "spark-sql" % "3.0.0" ,
  "org.apache.spark" %% "spark-streaming" % "3.0.0",
  "org.apache.spark" %% "spark-avro" % "3.0.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.0.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.0.0",
  "org.apache.kafka" % "kafka-clients" % "2.6.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10-assembly" % "3.0.0",
  "org.apache.commons" % "commons-pool2" % "2.8.1"
)