package automatic;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import starter.NLPServerStarter;

public class AutoGameStarter {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";
	private static final Logger logger = LogManager.getLogger(AutoGameStarter.class);

	public static void main(String[] args) {
		logger.info("AutoGameStarter started.");
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0) {
			configPath = args[0];
		}
		logger.info("Config file path: " + configPath);
		try {
			AutomaticStarterConfiguration config = AutomaticStarterConfiguration.load(configPath);
			if (config.isStartServer()) {
				Runnable r = () -> {
					try {
						NLPServerStarter starter = new NLPServerStarter();
						starter.start();
					} catch (Exception e) {
						logger.error(e);
					}
				};
				Thread thread = new Thread(r);
				thread.start();
			}
		} catch (NoSuchFieldException | IllegalAccessException | IOException e) {
			logger.error(e);
		}
	}
}
