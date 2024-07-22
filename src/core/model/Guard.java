package core.model;

public record Guard(int day, Agent agent, Agent target) {

	@Override
	public String toString() {
		return agent + "guarded " + target + "@" + day;
	}
}
