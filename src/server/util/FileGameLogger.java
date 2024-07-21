package server.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import common.util.CalendarTools;

/**
 * GameLogger using File
 * 
 * @author tori
 *
 */
public class FileGameLogger implements GameLogger {
	private static final Logger logger = LogManager.getLogger(FileGameLogger.class);

	protected File logFile;
	protected BufferedWriter bw;

	public FileGameLogger(String logFile) throws IOException {
		this(new File(logFile));
	}

	public FileGameLogger(File logFile) throws IOException {
		super();

		if (logFile != null) {
			if (logFile.isDirectory()) {
				String timeString = CalendarTools.toDateTime(System.currentTimeMillis()).replaceAll("[\\s-/:]", "");
				logFile = new File(logFile.getAbsolutePath() + "/" + timeString + ".log");
			}
			this.logFile = logFile;

			logFile.getParentFile().mkdirs();
			try {
				if (!logFile.getPath().endsWith(".gz")) {
					bw = new BufferedWriter(new FileWriter(logFile));
				} else {
					bw = new BufferedWriter(
							new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(logFile), true)));
				}
				return;
			} catch (IOException e) {
				logger.error(e);
			}
		}
		bw = new BufferedWriter(new OutputStreamWriter(System.out));
	}

	public void log(String text) {
		try {
			bw.append(text);
			bw.append("\n");
		} catch (IOException e) {
		}
	}

	public void flush() {
		try {
			bw.flush();
		} catch (IOException e) {
		}
	}

	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
