package core.model;

public record Judge(int day, Agent agent, Agent target, Species result) {
	@Override
	public String toString() {
		return String.format("%s->%s@%d:%s", agent, target, day, result);
	}
}
