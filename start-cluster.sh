#!/bin/sh

export KAFKA_HOME=/home/dthibau/Formations/Kafka/MyWork/kafka_2.12-2.4.1
export KAFKA_CLUSTER=/home/dthibau/Formations/Kafka/github/solutions/kafka-cluster


$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/broker-1/server.properties
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/broker-2/server.properties
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_CLUSTER/broker-3/server.properties
