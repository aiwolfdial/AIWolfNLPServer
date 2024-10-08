package launcher;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.Combinations;
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

	private static final Role[] USED_ROLES = {
			Role.SEER,
			Role.POSSESSED,
			Role.WEREWOLF
	};

	private final Config config;
	private final GameSetting gameSetting;
	private final Set<Connection> connections = new HashSet<>();

	public GameBuilder(List<Socket> sockets, Config config) throws IOException {
		Collections.shuffle(sockets);
		// コネクションとエージェントの紐付け
		Set<Integer> usedNumberSet = new HashSet<>();
		for (Socket socket : sockets) {
			Connection connection = new Connection(socket, config, usedNumberSet);
			usedNumberSet.add(connection.getAgent().idx);
			connections.add(connection);
		}
		this.config = config;
		this.gameSetting = new GameSetting(config);
	}

	private void close() {
		for (Connection connection : connections) {
			try {
				connection.getSocket().close();
			} catch (IOException e) {
				logger.error("Exception", e);
			}
		}
	}

	private List<Map<Agent, Role>> getCombinations() {
		List<Map<Agent, Role>> roleList = new ArrayList<>();
		// セット内の人数から組み合わせを生成
		Iterator<int[]> agentCombination = new Combinations(config.connectAgentNum(),
				config.battleAgentNum())
						.iterator();
		Iterator<int[]> roleCombination = new Combinations(
				config.battleAgentNum(), USED_ROLES.length).iterator();
		// nPrがライブラリに用意されていなかったので村人以外の役職について5C3のパターンを取った後にその3名に対して順列を取る
		while (agentCombination.hasNext()) {
			if (!roleCombination.hasNext()) {
				roleCombination = new Combinations(5, 3).iterator();
				continue;
			}
			int[] agentArray = agentCombination.next();
			while (roleCombination.hasNext()) {
				// 村人以外の役職の番号についてその順列を取る
				List<Integer> roleNumList = new ArrayList<>();
				for (int i : roleCombination.next())
					roleNumList.add(agentArray[i]);
				PermutationIterator<Integer> nonVillagerPermutationIterator = new PermutationIterator<>(roleNumList);
				while (nonVillagerPermutationIterator.hasNext()) {
					List<Integer> giftedAgentId = nonVillagerPermutationIterator.next();
					Map<Agent, Role> roleMap = new HashMap<>();
					for (int i = 0; i < giftedAgentId.size(); i++) {
						roleMap.put(Agent.getAgent(giftedAgentId.get(i) + 1), USED_ROLES[i]);
					}
					for (int i : agentArray) {
						if (roleMap.containsKey(Agent.getAgent(i + 1)))
							continue;
						roleMap.put(Agent.getAgent(i + 1), Role.VILLAGER);
					}
					roleList.add(roleMap);
				}
			}
		}
		return roleList;
	}

	@Override
	public void run() {
		logger.info("GameBuilder started.");
		// 役職リストの取得
		List<Map<Agent, Role>> agentRoleMapList = getCombinations();
		// ゲームサーバの生成
		GameServer gameServer = new GameServer(gameSetting, config, connections);
		int limit = config.prioritizeCombinations() ? agentRoleMapList.size()
				: config.gameNum();
		// 人間対戦時
		Agent human = null;
		if (config.joinHuman()) {
			for (Connection connection : connections) {
				if (connection.getAgent().name.equals(config.humanName())) {
					human = connection.getAgent();
				}
			}
		}
		// 全組み合わせ実行しない場合はランダムにするために役職組み合わせリストをシャッフル
		if (!config.prioritizeCombinations())
			Collections.shuffle(agentRoleMapList);
		for (int i = 0; i < limit; i++) {
			Map<Agent, Role> agentRoleMap = agentRoleMapList.get(i);
			if (config.joinHuman()
					&& !agentRoleMap.get(human).name().equals(config.humanRole().name()))
				continue;
			GameData gameData = new GameData(gameSetting);
			// 今回マッチングするエージェントのいずれかがロストしているならスキップする
			if (agentRoleMap.keySet().stream().anyMatch(agent -> connections.stream()
					.noneMatch(connection -> connection.getAgent().equals(agent) && connection.isAlive())))
				continue;
			String agentsName = agentRoleMap.keySet().stream()
					.map(agent -> agent.name)
					.collect(Collectors.joining("-"));
			String gameName = String.format("%s_[%03d]_%s",
					DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()), i + 1,
					agentsName);
			try {
				logger.info(String.format("### START GAME ### %s", gameName));
				RawFileLogger rawFileLogger = null;
				if (config.saveLog()) {
					File file = new File(config.logDir(), String.format("%s.log", gameName));
					rawFileLogger = new RawFileLogger(file);
				}
				Game game = new Game(config, gameSetting, gameServer, gameData, agentRoleMap, rawFileLogger);
				// ゲームの実行
				game.start();
				// 今回のゲームでエラーが発生したエージェントがいた場合はエラーログを出力する
				if (config.saveLog()) {
					Set<Entry<Agent, Connection>> newLostConnectionSet = connections.stream()
							.filter(Connection::getHasException)
							.collect(Collectors.toMap(Connection::getAgent, connection -> connection)).entrySet();
					File file = new File(config.logDir(), String.format("%s_ERROR.log", gameName));
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
			}
			// 全てのコネクションがロストした場合対戦を終了する
			if (connections.stream().noneMatch(Connection::isAlive)) {
				break;
			}
		}
		logger.info("GameBuilder finished.");
		close();
	}
}