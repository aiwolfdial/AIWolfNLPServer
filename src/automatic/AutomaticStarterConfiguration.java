package automatic;

import java.io.IOException;

import utility.IniLoader;

public class AutomaticStarterConfiguration {
	private String hostname;
	private int port;
	private int playerNum;
	private Class<?> playerClass;
	private boolean startServer;

	public static AutomaticStarterConfiguration load(String path)
			throws IOException, NoSuchFieldException, IllegalAccessException {
		return (AutomaticStarterConfiguration) IniLoader.loadFile(path, "automaticStarter",
				new AutomaticStarterConfiguration());
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public Class<?> getPlayerClass() {
		return playerClass;
	}

	public boolean isStartServer() {
		return startServer;
	}
}
