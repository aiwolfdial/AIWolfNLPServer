package core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.Request;
import core.model.Talk;
import core.packet.GameSetting;
import core.packet.Packet;
import libs.BidiMap;
import libs.CallableBufferedReader;
import utils.JsonParser;

public abstract class AbstractNLPServer implements GameServer {
	private static final Logger logger = LogManager.getLogger(AbstractNLPServer.class);

	// ゲーム設定
	protected GameSetting gameSetting;

	// 起動時オプション
	protected GameConfiguration config;

	// 1セット内の全エージェントとコネクションのマップ
	protected BidiMap<Agent, NLPAIWolfConnection> allAgentConnectionMap;

	// 現在使用しているエージェントリスト
	protected List<Agent> usingAgentList;

	protected GameData gameData;
	protected Map<Agent, Integer> lastTalkIdxMap = new HashMap<>();
	protected Map<Agent, Integer> lastWhisperIdxMap = new HashMap<>();

	public AbstractNLPServer(GameSetting gameSetting, GameConfiguration config,
			Map<Agent, NLPAIWolfConnection> agentConnectionMap) {
		this.gameSetting = gameSetting;
		this.config = config;
		this.allAgentConnectionMap = new BidiMap<>(agentConnectionMap);
	}

	protected Object catchException(Agent agent, Request request, Exception e) throws LostAgentConnectionException {
		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		if (connection.isAlive()) {
			logger.error(e);
			connection.catchException(agent, e, request);
		}
		if (config.isContinueExceptionAgent())
			return null;
		throw new LostAgentConnectionException(e, agent);
	}

	protected String getResponse(NLPAIWolfConnection connection, ExecutorService pool, Agent agent, Request request,
			long timeout)
			throws InterruptedException, ExecutionException, TimeoutException, IOException {
		send(agent, request);
		CallableBufferedReader task = new CallableBufferedReader(connection.getBufferedReader());
		Future<String> future = pool.submit(task);
		String line = timeout > 0 ? future.get(timeout, TimeUnit.MILLISECONDS) : future.get();
		if (!task.isSuccess()) {
			throw task.getIOException();
		}
		return line;
	}

	protected Object convertRequestData(Request request, String line) {
		if (line != null && line.isEmpty()) {
			line = null;
		}
		// 返す内容の決定
		return switch (request) {
			case TALK, NAME, ROLE, WHISPER -> line;
			case ATTACK, DIVINE, GUARD, VOTE -> JsonParser.decode(line, Agent.class);
			default -> null;
		};
	}

	protected abstract Object request(Agent agent, Request request);

	@Override
	public void close() {
		for (NLPAIWolfConnection connection : allAgentConnectionMap.values()) {
			connection.close();
		}

		allAgentConnectionMap.clear();
	}

	@Override
	public List<Agent> getConnectedAgentList() {
		return usingAgentList;
	}

	protected String getMessage(Agent agent, Request request) {
		Packet packet = null;
		boolean flag = false;

		// 各リクエストに応じたパケットの作成
		switch (request) {
			case DAILY_INITIALIZE:
			case INITIALIZE:
				lastTalkIdxMap.clear();
				lastWhisperIdxMap.clear();
				packet = new Packet(request, gameData.getGameInfo(agent), gameSetting);
				break;
			case NAME:
			case ROLE:
				packet = new Packet(request);
				break;
			case FINISH:
				packet = new Packet(request, gameData.getFinalGameInfo(agent));
				break;
			case DIVINE:
			case GUARD:
			case WHISPER:
				flag = gameData.getExecuted() != null;
				break;
			case VOTE:
				flag = !gameData.getLatestVoteList().isEmpty();
				break;
			case ATTACK:
				flag = !gameData.getLatestAttackVoteList().isEmpty() || gameData.getExecuted() != null;
				break;
			case DAILY_FINISH:
			case TALK:
				break;
		}
		if (flag)
			packet = new Packet(request, gameData.getGameInfo(agent));
		if (packet != null)
			return JsonParser.encode(packet);

		List<Talk> talkList = gameData.getTalkList();
		List<Talk> whisperList = gameData.getGameInfo(agent).getWhisperList();
		talkList = minimize(agent, talkList, lastTalkIdxMap);
		whisperList = minimize(agent, whisperList, lastWhisperIdxMap);
		packet = new Packet(request, talkList, whisperList);
		return JsonParser.encode(packet);
	}

	public String getName(Agent agent) {
		return requestName(agent);
	}

	@Override
	public String requestName(Agent agent) {
		return allAgentConnectionMap.get(agent).getName();
	}

	public Set<String> getNames() {
		Set<String> set = new HashSet<>();
		for (Agent agent : usingAgentList) {
			set.add(getName(agent));
		}
		return new HashSet<>(set);
	}

	protected List<Talk> minimize(Agent agent, List<Talk> list, Map<Agent, Integer> lastIdxMap) {
		int lastIdx = list.size();
		if (lastIdxMap.containsKey(agent) && list.size() >= lastIdxMap.get(agent)) {
			list = list.subList(lastIdxMap.get(agent), list.size());
		}
		lastIdxMap.put(agent, lastIdx);
		return list;
	}

	protected void send(Agent agent, Request request) {
		String message = getMessage(agent, request);

		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		BufferedWriter bw = connection.getBufferedWriter();
		try {
			bw.append(message);
			bw.append("\n");
			bw.flush();
		} catch (IOException e) {
			catchException(agent, request, e);
		}
	}

	public void updateUsingAgentList(Collection<Agent> agentCollection) {
		this.usingAgentList = new ArrayList<>(agentCollection);
	}
}
