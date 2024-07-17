package server.util;

public interface GameLogger {
	void log(String log);

	void flush();

	void close();
}
