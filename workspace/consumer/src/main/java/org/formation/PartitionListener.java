package org.formation;

import java.util.Collection;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

public class PartitionListener implements ConsumerRebalanceListener {

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		// TODO Auto-generated method stub
		System.out.println("=========  REVOKED " + partitions);
	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		// TODO Auto-generated method stub
		System.out.println("=========  ASSIGNED " + partitions);
	}

}
