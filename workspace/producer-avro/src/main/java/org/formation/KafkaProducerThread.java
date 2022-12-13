package org.formation;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.formation.model.Courier;
import org.formation.model.Position;


import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

public class KafkaProducerThread implements Runnable {

	public static String TOPIC ="position";
	KafkaProducer<String,Courier> producer;
	private long nbMessages,sleep;
	private SendMode sendMode;
	private ProducerCallback callback = new ProducerCallback();
	
	private Courier courier;
	
	public KafkaProducerThread(String id, long nbMessages, long sleep, SendMode sendMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.courier = new Courier(id, UUID.randomUUID().toString(), new Position(Math.random() + 45, Math.random() + 2));
		
		_initProducer();
		
	}

	@Override
	public void run() {
		
		for (int i =0; i< nbMessages; i++) {
			
			ProducerRecord<String, Courier> producerRecord = new ProducerRecord<String, Courier>(KafkaProducerApplication.TOPIC, courier.getId()+"", courier);
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
		}
		
	}
	
	public void fireAndForget(ProducerRecord<String,Courier> record) {
		
		producer.send(record);
		System.out.println("FireAndForget  - " + record);

		
	}
	
	public void synchronous(ProducerRecord<String,Courier> record) throws InterruptedException, ExecutionException {
		RecordMetadata metaData = producer.send(record).get();
		System.out.println("Synchronous  - " + metaData);
		
	}
	public void asynchronous(ProducerRecord<String,Courier> record) {
		producer.send(record,callback);
	}
	
	private void _initProducer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
		"localhost:19092");
		kafkaProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, KafkaProducerApplication.REGISTRY_URL);
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
		"org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
		"io.confluent.kafka.serializers.KafkaAvroSerializer");

		
		producer = new KafkaProducer<String, Courier>(kafkaProps);
	}
}
