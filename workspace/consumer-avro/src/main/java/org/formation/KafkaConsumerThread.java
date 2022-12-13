package org.formation;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

public class KafkaConsumerThread implements Runnable {

	public static String TOPIC = "avro-position";
	KafkaConsumer<String, GenericRecord> consumer;
	private long sleep;
	private String id;
	
	

	public KafkaConsumerThread(String id, long sleep) {
		this.id = id;
		this.sleep = sleep;

		_initConsumer();

	}

	@Override
	public void run() {
		Map<String, Integer> updateMap = new HashMap<>();
		try {
			while (true) {
				// poll envoie le heartbeat, on bloque pdt 100ms pour récupérer les messages
				ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofMillis(sleep));
				for (ConsumerRecord<String, GenericRecord> record : records) {
//					System.out.println(
//							"Offset :" + record.offset() + " - Key:" + record.key() + " timestamp :" + new Date(record.timestamp()));
//					System.out.println("Value is " + record.value());
					int updatedCount = 1;
					if (updateMap.containsKey(record.key())) {
						updatedCount = updateMap.get(record.key()) + 1;
					}
					updateMap.put(record.key(), updatedCount);
					System.out.println("Consommer " + id + " updateMap:" + updateMap);
				}
			}
		} finally {
			consumer.close();
		}

	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093");
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		kafkaProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-position-consumer");

		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		consumer = new KafkaConsumer<String, GenericRecord>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC));
	}
}
