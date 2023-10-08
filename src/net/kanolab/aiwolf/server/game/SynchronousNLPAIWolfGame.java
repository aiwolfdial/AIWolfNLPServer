package net.kanolab.aiwolf.server.game;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.GameData;
import org.aiwolf.server.net.GameServer;

/**
 * 自然言語対戦用ゲームクラス
 */
public class SynchronousNLPAIWolfGame extends AIWolfGame{
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

	public void setGameData(GameData gameData){
		this.gameData = gameData;
	}

	public void start(GameData gameData){
		setGameData(gameData);
		super.start();
	}
}
