package core.exception;

import core.model.Agent;

public class LostAgentConnectionException extends AIWolfException {
	public final Agent agent;

	public LostAgentConnectionException(Throwable arg0, Agent agent) {
		super(arg0);
		this.agent = agent;
	}

	public LostAgentConnectionException(String arg0, Throwable arg1, Agent agent) {
		super(arg0, arg1);
		this.agent = agent;
	}
}
