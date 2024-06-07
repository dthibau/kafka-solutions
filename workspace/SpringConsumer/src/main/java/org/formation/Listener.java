package org.formation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.formation.model.LogMessage;
import org.formation.model.LogMessageRepository;
import org.formation.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Listener implements ConsumerRebalanceListener {

	@Autowired
	ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory;
	@Autowired
	LogMessageRepository logMessageRepository;

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	Map<String, Integer> lastIndex = new HashMap<>();
	int nbMessages = 0;
	long start = -1;

	@PostConstruct
	public void init() {
		kafkaListenerContainerFactory.getContainerProperties().setConsumerRebalanceListener(this);

	}
	@KafkaListener(topics = "position")
	public void listenPartition0(ConsumerRecord<String, Message> record) throws InterruptedException {
		if (start == -1) {
			start = System.currentTimeMillis();
		}
		/*if (lastIndex.get(record.key()) == null ) {
			logger.info("Premier message: " + record.key() + " index = " + record.value().getIndex());
		}
		if (lastIndex.get(record.key()) != null && lastIndex.get(record.key()) != record.value().getIndex() - 1) {
			logger.info("Listener Thread ID: " + Thread.currentThread().getId() + " partition :" + record.partition());
			logger.info(record.value().getIndex() + " suit " + lastIndex.get(record.key()));
		}*/
		// Temps du traitement
		// Thread.sleep(10);
		String id = record.key() + "-" + record.value().getIndex();
		if ( logMessageRepository.existsById(id) ) {
			logger.info("DOUBLON !!! Message déjà traité {}",id);
		} else {
			LogMessage logMessage = new LogMessage();
			logMessage.setId(id);
			logMessageRepository.save(logMessage);
		}

		lastIndex.put(record.key(), record.value().getIndex());
		nbMessages++;
		long elapsedTime = System.currentTimeMillis() - start;
		if (nbMessages % 1000 == 0) {
			logger.info("Thread : " + Thread.currentThread().getId() + " Process " + nbMessages + " in " + elapsedTime
					+ "ms Débit : " + ((float)nbMessages / (float)elapsedTime)*1000 +" msg/s");
		}
	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> collection) {
		logger.info("Partitions revoked {}", collection);
		/*logger.info("lastIndex = " + lastIndex);
		lastIndex.clear();*/
	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> collection) {
		logger.info("Partitions assigned {}", collection);
	}
}
