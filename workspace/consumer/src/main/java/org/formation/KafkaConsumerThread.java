package org.formation;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.formation.dao.ConsumerDao;
import org.formation.model.Courier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);

	public static String TOPIC = "position";
	KafkaConsumer<String, Courier> consumer;
	private String id;

	private ConsumerDao consumerDao;
	
	

	public KafkaConsumerThread(String id) throws ClassNotFoundException {
		this.id = id;
		this.consumerDao = new ConsumerDao();

		_initConsumer();

	}

	@Override
	public void run() {
		try {
			while (true) {
				// poll envoie le heartbeat, on bloque pdt 100ms pour récupérer les messages
				ConsumerRecords<String, Courier> records = consumer.poll(Duration.ofMillis(1000));
				logger.info("Consommer " + id + " fetch :" +records.count() + " messages");
				for (ConsumerRecord<String, Courier> record : records) {
					try {
						consumerDao.insert(record.value().getId(), record.offset());
					} catch (SQLException e) {
						System.err.println("Erreur d'insertion dans la base de données : " + e.getMessage());
					}

				}
			}
		} finally {
			consumer.close();
		}

	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093");
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.formation.model.JsonDeserializer");
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "position-consumer");
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		consumer = new KafkaConsumer<String, Courier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC),new PartitionListener());
	}
}
