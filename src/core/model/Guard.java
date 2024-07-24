package core.model;

public record Guard(int day, Agent agent, Agent target) {
	@Override
	public String toString() {
		return String.format("Day%02d GUARD\t%s\t->\t%s", day, agent, target);
	}
}
