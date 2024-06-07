package org.formation;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.formation.model.Coursier;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer implements Deserializer<Coursier>{

	private ObjectMapper objectMapper = new ObjectMapper();
    private Class<Coursier> tClass = Coursier.class;
    
	@Override
	public Coursier deserialize(String topic, byte[] data) {
		if (data == null)
            return null;
        Coursier ret;
        try {
            ret = objectMapper.readValue(data, tClass);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
        return ret;
	}

}
