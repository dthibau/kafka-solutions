package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.formation.model.Courier;
import org.formation.model.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class PositionStream {
    public static String REGISTRY_URL = "http://localhost:8081";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-position-inverse-key");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Map<String, Object> config = new HashMap<>();
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL); // URL

        SpecificAvroSerde<Position> positionSerde = new SpecificAvroSerde<>();
        positionSerde.configure(config, true);

// Création d’une topolgie de processeurs
        final StreamsBuilder builder = new StreamsBuilder();
        builder.<String, Courier>stream("avro-position")
                .mapValues(coursier -> {
                    Position position = (Position)coursier.getPosition();
                    position.setLatitude((double)Math.round(position.getLatitude()));
                    position.setLongitude((double)Math.round(position.getLongitude()));
                    return coursier;
                })
                .selectKey((k, coursier) -> (Position)coursier.getPosition())
                .mapValues(courier -> courier.getId().toString())
                .to("avro-position-inverse-key", Produced.with(positionSerde, Serdes.String()));

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
