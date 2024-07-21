package client;

import java.io.Serial;

public class IllegalContentStringException extends IllegalArgumentException {

	@Serial
	private static final long serialVersionUID = 752514963976415066L;

	public IllegalContentStringException() {
	}

	public IllegalContentStringException(String s) {
		super(s);
	}
}
