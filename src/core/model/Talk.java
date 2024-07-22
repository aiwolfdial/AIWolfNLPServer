package core.model;

public record Talk(int idx, int day, int turn, Agent agent, String text) {
	final static public String OVER = "Over";
	final static public String SKIP = "Skip";
	final static public String FORCE_SKIP = "ForceSkip";

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
