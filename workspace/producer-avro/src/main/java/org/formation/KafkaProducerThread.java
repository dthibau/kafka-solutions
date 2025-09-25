package org.formation;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.formation.model.Coursier;
import org.formation.model.Position;

public class KafkaProducerThread implements Runnable {

	public static String TOPIC ="position";
	KafkaProducer<String, Coursier> producer;
	private long nbMessages,sleep;
	private SendMode sendMode;
	private ProducerCallback callback = new ProducerCallback();
	
	private Coursier coursier;
	
	public KafkaProducerThread(Long id, long nbMessages, long sleep, SendMode sendMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.coursier = new Coursier(id, new Position(Math.random() + 45, Math.random() + 2));
		
		_initProducer();
		
	}

	@Override
	public void run() {
		
		for (int i =0; i< nbMessages; i++) {
			_moveCoursier(coursier);
			ProducerRecord<String, Coursier> producerRecord = new ProducerRecord<String, Coursier>(KafkaProducerApplication.TOPIC, coursier.getId()+"", coursier);
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
		producer.flush();
		producer.close();
		
	}
	
	public void fireAndForget(ProducerRecord<String,Coursier> record) {
		
		producer.send(record);
		System.out.println("FireAndForget  - " + record);

		
	}
	
	public void synchronous(ProducerRecord<String,Coursier> record) throws InterruptedException, ExecutionException {
		RecordMetadata metaData = producer.send(record).get();
		System.out.println("Synchronous  - " + metaData);
		
	}
	public void asynchronous(ProducerRecord<String,Coursier> record) {
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

		
		producer = new KafkaProducer<String, Coursier>(kafkaProps);
	}

	private void _moveCoursier(Coursier coursier) {
		Position position = (Position)coursier.getPosition();
		position.setLatitude(position.getLatitude() + Math.random()-0.5);
		position.setLongitude(position.getLongitude() + Math.random()-0.5);

	}
}
