package common.data;

public class Talk {
	final static public String OVER = "Over";
	final static public String SKIP = "Skip";
	final static public String FORCE_SKIP = "ForceSkip";

	int idx;
	int day;
	int turn;
	Agent agent;
	String text;

	public Talk(int idx, int day, int turn, Agent agent, String text) {
		this.idx = idx;
		this.day = day;
		this.turn = turn;
		this.agent = agent;
		this.text = text;
	}

	public int getIdx() {
		return idx;
	}

	public int getDay() {
		return day;
	}

	public int getTurn() {
		return turn;
	}

	public Agent getAgent() {
		return agent;
	}

	public String getText() {
		return text;
	}

	public boolean isSkip() {
		return text.equals(SKIP);
	}

	public boolean isOver() {
		return text.equals(OVER);
	}

	@Override
	public String toString() {
		return String.format("Day%02d %02d[%03d]\t%s\t%s", day, turn, idx, agent, text);
	}
}
