package org.formation.model;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer implements Deserializer<Courier>{

	private ObjectMapper objectMapper = new ObjectMapper();
    private Class<Courier> tClass;
    
	@Override
	public Courier deserialize(String topic, byte[] data) {
		if (data == null)
            return null;
        Courier ret;
        try {
            ret = objectMapper.readValue(data, tClass);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
        return ret;
	}

}
