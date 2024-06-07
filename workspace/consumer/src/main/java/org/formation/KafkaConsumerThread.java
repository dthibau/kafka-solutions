package org.formation;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.formation.dao.ConsumerDao;
import org.formation.model.Courier;
import org.formation.model.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.formation.model.Coursier;
import org.formation.model.Position;

public class KafkaConsumerThread implements Runnable, ConsumerRebalanceListener {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);

	public static String TOPIC = "position";
	private String id;
	public static String OUTPUT_TOPIC = "position_distance";
	KafkaConsumer<String, Coursier> consumer;
	KafkaProducer<String, Coursier> producer; 

	

	public KafkaConsumerThread(String id) throws ClassNotFoundException {
		this.id = id;
		_initConsumer();
		_initProducer();

	}

	@Override
	public void run() {
		int nbMessages = 0;
		Position origin = new Position(48.87, 2.37);
		producer.initTransactions();
		try {
			while (true) {
				ConsumerRecords<String, Coursier> records = consumer.poll(Duration.ofMillis(100));
				logger.info("Consumer " + id + " vient de recevoir " + records.count() + " records");
				List<Coursier> coursiers = new ArrayList<>();
				for (var record : records) {
					nbMessages++;
					// Temps du traitement d'un message
					Coursier c = record.value();
					c.setDistance(c.getCurrentPosition().distance(origin));
					coursiers.add(c);
				}
				Thread.sleep(1000);
				System.out.println("Consumer " + id + " a reçu " + nbMessages + " messages au total");
				producer.beginTransaction();
				
                coursiers.forEach((c) -> producer.send(new ProducerRecord<String, Coursier>(OUTPUT_TOPIC, c.getId(), c)));

                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<String, Coursier>> partitionedRecords = records.records(partition);
                    long offset = partitionedRecords.get(partitionedRecords.size() - 1)
                        .offset();

                    offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
                }

                System.out.println("Sending offsets to transaction " + offsetsToCommit);
                producer.sendOffsetsToTransaction(offsetsToCommit, consumer.groupMetadata());
                
				producer.commitTransaction();
				
			}
		} catch (InterruptedException e) {
			producer.abortTransaction();
			throw new RuntimeException(e);
		} catch (WakeupException e) {
			producer.abortTransaction();
			System.out.println("CLOSING : Consumer " + id + " a reçu " + nbMessages + " messages au total");
		}  finally {
			consumer.close();
		}
	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();

		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19192,localhost:19292");
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.formation.JsonDeserializer");
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "position-transformer");
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
		kafkaProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

		consumer = new KafkaConsumer<String, Coursier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC),this);
	}
	
	private void _initProducer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093,localhost:19094");
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.formation.JsonSerializer");
		kafkaProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-distance"+id);
		kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

		producer = new KafkaProducer<String, Coursier>(kafkaProps);

		// A compléter
	}	

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		System.out.println("Process " +  ProcessHandle.current().pid() + " Thread " +id + " Revocation of " + partitions);
		
	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		System.out.println("Process " +  ProcessHandle.current().pid() + " Thread " +id + " Assignement of " + partitions);
	}
}
