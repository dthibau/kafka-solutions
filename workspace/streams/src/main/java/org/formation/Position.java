/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.formation;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.formation.model.Courier;
import org.formation.model.CourierSerde;

/**
 * In this example, we implement a simple LineSplit program using the high-level Streams DSL
 * that reads from a source topic "streams-plaintext-input", where the values of messages represent lines of text,
 * and writes the messages as-is into a sink topic "streams-pipe-output".
 */
public class Position {

    private static final String INPUT_TOPIC="position";
    private static final String DISTANCE_OUTPUT_TOPIC = "distance";
    private static final String AVERAGE_OUTPUT_TOPIC = "average";
    private static final double EARTH_RADIUS = 6371.01; // Kilometers
    private static final org.formation.model.Position ORIGIN=new org.formation.model.Position();
    static {
        ORIGIN.latitude = 40.7128; // Example latitude
        ORIGIN.longitude = -74.0060; // Example longitude
    }
    public static void main(String[] args) throws Exception {

        

    	
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-position");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, CourierSerde.class);

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Courier> stream = builder.stream(INPUT_TOPIC);
        stream.mapValues(c -> {
        	System.out.println(c);
            Courier ret = new Courier();
            ret.distance = calculateDistance(ORIGIN, c.currentPosition);
            ret.setId(c.getId());

            return ret;
        }).to(DISTANCE_OUTPUT_TOPIC);

        stream.groupByKey()
                .aggregate(
                        Courier::new,
                        (key, value, aggregate) -> {

                            value.averagePosition = calculateAverage(aggregate.averagePosition, value.currentPosition, 1);
                            return value;
                        },
                        Materialized.<String, Courier, KeyValueStore<Bytes, byte[]>>as("average-positions-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new CourierSerde())
                )
                .toStream()
                .to(AVERAGE_OUTPUT_TOPIC);

        final Topology topology = builder.build();
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

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    public static double calculateDistance(org.formation.model.Position pos1, org.formation.model.Position pos2) {
        double latDistance = Math.toRadians(pos2.latitude - pos1.latitude);
        double lngDistance = Math.toRadians(pos2.longitude - pos1.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(pos1.latitude)) * Math.cos(Math.toRadians(pos2.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    public static org.formation.model.Position calculateAverage(org.formation.model.Position pos1, org.formation.model.Position pos2, long count) {
        if (pos1 == null ) {
            return pos2;
        }
        double avgLatitude = (pos1.latitude + pos2.latitude * count) / (count + 1);
        double avgLongitude = (pos1.longitude + pos2.longitude * count) / (count + 1);

        org.formation.model.Position avgPosition = new org.formation.model.Position();
        avgPosition.latitude = avgLatitude;
        avgPosition.longitude = avgLongitude;

        return avgPosition;
    }
}
