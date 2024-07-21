package core.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import utils.AgentDeserializer;
import utils.AgentSerializer;

@JsonSerialize(using = AgentSerializer.class)
@JsonDeserialize(using = AgentDeserializer.class)
final public class Agent implements Comparable<Agent> {
	public final int agentIdx;
	public final String agentName;

	private static final Map<Integer, Agent> agentIndexMap = new HashMap<>();

	static public Agent getAgent(int idx) {
		return getAgent(idx, "UNDEFINED");
	}

	static public Agent getAgent(int idx, String name) {
		if (idx < 0) {
			return null;
		}
		if (!agentIndexMap.containsKey(idx)) {
			Agent agent = new Agent(idx, name);
			agentIndexMap.put(idx, agent);
		}
		return agentIndexMap.get(idx);
	}

	private Agent(int idx, String name) {
		this.agentIdx = idx;
		this.agentName = name;
	}

	@Override
	public String toString() {
		return String.format("Agent[%02d]", agentIdx);
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(agentIdx);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		return agentIdx == other.agentIdx;
	}

	@Override
	public int compareTo(Agent target) {
		if (target == null) {
			return 1;
		}
		return agentIdx - target.agentIdx;
	}
}
