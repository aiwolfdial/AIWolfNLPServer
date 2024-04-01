package net.kanolab.aiwolf.server.starter;

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
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.GameData;
import org.aiwolf.server.util.FileGameLogger;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.Combinations;

import net.kanolab.aiwolf.server.common.GameConfiguration;
import net.kanolab.aiwolf.server.common.NLPAIWolfConnection;
import net.kanolab.aiwolf.server.common.Option;
import net.kanolab.aiwolf.server.common.ViewerMode;
import net.kanolab.aiwolf.server.game.SynchronousNLPAIWolfGame;
import net.kanolab.aiwolf.server.server.AbstractNLPServer;
import net.kanolab.aiwolf.server.server.NLPCUIGameServer;
import net.kanolab.aiwolf.server.server.NLPGUIGameServer;
import net.kanolab.tminowa.util.Debugger;

/**
 * 1セット内の対戦の管理
 * @author tminowa
 *
 */
public class NLPGameBuilder extends Thread{
	//ログファイル名
	private static final String NORMAL_LOG_FILE_NAME = "%s%s_%03d_%s.log";

	//エラーログファイル名
	private static final String ERROR_LOG_FILE_NAME = "%s%s_%03d_err_%s.log";

	//対戦で使用する能力者
	private static final Role[] USED_ROLES = {
			Role.SEER,
			Role.POSSESSED,
			Role.WEREWOLF
	};

	//iniファイルのオプション
	private GameConfiguration config;

	//サーバに渡すGameSetting
	private GameSetting gameSetting;

	//同一セット内で扱うエージェント一覧（現在、エージェント番号は固定）
	private Map<Agent, NLPAIWolfConnection> agentConnectionMap = new HashMap<>();

	private Debugger debugger = new Debugger();

	/**
	 * GameSettingの作成とConnectionの登録
	 * @param port
	 * @param socketList
	 * @param config
	 */
	public NLPGameBuilder(List<Socket> socketList, GameConfiguration config){

		//順番が固定にならないように念のためシャッフル
		Collections.shuffle(socketList);

		//コネクションとエージェントの紐付け
		Set<Integer> usedNumberSet = new HashSet<>();
		int humanNum = config.getBoolean(Option.PLAY_HUMAN) ? config.getInt(Option.HUMAN_NUMBER) : -1;
		for(int i = 0; i < socketList.size(); i++){
			NLPAIWolfConnection connection = new NLPAIWolfConnection(socketList.get(i), config);
			int agentNum = 1;
			String name = connection.getName();
			if(name != null && name.equals(config.get(Option.HUMAN_NAME)) && humanNum > 0){
				agentNum = humanNum;
			}else{
				while(usedNumberSet.contains(agentNum) || agentNum == humanNum) agentNum++;
			}
			usedNumberSet.add(agentNum);
			Agent agent = Agent.getAgent(agentNum);
			this.agentConnectionMap.put(Agent.getAgent(agentNum), connection);
			connection.setAgent(agent);
		}

		this.config = config;
		this.gameSetting = createGameSetting();
	}

	/**
	 * ソケットを閉じる
	 */
	private void close(){
		for(Entry<Agent, NLPAIWolfConnection> entry : agentConnectionMap.entrySet()){
			try {

				entry.getValue().getSocket().close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	/**
	 * configの内容を反映したGameSettingを取得する
	 * @return
	 */
	private GameSetting createGameSetting(){
		GameSetting gameSetting = GameSetting.getDefaultGame(config.get(Option.BATTLE_AGENT_NUM));
		gameSetting.setTimeLimit(new Integer(config.get(Option.TIMEOUT).toString()));
		gameSetting.setValidateUtterance(false);
		gameSetting.setMaxTalk(config.get(Option.MAX_TALK_NUM));
		gameSetting.setMaxTalkTurn(config.get(Option.MAX_TALK_TURN));
		gameSetting.setMaxWhisper(config.get(Option.MAX_TALK_NUM));
		gameSetting.setMaxWhisperTurn(config.get(Option.MAX_TALK_TURN));
		gameSetting.setTalkOnFirstDay(config.get(Option.TALK_FIRST_DAY));
		return gameSetting;
	}

	/**
	 * 同一セット内のエージェントと役職の組み合わせ一覧を生成する
	 * @return
	 */
	private List<Map<Agent,Role>> createAgentRoleCombinations(){
		List<Map<Agent, Role>> roleList = new ArrayList<>();

		//セット内の人数から組み合わせを生成
		int connectAgentNum = config.get(Option.CONNECT_AGENT_NUM);
		int battleAgentNum = config.get(Option.BATTLE_AGENT_NUM);
		Iterator<int[]> agentCombination = new Combinations(connectAgentNum,battleAgentNum).iterator();
		Iterator<int[]> roleCombination = new Combinations(battleAgentNum, USED_ROLES.length).iterator();

		//nPrがライブラリに用意されていなかったので村人以外の役職について5C3のパターンを取った後にその3名に対して順列を取る
		while(agentCombination.hasNext()){
			if(!roleCombination.hasNext()){
				roleCombination = new Combinations(5,3).iterator();
				continue;
			}

			int[] agentArray = agentCombination.next();
			int totalCount = 0;
			while(roleCombination.hasNext()){
				//村人以外の役職の番号についてその順列を取る
				List<Integer> roleNumList = new ArrayList<>();
				for(int i : roleCombination.next()) roleNumList.add(agentArray[i]);
				PermutationIterator<Integer> nonVillagerPermutationIterator = new PermutationIterator<>(roleNumList);
				while(nonVillagerPermutationIterator.hasNext()){
					List<Integer> giftedAgentId = nonVillagerPermutationIterator.next();
					debugger.print(totalCount++ + " : " + giftedAgentId.toString());
					Map<Agent, Role> roleMap = new HashMap<>();
					for(int i =0; i < giftedAgentId.size(); i++){
						roleMap.put(Agent.getAgent(giftedAgentId.get(i)+1), USED_ROLES[i]);
					}
					for(int i : agentArray){
						if(roleMap.containsKey(Agent.getAgent(i+1))) continue;
						roleMap.put(Agent.getAgent(i+1), Role.VILLAGER);
						debugger.print(i + ", ");
					}
					roleList.add(roleMap);
					debugger.println();
				}
			}
		}

		//デバッグモードの場合、全パターンと各エージェントがそれぞれの役職になった回数をカウントして出力
		if(debugger.isActive()) printCombinationList(roleList);

		return roleList;
	}

	/**
	 * エージェントと役職のマップのリストを引数にとり、その内容とエージェントが各役職に何回なっているかをカウントした結果を出力する
	 * @param roleList
	 */
	private void printCombinationList(List<Map<Agent,Role>> roleList){
		for(int i = 0; i< roleList.size();i++){
			debugger.println(i + " : " + roleList.get(i));
		}
		Map<Agent,Map<Role,Integer>> map = new HashMap<>();
		for(Map<Agent,Role> roleMap : roleList){
			for(Entry<Agent,Role> entry : roleMap.entrySet()){
				Map<Role, Integer> roleNumMap = new HashMap<>();
				if(map.containsKey(entry.getKey())) roleNumMap = map.get(entry.getKey());
				int count = 1;
				if(roleNumMap.containsKey(entry.getValue())) count += roleNumMap.get(entry.getValue());
				roleNumMap.put(entry.getValue(), count);
				map.put(entry.getKey(), roleNumMap);
			}
		}
		for(Entry<Agent,Map<Role,Integer>> entry : map.entrySet()) debugger.println(entry);
	}

	@Override
	public void run(){
		//役職リストの取得
		List<Map<Agent, Role>> agentRoleMapList = createAgentRoleCombinations();

		//ゲームサーバの生成
		AbstractNLPServer nlpServer;
		if(config.getViewerMode() == ViewerMode.CUI) nlpServer = new NLPCUIGameServer(gameSetting, config, agentConnectionMap);
		else nlpServer = new NLPGUIGameServer(gameSetting, config, agentConnectionMap);

		boolean isPriorCombinations = config.get(Option.PRIORITY_COMBINATION);
		int limit = isPriorCombinations ? agentRoleMapList.size() : config.get(Option.GAME_NUM);

		//人間対戦時
		Agent human = null;
		if(config.getBoolean(Option.PLAY_HUMAN)){
			for(Entry<Agent, NLPAIWolfConnection> entry : agentConnectionMap.entrySet()){
				if(nlpServer.getName(entry.getKey()).equals(config.get(Option.HUMAN_NAME))){
					human = entry.getKey();
				}
			}
		}


		//全組み合わせ実行しない場合はランダムにするために役職組み合わせリストをシャッフル
		if(!isPriorCombinations) Collections.shuffle(agentRoleMapList);

		for(int i = 0; i < limit ; i++){
			Map<Agent, Role> agentRoleMap = agentRoleMapList.get(i);
			if(config.getBoolean(Option.PLAY_HUMAN) && agentRoleMap.get(human) != config.getHumanRole())
				continue;
			SynchronousNLPAIWolfGame game = new SynchronousNLPAIWolfGame(gameSetting, nlpServer);
			GameData gameData = new GameData(gameSetting);

			//現在対戦に使用しているエージェントの更新
			nlpServer.updateUsingAgentList(agentRoleMap.keySet());

			//今回マッチングするエージェントのいずれかがロストしているならスキップする
			if(agentRoleMap.keySet().stream().anyMatch(agent -> !agentConnectionMap.get(agent).isAlive()))
				continue;

			//役職の設定
			for(Entry<Agent, Role> entry : agentRoleMap.entrySet()){
				gameData.addAgent(entry.getKey(), Status.ALIVE, entry.getValue());
			}

			game.setRand(new Random());
			String clientNames = String.join("_", nlpServer.getNames());
			String subLogDirName = new SimpleDateFormat("MMddHHmmss").format(Calendar.getInstance().getTime());
			boolean saveLog = config.get(Option.IS_SAVE_LOG);

			try {
				//現在の対戦数を表示
				debugger.println("i = " + i);

				//ロガーを設定
				if(saveLog){
					String path = String.format(NORMAL_LOG_FILE_NAME, config.get(Option.LOG_DIR) ,subLogDirName, i, clientNames);
					System.out.println("log : " + path);
					game.setGameLogger(new FileGameLogger(new File(path)));
				}

				//ゲームの実行
				game.start(gameData);

				//今回のゲームでエラーが発生したエージェントがいた場合はエラーログを出力する
				if(saveLog){
					Set<Entry<Agent,NLPAIWolfConnection>> newLostConnectionSet = agentConnectionMap.entrySet()
							.stream().filter(entry -> entry.getValue().haveNewError())
							.collect(Collectors.toSet());
					String errPath = String.format(ERROR_LOG_FILE_NAME, config.get(Option.LOG_DIR) ,subLogDirName, i, clientNames);
					File errorLogFile = new File(errPath);
					FileGameLogger logger = new FileGameLogger(errorLogFile);
					for(Entry<Agent, NLPAIWolfConnection> entry : newLostConnectionSet){
						entry.getValue().reportError(logger, entry.getKey(), agentRoleMap.get(entry.getKey()));
					}

					//エラー出力がなければエラーログファイルを削除
					if(newLostConnectionSet.isEmpty()) errorLogFile.delete();
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

			//全てのコネクションがロストした場合対戦を終了する
			if(agentConnectionMap.values().stream().allMatch(connection -> !connection.isAlive())) break;
		}
		System.err.println("All Game was finished.");
		close();
	}
}
