package common.data;

public class Judge {
	int day;
	Agent agent;
	Agent target;
	Species result;

	public Judge(int day, Agent agent, Agent target, Species result) {
		this.day = day;
		this.agent = agent;
		this.target = target;
		this.result = result;
	}

	public int getDay() {
		return day;
	}

	public Agent getAgent() {
		return agent;
	}

	public Agent getTarget() {
		return target;
	}

	public Species getResult() {
		return result;
	}

	@Override
	public String toString() {
		return agent + "->" + target + "@" + day + ":" + result;
	}
}
