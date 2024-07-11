package server.util;

public interface GameLogger {
	public void log(String log);

	public void flush();

	public void close();
}
