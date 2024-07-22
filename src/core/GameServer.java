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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.GameSetting;
import core.model.Packet;
import core.model.Request;
import core.model.Role;
import core.model.Talk;
import libs.CallableBufferedReader;
import utils.JsonParser;

public class GameServer {
	private static final Logger logger = LogManager.getLogger(GameServer.class);

	protected GameSetting gameSetting;
	protected Config config;

	protected Set<Connection> connections = new HashSet<>();
	protected List<Agent> agents;

	protected GameData gameData;
	protected Map<Agent, Integer> lastTalkIdxMap = new HashMap<>();
	protected Map<Agent, Integer> lastWhisperIdxMap = new HashMap<>();

	public GameServer(GameSetting gameSetting, Config config,
			Set<Connection> connections) {
		this.gameSetting = gameSetting;
		this.config = config;
		this.connections = connections;
	}

	public void setGameSetting(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}

	public void setGameData(GameData gameData) {
		this.gameData = gameData;
	}

	private Connection getConnection(Agent agent) {
		return connections.stream().filter(c -> c.getAgent().equals(agent)).findFirst().orElse(null);
	}

	protected Object throwException(Agent agent, Request request, Exception e) throws LostAgentConnectionException {
		Connection connection = getConnection(agent);
		if (connection.isAlive()) {
			logger.error("Exception", e);
			connection.throwException(agent, e, request);
		}
		if (config.isContinueExceptionAgent())
			return null;
		throw new LostAgentConnectionException(e, agent);
	}

	protected String getResponse(Connection connection, ExecutorService pool, Agent agent, Request request,
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

	protected Object request(Agent agent, Request request) {
		// ゲーム設定からレスポンスとアクションのタイムアウトを取得
		long responseTimeout = gameSetting.responseTimeout();
		long actionTimeout = gameSetting.actionTimeout();
		// エージェントに関連付けられた接続を取得
		Connection connection = getConnection(agent);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try {
			try {
				// 短いタイムアウト内にレスポンスを取得
				String line = getResponse(connection, pool, agent, request, Math.min(responseTimeout, actionTimeout));
				if (line != null && line.equals(Talk.FORCE_SKIP)) {
					line = Talk.SKIP;
				}
				return convertRequestData(request, line);
			} catch (TimeoutException e) {
				// アクションのタイムアウトを超えた場合
				if (responseTimeout > actionTimeout) {
					try {
						// 接続が切れたかどうかを確認
						String line = getResponse(connection, pool, agent, Request.NAME,
								responseTimeout - actionTimeout);
						// 名前が一致するかを確認
						String expectedName = agent.agentName;
						if (expectedName.equals(line)) {
							return convertRequestData(Request.TALK, Talk.FORCE_SKIP);
						} else {
							return throwException(agent, request, new IOException("Name mismatch"));
						}
					} catch (TimeoutException e1) {
						// 再度タイムアウトした場合
						return throwException(agent, request, e1);
					}
				} else {
					// すでにレスポンスのタイムアウトを超えた場合
					return throwException(agent, request, e);
				}
			}
		} catch (IOException | InterruptedException | ExecutionException e) {
			// リクエスト中に発生する他の例外を処理
			return throwException(agent, request, e);
		} finally {
			pool.shutdownNow();
		}
	}

	public Agent requestAttackTarget(Agent agent) {
		return (Agent) request(agent, Request.ATTACK);
	}

	public Agent requestDivineTarget(Agent agent) {
		return (Agent) request(agent, Request.DIVINE);
	}

	public Agent requestGuardTarget(Agent agent) {
		return (Agent) request(agent, Request.GUARD);
	}

	public Role requestRequestRole(Agent agent) {
		String roleString = (String) request(agent, Request.ROLE);
		try {
			return roleString == null ? null : Role.valueOf(roleString);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String requestTalk(Agent agent) {
		return (String) request(agent, Request.TALK);
	}

	public Agent requestVote(Agent agent) {
		return (Agent) request(agent, Request.VOTE);
	}

	public String requestWhisper(Agent agent) {
		return (String) request(agent, Request.WHISPER);
	}

	public void close() {
		for (Connection connection : connections) {
			connection.close();
		}
		connections.clear();
	}

	public List<Agent> getAgents() {
		return agents;
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
		List<Talk> whisperList = gameData.getGameInfo(agent).whisperList;
		talkList = minimize(agent, talkList, lastTalkIdxMap);
		whisperList = minimize(agent, whisperList, lastWhisperIdxMap);
		packet = new Packet(request, talkList, whisperList);
		return JsonParser.encode(packet);
	}

	public String getName(Agent agent) {
		return getConnection(agent).getName();
	}

	public Set<String> getNames() {
		Set<String> set = new HashSet<>();
		for (Agent agent : agents) {
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

		Connection connection = getConnection(agent);
		BufferedWriter bw = connection.getBufferedWriter();
		try {
			bw.append(message);
			bw.append("\n");
			bw.flush();
		} catch (IOException e) {
			throwException(agent, request, e);
		}
	}

	public void setAgents(Collection<Agent> agents) {
		this.agents = new ArrayList<>(agents);
	}

	public void dayFinish(Agent agent) {
		send(agent, Request.DAILY_FINISH);
	}

	public void dayStart(Agent agent) {
		send(agent, Request.DAILY_INITIALIZE);
	}

	public void finish(Agent agent) {
		send(agent, Request.FINISH);
	}

	public void init(Agent agent) {
		send(agent, Request.INITIALIZE);
	}
}
