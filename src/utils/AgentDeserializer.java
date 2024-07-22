package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import core.model.Agent;

public class AgentDeserializer extends StdDeserializer<core.model.Agent> {
    public AgentDeserializer() {
        this(null);
    }

    public AgentDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public core.model.Agent deserialize(
            com.fasterxml.jackson.core.JsonParser jsonParser,
            com.fasterxml.jackson.databind.DeserializationContext deserializationContext)
            throws java.io.IOException {
        Agent agent = Agent.getAgent(jsonParser.getText());
        if (agent == null) {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String agentText = node.get("agent").asText();
            agent = Agent.getAgent(agentText);
            if (agent == null) {
                throw new java.io.IOException("Unable to deserialize Agent: " + agentText);
            }
		}
		return agent;
	}
}