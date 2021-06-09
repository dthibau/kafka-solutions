package org.formation;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

@SpringBootApplication
public class SpringbootKafkaApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringbootKafkaApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(SpringbootKafkaApplication.class, args);
	}

	@Bean
	public Consumer<Message<String>> position() {
		return message -> logger.info(message.getPayload() + " received from partition "
						+ message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID));
	}
}
