package libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CallableBufferedReader implements Callable<String> {
	private final BufferedReader bufferedReader;
	private IOException ioException = null;

	public CallableBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public String call() {
		try {
			return bufferedReader.readLine();
		} catch (IOException e) {
			ioException = e;
			return null;
		}
	}

	public IOException getIOException() {
		return ioException;
	}

	public boolean isSuccess() {
		return ioException == null;
	}
}
