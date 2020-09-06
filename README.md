###Développement architecture Big Data complete des tweets

#1.Présentation du sujet:
Dans ce projet, nous allons créer un outil qui permet de lister les dix hashtags les plus fréquents sur Twitter pour chaque heure.

Il faut donc:
    1. collecter les données
    2. les stocker dans des structures adaptées
    3. les traiter en temps-réel et en batch
    4. valider la robustesse architecturale de la solution proposée


#2.Etapes de traitement:
    1. Les données temps-réel seront envoyées à un cluster Kafka pour à la fois être stockées dans un système de fichiers distribués (HDFS) et transférées à un pipeline Storm.
    2. Batch layer: les données présentes dans HDFS seront analysées périodiquement et par lot (“batch”) par des jobs MapReduce (Hadoop ou Spark).
    3. View layer: le résultat de ces jobs MapReduce sera stocké dans MongoDb.
    4. Speed layer: les données traitées par le pipeline Storm/Kafka seront stockées dans MongoDb. Les données rendues inutiles par l’exécution des tâches de la batch layer devront être effacées au moment opportun.

#3.Utilisation:
    - Installer l'API python de Twitter avec: pip install twitter
            Puis lancer le producer avec python: python tweetsProducer.py
            !Attention: N'oubliez pas de renseigner vos accès à l'API Twitter dans producer.py
            
    - Démarrer l'application spark connectant Kafka->HDFS avec la commamnde: spark-submit --class Connecteur --master local ./target/scala-2.12/connecteurkafka2hdfs_2.12-0.1.jar
    
    !!!! Après avoir lancé la première fois l'appli kafka->HDFS, on n'arrive pas à écrire dans le repertoire HDFS.
	Ceci est dû au blockage d'accès en écriture du repertoire "checkpointLocation".
	Pour résoudre cela, il faut changer l'accès en accès du repertoir "/tmp" avec la commande: hdfs dfs -chmod 777 /tmp 
	

    - Démmarrer l'analyse en batch avec: spark-submit --class App --master local ./target/scala-2.12/tweet_batchprocessing_2.12-0.1.jar
    		spark-submit --class App --master local ./target/scala-2.12/tweet_batchprocessing_2.12-0.1.jar date=2020-08-24
    	
    	!! : N'oubliez pas de mettre à jour la date en fonction de cette existant dans votre HDFS

    - Pour soumettre l'application storm en fichier jar au cluster: ~/Bureau/data_Architect/storm/apache-storm-2.1.0/bin/storm jar ./target/tweetAnalytics-1.0-SNAPSHOT.jar tweetAnalytics.App remote
    
    
    - 
