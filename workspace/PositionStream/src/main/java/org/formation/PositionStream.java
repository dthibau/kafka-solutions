package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.formation.model.Courier;
import org.formation.model.Position;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class PositionStream {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-position");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

// Création d’une topolgie de processeurs
        final StreamsBuilder builder = new StreamsBuilder();
        builder.<String, Courier>stream("avro-position")
                .mapValues(coursier -> {
                    Position position = (Position)coursier.getPosition();
                    position.setLatitude((double)Math.round(position.getLatitude()));
                    position.setLongitude((double)Math.round(position.getLongitude()));
                    return coursier;
                })
                .to("avro-position-rounded");

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
