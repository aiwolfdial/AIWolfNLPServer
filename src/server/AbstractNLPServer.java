package server;

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

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Request;
import org.aiwolf.common.net.DataConverter;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.Packet;
import org.aiwolf.common.net.TalkToSend;
import org.aiwolf.common.util.BidiMap;
import org.aiwolf.server.GameData;
import org.aiwolf.server.LostClientException;
import org.aiwolf.server.net.GameServer;

import common.BRCallable;
import common.GameConfiguration;
import common.NLPAIWolfConnection;

/**
 *
 */
public abstract class AbstractNLPServer implements GameServer {
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

	/**
	 * クライアントが原因の際の例外処理
	 * 
	 * @param agent
	 * @param request
	 * @param e
	 * @throws LostClientException
	 */
	protected Object catchException(Agent agent, Request request, Exception e) throws LostClientException {
		NLPAIWolfConnection connection = allAgentConnectionMap.get(agent);
		if (connection.isAlive()) {
			e.printStackTrace();
			connection.catchException(agent, e, request);
		}
		if (config.isContinueExceptionAgent())
			return null;
		throw new LostClientException("Lost connection with " + agent + "\t" + getName(agent), e, agent);
	}

	/**
	 * clientにリクエストを送信し、結果を受け取る
	 * 
	 * @param connection
	 * @param pool
	 * @param agent
	 * @param request
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws IOException
	 */
	protected String getResponse(NLPAIWolfConnection connection, ExecutorService pool, Agent agent, Request request)
			throws InterruptedException, ExecutionException, TimeoutException, ActionTimeoutException, IOException {
		// clientにrequestを送信し、結果を受け取る
		send(agent, request);

		BRCallable task = new BRCallable(connection.getBufferedReader());
		Future<String> future = pool.submit(task);
		long responseTimeout = config.getResponseTimeout();
		long actionTimeout = config.getActionTimeout();
		String line = null;

		try {
			if (responseTimeout > 0 && actionTimeout > 0) {
				line = future.get(Math.min(responseTimeout, actionTimeout), TimeUnit.MILLISECONDS);
			} else if (responseTimeout > 0) {
				line = future.get(responseTimeout, TimeUnit.MILLISECONDS);
			} else if (actionTimeout > 0) {
				line = future.get(actionTimeout, TimeUnit.MILLISECONDS);
			} else {
				line = future.get();
			}
		} catch (TimeoutException e) {
			if (responseTimeout > 0 && actionTimeout > 0) {
				if (responseTimeout < actionTimeout) {
					throw e;
				} else {
					throw new ActionTimeoutException();
				}
			} else if (responseTimeout > 0) {
				throw e;
			} else {
				throw new ActionTimeoutException();
			}
		}
		if (!task.isSuccess()) {
			throw task.getIOException();
		}
		return line;
	}

	/**
	 * 受け取ったリクエストを変換
	 * 
	 * @param request
	 * @param line
	 * @return
	 */
	protected Object convertRequestData(Request request, String line) {
		if (line != null && line.isEmpty()) {
			line = null;
		}
		// 返す内容の決定
		switch (request) {
			case TALK:
			case NAME:
			case ROLE:
			case WHISPER:
				return line;
			case ATTACK:
			case DIVINE:
			case GUARD:
			case VOTE:
				return DataConverter.getInstance().toAgent(line);
			default:
				return null;
		}
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

	/**
	 * Requestから対応するPacketを作成し、それをStringに変換したものを返す
	 * 
	 * @param agent
	 * @param request
	 * @return
	 */
	protected String getMessage(Agent agent, Request request) {
		Packet packet = null;
		boolean flag = false;

		// 各リクエストに応じたパケットの作成
		switch (request) {
			case DAILY_INITIALIZE:
			case INITIALIZE:
				lastTalkIdxMap.clear();
				lastWhisperIdxMap.clear();
				packet = new Packet(request, gameData.getGameInfoToSend(agent), gameSetting);
				break;
			case NAME:
			case ROLE:
				packet = new Packet(request);
				break;
			case FINISH:
				packet = new Packet(request, gameData.getFinalGameInfoToSend(agent));
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
		}
		if (flag)
			packet = new Packet(request, gameData.getGameInfoToSend(agent));
		if (packet != null)
			return DataConverter.getInstance().convert(packet);

		List<TalkToSend> talkList = gameData.getGameInfoToSend(agent).getTalkList();
		List<TalkToSend> whisperList = gameData.getGameInfoToSend(agent).getWhisperList();
		talkList = minimize(agent, talkList, lastTalkIdxMap);
		whisperList = minimize(agent, whisperList, lastWhisperIdxMap);
		packet = new Packet(request, talkList, whisperList);
		return DataConverter.getInstance().convert(packet);
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
		return new HashSet<String>(set);
	}

	/**
	 * すでに送った発話の削除
	 * 
	 * @param agent
	 * @param list
	 * @param lastIdxMap
	 * @return
	 */
	protected List<TalkToSend> minimize(Agent agent, List<TalkToSend> list, Map<Agent, Integer> lastIdxMap) {
		int lastIdx = list.size();
		if (lastIdxMap.containsKey(agent) && list.size() >= lastIdxMap.get(agent)) {
			list = list.subList(lastIdxMap.get(agent), list.size());
		}
		lastIdxMap.put(agent, lastIdx);
		return list;
	}

	/**
	 * メッセージの送信
	 * 
	 * @param agent
	 * @param request
	 */
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

	/**
	 * 現在対戦に使用しているエージェント一覧の更新
	 * 
	 * @param agentList
	 */
	public void updateUsingAgentList(Collection<Agent> agentCollection) {
		this.usingAgentList = new ArrayList<>(agentCollection);
	}
}
