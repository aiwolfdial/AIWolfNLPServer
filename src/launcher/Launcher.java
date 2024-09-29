package launcher;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;

public class Launcher {
    private static final Logger logger = LogManager.getLogger(Launcher.class);

    private static final String DEFAULT_CONFIG_PATH = "./config/Config.ini";

    private final Config config;

    public static void main(String[] args) {
        try {
            Launcher launcher = new Launcher();
            launcher.start();
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    public Launcher() throws IOException, ReflectiveOperationException {
        this.config = Config.load(DEFAULT_CONFIG_PATH);
        logger.info(config);
    }

    public void start() {
        logger.info("Launcher started.");
        new GameStarter(config).start();
    }
}
