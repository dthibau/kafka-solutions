package org.formation.service;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.formation.event.Position;

import io.quarkus.logging.Log;

@ApplicationScoped
public class PositionService {


	

	@Incoming("position")
    public CompletionStage<Void> consume(Message<Position> msg) {
		Log.info(msg.getPayload() + " received from partition "
						+ msg.getMetadata());
        // Traitement
        // Puis Commit du offset 
        return msg.ack();
    }



	

}
