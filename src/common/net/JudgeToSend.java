package common.net;

import common.AIWolfRuntimeException;
import common.data.Agent;
import common.data.Judge;
import common.data.Species;

public class JudgeToSend {
	int day;
	int agent;
	int target;
	String result;

	public JudgeToSend(Judge judge) {
		this.day = judge.getDay();
		this.agent = judge.getAgent().getAgentIdx();
		this.target = judge.getTarget().getAgentIdx();
		this.result = judge.getResult().toString();
		if (this.result == null) {
			throw new AIWolfRuntimeException("judge result = null");
		}
	}

	public Judge toJudge() {
		return new Judge(day, Agent.getAgent(agent), Agent.getAgent(target), Species.valueOf(result));
	}
}
