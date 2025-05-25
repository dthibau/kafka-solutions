package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.formation.model.Courier;
import org.formation.model.Position;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class PositionStream {
    public static String REGISTRY_URL = "http://localhost:8081";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "windowed-count-streams-position-branch");
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

// Création d’une topolgie de processeurs
        final StreamsBuilder builder = new StreamsBuilder();
        Map<String, KStream<Position, String>> branches = builder.<String, Courier>stream("avro-position")
                .mapValues(coursier -> {
                    Position position = (Position)coursier.getPosition();
                    position.setLatitude((double)Math.round(position.getLatitude()));
                    position.setLongitude((double)Math.round(position.getLongitude()));
                    return coursier;
                })
                .selectKey((k, coursier) -> (Position)coursier.getPosition())
                .mapValues(courier -> courier.getId().toString())
                .split(Named.as("Branch-"))
                .branch((key, value) -> key.getLatitude() == 45,  /* first predicate  */
                        Branched.as("South"))
                .defaultBranch(Branched.as("North"));

                branches.get("Branch-South")
                        .groupByKey(Grouped.with(positionSerde, Serdes.String()))
                        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                        .count(Materialized.with(positionSerde, Serdes.Long())).toStream()
                        .map((windowedPosition,count) -> {
                            return new KeyValue<Position,String>(windowedPosition.key(),count.toString());
                        })
                        .to("windowed-count-avro-position-south", Produced.with(positionSerde, Serdes.String()));
                branches.get("Branch-North")
                        .groupByKey(Grouped.with(positionSerde, Serdes.String()))
                        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                        .count(Materialized.with(positionSerde, Serdes.Long())).toStream()
                        .map((windowedPosition,count) -> {
                            return new KeyValue<Position,String>(windowedPosition.key(),count.toString());
                        })
                        .to("windowed-count-avro-position-north", Produced.with(positionSerde, Serdes.String()));

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
