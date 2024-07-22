package core.model;

public record Vote(int day, Agent agent, Agent target) {
	@Override
	public String toString() {
		return String.format("%s voted %s@%d", agent, target, day);
	}
}
