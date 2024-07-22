package utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AgentSerializer extends StdSerializer<core.model.Agent> {
    public AgentSerializer() {
        this(null);
    }

    public AgentSerializer(Class<core.model.Agent> t) {
        super(t);
    }

    @Override
    public void serialize(
            core.model.Agent agent,
            com.fasterxml.jackson.core.JsonGenerator jsonGenerator,
            com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException {
        if (agent != null) {
            jsonGenerator.writeString(agent.toString());
        }
    }
}