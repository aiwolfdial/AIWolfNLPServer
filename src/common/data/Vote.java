package common.data;

public class Vote {
	int day;
	Agent agent;
	Agent target;

	public Vote(int day, Agent agent, Agent target) {
		this.day = day;
		this.agent = agent;
		this.target = target;
	}

	public int getDay() {
		return day;
	}

	public Agent getAgent() {
		return agent;
	}

	public Agent getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return agent + "voted" + target + "@" + day;
	}
}
