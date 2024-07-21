package core.model;

public class Guard {
	int day;
	Agent agent;
	Agent target;

	public Guard(int day, Agent agent, Agent target) {
		this.day = day;
		this.agent = agent;
		this.target = target;
	}

	public Agent getAgent() {
		return agent;
	}

	public Agent getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return agent + "guarded " + target + "@" + day;
	}
}
