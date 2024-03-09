package net.kanolab.aiwolf.server.game;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.GameData;
import org.aiwolf.server.net.GameServer;

// 役職の割り振りを保存するためのインポート
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collections;
import net.kanolab.aiwolf.server.common.Option;
import net.kanolab.aiwolf.server.common.GameConfiguration;

/**
 * 自然言語対戦用ゲームクラス
 */
public class SynchronousNLPAIWolfGame extends AIWolfGame{
	private static final String DEFAULT_INI_PATH  = "./res/AIWolfGameServer.ini";

	public SynchronousNLPAIWolfGame(GameSetting gameSetting, GameServer gameServer) {
		super(gameSetting, gameServer);
	}

	/**
	 * 役職の組み合わせをログに残すか判断する
	 * 
	 * @author nwatanabe
	 */
	private boolean isWriteRoleCombinations(GameConfiguration config){
		return config.getBoolean(Option.IS_SAVE_ROLE_COMBINATIONS) && (Integer)config.get(Option.ALL_PARTICIPANT_NUM) > (Integer)config.get(Option.BATTLE_AGENT_NUM);
	}

	/**
	 * 現在の役職の組み合わせから1行のテキストを作成する
	 * 
	 * @author nwatanabe
	 */
	private String makeRoleCombinationsText(){
		boolean includeAgentNum = false;
		ArrayList<String> combinationText = new ArrayList<>();

		for(Agent agent:new TreeSet<Agent>(gameData.getAgentList())){

			if(includeAgentNum){
				combinationText.add(String.format("%d,%s,%s", agent.getAgentIdx(),gameData.getRole(agent), agentNameMap.get(agent)));
			}
			else{
				combinationText.add(String.format("%s,%s",gameData.getRole(agent), agentNameMap.get(agent)));
			}
		}

		if(!includeAgentNum){
			Collections.sort(combinationText);
		}

		return String.join(",",combinationText);
	}

	/**
	 * 今までの組み合わせを保存しているログに今回のログがあるか確認する
	 * 
	 * @author nwatanabe
	 */
	private boolean isDoneCombinations(GameConfiguration config, String checkCombinationText){
		File file = new File((String)config.get(Option.ROLE_COMBINATIONS_SAVE_PATH) + (String)config.get(Option.ROLE_COMBINATIONS_SAVE_FILE_NAME));
		
		if(!file.exists()){
			return false;
		}

		try{
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			String doneCombinationText = "";

			while((doneCombinationText = bufferReader.readLine()) != null){
				if(doneCombinationText.equals(checkCombinationText)){
					bufferReader.close();
					return true;
				}
			}

			bufferReader.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		
		return false;
	}

	/**
	 * 毎ゲーム開始時の初期化
	 * 
	 * ・追記 2024/03/09 
	 * 	@author nwatanabe
	 * 	isWriteRoleCombinations = true なら重複が無いか確認する
	 */
	protected void init(){
		gameDataMap = new TreeMap<Integer, GameData>();
		agentNameMap = new HashMap<Agent, String>();
		gameServer.setGameData(gameData);

		List<Agent> agentList = gameServer.getConnectedAgentList();
		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameSetting(gameSetting);
		for(Agent agent : agentList){
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
	private void saveRoleCombinations(){
		GameConfiguration config = new GameConfiguration(DEFAULT_INI_PATH);

		if(!isWriteRoleCombinations(config)){
			return;
		}

		String saveText = makeRoleCombinationsText() + "\r\n";

		try{
			File file = new File((String)config.get(Option.ROLE_COMBINATIONS_SAVE_PATH) + (String)config.get(Option.ROLE_COMBINATIONS_SAVE_FILE_NAME));
			
			if (!file.canWrite()){
				file.setWritable(true);
    	    }

			FileWriter fileWriter = new FileWriter(file,true);
			fileWriter.write(saveText);
			fileWriter.close();

		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public void setGameData(GameData gameData){
		this.gameData = gameData;
	}

	public void start(GameData gameData){
		setGameData(gameData);
		init();

		// check same pattern exist or not
		GameConfiguration config = new GameConfiguration(DEFAULT_INI_PATH);
		if(isWriteRoleCombinations(config)){
			String currentText = makeRoleCombinationsText();
			
			if(isDoneCombinations(config,currentText)){
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
	public void finish(){
		saveRoleCombinations();
		super.finish();
	}
}
