package common.data;

import java.util.HashMap;
import java.util.Map;

final public class Agent implements Comparable<Agent> {
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

	private final int agentIdx;
	private final String agentName;

	private Agent(int idx, String name) {
		this.agentIdx = idx;
		this.agentName = name;
	}

	public int getAgentIdx() {
		return agentIdx;
	}

	public String getAgentName() {
		return agentName;
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
		return getAgentIdx() - target.getAgentIdx();
	}
}
