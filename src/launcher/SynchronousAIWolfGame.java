package launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.AIWolfGame;
import core.GameConfiguration;
import core.GameData;
import core.GameServer;
import core.model.Agent;
import core.packet.GameSetting;

public class SynchronousAIWolfGame extends AIWolfGame {
	private static final Logger logger = LogManager.getLogger(SynchronousAIWolfGame.class);

	private final GameConfiguration gameConfiguration;
	private final GameData gameData;

	public SynchronousAIWolfGame(GameSetting gameSetting, GameServer gameServer, GameConfiguration gameConfiguration,
			GameData gameData) {
		super(gameSetting, gameServer);
		this.gameConfiguration = gameConfiguration;
		this.gameData = gameData;
	}

	private String makeRoleCombinationsText() {
		boolean includeAgentNum = false; // 1,VILLAGER,UECIL_3,2,VILLAGER,satozaki4
											// みたいに同じ役職の割り振りでもエージェントのindexで区別する(本質的に同じ割り振りの配役になる可能性がある)
		boolean includeAgentNameNum = false; // 例 kanolab1, shinshu_univ1 true: VILLAGER,kanolab1,VILLAGER,shinshu_univ2
												// false: VILLAGER,kanolab,VILLAGER,shinshu_univ
												// (保存時にエージェントの識別番号を取るか取らないか。本質的に同じ配役を排除したいけど、前ゲームkanolab1、次ゲームkanolab2みたいな状況を排除するかしないか)
		ArrayList<String> combinationText = new ArrayList<>();

		for (Agent agent : new TreeSet<>(gameData.getAgentList())) {
			String agentName = agentNameMap.get(agent);

			if (!includeAgentNameNum) {
				agentName = agentName.replaceAll("[0-9]", "");
			}

			if (includeAgentNum) {
				combinationText.add(String.format("%d,%s,%s", agent.agentIdx, gameData.getRole(agent), agentName));
			} else {
				combinationText.add(String.format("%s,%s", gameData.getRole(agent), agentName));
			}
		}

		if (!includeAgentNum) {
			Collections.sort(combinationText);
		}

		return String.join(",", combinationText);
	}

	private boolean isDoneCombinations(String checkCombinationText) {
		File file = Paths.get(gameConfiguration.getRoleCombinationDir(), gameConfiguration.getRoleCombinationFilename())
				.toFile();

		if (!file.exists()) {
			return false;
		}

		try (BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
			String doneCombinationText;

			while ((doneCombinationText = bufferReader.readLine()) != null) {
				if (doneCombinationText.equals(checkCombinationText)) {
					return true;
				}
			}
		} catch (IOException e) {
			logger.error("Exception", e);
		}

		return false;
	}

	protected void init() {
		gameDataMap = new TreeMap<>();
		agentNameMap = new HashMap<>();
		gameServer.setGameData(gameData);

		List<Agent> agentList = gameServer.getConnectedAgentList();
		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameSetting(gameSetting);
		for (Agent agent : agentList) {
			gameServer.init(agent);
			String requestName = gameServer.requestName(agent);
			agentNameMap.put(agent, requestName);
		}
	}

	public void start() {
		init();
		try {
			if (gameConfiguration.isSaveRoleCombination()) {
				String currentText = makeRoleCombinationsText();
				if (isDoneCombinations(currentText)) {
					super.finish();
					return;
				}
			}
			super.start();
		} catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	public void finish() {
		try {
			if (!gameConfiguration.isSaveRoleCombination()) {
				return;
			}
			String saveText = makeRoleCombinationsText() + "\r\n";
			File file = Paths
					.get(gameConfiguration.getRoleCombinationDir(), gameConfiguration.getRoleCombinationFilename())
					.toFile();
			if (!file.canWrite()) {
				file.setWritable(true);
			}
			try (FileWriter fileWriter = new FileWriter(file, true)) {
				fileWriter.write(saveText);
			}
		} catch (Exception e) {
			logger.error("Exception", e);
		} finally {
			super.finish();
		}
	}
}
