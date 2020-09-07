#! /usr/bin/env python
# @carolio7
#

import sys
from pyspark import SparkContext
from pyspark.sql import SparkSession, Row
from pyspark.sql import SQLContext


sc = SparkContext()
spark = SparkSession.builder.config("spark.sql.broadcastTimeout", "36000").getOrCreate()

def main():

    # Read command line arguments
    date = sys.argv[1]	# Sous forme 2020-09-03
    heure_debut = int(sys.argv[2])
    #heure_fin = sys.argv[3]

    df = spark.read.format("mongo").option("uri","mongodb://127.0.0.1/twitter.batchView").load()

    df.createOrReplaceTempView("premier_df")

    df2 = spark.sql("""SELECT hashtags, count FROM premier_df df1 WHERE df1.date == '{0}' AND df1.heureDebut == '{1}h:00m:00s'""".format(date, heure_debut )).orderBy("count", ascending=False)

    print("***********************************************************************************************")
    print("Le {0} entre {1}:00:00 et {2}:00:00, ci-dessous les hashtags les plus utilisés: ".format(date, heure_debut, heure_debut +1 ))
    df2.show()



if __name__ == "__main__":
    main()
