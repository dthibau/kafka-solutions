package org.formation;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerApplication {

	public static void main(String[] args) throws URISyntaxException {


		int nbThreads = 0;

		try {
			nbThreads = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.err.println("Usage is <run> <nbThreads>");
			System.exit(1);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);

		long top = System.currentTimeMillis();

		List<KafkaConsumer> consumers = new ArrayList<>();
		
		for (int i = 0; i < nbThreads; i++) {
			KafkaConsumerThread r = new KafkaConsumerThread(i);
			executorService.execute(r);
			consumers.add(r.consumer);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Starting exit...");
				for (var consumer : consumers) {
					consumer.wakeup();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		executorService.shutdown();

		try {
			executorService.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED");
		}
		System.out.println("Execution in " + (System.currentTimeMillis() - top) + "ms");
		System.exit(0);
	}


}
