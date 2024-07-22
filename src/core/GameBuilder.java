package core;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import core.model.GameSetting;
import core.model.Role;
import core.model.Status;
import libs.FileGameLogger;

public class GameBuilder extends Thread {
	private static final Logger logger = LogManager.getLogger(GameBuilder.class);

	private static final String NORMAL_LOG_FILE_NAME = "%s%s_%03d_%s.log";
	private static final String ERROR_LOG_FILE_NAME = "%s%s_%03d_err_%s.log";

	private static final Role[] USED_ROLES = {
			Role.SEER,
			Role.POSSESSED,
			Role.WEREWOLF
	};

	private final GameConfiguration gameConfiguration;
	private final GameSetting gameSetting;
	// private final Map<Agent, AIWolfConnection> agentConnectionMap = new
	// HashMap<>();
	private final Set<AIWolfConnection> aiWolfConnections = new HashSet<>();

	public GameBuilder(List<Socket> socketList, GameConfiguration gameConfiguration) {
		// 順番が固定にならないように念のためシャッフル
		Collections.shuffle(socketList);

		// コネクションとエージェントの紐付け
		Set<Integer> usedNumberSet = new HashSet<>();
		for (Socket socket : socketList) {
			AIWolfConnection connection = new AIWolfConnection(socket, gameConfiguration, usedNumberSet);
			usedNumberSet.add(connection.getAgent().agentIdx);
			aiWolfConnections.add(connection);
		}
		this.gameConfiguration = gameConfiguration;
		this.gameSetting = new GameSetting(gameConfiguration);
	}

	private void close() {
		for (AIWolfConnection connection : aiWolfConnections) {
			try {
				connection.getSocket().close();
			} catch (IOException e) {
				logger.error("Exception", e);
			}
		}
	}

	private List<Map<Agent, Role>> createAgentRoleCombinations() {
		List<Map<Agent, Role>> roleList = new ArrayList<>();

		// セット内の人数から組み合わせを生成
		Iterator<int[]> agentCombination = new Combinations(gameConfiguration.getConnectAgentNum(),
				gameConfiguration.getBattleAgentNum())
						.iterator();
		Iterator<int[]> roleCombination = new Combinations(
				gameConfiguration.getBattleAgentNum(), USED_ROLES.length).iterator();

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
		printCombinationList(roleList);
		return roleList;
	}

	private void printCombinationList(List<Map<Agent, Role>> roleList) {
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
		List<Map<Agent, Role>> agentRoleMapList = createAgentRoleCombinations();

		// ゲームサーバの生成
		GameServer gameServer = new GameServer(gameSetting, gameConfiguration, aiWolfConnections);

		int limit = gameConfiguration.isPrioritizeCombinations() ? agentRoleMapList.size()
				: gameConfiguration.getGameNum();

		// 人間対戦時
		Agent human = null;
		if (gameConfiguration.isJoinHuman()) {
			for (AIWolfConnection connection : aiWolfConnections) {
				if (gameServer.getName(connection.getAgent()).equals(gameConfiguration.getHumanName())) {
					human = connection.getAgent();
				}
			}
		}

		// 全組み合わせ実行しない場合はランダムにするために役職組み合わせリストをシャッフル
		if (!gameConfiguration.isPrioritizeCombinations())
			Collections.shuffle(agentRoleMapList);

		for (int i = 0; i < limit; i++) {
			Map<Agent, Role> agentRoleMap = agentRoleMapList.get(i);
			if (gameConfiguration.isJoinHuman()
					&& !agentRoleMap.get(human).name().equals(gameConfiguration.getHumanRole().name()))
				continue;
			AIWolfGame game = new AIWolfGame(gameConfiguration, gameSetting, gameServer);
			GameData gameData = new GameData(gameSetting);

			// 現在対戦に使用しているエージェントの更新
			gameServer.updateUsingAgentList(agentRoleMap.keySet());

			// 今回マッチングするエージェントのいずれかがロストしているならスキップする
			if (agentRoleMap.keySet().stream().anyMatch(agent -> aiWolfConnections.stream()
					.noneMatch(connection -> connection.getAgent().equals(agent) && connection.isAlive())))
				continue;

			// 役職の設定
			for (Entry<Agent, Role> entry : agentRoleMap.entrySet()) {
				gameData.addAgent(entry.getKey(), Status.ALIVE, entry.getValue());
			}

			String clientNames = String.join("_", gameServer.getNames());
			String subLogDirName = new SimpleDateFormat("MMddHHmmss").format(Calendar.getInstance().getTime());

			try {
				// 現在の対戦数を表示
				logger.debug(String.format("I: %d", i));
				// ロガーを設定
				if (gameConfiguration.isSaveLog()) {
					String path = String.format(NORMAL_LOG_FILE_NAME, gameConfiguration.getLogDir(), subLogDirName, i,
							clientNames);
					logger.debug(String.format("Path: %s", path));
					game.setGameLogger(new FileGameLogger(new File(path)));
				}

				// ゲームの実行
				game.start(gameData);

				// 今回のゲームでエラーが発生したエージェントがいた場合はエラーログを出力する
				if (gameConfiguration.isSaveLog()) {
					Set<Entry<Agent, AIWolfConnection>> newLostConnectionSet = aiWolfConnections.stream()
							.filter(connection -> connection.getReportError())
							.collect(Collectors.toMap(AIWolfConnection::getAgent, connection -> connection)).entrySet();
					String errPath = String.format(ERROR_LOG_FILE_NAME, gameConfiguration.getLogDir(), subLogDirName, i,
							clientNames);
					File errorLogFile = new File(errPath);
					FileGameLogger logger = new FileGameLogger(errorLogFile);
					for (Entry<Agent, AIWolfConnection> entry : newLostConnectionSet) {
						entry.getValue().reportError(logger, entry.getKey(), agentRoleMap.get(entry.getKey()));
					}

					// エラー出力がなければエラーログファイルを削除
					if (newLostConnectionSet.isEmpty())
						errorLogFile.delete();
				}
			} catch (IOException e) {
				logger.error("Exception", e);
			}

			// 全てのコネクションがロストした場合対戦を終了する
			if (aiWolfConnections.stream().noneMatch(AIWolfConnection::isAlive))
				break;
		}
		logger.info("GameBuilder end.");
		close();
	}
}