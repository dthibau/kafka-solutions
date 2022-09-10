package org.formation;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.formation.model.AckMode;
import org.formation.model.Courier;
import org.formation.model.Position;
import org.formation.model.SendMode;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

public class KafkaProducerThread implements Runnable {

	
	KafkaProducer<String,Courier> producer;
	private long nbMessages,sleep;
	private SendMode sendMode;
	private AckMode ackMode;
	private ProducerCallback callback = new ProducerCallback();
	
	private Courier courier;
	
	public KafkaProducerThread(String id, long nbMessages, long sleep, SendMode sendMode, AckMode ackMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.ackMode = ackMode;
		this.courier = new Courier();
		this.courier.setId(id);
		Position position = new Position();
		position.setLatitude(Math.random() + 45);
		position.setLongitude(Math.random() + 2);
		this.courier.setPosition(position);
		
		_initProducer();
		
	}

	@Override
	public void run() {
		
		for (int i =0; i< nbMessages; i++) {
			
			ProducerRecord<String, Courier> producerRecord = new ProducerRecord<String, Courier>(KafkaProducerApplication.TOPIC, courier.getId()+"/"+i, courier);
			switch (sendMode) {
			case FIRE_AND_FORGET:
				fireAndForget(producerRecord);
				break;
			case SYNCHRONOUS:
				try {
					synchronous(producerRecord);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case ASYNCHRONOUS:
				asynchronous(producerRecord);
				break;
			default:
				break;
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.err.println("INTERRUPTED");
			}
			System.out.println("Send " + courier.getId()+"/"+i);
		}
		
	}
	
	public void fireAndForget(ProducerRecord<String,Courier> record) {
		
		producer.send(record);
//		System.out.println("FireAndForget  - " + record);

		
	}
	
	public void synchronous(ProducerRecord<String,Courier> record) throws InterruptedException, ExecutionException {
		RecordMetadata metaData = producer.send(record).get();
//		System.out.println("Synchronous  - " + metaData);
		
	}
	public void asynchronous(ProducerRecord<String,Courier> record) {
		producer.send(record,callback);
	}
	
	private void _initProducer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put("bootstrap.servers",
		"localhost:9092");
		kafkaProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, KafkaProducerApplication.REGISTRY_URL);
		kafkaProps.put("key.serializer",
		"org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put("value.serializer",
		"io.confluent.kafka.serializers.KafkaAvroSerializer");
//		kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true);
		kafkaProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,4);
		kafkaProps.put(ProducerConfig.RETRIES_CONFIG,Integer.MAX_VALUE);
		kafkaProps.put(ProducerConfig.ACKS_CONFIG,"0");
		if ( this.ackMode == AckMode.ALL )
			kafkaProps.put(ProducerConfig.ACKS_CONFIG, "all");


		producer = new KafkaProducer<String, Courier>(kafkaProps);
	}
}
