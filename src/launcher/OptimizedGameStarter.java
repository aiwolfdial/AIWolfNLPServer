package launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    private final File optimizedFile;

    private final List<Pair<Map<Pair<InetAddress, Integer>, Role>, OptimizedGameBuilder>> builders = new ArrayList<>();
    private final Map<Pair<InetAddress, Integer>, Boolean> socketMap = new ConcurrentHashMap<>();
    private final Config config;
    private final ReentrantLock lock = new ReentrantLock();

    public OptimizedGameStarter(Config config) {
        this(config, false);
    }

    public OptimizedGameStarter(Config config, boolean resume) {
        this.config = config;
        if (resume) {
            File logDir = new File(config.logDir());
            File[] logFiles = logDir
                    .listFiles((dir, name) -> name.startsWith("OptimizedCombination_") && name.endsWith(".log"));
            if (logFiles != null && logFiles.length > 0) {
                File mostRecentFile = logFiles[0];
                for (File file : logFiles) {
                    if (file.lastModified() > mostRecentFile.lastModified()) {
                        mostRecentFile = file;
                    }
                }
                optimizedFile = mostRecentFile;
            } else {
                throw new IllegalArgumentException("No optimized combination log file found.");
            }
        } else {
            optimizedFile = new File(config.logDir(), String.format("OptimizedCombination_%s.log",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))));
        }
    }

    public OptimizedGameStarter(Config config, File optimizedFile) {
        this.config = config;
        this.optimizedFile = optimizedFile;
    }

    public void addSocket(Pair<InetAddress, Integer> socket) {
        socketMap.put(socket, false);
    }

    public boolean hasAllParticipants() {
        return socketMap.size() == config.allParticipantNum();
    }

    public void writeOptimizedCombinations(List<Map<Pair<InetAddress, Integer>, Role>> combinations) {
        try (FileWriter fileWriter = new FileWriter(optimizedFile)) {
            for (Map<Pair<InetAddress, Integer>, Role> combination : combinations) {
                StringBuilder sb = new StringBuilder();
                for (Pair<InetAddress, Integer> socket : combination.keySet()) {
                    sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combination.get(socket)));
                }
                fileWriter.write(sb.toString().trim());
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("Failed to write optimized combinations", e);
        }
    }

    public void appendFlagOptimizedCombinations(Map<Pair<InetAddress, Integer>, Role> combination) {
        try (FileWriter fileWriter = new FileWriter(optimizedFile, true)) {
            StringBuilder sb = new StringBuilder("#");
            for (Pair<InetAddress, Integer> socket : combination.keySet()) {
                sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combination.get(socket)));
            }
            fileWriter.write(sb.toString().trim());
            fileWriter.write(System.lineSeparator());
            logger.info("Flagged optimized combination");
        } catch (IOException e) {
            logger.error("Failed to append optimized combination", e);
        }
    }

    public List<Map<Pair<InetAddress, Integer>, Role>> readOptimizedCombinations() {
        List<Map<Pair<InetAddress, Integer>, Role>> combinations = new ArrayList<>();
        List<Map<Pair<InetAddress, Integer>, Role>> toRemove = new ArrayList<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(optimizedFile.toPath());
            for (String line : lines) {
                if (line.startsWith("#")) {
                    Map<Pair<InetAddress, Integer>, Role> combination = parseOptimizedCombination(line.substring(1));
                    toRemove.add(combination);
                } else {
                    Map<Pair<InetAddress, Integer>, Role> combination = parseOptimizedCombination(line);
                    combinations.add(combination);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read optimized combinations", e);
        }
        combinations.removeAll(toRemove);
        return combinations;
    }

    private Map<Pair<InetAddress, Integer>, Role> parseOptimizedCombination(String line) {
        Map<Pair<InetAddress, Integer>, Role> combination = new HashMap<>();
        String[] parts = line.trim().split(" ");
        for (String part : parts) {
            String[] socketRole = part.split("-");
            String[] socket = socketRole[0].split(":");
            try {
                String addr = socket[0];
                if (addr.startsWith("/")) {
                    addr = addr.substring(1);
                }
                combination.put(new Pair<>(InetAddress.getByName(
                        addr), Integer.parseInt(socket[1])),
                        Role.valueOf(socketRole[1]));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse optimized combination", e);
            } catch (UnknownHostException e) {
                logger.warn("Failed to parse optimized combination", e);
            }
        }
        return combination;
    }

    @Override
    public void run() {
        logger.info("OptimizedGameStarter started.");
        List<Map<Pair<InetAddress, Integer>, Role>> combinations;
        logger.info("File: " + optimizedFile.getAbsolutePath());
        if (optimizedFile.exists()) {
            logger.info("Optimized combination file already exists.");
            combinations = readOptimizedCombinations();
        } else {
            logger.info("Optimized combination file does not exist.");
            OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(
                    new ArrayList<>(socketMap.keySet()),
                    config.gameNum(), config.battleAgentNum());
            combinations = generator.toList();
            logger.info("Generated agent role combinations.");
            logger.info(generator);
            writeOptimizedCombinations(combinations);
        }
        logger.info("Optimized combinations are ready.");
        logger.info(String.format("Optimized combinations: %d", combinations.size()));

        while (true) {
            extracted();
            if (!canExecuteInParallel()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for parallel exec", e);
                }
                continue;
            }

            if (combinations.isEmpty() && builders.isEmpty()) {
                logger.info("All games have been started.");
                break;
            }

            Map<Pair<InetAddress, Integer>, Role> combination = combinations.stream()
                    .filter(this::checkSocketsAvailability)
                    .findFirst()
                    .orElse(null);
            if (combination == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for sockets to become available", e);
                }
                continue;
            }

            extracted();
            Map<Socket, Role> sockets = new HashMap<>();
            try {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for sockets to become available", e);
                }
                extracted();
                List<Pair<InetAddress, Integer>> remainingPairs = new ArrayList<>(combination.keySet());
                while (!remainingPairs.isEmpty()) {
                    Iterator<Pair<InetAddress, Integer>> iterator = remainingPairs.iterator();
                    while (iterator.hasNext()) {
                        Pair<InetAddress, Integer> pair = iterator.next();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            logger.error("Interrupted while waiting for sockets to become available", e);
                        }
                        try {
                            Socket socket = new Socket(pair.key(), pair.value());
                            sockets.put(socket, combination.get(pair));
                            logger.info(String.format("Successfully created socket %s:%d", pair.key(), pair.value()));
                            iterator.remove();
                        } catch (IOException e) {
                            logger.error(String.format("Failed to create socket %s:%d", pair.key(), pair.value()));
                        }
                    }
                }
                if (sockets.size() != combination.size()) {
                    logger.warn("Failed to create all sockets.");
                    releaseSockets(sockets.keySet());
                    clearSocketsAvailability(combination);
                    continue;
                }
                OptimizedGameBuilder builder = new OptimizedGameBuilder(sockets, config);
                builders.add(new Pair<>(combination, builder));
                combinations.remove(combination);
                builder.start();
                logger.info("Started a new game with a group of sockets.");
            } catch (Exception e) {
                logger.error("Exception", e);
                releaseSockets(sockets.keySet());
            }
        }
        logger.info("OptimizedGameStarter finished.");
    }

    private void extracted() {
        builders.removeIf(builder -> {
            if (!builder.value().isAlive()) {
                appendFlagOptimizedCombinations(builder.key());
                releaseSockets(builder.value().getSocketSet());
                return true;
            }
            return false;
        });
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

    @Override
    public void interrupt() {
        super.interrupt();
        builders.forEach(builder -> {
            builder.value().interrupt();
            releaseSockets(builder.value().getSocketSet());
        });
    }
}
