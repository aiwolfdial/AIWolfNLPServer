package common.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import common.AIWolfRuntimeException;
import common.data.Player;
import common.data.Role;
import common.data.Talk;

public class TcpClient implements Runnable, GameClient {
	String host;
	int port;

	Socket socket;

	Player player;
	Role requestRole;

	boolean isRunning;
	boolean isConnecting;

	GameInfo lastGameInfo;

	String playerName;

	public TcpClient(String host, int port) {
		this.host = host;
		this.port = port;
		isRunning = false;
	}

	public TcpClient(String host, int port, Role requestRole) {
		this.host = host;
		this.port = port;
		this.requestRole = requestRole;
		isRunning = false;
	}

	public boolean connect(Player player) {
		this.player = player;
		try {
			// ソケットを作成してサーバに接続する。
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port));
			isConnecting = true;

			Thread th = new Thread(this);
			th.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			isConnecting = false;
			return false;
		}
	}

	@Override
	public void run() {
		try {
			// サーバと接続されたソケットを利用して処理を行う。
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				Packet packet = DataConverter.toPacket(line);

				Object obj = receive(packet);
				if (packet.getRequest().hasReturn()) {
					if (obj == null) {
						bw.append("\n");
					} else if (obj instanceof String) {
						bw.append(String.valueOf(obj)).append("\n");
					} else {
						bw.append(DataConverter.convert(obj)).append("\n");
					}
					bw.flush();
				}
			}
		} catch (IOException e) {
			if (isConnecting) {
				isConnecting = false;
				if (isRunning) {
					isRunning = false;
					throw new AIWolfRuntimeException(e);
				}
			}
		}
	}

	@Override
	public Object receive(Packet packet) {
		GameInfo gameInfo = lastGameInfo;
		GameSetting gameSetting = packet.getGameSetting();

		if (packet.getGameInfo() != null) {
			gameInfo = packet.getGameInfo().toGameInfo();
			lastGameInfo = gameInfo;
		}

		if (packet.getTalkHistory() != null) {
			Talk lastTalk = null;
			if (gameInfo.getTalkList() != null && !gameInfo.getTalkList().isEmpty()) {
				lastTalk = gameInfo.getTalkList().getLast();
			}
			for (TalkToSend talk : packet.getTalkHistory()) {
				if (isAfter(talk, lastTalk)) {
					gameInfo.talkList.add(talk.toTalk());
				}
			}
		}

		if (packet.getWhisperHistory() != null) {
			Talk lastWhisper = null;
			if (gameInfo.getWhisperList() != null && !gameInfo.getWhisperList().isEmpty()) {
				lastWhisper = gameInfo.getWhisperList().getLast();
			}
			for (TalkToSend whisper : packet.getWhisperHistory()) {
				if (isAfter(whisper, lastWhisper)) {
					gameInfo.whisperList.add(whisper.toTalk());
				}
			}
		}

		Object returnObject = null;
		switch (packet.getRequest()) {
			case INITIALIZE:
				isRunning = true;
				player.initialize(gameInfo, gameSetting);
				break;
			case DAILY_INITIALIZE:
				player.update(gameInfo);
				player.dayStart();
				break;
			case DAILY_FINISH:
				player.update(gameInfo);
				break;
			case NAME:
				if (playerName == null) {
					returnObject = player.getName();
					if (returnObject == null) {
						returnObject = player.getClass().getName();
					}
				} else {
					returnObject = playerName;
				}
				break;
			case ROLE:
				if (requestRole != null) {
					returnObject = requestRole.toString();
				} else {
					returnObject = "none";
				}
				break;
			case ATTACK:
				player.update(gameInfo);
				returnObject = player.attack();
				break;
			case TALK:
				player.update(gameInfo);
				returnObject = player.talk();
				if (returnObject == null) {
					returnObject = Talk.SKIP;
				}
				break;
			case WHISPER:
				player.update(gameInfo);
				returnObject = player.whisper();
				if (returnObject == null) {
					returnObject = Talk.SKIP;
				}
				break;
			case DIVINE:
				player.update(gameInfo);
				returnObject = player.divine();
				break;
			case GUARD:
				player.update(gameInfo);
				returnObject = player.guard();
				break;
			case VOTE:
				player.update(gameInfo);
				returnObject = player.vote();
				break;
			case FINISH:
				player.update(gameInfo);
				finish();
			default:
				break;
		}
		return returnObject;
	}

	private boolean isAfter(TalkToSend talk, Talk lastTalk) {
		if (lastTalk != null) {
			if (talk.getDay() < lastTalk.getDay()) {
				return false;
			}
			return talk.getDay() != lastTalk.getDay() || talk.getIdx() > lastTalk.getIdx();
		}
		return true;
	}

	protected void finish() {
		player.finish();
		isRunning = false;
	}

	public void setName(String name) {
		this.playerName = name;
	}
}