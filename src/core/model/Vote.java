package core.model;

public class Vote {
	public final int day;
	public final Agent agent;
	public final Agent target;

	public Vote(int day, Agent agent, Agent target) {
		this.day = day;
		this.agent = agent;
		this.target = target;
	}

	@Override
	public String toString() {
		return agent + "voted" + target + "@" + day;
	}
}
