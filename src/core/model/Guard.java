package core.model;

public record Guard(int day, Agent agent, Agent target) {
	@Override
	public String toString() {
		return String.format("%s guarded %s@%d", agent, target, day);
	}
}
