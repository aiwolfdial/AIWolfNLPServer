package launcher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.model.Config;
import core.model.Role;
import libs.Pair;
import utils.OptimizedAgentRoleGenerator;

public class OptimizedGameStarter extends Thread {
    private static final Logger logger = LogManager.getLogger(OptimizedGameStarter.class);

    private final List<OptimizedGameBuilder> builders = new ArrayList<>();
    private final Map<Pair<InetAddress, Integer>, Boolean> socketMap = new ConcurrentHashMap<>();
    private final Config config;
    private final ReentrantLock lock = new ReentrantLock();

    public OptimizedGameStarter(Config config) {
        this.config = config;
    }

    public void addSocket(Pair<InetAddress, Integer> socket) {
        socketMap.put(socket, false);
    }

    public boolean hasAllParticipants() {
        return socketMap.size() == config.allParticipantNum();
    }

    @Override
    public void run() {
        logger.info("OptimizedGameStarter started.");
        OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(
                new ArrayList<>(socketMap.keySet()),
                config.gameNum(), config.battleAgentNum());
        List<Map<Pair<InetAddress, Integer>, Role>> combinations = generator.toList();
        logger.info("Generated agent role combinations.");
        logger.info(generator);

        while (true) {
            builders.removeIf(builder -> {
                if (!builder.isAlive()) {
                    releaseSockets(builder.getSocketSet());
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

            Map<Pair<InetAddress, Integer>, Role> combination = combinations.stream()
                    .filter(this::checkSocketsAvailability)
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

            Map<Socket, Role> sockets = new HashMap<>();
            try {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for sockets to become available", e);
                }
                for (Pair<InetAddress, Integer> pair : combination.keySet()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting for sockets to become available", e);
                    }
                    try {
                        Socket socket = new Socket(pair.key(), pair.value());
                        sockets.put(socket, combination.get(pair));
                    } catch (IOException e) {
                        logger.error(String.format("Failed to create socket %s:%d", pair.key(), pair.value()), e);
                        releaseSockets(sockets.keySet());
                        clearSocketsAvailability(combination);
                        break;
                    }
                }
                if (sockets.size() != combination.size()) {
                    logger.warn("Failed to create all sockets.");
                    continue;
                }
                OptimizedGameBuilder builder = new OptimizedGameBuilder(sockets, config);
                builders.add(builder);
                builder.start();
                logger.info("Started a new game with a group of sockets.");
                combinations.remove(combination);
            } catch (Exception e) {
                logger.error("Exception", e);
                releaseSockets(sockets.keySet());
            }
        }
    }

    private boolean checkSocketsAvailability(Map<Pair<InetAddress, Integer>, Role> combination) {
        lock.lock();
        try {
            for (Pair<InetAddress, Integer> socket : combination.keySet()) {
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

    private void clearSocketsAvailability(Map<Pair<InetAddress, Integer>, Role> combination) {
        lock.lock();
        try {
            combination.keySet().forEach(socket -> socketMap.put(socket, false));
        } finally {
            lock.unlock();
        }
    }

    private void releaseSockets(Set<Socket> sockets) {
        lock.lock();
        try {
            for (Socket socket : sockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Failed to close socket", e);
                }
                Pair<InetAddress, Integer> pair = new Pair<>(socket.getInetAddress(), socket.getPort());
                if (socketMap.containsKey(pair))
                    socketMap.put(pair, false);
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean canExecuteInParallel() {
        return builders.size() < config.maxParallelExec();
    }

    public boolean isWaitingGame() {
        return !socketMap.values().stream().allMatch(available -> available);
    }

    public boolean isGameRunning() {
        return !builders.isEmpty();
    }
}
