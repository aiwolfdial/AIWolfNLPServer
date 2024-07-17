package automatic;

import starter.NLPServerStarter;

public class AutoGameStarter {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0)
			configPath = args[0];
		AutomaticStarterConfiguration config = new AutomaticStarterConfiguration(configPath);
		if (config.isStartServer()) {
			Runnable r = new Runnable() {
				public void run() {
					NLPServerStarter starter = new NLPServerStarter();
					starter.start();
				}
			};

			Thread thread = new Thread(r);
			thread.start();
		}
	}
}
