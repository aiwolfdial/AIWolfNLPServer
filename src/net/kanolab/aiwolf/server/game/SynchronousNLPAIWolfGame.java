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
import java.util.TreeSet;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	 * 毎ゲーム開始時の初期化
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

	private void saveRoleCombinations(){
		GameConfiguration config = new GameConfiguration(DEFAULT_INI_PATH);

		if(!config.getBoolean(Option.IS_SAVE_ROLE_COMBINATIONS) || (Integer)config.get(Option.ALL_PARTICIPANT_NUM) <= (Integer)config.get(Option.BATTLE_AGENT_NUM)){
			return;
		}

		String saveText = "";
		for(Agent agent:new TreeSet<Agent>(gameData.getAgentList())){
			saveText += String.format("%d,%s,%s", agent.getAgentIdx(),gameData.getRole(agent), agentNameMap.get(agent));
		}

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
