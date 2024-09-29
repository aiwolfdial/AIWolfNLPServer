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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import core.model.Config;
import core.model.Role;
import libs.Pair;
import utils.DefaultAgentRoleCombos;

public class GameStarter extends Thread {
    private static final Logger logger = LogManager.getLogger(GameStarter.class);

    private final File comboLogFile;
    private final Config config;

    public GameStarter(Config config) {
        this.config = config;
        File comboLogFile = new File(config.resumeComboLogPath());
        if (Strings.isEmpty(config.resumeComboLogPath()) || !comboLogFile.exists() || !comboLogFile.isFile()) {
            comboLogFile = new File(config.comboLogDir(), String.format("combo_%s.log",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))));
        }
        this.comboLogFile = comboLogFile;
    }

    public GameStarter(Config config, File comboLogFile) {
        this.config = config;
        this.comboLogFile = comboLogFile;
    }

    public void writeCombos(List<Map<Pair<InetAddress, Integer>, Role>> combos) {
        try (FileWriter fileWriter = new FileWriter(comboLogFile)) {
            for (Map<Pair<InetAddress, Integer>, Role> combo : combos) {
                StringBuilder sb = new StringBuilder();
                for (Pair<InetAddress, Integer> socket : combo.keySet()) {
                    sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combo.get(socket)));
                }
                fileWriter.write(sb.toString().trim());
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("Failed to write combos", e);
        }
    }

    public void appendFlagCombo(Map<Pair<InetAddress, Integer>, Role> combos) {
        try (FileWriter fileWriter = new FileWriter(comboLogFile, true)) {
            StringBuilder sb = new StringBuilder("#");
            for (Pair<InetAddress, Integer> socket : combos.keySet()) {
                sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combos.get(socket)));
            }
            fileWriter.write(sb.toString().trim());
            fileWriter.write(System.lineSeparator());
            logger.info("Flagged combo");
        } catch (IOException e) {
            logger.error("Failed to append combo", e);
        }
    }

    public List<Map<Pair<InetAddress, Integer>, Role>> readCombos() {
        List<Map<Pair<InetAddress, Integer>, Role>> combos = new ArrayList<>();
        List<Map<Pair<InetAddress, Integer>, Role>> toRemove = new ArrayList<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(comboLogFile.toPath());
            for (String line : lines) {
                if (line.startsWith("#")) {
                    Map<Pair<InetAddress, Integer>, Role> combo = parseCombos(line.substring(1));
                    toRemove.add(combo);
                } else {
                    Map<Pair<InetAddress, Integer>, Role> combo = parseCombos(line);
                    combos.add(combo);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read combos", e);
        }
        combos.removeAll(toRemove);
        return combos;
    }

    private Map<Pair<InetAddress, Integer>, Role> parseCombos(String line) {
        Map<Pair<InetAddress, Integer>, Role> combos = new HashMap<>();
        String[] parts = line.trim().split(" ");
        for (String part : parts) {
            String[] socketRole = part.split("-");
            String[] socket = socketRole[0].split(":");
            try {
                String addr = socket[0];
                if (addr.startsWith("/")) {
                    addr = addr.substring(1);
                }
                combos.put(new Pair<>(InetAddress.getByName(
                        addr), Integer.parseInt(socket[1])),
                        Role.valueOf(socketRole[1]));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse combos", e);
            } catch (UnknownHostException e) {
                logger.warn("Failed to parse combos", e);
            }
        }
        return combos;
    }

    @Override
    public void run() {
        logger.info("GameStarter started.");
        List<Map<Pair<InetAddress, Integer>, Role>> combos;
        logger.info("File: " + comboLogFile.getAbsolutePath());
        if (comboLogFile.exists()) {
            logger.info("Combo file already exists.");
            combos = readCombos();
        } else {
            logger.info("Combo file does not exist.");
            String[] agentAddresses = config.agentAddresses().replace("[", "").replace("]", "").split(",\\s*");
            List<Pair<InetAddress, Integer>> agentPairs = new ArrayList<>();
            for (String address : agentAddresses) {
                String[] socket = address.split(":");
                try {
                    InetAddress inetAddress = InetAddress.getByName(socket[0]);
                    int port = Integer.parseInt(socket[1]);
                    agentPairs.add(new Pair<>(inetAddress, port));
                } catch (Exception e) {
                    logger.error(String.format("Failed to parse agent address %s", address), e);
                }
            }
            // OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(
            // agentPairs,
            // config.gameNum(), config.battleAgentNum());
            DefaultAgentRoleCombos generator = new DefaultAgentRoleCombos(agentPairs);
            combos = generator.toList();
            logger.info("Generated agent role combos.");
            logger.info(generator);
            writeCombos(combos);
        }
        logger.info("Combos are ready.");
        logger.info(String.format("Combos: %d", combos.size()));

        while (true) {
            if (combos.isEmpty()) {
                logger.info("All games have been started.");
                break;
            }

            Map<Pair<InetAddress, Integer>, Role> combo = combos.stream()
                    .findFirst()
                    .orElse(null);
            if (combo == null) {
                logger.error("Failed to get a combo.");
                break;
            }

            Map<Socket, Role> sockets = new HashMap<>();
            try {
                try {
                    Thread.sleep(1000 * 60 * 10);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for sockets to become available", e);
                }
                List<Pair<InetAddress, Integer>> remainingPairs = new ArrayList<>(combo.keySet());
                while (!remainingPairs.isEmpty()) {
                    Iterator<Pair<InetAddress, Integer>> iterator = remainingPairs.iterator();
                    while (iterator.hasNext()) {
                        Pair<InetAddress, Integer> pair = iterator.next();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            logger.error("Interrupted while waiting for sockets to become available", e);
                        }
                        Role role = combo.get(pair);
                        try {
                            Socket socket = new Socket(pair.key(), pair.value());
                            sockets.put(socket, role);
                            logger.info(String.format("Successfully created socket %s:%d", pair.key(), pair.value()));
                        } catch (IOException e) {
                            logger.error(String.format("Failed to create socket %s:%d", pair.key(), pair.value()));

                            Socket socket = getDummySocket();
                            if (socket == null) {
                                logger.error("Failed to create dummy socket");
                                releaseSockets(sockets.keySet());
                                break;
                            }

                            sockets.put(socket, role);
                            logger.info(
                                    String.format("Successfully created dummy socket %s:%d", pair.key(), pair.value()));
                        } finally {
                            iterator.remove();
                        }
                    }
                }
                if (sockets.size() != combo.size()) {
                    logger.warn("Failed to create all sockets.");
                    releaseSockets(sockets.keySet());
                    return;
                }
                GameBuilder builder = new GameBuilder(sockets, config);
                combos.remove(combo);
                builder.start();
                logger.info("Started a new game with a group of sockets.");
                builder.join();
                appendFlagCombo(combo);
            } catch (Exception e) {
                logger.error("Exception", e);
                releaseSockets(sockets.keySet());
            }
        }
        logger.info("GameStarter finished.");
    }

    private Socket getDummySocket() {
        for (int socket = 30000; socket < 30005; socket++) {
            try {
                return new Socket("127.0.0.1", socket);
            } catch (IOException e) {
                logger.error("Failed to create dummy socket", e);
            }
        }
        return null;
    }

    private void releaseSockets(Set<Socket> sockets) {
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Failed to close socket", e);
            }
        }
    }
}
