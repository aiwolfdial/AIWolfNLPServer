package server.exception;

import java.io.Serial;

public class AIWolfException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 3533967066982907891L;

	public AIWolfException() {
	}

	public AIWolfException(String message) {
		super(message);
	}

	public AIWolfException(String message, Throwable cause) {
		super(message, cause);
	}

	public AIWolfException(Throwable cause) {
		super(cause);
	}
}
