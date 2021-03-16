package org.formation;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.formation.model.Courier;
import org.formation.model.Position;
import org.formation.model.SendMode;

public class KafkaProducerThread implements Runnable {

	public static String TOPIC ="position";
	public static int BATCH=10;
	KafkaProducer<String,Courier> producer;
	private long nbMessages,sleep;
	private SendMode sendMode;
	private String ackMode;
	private ProducerCallback callback = new ProducerCallback();
	
	private Courier courier;
	
	public KafkaProducerThread(String id, long nbMessages, long sleep, SendMode sendMode, String ackMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.ackMode = ackMode;
		this.courier = new Courier(id, new Position(Math.random() + 45, Math.random() + 2));
		
		_initProducer();
		
	}

	@Override
	public void run() {
		int batch=0;
		producer.initTransactions();
		// Send ten by ten
		for (int i =0; i< nbMessages; i++) {
			
			if ( i%BATCH == 0 ) {
				producer.beginTransaction();
				batch=0;
			}
			ProducerRecord<String, Courier> producerRecord = new ProducerRecord<String, Courier>(TOPIC, courier.getId(), courier);
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
			courier.move();
			batch++;
			if ( batch == BATCH ) {
				System.out.println("Committing messages");
				producer.commitTransaction();
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
		KafkaProducerApplication.props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				KafkaProducerApplication.props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				KafkaProducerApplication.props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
		kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true);
		kafkaProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,4);
		kafkaProps.put(ProducerConfig.RETRIES_CONFIG,Integer.MAX_VALUE);
		kafkaProps.put(ProducerConfig.ACKS_CONFIG,"all");
		kafkaProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, KafkaProducerApplication.props.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG));
		kafkaProps.put("security.protocol","SSL");
		kafkaProps.put("ssl.truststore.location","/home/dthibau/Formations/Kafka/github/solutions/ssl/client.truststore.jks");
		kafkaProps.put("ssl.truststore.password","secret");

		producer = new KafkaProducer<String, Courier>(kafkaProps);
	}
}
