import json
from kafka import KafkaConsumer


consumer = KafkaConsumer("tweetsTopic", bootstrap_servers='localhost:9092', group_id="tweetis")
for message in consumer:
    tweet = json.loads(message.value.decode())

    print("===================================")
    
    print(tweet["text"])
    # print(tweet)

    # Collect hashtags

    hashtags = [h['text'] for h in tweet["entities"]["hashtags"]]
    if len(hashtags) > 0:
        print(hashtags)
