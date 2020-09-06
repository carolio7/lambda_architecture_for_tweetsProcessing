#! /usr/bin/env python

import sys
from pyspark import SparkContext
from pyspark.sql import SparkSession, Row
from pyspark.sql import SQLContext
from pyspark.sql.functions import explode


sc = SparkContext()
spark = SparkSession.builder.config("spark.sql.broadcastTimeout", "36000").getOrCreate()


df = spark.read.format("mongo").option("uri","mongodb://127.0.0.1/twitter.speedView").load()
#df.printSchema()
df.persist()


def afficherTopTenInTime(dataframe, date, heure_debut_int, heure_fin_int):
	"""
		Cette fonction filtre les heures dans le dataframe provenant de mongoDB
		et affiches les classement des HASHTAGS dans l'ordre
		Cette fonction prend en paramètre:
			- df : dataframe provenant de mongoDB
			- date est au format 2020-08-24
			-heure de début : int entre 0, et 23
			-heure de début : int entre 1, et 23
	"""
	df1 = dataframe.\
		filter("date_debut > timestamp'{0} {1}:00:00' AND date_fin < timestamp'{0} {2}:00:00'".format(date, heure_debut_int, heure_fin_int))\
		.select("hashtags")

	df1.persist()
	#print(df1.count())

	df_exploded = df1.select(explode(df1.hashtags))
	df_exploded2 = df_exploded\
		.select(df_exploded.col.hashtagUsed, df_exploded.col.quantite)\
		.withColumnRenamed('col.hashtagUsed', 'hashtagUsed')\
		.withColumnRenamed('col.quantite', 'quantitee')

	result_df = df_exploded2.groupBy('hashtagUsed').sum().orderBy("sum(quantitee)", ascending=False).limit(10)
	print("***********************************************************************************************")
	print("Le {0} entre {1}:00:00 et {2}:00:00, ci-dessous les hashtags les plus utilisés: ".format(date, heure_debut_int, heure_fin_int))

	result_df.show()

	df1.unpersist()


# On parcours toutes les heure depuis minuit
for x in range(12):
  afficherTopTenInTime(df, '2020-09-06', x, x+1)


print("Fin de l'affichage ! ")