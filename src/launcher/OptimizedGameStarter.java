package launcher;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;
import core.model.Role;
import utils.OptimizedAgentRoleGenerator;

public class OptimizedGameStarter extends Thread {
    private static final Logger logger = LogManager.getLogger(OptimizedGameStarter.class);

    private final List<OptimizedGameBuilder> gameBuilders = new ArrayList<>();
    private final Map<Socket, Boolean> socketMap = new ConcurrentHashMap<>();
    private final Config config;
    private final ReentrantLock lock = new ReentrantLock();

    public OptimizedGameStarter(Config config) {
        this.config = config;
    }

    public void addSocket(Socket socket) {
        socketMap.put(socket, false);
    }

    @Override
    public void run() {
        logger.info("OptimizedGameStarter started.");
        OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(new ArrayList<>(socketMap.keySet()),
                config.gameNum(), config.battleAgentNum());
        Iterator<Map<Socket, Role>> iterator = generator.iterator();
        logger.info("Generated agent role combinations.");
        logger.info(generator);

        while (true) {
            cleanUpFinishedGames();
            if (allGamesStarted(iterator))
                break;
            if (!iterator.hasNext())
                continue;

            Map<Socket, Role> combination = iterator.next();
            if (combination == null)
                continue;

            if (!areAllSocketsAvailable(combination)) {
                waitForSocketsToBecomeAvailable();
                continue;
            }

            startNewGame(combination);
        }
    }

    private void cleanUpFinishedGames() {
        gameBuilders.removeIf(server -> {
            if (!server.isAlive()) {
                lock.lock();
                try {
                    server.getConnections().forEach(connection -> socketMap.put(connection.getSocket(), false));
                } finally {
                    lock.unlock();
                }
                return true;
            }
            return false;
        });
    }

    private boolean allGamesStarted(Iterator<Map<Socket, Role>> iterator) {
        if (!iterator.hasNext() && gameBuilders.isEmpty()) {
            if (socketMap.values().stream().allMatch(available -> !available)) {
                logger.info("All games have been started.");
                return true;
            }
        }
        return false;
    }

    private boolean areAllSocketsAvailable(Map<Socket, Role> combination) {
        lock.lock();
        try {
            for (Socket socket : combination.keySet()) {
                if (socketMap.get(socket)) {
                    return false;
                }
            }
            combination.keySet().forEach(socket -> socketMap.put(socket, true));
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void waitForSocketsToBecomeAvailable() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for sockets to become available", e);
        }
    }

    private void startNewGame(Map<Socket, Role> combination) {
        OptimizedGameBuilder builder;
        try {
            builder = new OptimizedGameBuilder(combination, config);
        } catch (IOException e) {
            logger.error("Exception", e);
            resetSocketAvailability(combination);
            return;
        }
        gameBuilders.add(builder);
        builder.start();
        logger.info("Started a new game with a group of sockets.");
        synchronized (gameBuilders) {
            if (gameBuilders.size() >= config.maxParallelExec()) {
                return;
            }
        }
    }

    private void resetSocketAvailability(Map<Socket, Role> combination) {
        lock.lock();
        try {
            combination.keySet().forEach(socket -> socketMap.put(socket, false));
        } finally {
            lock.unlock();
        }
    }

    public boolean isWaitingGame() {
        return !socketMap.values().stream().allMatch(available -> available);
    }

    public boolean isGameRunning() {
        return !gameBuilders.isEmpty();
    }
}
