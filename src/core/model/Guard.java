package core.model;

public class Guard {
	public final int day;
	public final Agent agent;
	public final Agent target;

	public Guard(int day, Agent agent, Agent target) {
		this.day = day;
		this.agent = agent;
		this.target = target;
	}

	@Override
	public String toString() {
		return agent + "guarded " + target + "@" + day;
	}
}
