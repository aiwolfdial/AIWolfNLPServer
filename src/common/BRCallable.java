package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

public class BRCallable implements Callable<String> {
	private BufferedReader br;
	private IOException ioException = null;

	public BRCallable(BufferedReader br) {
		this.br = br;
	}

	public String call() {
		try {
			return br.readLine();
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
