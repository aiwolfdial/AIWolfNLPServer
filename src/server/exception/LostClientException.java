package server.exception;

import common.AIWolfRuntimeException;
import common.data.Agent;

public class LostClientException extends AIWolfRuntimeException {
	Agent agent;

	public LostClientException(Throwable arg0, Agent agent) {
		super(arg0);
		this.agent = agent;
	}

	public LostClientException(String arg0, Throwable arg1, Agent agent) {
		super(arg0, arg1);
		this.agent = agent;
	}

	public Agent getAgent() {
		return agent;
	}
}
