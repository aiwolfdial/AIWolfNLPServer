package automatic;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import starter.NLPServerStarter;

public class AutoGameStarter {
	private static final Logger logger = LogManager.getLogger(AutoGameStarter.class);

	public static void main(String[] args) {
		logger.info("AutoGameStarter started.");
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
}
