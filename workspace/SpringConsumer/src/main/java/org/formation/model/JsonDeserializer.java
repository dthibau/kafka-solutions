package org.formation.model;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer implements Deserializer<Message>{

	private ObjectMapper objectMapper = new ObjectMapper();
    
	@Override
	public Message deserialize(String topic, byte[] data) {
		if (data == null)
            return null;
        Message ret;
        try {
            ret = objectMapper.readValue(data, Message.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
        return ret;
	}

}
