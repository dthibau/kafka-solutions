package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.formation.model.Courier;
import org.formation.model.Position;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class PositionStream {
    public static String REGISTRY_URL = "http://localhost:8081";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "list-streams-position");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG,5);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Map<String, Object> config = new HashMap<>();
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL); // URL

        SpecificAvroSerde<Position> positionSerde = new SpecificAvroSerde<>();
        positionSerde.configure(config, true);

        SpecificAvroSerde<Courier> coursierSerde = new SpecificAvroSerde<>();
        coursierSerde.configure(config, true);

        Serde<List<String>> listSerde = Serdes.ListSerde(ArrayList.class, Serdes.String());

// Création d’une topolgie de processeurs
        final StreamsBuilder builder = new StreamsBuilder();

        // Arrondir les Positions
        KStream<String,Position> coursierStream = builder.<String, Courier>stream("avro-position")
                .mapValues(coursier -> {
                    Position position = (Position)coursier.getPosition();
                    position.setLatitude((double)Math.round(position.getLatitude()));
                    position.setLongitude((double)Math.round(position.getLongitude()));
                    return position;
                });

               // Stocker les dernières positions dans uen table
                                KTable<String,Position> coursiersTable = coursierStream.toTable(
                                Materialized.<String, Position, KeyValueStore<Bytes, byte[]>>as("coursiers-last-position")
                                                .withKeySerde(Serdes.String())
                                        .withValueSerde(positionSerde)
                        );
                        // Grouper (partitionner) par rapport à la Position
                        KGroupedTable<Position, String> groupedTable = coursiersTable.groupBy((key, value) ->  KeyValue.pair(value, key), Grouped.with(positionSerde, Serdes.String()));

                        // Agrégation avec adder et substractor
                                KTable<Position,List<String>> listeCoursierParPosition = groupedTable
                                 .aggregate(
                                  () -> {
                                          System.out.println("Initializing");
                                          return new ArrayList<String>();
                                      },
                                    (position, newCoursier, currentList) -> {
                                          System.out.println("Current list for position " + position + " is " + currentList + "adding " + newCoursier);
                                          if ( !currentList.contains(newCoursier) ) {
                                                  currentList.add(newCoursier);
                                              }
                                            return currentList;
                                        },
                                   (position, oldCoursier, currentList)  -> {
                                            System.out.println("Current list for position " + position + " is " + currentList + "removing " + oldCoursier);
                                            ArrayList<String> newList = new ArrayList<>(currentList);
                                            newList.remove(oldCoursier);
                                            return newList;
                                        },
                                        Materialized.<Position, List<String>, KeyValueStore<Bytes, byte[]>>as("aggregated-table-store") /* state store name */
                                                        .withValueSerde(listSerde)
                                                .withKeySerde(positionSerde)/* serde for aggregate value */
                                );

                        listeCoursierParPosition.toStream().mapValues(v -> v.toString()).to("coursiersParPosition",Produced.with(positionSerde,Serdes.String()));

        final Topology topology = builder.build();

// Instanciation du Stream à partir d’une topologie et des propriétés
        final KafkaStreams streams = new KafkaStreams(topology, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        // Démarrage du stream
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
