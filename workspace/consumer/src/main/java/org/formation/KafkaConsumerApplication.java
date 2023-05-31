package org.formation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaConsumerApplication {

	static Properties props;

	public static void main(String[] args) throws URISyntaxException, ClassNotFoundException, IOException {

		props = new Properties();
		props.load(KafkaConsumerApplication.class.getClassLoader().getResourceAsStream("consumer.properties"));


		int nbThreads = 0;
		int sleep = 1000;

		try {
			nbThreads = Integer.parseInt(args[0]);
			sleep = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("Usage is <run> <nbThreads> <sleep>");
			System.exit(1);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
		
		long top = System.currentTimeMillis();

		for (int i = 0; i < nbThreads; i++) {
			Runnable r = new KafkaConsumerThread("" + i, sleep);
			executorService.execute(r);
		}

		executorService.shutdown();

		try {
			System.out.println(executorService.awaitTermination(30, TimeUnit.MINUTES));
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED");
		}
		System.out.println("Execution in "+ (System.currentTimeMillis()-top) + "ms");
		System.exit(0);
	}

}
