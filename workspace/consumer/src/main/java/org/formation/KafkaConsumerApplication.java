package org.formation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaConsumerApplication {
	
	static Properties props;
	public static void main(String[] args) throws URISyntaxException, IOException {

		props = new Properties();
		props.load(KafkaConsumerApplication.class.getClassLoader().getResourceAsStream("consumer.properties"));
	
		
		int nbThreads = 0;
		String groupId = "position-consumer";
		int sleep = 1000;

		try {
			nbThreads = Integer.parseInt(args[0]);
			groupId = args[1];
			sleep = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Usage is <run> <nbThreads> <group-id> <sleep>");
			System.exit(1);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
		
		long top = System.currentTimeMillis();

		for (int i = 0; i < nbThreads; i++) {
			Runnable r = new KafkaConsumerThread("" + i, groupId, sleep);
			executorService.execute(r);
		}

		executorService.shutdown();

		try {
			System.out.println(executorService.awaitTermination(5, TimeUnit.MINUTES));
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED");
		}
		System.out.println("Execution in "+ (System.currentTimeMillis()-top) + "ms");
		System.exit(0);
	}

}
