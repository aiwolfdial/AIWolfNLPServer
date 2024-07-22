package core.model;

public record Vote(int day, Agent agent, Agent target) {

	@Override
	public String toString() {
		return agent + "voted" + target + "@" + day;
	}
}
