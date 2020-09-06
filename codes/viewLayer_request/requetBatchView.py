#! /usr/bin/env python

import sys
from pyspark import SparkContext
from pyspark.sql import SparkSession, Row
from pyspark.sql import SQLContext


sc = SparkContext()
spark = SparkSession.builder.config("spark.sql.broadcastTimeout", "36000").getOrCreate()


df = spark.read.format("mongo").option("uri","mongodb://127.0.0.1/twitter.batchView").load()
df.printSchema()
df.show()
df.createOrReplaceTempView("premier_df")
df2 = spark.sql("""
SELECT hashtags, count FROM premier_df df1 
WHERE df1.date == '2020-09-03'
AND df1.heureDebut == '19h:00m:00s'
AND df1.heureFin == '20h:00m:00s'
""").orderBy("count", ascending=False)


df2.show()