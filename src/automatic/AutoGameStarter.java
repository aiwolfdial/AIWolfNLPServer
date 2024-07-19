package automatic;

import java.io.IOException;

import starter.NLPServerStarter;

public class AutoGameStarter {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0)
			configPath = args[0];
		AutomaticStarterConfiguration config;
		try {
			config = AutomaticStarterConfiguration.load(configPath);
			if (config.isStartServer()) {
				Runnable r = new Runnable() {
					public void run() {
						try {
							NLPServerStarter starter = new NLPServerStarter();
							starter.start();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				Thread thread = new Thread(r);
				thread.start();
			}
		} catch (NoSuchFieldException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}
}
