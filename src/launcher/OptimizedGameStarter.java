package launcher;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<Map<Socket, Role>> combinations = generator.toList();
        logger.info("Generated agent role combinations.");
        logger.info(generator);

        while (true) {
            gameBuilders.removeIf(server -> {
                if (!server.isAlive()) {
                    releaseSockets(server.getSocketSet());
                    return true;
                }
                return false;
            });

            if (!canExecuteInParallel()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for parallel exec", e);
                }
                continue;
            }

            Map<Socket, Role> combination = combinations.stream().filter(this::checkSocketsAvailability)
                    .findFirst()
                    .orElse(null);
            if (combination == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for sockets to become available", e);
                }
                continue;
            }

            try {
                OptimizedGameBuilder builder = new OptimizedGameBuilder(combination, config);
                gameBuilders.add(builder);
                builder.start();
                logger.info("Started a new game with a group of sockets.");
                combinations.remove(combination);
            } catch (Exception e) {
                logger.error("Exception", e);
                releaseSockets(combination.keySet());
            }
        }
    }

    private boolean checkSocketsAvailability(Map<Socket, Role> combination) {
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

    private void releaseSockets(Set<Socket> sockets) {
        lock.lock();
        try {
            sockets.forEach(socket -> socketMap.put(socket, false));
        } finally {
            lock.unlock();
        }
    }

    private boolean canExecuteInParallel() {
        return gameBuilders.size() < config.maxParallelExec();
    }

    public boolean isWaitingGame() {
        return !socketMap.values().stream().allMatch(available -> available);
    }

    public boolean isGameRunning() {
        return !gameBuilders.isEmpty();
    }
}
