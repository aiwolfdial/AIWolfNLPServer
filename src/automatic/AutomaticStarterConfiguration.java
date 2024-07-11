package automatic;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import common.data.Agent;

public class AutomaticStarterConfiguration {
	private String hostname = "localhost";
	private int port = 10000;
	private int playerNum = 5;
	private Class<?> playerClass = Agent.class;
	private boolean startServer = false;

	public AutomaticStarterConfiguration(String path) {
		File file = new File(path);
		if (!file.exists()) {
			System.err.println("File not found: " + path);
			return;
		}
		try {
			Ini ini = new Ini(file);
			Section section = ini.get("automaticStarter");
			if (section.containsKey("hostname")) {
				hostname = section.get("hostname");
			}
			if (section.containsKey("port")) {
				port = Integer.parseInt(section.get("port"));
			}
			if (section.containsKey("playerNum")) {
				playerNum = Integer.parseInt(section.get("playerNum"));
			}
			if (section.containsKey("className")) {
				try {
					playerClass = Class.forName(section.get("className"));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			if (section.containsKey("startServer")) {
				startServer = Boolean.parseBoolean(section.get("startServer"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Configuration loaded from " + path);
		System.out.println("hostname: " + hostname);
		System.out.println("port: " + port);
		System.out.println("playerNum: " + playerNum);
		System.out.println("playerClass: " + playerClass.getName());
		System.out.println("startServer: " + startServer);
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
