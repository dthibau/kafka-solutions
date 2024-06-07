package org.formation;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.formation.model.Coursier;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer<Coursier> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public byte[] serialize(String topic, Coursier data) {
		if (data == null)
			return null;
		try {
			return objectMapper.writeValueAsBytes(data);
		} catch (Exception e) {
			throw new SerializationException("Error serializing JSON message", e);
		}
	}

}
