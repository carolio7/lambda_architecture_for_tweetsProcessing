#! /usr/bin/env python

import twitter # pip install twitter
import json
import fastavro

def main():

    # Put here your Twitter API credentials obtained at https://apps.twitter.com/

    # Note: you need a Twitter account to create an app.
    with open("config.json") as fichierDeConfig:
      config = json.load(fichierDeConfig)

    #oauth = twitter.OAuth("token", "token_secret", "consumer_key", "consumer_secret")
    oauth = twitter.OAuth(config["accessToken"],\
                          config["accessTokenSecret"],\
                          config["apiKey"],\
                          config["apiSecretKey"])

    t = twitter.TwitterStream(auth=oauth)


    sample_tweets_in_english = t.statuses.sample(language="en")
    
    maxLenList = 60
    listTweets = []
    # Read schema
    schema = json.load(open("./schemaTweets.avsc"))

    for tweet in sample_tweets_in_english:
    	if "delete" in tweet:
    		# Deleted tweet events do not have any associated text
    		continue

    	hashtags = [h['text'] for h in tweet["entities"]["hashtags"]]

    	if len(hashtags) > 0 :
    		print(hashtags)
    		listTweets.append(tweet)
    		if len(listTweets) > maxLenList :
    			with open("./exempleTweets.avro", 'wb') as avro_file:
    				print("**************************************")
    				print("Ecriture sauvegardes des fichiers sérialisés")
    				fastavro.writer(avro_file, schema, listTweets)
    				print("**************************************")
    			
    			listTweets = []

      


if __name__ == "__main__":

    main()
