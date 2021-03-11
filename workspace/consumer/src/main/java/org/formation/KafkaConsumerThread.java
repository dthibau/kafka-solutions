package org.formation;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.formation.model.Courier;

public class KafkaConsumerThread implements Runnable {

	public static String TOPIC = "position";
	KafkaConsumer<String, Courier> consumer;
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
				ConsumerRecords<String, Courier> records = consumer.poll(Duration.ofMillis(sleep));
				for (ConsumerRecord<String, Courier> record : records) {
					System.out.println(
							"Offset :" + record.offset() + " - Key:" + record.key() + " timestamp :" + new Date(record.timestamp()));

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
		kafkaProps.put("bootstrap.servers", "localhost:9092,localhost:9093");
		kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaProps.put("value.deserializer", "org.formation.model.JsonDeserializer");
		kafkaProps.put("group.id", "position-consumer");

		consumer = new KafkaConsumer<String, Courier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC));
	}
}
