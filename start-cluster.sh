#!/bin/sh

export KAFKA_HOME=/home/dthibau/Formations/Kafka/MyWork/kafka_2.13-4.2.0


$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server1.properties 
$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server2.properties 
$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server3.properties
