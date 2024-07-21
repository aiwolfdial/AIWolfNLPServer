package core.model;

public class Judge {
	public final int day;
	public final Agent agent;
	public final Agent target;
	public final Species result;

	public Judge(int day, Agent agent, Agent target, Species result) {
		this.day = day;
		this.agent = agent;
		this.target = target;
		this.result = result;
	}

	@Override
	public String toString() {
		return agent + "->" + target + "@" + day + ":" + result;
	}
}
