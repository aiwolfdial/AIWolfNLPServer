package common.net;

import common.data.Agent;
import common.data.Vote;

public class VoteToSend {
	int day;
	int agent;
	int target;

	public VoteToSend(Vote vote) {
		this.day = vote.getDay();
		this.agent = vote.getAgent().getAgentIdx();
		this.target = vote.getTarget().getAgentIdx();
	}

	public Vote toVote() {
		Vote vote = new Vote(day, Agent.getAgent(agent), Agent.getAgent(target));
		return vote;
	}
}
