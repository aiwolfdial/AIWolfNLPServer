package server.exception;

import common.AIWolfRuntimeException;

public class IllegalPlayerNumException extends AIWolfRuntimeException {
	public IllegalPlayerNumException() {
		super();
	}

	public IllegalPlayerNumException(String arg0) {
		super(arg0);
	}
}
