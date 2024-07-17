/**
 * Agent.java
 * 
 * Copyright (c) 2014 人狼知能プロジェクト
 */
package common.data;

import java.util.HashMap;
import java.util.Map;

/**
 * <div lang="ja">
 * 
 * プレイヤーのエージェントです。 各プレイヤーは、エージェントとして他のプレイヤーを識別することができます。
 * 各エージェントは、一意のインデックスを持っています。
 * 
 * </div>
 * 
 * <div lang="en">
 * 
 * Player Agent. Each players can identify other players as Agent. Each agent
 * has unique index.
 * 
 * </div>
 * 
 * @author tori and otsuki
 *
 */
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
		final int prime = 31;
		int result = 1;
		result = prime * result + agentIdx;
		return result;
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
