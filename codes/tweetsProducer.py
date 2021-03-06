#! /usr/bin/env python
# @carolio7

import twitter # pip install twitter
import json
from kafka import KafkaProducer


def main():

    producer = KafkaProducer(bootstrap_servers="localhost:9092")
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

    for tweet in sample_tweets_in_english:

       if "delete" in tweet:

           # Deleted tweet events do not have any associated text

           continue


       producer.send("tweetsTopic", json.dumps(tweet).encode())


       # Tweet text

       # print(tweet["text"])
       #print(tweet)

       # Collect hashtags

       """
       hashtags = [h['text'] for h in tweet["entities"]["hashtags"]]

       if len(hashtags) > 0:

           print(hashtags)
      """


if __name__ == "__main__":

    main()