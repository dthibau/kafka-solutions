package org.formation;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerCallback implements Callback {

	@Override
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		if ( exception != null) {
			System.err.println("Error while producing message to topic : " + metadata.topic() + " partition : " + metadata.partition() + " offset : " + metadata.offset());
			exception.printStackTrace();
			return;
		}
	}

}
