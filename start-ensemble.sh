#!/bin/sh

export ZK_HOME=/home/dthibau/Formations/Kafka/MyWork/apache-zookeeper-3.5.7-bin/
export ZK_ENSEMBLE=/home/dthibau/Formations/Kafka/github/solutions/zookeeper-ensemble


$ZK_HOME/bin/zkServer.sh --config $ZK_ENSEMBLE/1/conf start 
$ZK_HOME/bin/zkServer.sh --config $ZK_ENSEMBLE/2/conf start
$ZK_HOME/bin/zkServer.sh --config $ZK_ENSEMBLE/3/conf start
