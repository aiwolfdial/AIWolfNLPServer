package launcher;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;

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
        if (config.maxParallelExec() > 1) {
            logger.fatal("Parallel execution is not supported in OptimizedLauncher.");
            return;
        }
        if (config.continueCombinations()) {
            logger.fatal("Continue combinations mode is not supported in OptimizedLauncher with demo.");
            return;
        }
        new OptimizedGameStarter(config, config.continueCombinations()).start();
    }
}
