package net.kanolab.aiwolf.server.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Request;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.GameData;

import net.kanolab.aiwolf.server.common.GameConfiguration;
import net.kanolab.aiwolf.server.common.NLPAIWolfConnection;

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

	protected Object request(Agent agent, Request request) {
		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		ExecutorService pool = Executors.newSingleThreadExecutor();

		try {
			String line = getResponse(connection, pool, agent, request);
			return convertRequestData(request, line);
		} catch (ActionTimeoutException e) {
			return convertRequestData(Request.NAME, null);
		} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
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
