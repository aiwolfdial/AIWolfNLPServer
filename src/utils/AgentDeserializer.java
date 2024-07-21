package utils;

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
        return Agent.getAgent(jsonParser.getIntValue());
    }
}