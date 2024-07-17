package server;

import java.io.IOException;
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
import common.net.GameSetting;

/**
 * mamo: https://github.com/aiwolf/AIWolfServer/blob/0.6.x/src/org/aiwolf/server/net/TcpipServer.java#L418
 * 	https://github.com/aiwolf/AIWolfServer/blob/0.6.x/src/org/aiwolf/server/AIWolfGame.java#L614
 */

/**
 * ゲームの進行
 * 
 * @author tminowa
 *
 */
public class NLPCUIGameServer extends AbstractNLPServer {

	public NLPCUIGameServer(GameSetting gameSetting, GameConfiguration config,
			Map<Agent, NLPAIWolfConnection> agentConnectionMap) {
		super(gameSetting, config, agentConnectionMap);
	}

	@Override
	public void dayFinish(Agent agent) {
		send(agent, Request.DAILY_FINISH);
	}

	@Override
	public void dayStart(Agent agent) {
		send(agent, Request.DAILY_INITIALIZE);
	}

	@Override
	public void finish(Agent agent) {
		send(agent, Request.FINISH);
	}

	@Override
	public void init(Agent agent) {
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
						// 名前が一致するかを確認
						String expectedName = agent.getAgentName();
						if (expectedName.equals(line)) {
							return null; // 一致した場合に何も返さない
						} else {
							return catchException(agent, request, new IOException("Name mismatch"));
						}
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
