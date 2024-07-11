package game;

import java.io.BufferedReader;
// 役職の割り振りを保存するためのインポート
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

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.GameData;
import org.aiwolf.server.net.GameServer;

import common.GameConfiguration;

/**
 * 自然言語対戦用ゲームクラス
 */
public class SynchronousNLPAIWolfGame extends AIWolfGame {
	private static final String DEFAULT_INI_PATH = "./res/AIWolfGameServer.ini";

	public SynchronousNLPAIWolfGame(GameSetting gameSetting, GameServer gameServer) {
		super(gameSetting, gameServer);
	}

	/**
	 * 役職の組み合わせをログに残すか判断する
	 * 
	 * @author nwatanabe
	 */
	private boolean isWriteRoleCombinations(GameConfiguration config) {
		return config.isSaveRoleCombination()
				&& config.getAllParticipantNum() >= config.getBattleAgentNum();
	}

	/**
	 * 現在の役職の組み合わせから1行のテキストを作成する
	 * 
	 * @author nwatanabe
	 */
	private String makeRoleCombinationsText() {
		boolean includeAgentNum = false; // 1,VILLAGER,UECIL_3,2,VILLAGER,satozaki4
											// みたいに同じ役職の割り振りでもエージェントのindexで区別する(本質的に同じ割り振りの配役になる可能性がある)
		boolean includeAgentNameNum = false; // 例 kanolab1, shinshu_univ1 true: VILLAGER,kanolab1,VILLAGER,shinshu_univ2
												// false: VILLAGER,kanolab,VILLAGER,shinshu_univ
												// (保存時にエージェントの識別番号を取るか取らないか。本質的に同じ配役を排除したいけど、前ゲームkanolab1、次ゲームkanolab2みたいな状況を排除するかしないか)
		ArrayList<String> combinationText = new ArrayList<>();

		for (Agent agent : new TreeSet<Agent>(gameData.getAgentList())) {
			String agentName = agentNameMap.get(agent);

			if (!includeAgentNameNum) {
				// remove number from agent name
				agentName = agentName.replaceAll("[0-9]", "");
			}

			if (includeAgentNum) {
				combinationText.add(String.format("%d,%s,%s", agent.getAgentIdx(), gameData.getRole(agent), agentName));
			} else {
				combinationText.add(String.format("%s,%s", gameData.getRole(agent), agentName));
			}
		}

		if (!includeAgentNum) {
			Collections.sort(combinationText);
		}

		return String.join(",", combinationText);
	}

	/**
	 * 今までの組み合わせを保存しているログに今回のログがあるか確認する
	 * 
	 * @author nwatanabe
	 */
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
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 毎ゲーム開始時の初期化
	 * 
	 * ・追記 2024/03/09
	 * 
	 * @author nwatanabe
	 *         isWriteRoleCombinations = true なら重複が無いか確認する
	 */
	protected void init() {
		gameDataMap = new TreeMap<Integer, GameData>();
		agentNameMap = new HashMap<Agent, String>();
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

	/**
	 * 今回の役職の組み合わせを保存する
	 * 
	 * @author nwatanabe
	 */
	private void saveRoleCombinations() {
		GameConfiguration config = new GameConfiguration(DEFAULT_INI_PATH);

		if (!isWriteRoleCombinations(config)) {
			return;
		}

		String saveText = makeRoleCombinationsText() + "\r\n";

		try {
			File file = new File(config.getRoleCombinationDir() + config.getRoleCombinationFilename());

			if (!file.canWrite()) {
				file.setWritable(true);
			}

			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(saveText);
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setGameData(GameData gameData) {
		this.gameData = gameData;
	}

	public void start(GameData gameData) {
		setGameData(gameData);
		init();

		// check same pattern exist or not
		GameConfiguration config = new GameConfiguration(DEFAULT_INI_PATH);
		if (isWriteRoleCombinations(config)) {
			String currentText = makeRoleCombinationsText();

			if (isDoneCombinations(config, currentText)) {
				super.finish();
				return;
			}

		}

		super.start();
	}

	/**
	 * 2024/03/09
	 * finishまで来たらゲームが正常終了したものとみなし、その配役をメモする
	 * 
	 * @author nwatanabe
	 */
	public void finish() {
		saveRoleCombinations();
		super.finish();
	}
}
