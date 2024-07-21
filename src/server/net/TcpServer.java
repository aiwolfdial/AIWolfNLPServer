package server.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import common.BRCallable;
import common.data.Agent;
import common.data.Request;
import common.data.Role;
import common.data.Talk;
import common.net.GameSetting;
import common.net.Packet;
import common.util.BidiMap;
import server.GameData;
import server.exception.IllegalPlayerNumException;
import server.exception.LostClientException;
import utility.parser.JSONParser;

public class TcpServer implements GameServer {
	private static final Logger logger = LogManager.getLogger(TcpServer.class);

	protected int port;
	protected int limit;

	protected boolean isWaitForClient;

	protected BidiMap<Socket, Agent> socketAgentMap;

	protected GameData gameData;

	protected GameSetting gameSetting;

	protected Map<Agent, String> nameMap;

	protected Map<Agent, Integer> lastTalkIdxMap;
	protected Map<Agent, Integer> lastWhisperIdxMap;

	protected ServerSocket serverSocket;

	protected int responseTimeout = 1000;

	public TcpServer(int port, int limit, GameSetting gameSetting) {
		this.gameSetting = gameSetting;
		this.port = port;
		this.limit = limit;
		if (gameSetting.getResponseTimeout() != -1) {
			this.responseTimeout = gameSetting.getResponseTimeout();
		}

		socketAgentMap = new BidiMap<>();
		nameMap = new HashMap<>();
		lastTalkIdxMap = new HashMap<>();
		lastWhisperIdxMap = new HashMap<>();
	}

	public void waitForConnection() throws IOException, SocketTimeoutException {
		for (Socket sock : socketAgentMap.keySet()) {
			if (sock != null && sock.isConnected()) {
				sock.close();
			}
		}

		socketAgentMap.clear();
		nameMap.clear();

		logger.info("Waiting for connection...");

		serverSocket = new ServerSocket(port);

		isWaitForClient = true;

		List<Agent> shuffledAgentList = new ArrayList<>();
		for (int i = 1; i <= limit; i++) {
			shuffledAgentList.add(Agent.getAgent(i));
		}
		Collections.shuffle(shuffledAgentList);

		while (socketAgentMap.size() < limit && isWaitForClient) {
			Socket socket = serverSocket.accept();

			synchronized (socketAgentMap) {
				Agent agent = null;
				for (int i = 0; i < limit; i++) {
					if (!socketAgentMap.containsValue(shuffledAgentList.get(i))) {
						agent = shuffledAgentList.get(i);
						break;
					}
				}
				if (agent == null) {
					throw new IllegalPlayerNumException("Fail to create agent");
				}
				socketAgentMap.put(socket, agent);
				String name = requestName(agent);
				nameMap.put(agent, name);

				// socket, agent, name をセットに追加していた
			}
		}
		isWaitForClient = false;
		serverSocket.close();
	}

	@Override
	public List<Agent> getConnectedAgentList() {
		synchronized (socketAgentMap) {
			return new ArrayList<>(socketAgentMap.values());
		}
	}

	// :TODO Whisperの際に毎回GameInfoを毎回送ってしまう問題の解決．必要が無ければGameInfoを送らなくする
	protected void send(Agent agent, Request request) {
		try {
			String message;
			if (request == Request.INITIALIZE) {
				lastTalkIdxMap.clear();
				lastWhisperIdxMap.clear();
				Packet packet = new Packet(request, gameData.getGameInfoToSend(agent), gameSetting);
				message = JSONParser.encode(packet);
			} else if (request == Request.DAILY_INITIALIZE) {
				lastTalkIdxMap.clear();
				lastWhisperIdxMap.clear();
				Packet packet = new Packet(request, gameData.getGameInfoToSend(agent));
				message = JSONParser.encode(packet);
			} else if (request == Request.NAME || request == Request.ROLE) {
				Packet packet = new Packet(request);
				message = JSONParser.encode(packet);
			} else if (request != Request.FINISH) {
				// Packet packet = new Packet(request, gameData.getGameInfoToSend(agent),
				// gameSetting);
				// message = DataConverter.convert(packet);
				if (request == Request.VOTE && !gameData.getLatestVoteList().isEmpty()) {
					// 追放再投票の場合，latestVoteListで直前の投票状況を知らせるためGameInfo入りのパケットにする
					Packet packet = new Packet(request, gameData.getGameInfoToSend(agent));
					message = JSONParser.encode(packet);
				} else if (request == Request.ATTACK && !gameData.getLatestAttackVoteList().isEmpty()) {
					// 襲撃再投票の場合，latestAttackVoteListで直前の投票状況を知らせるためGameInfo入りのパケットにする
					Packet packet = new Packet(request, gameData.getGameInfoToSend(agent));
					message = JSONParser.encode(packet);
				} else if (gameData.getExecuted() != null
						&& (request == Request.DIVINE || request == Request.GUARD || request == Request.WHISPER
								|| request == Request.ATTACK)) {
					// 追放後の各リクエストではlatestExecutedAgentで追放者を知らせるためGameInfo入りのパケットにする
					Packet packet = new Packet(request, gameData.getGameInfoToSend(agent));
					message = JSONParser.encode(packet);
				} else {
					List<Talk> talkList = gameData.getGameInfoToSend(agent).getTalkList();
					List<Talk> whisperList = gameData.getGameInfoToSend(agent).getWhisperList();

					talkList = minimize(agent, talkList, lastTalkIdxMap);
					whisperList = minimize(agent, whisperList, lastWhisperIdxMap);

					Packet packet = new Packet(request, talkList, whisperList);
					message = JSONParser.encode(packet);
				}
			} else {
				Packet packet = new Packet(request, gameData.getFinalGameInfoToSend(agent));
				message = JSONParser.encode(packet);
			}

			Socket sock = socketAgentMap.getKey(agent);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			bw.append(message);
			bw.append("\n");
			bw.flush();
		} catch (IOException e) {
			throw new LostClientException(e, agent);
		}
	}

	protected List<Talk> minimize(Agent agent, List<Talk> list, Map<Agent, Integer> lastIdxMap) {
		int lastIdx = list.size();
		if (lastIdxMap.containsKey(agent) && list.size() >= lastIdxMap.get(agent)) {
			list = list.subList(lastIdxMap.get(agent), list.size());
		}
		lastIdxMap.put(agent, lastIdx);
		return list;
	}

	protected Object request(Agent agent, Request request) {
		try {
			Socket sock = socketAgentMap.getKey(agent);
			final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			send(agent, request);

			String line = null;
			ExecutorService pool = Executors.newSingleThreadExecutor();
			BRCallable task = new BRCallable(br);
			try {
				Future<String> future = pool.submit(task);
				try {
					line = future.get(responseTimeout, TimeUnit.MILLISECONDS); // 1秒でタイムアウト
				} catch (TimeoutException e) {
					sock.close();
					throw e;
				}
			} finally {
				pool.shutdownNow();
			}

			if (!task.isSuccess()) {
				throw task.getIOException();
			}

			if (line != null && line.isEmpty()) {
				line = null;
			}
			if (request == Request.NAME || request == Request.ROLE) {
				return line;
			} else if (request == Request.TALK || request == Request.WHISPER) {
				return line;
			} else if (request == Request.ATTACK || request == Request.DIVINE || request == Request.GUARD
					|| request == Request.VOTE) {
				Agent target = JSONParser.decode(line, Agent.class);
				if (gameData.contains(target)) {
					return target;
				} else {
					return null;
				}
			} else {
				return null;
			}

		} catch (InterruptedException | ExecutionException | IOException e) {
			throw new LostClientException("Lost connection with " + agent + "\t" + nameMap.get(agent), e, agent);
		} catch (TimeoutException e) {
			throw new LostClientException(String.format("Timeout %s(%s) %s", agent,
					nameMap.get(agent), request), e, agent);
		}
	}

	@Override
	public void init(Agent agent) {
		send(agent, Request.INITIALIZE);
	}

	@Override
	public void dayStart(Agent agent) {
		send(agent, Request.DAILY_INITIALIZE);
	}

	@Override
	public void dayFinish(Agent agent) {
		send(agent, Request.DAILY_FINISH);
	}

	@Override
	public String requestName(Agent agent) {
		if (nameMap.containsKey(agent)) {
			return nameMap.get(agent);
		} else {
			String name = (String) request(agent, Request.NAME);
			nameMap.put(agent, name);
			return name;
		}
	}

	@Override
	public Role requestRequestRole(Agent agent) {
		String roleString = (String) request(agent, Request.ROLE);
		try {
			return Role.valueOf(roleString);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String requestTalk(Agent agent) {
		return (String) request(agent, Request.TALK);
	}

	@Override
	public String requestWhisper(Agent agent) {
		return (String) request(agent, Request.WHISPER);
	}

	@Override
	public Agent requestVote(Agent agent) {
		return (Agent) request(agent, Request.VOTE);
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
	public Agent requestAttackTarget(Agent agent) {
		return (Agent) request(agent, Request.ATTACK);
	}

	@Override
	public void finish(Agent agent) {
		send(agent, Request.FINISH);
	}

	@Override
	public void setGameData(GameData gameData) {
		this.gameData = gameData;
	}

	@Override
	public void setGameSetting(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}

	@Override
	public void close() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (Socket socket : socketAgentMap.keySet()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socketAgentMap.clear();
	}
}
