package org.formation;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.formation.model.Courier;
import org.formation.model.Message;
import org.formation.model.Position;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerThread implements Runnable {

	public static String TOPIC ="position";
	KafkaTemplate<Object, Object> template;
	int nbMessages=10000;
	private Courier courier;
	private boolean async;
	
	public KafkaProducerThread(String id, KafkaTemplate<Object, Object> template , boolean async) {
		this.courier = new Courier(id, new Position(Math.random() + 45, Math.random() + 2));
		this.template = template;
		this.async = async;
				
	}

    @Override
	public void run() {
		for (int i =0; i< nbMessages; i++) {
			try {
				if ( async )
					this.template.send("position", courier.getId(), new Message(i,new Date(),courier));
				else
					this.template.send("position", courier.getId(), new Message(i,new Date(),courier)).get();

				Thread.sleep(10);
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("INTERRUPTED");
			}
			courier.move();

		}
		
	}
	
	
}
