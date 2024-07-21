package launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	private static final String DEFAULT_INI_PATH = "./config/AIWolfGameServer.ini";

	public SynchronousAIWolfGame(GameSetting gameSetting, GameServer gameServer) {
		super(gameSetting, gameServer);
	}

	private boolean isWriteRoleCombinations(GameConfiguration config) {
		return config.isSaveRoleCombination()
				&& config.getAllParticipantNum() >= config.getBattleAgentNum();
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

	private boolean isDoneCombinations(GameConfiguration config, String checkCombinationText) {
		File file = new File(config.getRoleCombinationDir()
				+ config.getRoleCombinationFilename());

		if (!file.exists()) {
			return false;
		}

		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			String doneCombinationText = "";

			while ((doneCombinationText = bufferReader.readLine()) != null) {
				if (doneCombinationText.equals(checkCombinationText)) {
					bufferReader.close();
					return true;
				}
			}

			bufferReader.close();
		} catch (IOException e) {
			logger.error(e);
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

	private void saveRoleCombinations() {
		try {
			GameConfiguration config = GameConfiguration.load(DEFAULT_INI_PATH);

			if (!isWriteRoleCombinations(config)) {
				return;
			}

			String saveText = makeRoleCombinationsText() + "\r\n";

			File file = new File(config.getRoleCombinationDir() + config.getRoleCombinationFilename());

			if (!file.canWrite()) {
				file.setWritable(true);
			}

			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(saveText);
			fileWriter.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void start(GameData gameData) {
		this.gameData = gameData;
		init();

		// check same pattern exist or not
		GameConfiguration config;
		try {
			config = GameConfiguration.load(DEFAULT_INI_PATH);
			if (isWriteRoleCombinations(config)) {
				String currentText = makeRoleCombinationsText();

				if (isDoneCombinations(config, currentText)) {
					super.finish();
					return;
				}

			}

			super.start();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void finish() {
		saveRoleCombinations();
		super.finish();
	}
}
