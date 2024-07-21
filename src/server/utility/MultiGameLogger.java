package server.utility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author tori
 *
 */
public class MultiGameLogger implements GameLogger {

	protected Set<GameLogger> gameLoggerSet;

	public MultiGameLogger() {
		gameLoggerSet = new HashSet<>();
	}

	public MultiGameLogger(GameLogger... loggers) {
		gameLoggerSet = new HashSet<>();
		Collections.addAll(gameLoggerSet, loggers);
	}

	public void add(GameLogger gameLogger) {
		gameLoggerSet.add(gameLogger);
	}

	public void remove(GameLogger gameLogger) {
		gameLoggerSet.remove(gameLogger);
	}

	@Override
	public void log(String log) {
		for (GameLogger gl : gameLoggerSet) {
			gl.log(log);
		}

	}

	@Override
	public void flush() {
		for (GameLogger gl : gameLoggerSet) {
			gl.flush();
		}
	}

	@Override
	public void close() {
		for (GameLogger gl : gameLoggerSet) {
			gl.close();
		}
	}

}
