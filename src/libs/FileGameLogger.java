package libs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FileGameLogger {
	private static final Logger logger = LogManager.getLogger(FileGameLogger.class);

	protected BufferedWriter bw;

	public FileGameLogger(File file) throws IOException {
		file.getParentFile().mkdirs();
		bw = new BufferedWriter(new FileWriter(file));
	}

	public void log(String text) {
		try {
			bw.append(text);
			bw.append("\n");
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void flush() {
		try {
			bw.flush();
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}
}
