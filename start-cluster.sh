#!/bin/sh

export KAFKA_HOME=/home/dthibau/Formations/Kafka/MyWork/kafka_2.13-4.2.0
export KAFKA_OPTS="-Dorg.apache.kafka.sasl.oauthbearer.allowed.urls=http://localhost:9090/realms/kafka/protocol/openid-connect/certs,http://localhost:9090/realms/kafka/protocol/openid-connect/token" 

$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server1.properties 
$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server2.properties 
$KAFKA_HOME/bin/kafka-server-start.sh -daemon config/server3.properties
