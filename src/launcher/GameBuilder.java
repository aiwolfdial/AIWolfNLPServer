package launcher;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.Connection;
import core.Game;
import core.GameData;
import core.GameServer;
import core.exception.DuplicateCombinationException;
import core.exception.IllegalPlayerNumberException;
import core.model.Agent;
import core.model.Config;
import core.model.GameSetting;
import core.model.Role;
import libs.RawFileLogger;

public class GameBuilder extends Thread {
    private static final Logger logger = LogManager.getLogger(GameBuilder.class);

    private final Config config;
    private final GameSetting gameSetting;
    private final Set<Connection> connections = new HashSet<>();
    private final Map<Agent, Role> agentRoleMap = new HashMap<>();

    public GameBuilder(Map<Socket, Role> sockets, Config config) throws IOException {
        Set<Integer> usedNumberSet = new HashSet<>();
        for (Socket socket : sockets.keySet()) {
            Connection connection = new Connection(socket, config, usedNumberSet);
            usedNumberSet.add(connection.getAgent().idx);
            connections.add(connection);
            agentRoleMap.put(connection.getAgent(), sockets.get(socket));
        }
        this.config = config;
        this.gameSetting = new GameSetting(config);
    }

    public Set<Socket> getSocketSet() {
        return connections.stream().map(Connection::getSocket).collect(Collectors.toSet());
    }

    @Override
    public void run() {
        logger.info("GameBuilder started.");
        GameServer gameServer = new GameServer(gameSetting, config, connections);
        GameData gameData = new GameData(gameSetting);

        if (connections.stream().anyMatch(connection -> !connection.isAlive())) {
            logger.error("Connection is not alive.");
            return;
        }

        String agentsName = connections.stream().map(connection -> connection.getAgent().name)
                .collect(Collectors.joining("-"));
        String gameName = String.format("%s_[%03d]_%s",
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()), 1,
                agentsName);

        try {
            logger.info(String.format("### START GAME ### %s", gameName));
            RawFileLogger rawFileLogger = null;
            if (config.saveGameLog()) {
                File file = new File(config.gameLogDir(), String.format("%s.log", gameName));
                rawFileLogger = new RawFileLogger(file);
            }
            Game game = new Game(gameSetting, gameServer, gameData, agentRoleMap, rawFileLogger);
            game.start();
            if (config.saveGameLog()) {
                Set<Entry<Agent, Connection>> newLostConnectionSet = connections.stream()
                        .filter(Connection::getHasException)
                        .collect(Collectors.toMap(Connection::getAgent, connection -> connection)).entrySet();
                File file = new File(config.gameLogDir(), String.format("%s_ERROR.log", gameName));
                RawFileLogger logger = new RawFileLogger(file);
                for (Entry<Agent, Connection> entry : newLostConnectionSet) {
                    entry.getValue().printException(logger, entry.getKey(), agentRoleMap.get(entry.getKey()));
                }
                // エラー出力がなければエラーログファイルを削除
                if (newLostConnectionSet.isEmpty()) {
                    file.delete();
                }
            }
            logger.info(String.format("### END GAME ### %s", gameName));
        } catch (IllegalPlayerNumberException | DuplicateCombinationException e) {
            logger.info(String.format("### SKIP GAME ### %s", gameName));
            logger.warn("Skip game.", e);
        } catch (IOException e) {
            logger.error("Exception", e);
        } finally {
            for (Connection connection : connections) {
                connection.close();
            }
        }

        logger.info("GameBuilder finished.");
    }
}
