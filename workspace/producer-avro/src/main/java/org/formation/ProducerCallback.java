package org.formation;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerCallback implements Callback {

	@Override
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		System.out.println("ASynchronous  - " + metadata);
		
	}

}
