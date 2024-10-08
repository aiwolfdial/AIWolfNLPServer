package core.model;

public enum Request {
	NAME(true), ROLE(true), TALK(true), WHISPER(true), VOTE(true), DIVINE(true), GUARD(true), ATTACK(true), INITIALIZE(
			false), DAILY_INITIALIZE(false), DAILY_FINISH(false), FINISH(false);

	public final boolean hasReturn;

	Request(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	public boolean hasReturn() {
		return hasReturn;
	}
}
