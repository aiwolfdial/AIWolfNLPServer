package utility.parser;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonParser {
	private static final Logger logger = LogManager.getLogger(JsonParser.class);

	public static String encode(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(common.data.Agent.class, new AgentSerializer());
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
		SimpleModule module = new SimpleModule();
		module.addDeserializer(common.data.Agent.class, new AgentDeserializer());
		mapper.registerModule(module);
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
