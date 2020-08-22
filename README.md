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
            !Attention: N'oubliez pas de renseigner vos accès à l'API Twitter dans le producer.py
    - Démarrer l'application spark connectant Kafka->HDFS avec la commamnde: spark-submit --class Connecteur --master local ./target/scala-2.12/connecteurkafka2hdfs_2.12-0.1.jar
    - 