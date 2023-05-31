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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.formation.dao.ConsumerDao;
import org.formation.model.Courier;
import org.formation.model.JsonDeserializer;
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

		int count = 0;
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

				count += records.count();
				System.out.println("A consommé " + count + " messages");
			}
		} finally {
			consumer.close();
		}

	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.GROUP_ID_CONFIG));
		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
		kafkaProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");


		consumer = new KafkaConsumer<String, Courier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC),new PartitionListener());
	}
}
