package libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallableBufferedReader implements Callable<String> {
	private final BufferedReader bufferedReader;
	private Exception exception = null;
	private final AtomicBoolean isCancelled = new AtomicBoolean(false);

	public CallableBufferedReader(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public String call() {
		try {
			while (!isCancelled.get()) {
				if (bufferedReader.ready()) {
					return bufferedReader.readLine();
				}
				Thread.sleep(10);
			}
			return null;
		} catch (IOException | InterruptedException e) {
			exception = e;
			return null;
		}
	}

	public void cancel() {
		isCancelled.set(true);
	}

	public Exception getException() {
		return exception;
	}

	public boolean isSuccess() {
		return exception == null;
	}
}
