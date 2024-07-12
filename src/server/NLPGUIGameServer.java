package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import common.GameConfiguration;
import common.NLPAIWolfConnection;
import common.data.Agent;
import common.data.Request;
import common.data.Role;
import common.data.Species;
import common.data.Status;
import common.data.Vote;
import common.net.GameInfo;
import common.net.GameSetting;
import gui.GUIConnector;
import gui.TextConverter;

public class NLPGUIGameServer extends AbstractNLPServer {
	// GUI接続用コネクタ
	private final GUIConnector connector;

	// ソケット通信で送信する内容の変換
	private final TextConverter converter;

	private boolean isAlreadySendDayStartInfo;
	private boolean isAlreadySendGameFinishInfo;

	public NLPGUIGameServer(GameSetting gameSetting, GameConfiguration config,
			Map<Agent, NLPAIWolfConnection> agentConnectionMap) {
		super(gameSetting, config, agentConnectionMap);
		this.connector = new GUIConnector(config.getGuiIp(), config.getGuiPort());
		this.converter = new TextConverter();
	}

	@Override
	public void dayFinish(Agent agent) {
		isAlreadySendDayStartInfo = false;
		send(agent, Request.DAILY_FINISH);
	}

	@Override
	public void dayStart(Agent agent) {
		GameInfo gameInfo = gameData.getGameInfo();
		int day = gameData.getGameInfo().getDay();

		// まだday日の情報を送っていなければ送信する
		if (!isAlreadySendDayStartInfo) {

			// 昨晩死んだエージェントのリスト
			List<Agent> lastDeadAgentList = gameInfo.getLastDeadAgentList();
			Agent executedAgent = gameInfo.getExecutedAgent();
			if (executedAgent != null)
				lastDeadAgentList.add(executedAgent);

			// 挨拶ターンではなかった場合
			if (day > 0) {
				// 投票のリスト
				for (Vote vote : gameInfo.getVoteList()) {
					if (vote.getDay() == day - 1)
						connector.send(converter.vote(vote));
				}
			}

			// 1日進んだことを送信する
			connector.send(converter.nextDay(day));

			// 死んだエージェントのリストを送信
			connector.send(converter.dead(lastDeadAgentList));

			isAlreadySendDayStartInfo = true;
		}

		send(agent, Request.DAILY_INITIALIZE);
	}

	@Override
	public void finish(Agent agent) {
		GameInfo gameInfo = gameData.getGameInfo();
		int day = gameInfo.getDay();

		if (!isAlreadySendGameFinishInfo) {
			// 投票のリスト
			List<Vote> list = gameInfo.getVoteList();
			for (Vote vote : list) {
				if (vote.getDay() == day - 1)
					connector.send(converter.vote(vote));
			}

			int aliveCount = 0;
			for (Agent a : gameInfo.getAgentList()) {
				if (gameInfo.getStatusMap().get(a) == Status.DEAD)
					continue;
				if (gameInfo.getRoleMap().get(a).getSpecies() == Species.HUMAN)
					aliveCount++;
				else
					aliveCount--;
			}

			// 勝敗を送る
			String message = converter.result(aliveCount > 0 ? "VILL" : "WOLF");
			connector.send(message);

			isAlreadySendGameFinishInfo = true;
		}
		send(agent, Request.FINISH);
	}

	@Override
	public void init(Agent agent) {
		isAlreadySendDayStartInfo = false;
		isAlreadySendGameFinishInfo = false;
		send(agent, Request.INITIALIZE);
	}

	@Override
	protected Object request(Agent agent, Request request) {
		// ゲーム設定からレスポンスとアクションのタイムアウトを取得
		long responseTimeout = gameSetting.getResponseTimeout();
		long actionTimeout = gameSetting.getActionTimeout();
		// エージェントに関連付けられた接続を取得
		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try {
			try {
				// 短いタイムアウト内にレスポンスを取得
				String line = getResponse(connection, pool, agent, request, Math.min(responseTimeout, actionTimeout));
				return convertRequestData(request, line);
			} catch (TimeoutException e) {
				// アクションのタイムアウトを超えた場合
				if (responseTimeout > actionTimeout) {
					try {
						// 接続が切れたかどうかを確認
						String line = getResponse(connection, pool, agent, Request.NAME,
								responseTimeout - actionTimeout);
						return convertRequestData(request, line);
					} catch (TimeoutException e1) {
						// 再度タイムアウトした場合
						return catchException(agent, request, e1);
					}
				} else {
					// すでにレスポンスのタイムアウトを超えた場合
					return catchException(agent, request, e);
				}
			}
		} catch (IOException | InterruptedException | ExecutionException e) {
			// リクエスト中に発生する他の例外を処理
			return catchException(agent, request, e);
		} finally {
			pool.shutdownNow();
		}
	}

	@Override
	public Agent requestAttackTarget(Agent agent) {
		return (Agent) request(agent, Request.ATTACK);
	}

	@Override
	public Agent requestDivineTarget(Agent agent) {
		return (Agent) request(agent, Request.DIVINE);
	}

	@Override
	public Agent requestGuardTarget(Agent agent) {
		return (Agent) request(agent, Request.GUARD);
	}

	@Override
	public Role requestRequestRole(Agent agent) {
		String roleString = (String) request(agent, Request.ROLE);
		try {
			return roleString == null ? null : Role.valueOf(roleString);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String requestTalk(Agent agent) {
		return (String) request(agent, Request.TALK);
	}

	@Override
	public Agent requestVote(Agent agent) {
		return (Agent) request(agent, Request.VOTE);
	}

	@Override
	public String requestWhisper(Agent agent) {
		return (String) request(agent, Request.WHISPER);
	}

	@Override
	public void setGameData(GameData gameData) {
		this.gameData = gameData;
	}

	@Override
	public void setGameSetting(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}
}
