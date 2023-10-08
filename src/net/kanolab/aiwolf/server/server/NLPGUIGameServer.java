package net.kanolab.aiwolf.server.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Request;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.GameData;

import net.kanolab.aiwolf.server.common.GameConfiguration;
import net.kanolab.aiwolf.server.common.NLPAIWolfConnection;
import net.kanolab.aiwolf.server.common.Option;
import net.kanolab.aiwolf.server.gui.GUIConnector;
import net.kanolab.aiwolf.server.gui.TextConverter;

public class NLPGUIGameServer extends AbstractNLPServer{
	//GUI接続用コネクタ
	private GUIConnector connector;

	//ソケット通信で送信する内容の変換
	private TextConverter converter;

	private boolean isAlreadySendDayStartInfo;
	private boolean isAlreadySendGameFinishInfo;

	public NLPGUIGameServer(GameSetting gameSetting, GameConfiguration config, Map<Agent, NLPAIWolfConnection> agentConnectionMap){
		super(gameSetting, config, agentConnectionMap);
		this.connector = new GUIConnector(config.get(Option.GUI_ADDRESS), config.get(Option.GUI_PORT));
		this.converter = new TextConverter();
	}

	@Override
	public void dayFinish(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		isAlreadySendDayStartInfo = false;
		send(agent, Request.DAILY_FINISH);
	}

	@Override
	public void dayStart(Agent agent) {
		GameInfo gameInfo = gameData.getGameInfo();
		int day = gameData.getGameInfo().getDay();

		//まだday日の情報を送っていなければ送信する
		if(!isAlreadySendDayStartInfo){

			// 昨晩死んだエージェントのリスト
			List<Agent> lastDeadAgentList = gameInfo.getLastDeadAgentList();
			Agent executedAgent = gameInfo.getExecutedAgent();
			if (executedAgent != null) lastDeadAgentList.add(executedAgent);


			// 挨拶ターンではなかった場合
			if (day > 0) {
				// 投票のリスト
				for (Vote vote : gameInfo.getVoteList()) {
					if (vote.getDay() == day - 1) connector.send(converter.vote(vote));
				}
			}

			// 1日進んだことを送信する
			connector.send(converter.nextDay(day));

			// 死んだエージェントのリストを送信
			connector.send(converter.dead(lastDeadAgentList));

			isAlreadySendDayStartInfo = true;
		}

		// TODO 自動生成されたメソッド・スタブ
		send(agent, Request.DAILY_INITIALIZE);
	}

	@Override
	public void finish(Agent agent) {
		GameInfo gameInfo = gameData.getGameInfo();
		int day = gameInfo.getDay();

		if (!isAlreadySendGameFinishInfo){
			Map<Agent, Status> statusMap = gameInfo.getStatusMap();

			// 投票のリスト
			List<Vote> list = gameInfo.getVoteList();
			for (Vote vote : list) {
				if (vote.getDay() == day - 1) connector.send(converter.vote(vote));
			}

			int aliveCount = 0;
			for (Agent a: gameInfo.getAgentList()){
				if(gameInfo.getStatusMap().get(a) == Status.DEAD) continue;
				if(gameInfo.getRoleMap().get(a).getSpecies() == Species.HUMAN) aliveCount++;
				else aliveCount--;
			}

			// 勝敗を送る
			String message = converter.result(aliveCount > 0 ? "VILL" : "WOLF");
			connector.send(message);

			isAlreadySendGameFinishInfo = true;
		}
		// TODO 自動生成されたメソッド・スタブ
		send(agent, Request.FINISH);
	}

	@Override
	public void init(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		isAlreadySendDayStartInfo = false;
		isAlreadySendGameFinishInfo = false;
		send(agent, Request.INITIALIZE);
	}

	@Override
	protected Object request(Agent agent, Request request) {
		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		ExecutorService pool = Executors.newSingleThreadExecutor();

		try{
			String line = getResponse(connection, pool, agent, request);
			if(request == Request.TALK){
				connector.send(converter.talk(agent.getAgentIdx(), line));
				line = line.replaceAll("%\\d%", "");
			}
			return convertRequestData(request, line);
		}catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
			return catchException(agent,request,e);
		}finally{
			pool.shutdownNow();
		}
	}

	@Override
	public Agent requestAttackTarget(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (Agent) request(agent, Request.ATTACK);
	}

	@Override
	public Agent requestDivineTarget(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (Agent) request(agent, Request.DIVINE);
	}

	@Override
	public Agent requestGuardTarget(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (Agent) request(agent, Request.GUARD);
	}

	@Override
	public Role requestRequestRole(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		String roleString = (String) request(agent, Request.ROLE);
		try {
			return roleString == null ? null : Role.valueOf(roleString);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String requestTalk(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (String) request(agent, Request.TALK);
	}

	@Override
	public Agent requestVote(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (Agent) request(agent, Request.VOTE);
	}

	@Override
	public String requestWhisper(Agent agent) {
		// TODO 自動生成されたメソッド・スタブ
		return (String) request(agent, Request.WHISPER);
	}

	@Override
	public void setGameData(GameData gameData) {
		// TODO 自動生成されたメソッド・スタブ
		this.gameData = gameData;
	}

	@Override
	public void setGameSetting(GameSetting gameSetting) {
		// TODO 自動生成されたメソッド・スタブ
		this.gameSetting = gameSetting;
	}
}
