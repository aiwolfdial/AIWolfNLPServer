package core;

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

import core.model.Agent;
import core.model.Config;
import core.model.GameSetting;
import core.model.Role;
import core.model.Status;
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
		// 順番が固定にならないように念のためシャッフル
		Collections.shuffle(sockets);

		// コネクションとエージェントの紐付け
		Set<Integer> usedNumberSet = new HashSet<>();
		for (Socket socket : sockets) {
			Connection connection = new Connection(socket, config, usedNumberSet);
			usedNumberSet.add(connection.getAgent().agentIdx);
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

		// デバッグモードの場合、全パターンと各エージェントがそれぞれの役職になった回数をカウントして出力
		printCombinations(roleList);
		return roleList;
	}

	private void printCombinations(List<Map<Agent, Role>> roleList) {
		for (int i = 0; i < roleList.size(); i++) {
			logger.debug(String.format("%d: %s", i, roleList.get(i)));
		}
		Map<Agent, Map<Role, Integer>> map = new HashMap<>();
		for (Map<Agent, Role> roleMap : roleList) {
			for (Entry<Agent, Role> entry : roleMap.entrySet()) {
				Map<Role, Integer> roleNumMap = new HashMap<>();
				if (map.containsKey(entry.getKey()))
					roleNumMap = map.get(entry.getKey());
				int count = 1;
				if (roleNumMap.containsKey(entry.getValue()))
					count += roleNumMap.get(entry.getValue());
				roleNumMap.put(entry.getValue(), count);
				map.put(entry.getKey(), roleNumMap);
			}
		}
		for (Entry<Agent, Map<Role, Integer>> entry : map.entrySet()) {
			logger.debug(String.format("%s: %s", entry.getKey(), entry.getValue()));
		}
	}

	@Override
	public void run() {
		logger.info("GameBuilder start.");
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
				if (connection.getAgent().agentName.equals(config.humanName())) {
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

			// 現在対戦に使用しているエージェントの更新
			gameServer.setAgents(agentRoleMap.keySet());

			// 今回マッチングするエージェントのいずれかがロストしているならスキップする
			if (agentRoleMap.keySet().stream().anyMatch(agent -> connections.stream()
					.noneMatch(connection -> connection.getAgent().equals(agent) && connection.isAlive())))
				continue;

			// 役職の設定
			for (Entry<Agent, Role> entry : agentRoleMap.entrySet()) {
				gameData.addAgent(entry.getKey(), Status.ALIVE, entry.getValue());
			}

			String gameName = String.format("%s_[%03d]_%s",
					DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()), i + 1,
					gameServer.getNames().stream().collect(Collectors.joining(":")));

			try {
				Game game = new Game(config, gameSetting, gameServer, gameData);
				// 現在の対戦数を表示
				logger.debug(String.format("I: %d", i));
				// ロガーを設定
				if (config.saveLog()) {
					File file = new File(config.logDir(), String.format("%s.log", gameName));
					game.setRawFileLogger(new RawFileLogger(file));
				}

				// ゲームの実行
				game.start();

				// 今回のゲームでエラーが発生したエージェントがいた場合はエラーログを出力する
				if (config.saveLog()) {
					Set<Entry<Agent, Connection>> newLostConnectionSet = connections.stream()
							.filter(connection -> connection.getHasException())
							.collect(Collectors.toMap(Connection::getAgent, connection -> connection)).entrySet();
					File file = new File(config.logDir(), String.format("%s_ERROR.log", gameName));
					RawFileLogger logger = new RawFileLogger(file);
					for (Entry<Agent, Connection> entry : newLostConnectionSet) {
						entry.getValue().printException(logger, entry.getKey(), agentRoleMap.get(entry.getKey()));
					}

					// エラー出力がなければエラーログファイルを削除
					if (newLostConnectionSet.isEmpty())
						file.delete();
				}
			} catch (IOException e) {
				logger.error("Exception", e);
			}

			// 全てのコネクションがロストした場合対戦を終了する
			if (connections.stream().noneMatch(Connection::isAlive))
				break;
		}
		logger.info("GameBuilder end.");
		close();
	}
}