package core.exception;

import core.model.Agent;

public class LostAgentConnectionException extends AIWolfException {
	public final Agent agent;

	public LostAgentConnectionException(Throwable cause, Agent agent) {
		super(cause);
		this.agent = agent;
	}
}
