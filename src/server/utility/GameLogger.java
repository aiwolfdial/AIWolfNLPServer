package server.utility;

public interface GameLogger {
	void log(String log);

	void flush();

	void close();
}
