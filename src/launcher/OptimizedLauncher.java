package launcher;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;
import libs.Pair;

public class OptimizedLauncher {
    private static final Logger logger = LogManager.getLogger(OptimizedLauncher.class);

    private static final String DEFAULT_CONFIG_PATH = "./config/Config.ini";

    private final Config config;

    public static void main(String[] args) {
        try {
            OptimizedLauncher launcher = new OptimizedLauncher();
            launcher.start();
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    public OptimizedLauncher() throws IOException, ReflectiveOperationException {
        this.config = Config.load(DEFAULT_CONFIG_PATH);
        logger.info(config);
    }

    public void start() {
        logger.info("### THIS IS OPTIMIZED LAUNCHER ###");
        logger.info("OptimizedLauncher started.");
        if (config.isServer()) {
            logger.fatal("Server mode is not supported in OptimizedLauncher.");
            return;
        }
        if (config.listenPort()) {
            logger.fatal("Listen port mode is not supported in OptimizedLauncher.");
            return;
        }

        OptimizedGameStarter gameStarter = new OptimizedGameStarter(config, false);

        String[] agentAddresses = config.agentAddresses().replace("[", "").replace("]", "").split(",\\s*");

        while (!gameStarter.hasAllParticipants()) {
            for (int i = 0; i < agentAddresses.length; i++) {
                try {
                    String[] address = agentAddresses[i].split(":");
                    InetAddress inetAddress = InetAddress.getByName(address[0]);
                    gameStarter.addSocket(new Pair<>(inetAddress, Integer.parseInt(address[1])));
                    logger.info(String.format("Connected to agent at %s:%s", address[0], address[1]));
                } catch (Exception e) {
                    logger.error(String.format("Failed to connect to agent at %s", agentAddresses[i]), e);
                }
            }
            if (!gameStarter.hasAllParticipants()) {
                try {
                    logger.info("Waiting to connect to all participants...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting to connect to all participants", e);
                }
            }
        }

        gameStarter.start();
    }
}
