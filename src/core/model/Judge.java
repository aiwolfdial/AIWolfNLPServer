package core.model;

public record Judge(int day, Agent agent, Agent target, Species result) {
	@Override
	public String toString() {
		return String.format("Day%02d JUDGE\t%s\t->\t%s\t%s", day, agent, target, result);
	}
}
