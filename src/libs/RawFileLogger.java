package libs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RawFileLogger implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(RawFileLogger.class);

	private final BufferedWriter bufferedWriter;

	public RawFileLogger(File file) throws IOException {
		file.getParentFile().mkdirs();
		bufferedWriter = new BufferedWriter(new FileWriter(file, true));
	}

	public void log(String text) {
		try {
			bufferedWriter.append(text);
			bufferedWriter.append(System.lineSeparator());
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void flush() {
		try {
			bufferedWriter.flush();
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	@Override
	public void close() {
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}
}
