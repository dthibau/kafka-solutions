package org.formation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.formation.model.SendMode;

public class KafkaProducerApplication {
	
	static Properties props;

	public static void main(String[] args) throws URISyntaxException, IOException {

		props = new Properties();
		props.load(KafkaProducerApplication.class.getClassLoader().getResourceAsStream("producer.properties"));
		
		int nbThreads = 0;
		long nbMessages = 0;
		int sleep = 1000;
		String ackMode = "0";
		SendMode sendMode = SendMode.FIRE_AND_FORGET;

		try {
			nbThreads = Integer.parseInt(args[0]);
			nbMessages = Long.parseLong(args[1]);
			sleep = Integer.parseInt(args[2]);
			int mode = Integer.parseInt(args[3]);
			if (mode == 0) {
				sendMode = SendMode.FIRE_AND_FORGET;
			} else if (mode == 1) {
				sendMode = SendMode.SYNCHRONOUS;
			} else {
				sendMode = SendMode.ASYNCHRONOUS;
			}
			ackMode = args[4];
			
		} catch (Exception e) {
			System.err.println("Usage is <run> <nbThreads> <nbMessages> <sleep> <0|1|2> <0|1|all>");
			System.exit(1);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
		
		long top = System.currentTimeMillis();

		for (int i = 0; i < nbThreads; i++) {
			Runnable r = new KafkaProducerThread("" + i, nbMessages, sleep, sendMode, ackMode);
			executorService.execute(r);
		}

		executorService.shutdown();

		try {
			System.out.println(executorService.awaitTermination(nbMessages*sleep + 1000, TimeUnit.MILLISECONDS));
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED");
		}
		System.out.println("Produced " + nbThreads*nbMessages + " messages in "+ (System.currentTimeMillis()-top) + "ms");
		System.exit(0);
	}

}
