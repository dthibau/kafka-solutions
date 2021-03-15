package org.formation;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.formation.model.AckMode;
import org.formation.model.SendMode;

public class KafkaProducerApplication {

	public static void main(String[] args) throws URISyntaxException {

		int nbThreads = 0;
		long nbMessages = 0;
		int sleep = 1000;
		SendMode sendMode = SendMode.FIRE_AND_FORGET;
		AckMode ackMode =  AckMode.ZER0;
;

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
			if (args[4].equalsIgnoreCase("all") ) {
				ackMode = AckMode.ALL;
			} 

		} catch (Exception e) {
			System.err.println("Usage is <run> <nbThreads> <nbMessages> <sleep> <0|1|2> <0|all>");
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
			System.out.println(executorService.awaitTermination(2, TimeUnit.MINUTES));
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED");
		}
		System.out.println("Execution in "+ (System.currentTimeMillis()-top) + "ms");
		System.exit(0);
	}

}
