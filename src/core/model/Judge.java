package core.model;

public record Judge(int day, Agent agent, Agent target, Species result) {

	@Override
	public String toString() {
		return agent + "->" + target + "@" + day + ":" + result;
	}
}
