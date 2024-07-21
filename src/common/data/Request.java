package common.data;

public enum Request {
	NAME(true), ROLE(true), TALK(true), WHISPER(true), VOTE(true), DIVINE(true), GUARD(true), ATTACK(true), INITIALIZE(
			false), DAILY_INITIALIZE(false), DAILY_FINISH(false), FINISH(false);

	private final boolean hasReturn;

	Request(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	public boolean hasReturn() {
		return hasReturn;
	}
}
