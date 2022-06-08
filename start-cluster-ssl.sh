#!/bin/sh

export KAFKA_HOME=/home/dthibau/Formations/Kafka/MyWork/kafka_2.13-3.2.0
export KAFKA_CLUSTER=/home/dthibau/Formations/Kafka/github/solutions/kafka-cluster
export KAFKA_OPTS="-Djavax.net.debug=ssl:handshake:verbose"
export KAFKA_LOGS=/home/dthibau/Formations/Kafka/MyWork/kafka-logs


$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=1 --override log.dirs=$KAFKA_LOGS/broker-1/kafka-logs --override listeners=PLAINTEXT://:9092,SSL://localhost:9192
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=2 --override log.dirs=$KAFKA_LOGS/broker-2/kafka-logs --override listeners=PLAINTEXT://:9093,SSL://localhost:9193
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=3 --override log.dirs=$KAFKA_LOGS/broker-3/kafka-logs --override listeners=PLAINTEXT://:9094,SSL://localhost:9194
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/server.properties --override broker.id=4 --override log.dirs=$KAFKA_LOGS/broker-4/kafka-logs --override listeners=PLAINTEXT://:9095,SSL://localhost:9195
