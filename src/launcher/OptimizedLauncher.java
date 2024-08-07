package launcher;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;

public class OptimizedLauncher {
    private static final Logger logger = LogManager.getLogger(OptimizedLauncher.class);

    private static final String DEFAULT_CONFIG_PATH = "./config/Config.ini";

    private final Config config;
    private final List<Socket> sockets = new ArrayList<>();

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

        OptimizedGameStarter gameStarter = new OptimizedGameStarter(config);

        String[] agentAddresses = config.agentAddresses().replace("[", "").replace("]", "").split(",\\s*");

        while (sockets.size() < config.allParticipantNum()) {
            for (int i = 0; i < agentAddresses.length; i++) {
                try {
                    String[] address = agentAddresses[i].split(":");
                    Socket socket = new Socket(address[0], Integer.parseInt(address[1]));
                    sockets.add(socket);
                    gameStarter.addSocket(socket);
                    logger.info(String.format("Connected to agent at %s:%s", address[0], address[1]));
                } catch (Exception e) {
                    logger.error(String.format("Failed to connect to agent at %s", agentAddresses[i]), e);
                }
            }
            if (sockets.size() < config.allParticipantNum()) {
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
