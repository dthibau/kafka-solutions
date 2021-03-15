package org.formation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.formation.model.Courier;

public class KafkaConsumerThread implements Runnable, ConsumerRebalanceListener {

	public static String TOPIC = "position";
	KafkaConsumer<String, Courier> consumer;
	private long sleep;
	private String id;
	private String groupId;
	
	

	public KafkaConsumerThread(String id, String groupId, long sleep) {
		this.id = id;
		this.groupId = groupId;
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
							" Partition/offset;" + record.partition() + ";" + record.offset() + ";" + record.key());

					int updatedCount = 1;
					if (updateMap.containsKey(record.key())) {
						updatedCount = updateMap.get(record.key()) + 1;
					}
					updateMap.put(record.key(), updatedCount);
//					System.out.println("Consommer " + id + " updateMap:" + updateMap);
				}
//				consumer.commitSync();
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
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConsumerApplication.props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

		consumer = new KafkaConsumer<String, Courier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC),this);
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
