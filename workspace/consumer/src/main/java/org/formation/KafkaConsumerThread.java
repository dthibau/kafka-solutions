package org.formation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.formation.model.Courier;

public class KafkaConsumerThread implements Runnable, ConsumerRebalanceListener {

	public static String TOPIC = "position";
	KafkaConsumer<String, Courier> consumer;
	KafkaProducer<String, Courier> producer;

	private long sleep;
	private String id;
	private String groupId;

	public KafkaConsumerThread(String id, String groupId, long sleep) {
		this.id = id;
		this.groupId = groupId;
		this.sleep = sleep;

		_initConsumer();
		_initProducer();

	}

	@Override
	public void run() {
		Map<String, Integer> updateMap = new HashMap<>();
		producer.initTransactions();
		try {
			while (true) {
				// poll envoie le heartbeat, on bloque pdt 100ms pour récupérer les messages
				ConsumerRecords<String, Courier> records = consumer.poll(Duration.ofMillis(sleep));
				producer.beginTransaction();

				for (ConsumerRecord<String, Courier> record : records) {
					System.out.println(
							" Partition/offset;" + record.partition() + ";" + record.offset() + ";" + record.key());

					int updatedCount = 1;
					if (updateMap.containsKey(record.key())) {
						updatedCount = updateMap.get(record.key()) + 1;
					}
					updateMap.put(record.key(), updatedCount);
				}
				Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

				// Retreive offsets for each partition
				for (TopicPartition partition : records.partitions()) {
					List<ConsumerRecord<String, Courier>> partitionedRecords = records.records(partition);
					long offset = partitionedRecords.get(partitionedRecords.size() - 1).offset();
					offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
				}
				// Commit Offset for consumer associated with the commit of the transaction
				producer.sendOffsetsToTransaction(offsetsToCommit, new ConsumerGroupMetadata(groupId));
				producer.commitTransaction();
			}
		} finally {
			consumer.close();
		}

	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaConsumerApplication.props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				KafkaConsumerApplication.props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				KafkaConsumerApplication.props.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
				KafkaConsumerApplication.props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
		kafkaProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

		consumer = new KafkaConsumer<String, Courier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC), this);
	}

	private void _initProducer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				KafkaConsumerApplication.props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				KafkaConsumerApplication.props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				KafkaConsumerApplication.props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		kafkaProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 4);
		kafkaProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
		kafkaProps.put(ProducerConfig.ACKS_CONFIG, "all");
		kafkaProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,
				KafkaConsumerApplication.props.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG)+id);

		producer = new KafkaProducer<String, Courier>(kafkaProps);
	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		System.out.println(this + " - Partition Revoked " + partitions);

	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		System.out.println(this + "Partition Assigned " + partitions);

	}
}
