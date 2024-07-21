package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import core.model.Agent;

public class JsonParser {
	private static final Logger logger = LogManager.getLogger(JsonParser.class);

	public static String encode(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addKeySerializer(Agent.class, new AgentSerializer());
		mapper.registerModule(module);
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public static <T> T decode(String json, Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
