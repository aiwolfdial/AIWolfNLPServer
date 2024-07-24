package core.model;

public record Vote(int day, Agent agent, Agent target) {
	@Override
	public String toString() {
		return String.format("Day%02d VOTE\t%s\t->\t%s", day, agent, target);
	}
}
