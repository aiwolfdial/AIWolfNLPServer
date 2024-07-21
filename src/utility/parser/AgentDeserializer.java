package utility.parser;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import common.data.Agent;

public class AgentDeserializer extends StdDeserializer<common.data.Agent> {
    public AgentDeserializer() {
        this(null);
    }

    public AgentDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public common.data.Agent deserialize(
            com.fasterxml.jackson.core.JsonParser jsonParser,
            com.fasterxml.jackson.databind.DeserializationContext deserializationContext)
            throws java.io.IOException {
        return Agent.getAgent(jsonParser.getIntValue());
    }
}