#!/bin/sh

export KAFKA_HOME=/home/dthibau/Formations/Kafka/MyWork/kafka_2.12-2.4.1
export KAFKA_CLUSTER=/home/dthibau/Formations/Kafka/github/solutions/kafka-cluster

export OLD_KAFKA_OPTS="$KAFKA_OPTS"
export KAFKA_OPTS="$OLD_KAFKA_OPTS -javaagent:/home/dthibau/Formations/Kafka/MyWork/promotheus/jmx_prometheus_javaagent-0.6.jar=7071:/home/dthibau/Formations/Kafka/MyWork/promotheus/kafka-2_0_0.yml"
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=1 --override log.dirs=/home/dthibau/Formations/Kafka/MyWork/kafka-cluster/broker-1/kafka-logs

export KAFKA_OPTS="$OLD_KAFKA_OPTS -javaagent:/home/dthibau/Formations/Kafka/MyWork/promotheus/jmx_prometheus_javaagent-0.6.jar=7072:/home/dthibau/Formations/Kafka/MyWork/promotheus/kafka-2_0_0.yml"
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=2 --override log.dirs=/home/dthibau/Formations/Kafka/MyWork/kafka-cluster/broker-2/kafka-logs --override listeners=PLAINTEXT://:9093

export KAFKA_OPTS="$OLD_KAFKA_OPTS -javaagent:/home/dthibau/Formations/Kafka/MyWork/promotheus/jmx_prometheus_javaagent-0.6.jar=7073:/home/dthibau/Formations/Kafka/MyWork/promotheus/kafka-2_0_0.yml"
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=3 --override log.dirs=/home/dthibau/Formations/Kafka/MyWork/kafka-cluster/broker-3/kafka-logs --override listeners=PLAINTEXT://:9094

export KAFKA_OPTS="$OLD_KAFKA_OPTS -javaagent:/home/dthibau/Formations/Kafka/MyWork/promotheus/jmx_prometheus_javaagent-0.6.jar=7074:/home/dthibau/Formations/Kafka/MyWork/promotheus/kafka-2_0_0.yml"
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=4 --override log.dirs=/home/dthibau/Formations/Kafka/MyWork/kafka-cluster/broker-4/kafka-logs --override listeners=PLAINTEXT://:9095
