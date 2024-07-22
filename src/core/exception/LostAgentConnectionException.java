package core.exception;

import core.model.Agent;

public class LostAgentConnectionException extends AIWolfException {
	public final Agent agent;

	public LostAgentConnectionException(String message, Agent agent) {
		super(message);
		this.agent = agent;
	}

	public LostAgentConnectionException(Throwable cause, Agent agent) {
		super(cause);
		this.agent = agent;
	}

	public LostAgentConnectionException(String message, Throwable cause, Agent agent) {
		super(message, cause);
		this.agent = agent;
	}
}
