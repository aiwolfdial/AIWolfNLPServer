package starter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import common.BRCallable;
import common.GameConfiguration;
import common.NLPAIWolfConnection;
import common.data.Request;
import common.net.DataConverter;
import common.net.Packet;
import common.util.Pair;

/**
 * 継続してクライアントからの接続を受け付ける人狼知能対戦用サーバ
 * 人狼知能プラットフォーム標準のままだとAgent.getAgentが並列対応しておらず、バグるためgetAgentメソッドを並列処理できるようにする必要がある
 */
public class NLPServerStarter {
	private static final Logger logger = LogManager.getLogger(NLPServerStarter.class);

	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";

	private final GameConfiguration config;
	private final Queue<List<Socket>> socketQue = new ArrayDeque<>();

	private final Map<String, Map<String, List<Pair<Long, Socket>>>> waitingSockets = new HashMap<>();

	private boolean isRunning = false;

	public static void main(String[] args) {
		logger.info("NLPServerStarter started.");
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0) {
			configPath = args[0];
		}
		logger.info(String.format("Config file path: %s", configPath));
		NLPServerStarter starter;
		try {
			starter = new NLPServerStarter(configPath);
			starter.start();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public NLPServerStarter() throws Exception {
		this.config = GameConfiguration.load(DEFAULT_CONFIG_PATH);
	}

	public NLPServerStarter(String path) throws Exception {
		this.config = GameConfiguration.load(path);
	}

	private void acceptClients() {
		logger.info("Accept clients.");
		// 必要なエージェント名が設定されているかどうかを確認
		boolean isSetRequiredAgentName = !config.getRequiredAgentName().isEmpty();
		ServerSocket serverSocket = null;
		try {
			// サーバーソケットを指定されたポートで作成
			serverSocket = new ServerSocket(config.getPort());
		} catch (IOException e) {
			logger.error(e);
		}
		Set<Socket> requiredSockets = new HashSet<>();
		isRunning = true;
		while (true) {
			try {
				// クライアントのIPアドレスをキーとし、そのIPアドレスに関連付けられたソケットのリストを値とするマップ
				Map<String, List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();
				// クライアントからの接続を受け入れる
				Socket socket = serverSocket.accept();
				// エントリーソケットマップのキーを生成
				String key = String.valueOf(entrySocketMap.hashCode());
				if (config.isSingleAgentPerIp()) {
					key = socket.getInetAddress().getHostAddress();
				} else if (!waitingSockets.isEmpty()) {
					key = new ArrayList<>(waitingSockets.keySet()).getFirst();
				}
				// エントリーソケットマップを更新
				entrySocketMap = waitingSockets.getOrDefault(key, new HashMap<>());
				waitingSockets.putIfAbsent(key, entrySocketMap);
				logger.debug(String.format("Socket connected: %s", key));
				// ソケットの名前を取得
				String name = getName(socket);
				if (isSetRequiredAgentName && name.contains(config.getRequiredAgentName())) {
					requiredSockets.add(socket);
				}
				// ソケットと現在の時間のペアを作成
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				entrySocketMap.computeIfAbsent(socket.getInetAddress().getHostAddress(), k -> new ArrayList<>())
						.add(pair);
				// 無効な接続を削除
				removeInvalidConnection(config.getIdleConnectionTimeout());
				// アクティブな接続を表示
				printActiveConnection();
				if (isSetRequiredAgentName && requiredSockets.isEmpty()) {
					continue;
				}
				// 接続キューを送信
				sendConnectionQueue(config.getConnectAgentNum(), config.isSingleAgentPerIp(), requiredSockets);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	// サーバーソケットを取得するメソッド
	private Socket getSocketFromIndex(int index, String line, Set<Integer> entryAgentIndex)
			throws UnknownHostException, ConnectException, NoRouteToHostException, IOException {
		logger.info(String.format("Get socket from index: %d", index));
		// 他の組み合わせを続行する設定が有効な場合、ランダムにインデックスを選択
		if (config.isContinueOtherCombinations()) {
			Random rand = new Random();
			do {
				index = rand.nextInt(config.getAllParticipantNum()) + 1;
			} while (entryAgentIndex.contains(index));
			entryAgentIndex.add(index);
		}
		logger.debug(String.format("Index: %d", index));
		// インデックスに基づいてサーバー情報を設定
		return switch (index) {
			case 1 -> getSocket(config.getPlayer1Ip(), config.getPlayer1Port());
			case 2 -> getSocket(config.getPlayer2Ip(), config.getPlayer2Port());
			case 3 -> getSocket(config.getPlayer3Ip(), config.getPlayer3Port());
			case 4 -> getSocket(config.getPlayer4Ip(), config.getPlayer4Port());
			case 5 -> getSocket(config.getPlayer5Ip(), config.getPlayer5Port());
			case 6 -> getSocket(config.getPlayer6Ip(), config.getPlayer6Port());
			case 7 -> getSocket(config.getPlayer7Ip(), config.getPlayer7Port());
			case 8 -> getSocket(config.getPlayer8Ip(), config.getPlayer8Port());
			case 9 -> getSocket(config.getPlayer9Ip(), config.getPlayer9Port());
			case 10 -> getSocket(config.getPlayer10Ip(), config.getPlayer10Port());
			case 10000, 10001, 10002, 10003, 10004 -> getSocket("localhost",
					Integer.parseInt(line.split("\\s")[index % 10000]));
			default -> throw new IllegalArgumentException("Invalid index: " + index);
		};
	}

	private Socket getSocket(String hostname, int port) throws UnknownHostException, IOException {
		Socket sock = new Socket(hostname, port);
		logger.debug(String.format("Socket connected: %s:%d", hostname, port));
		try {
			String name = getName(sock);
			logger.debug(String.format("Socket name: %s", name));
		} catch (Exception e) {
			throw new UnknownHostException();
		}
		return sock;
	}

	private void connectToPlayerServer() {
		// サーバースターターの初期化
		logger.info("Connect to player server.");
		isRunning = true;
		int index = 1;
		String line = "";
		try {
			// サーバーがポートをリッスンするかどうかを確認
			if (config.isListenPort()) {
				logger.debug("Listen port.");
				try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
					Socket socket = serverSocket.accept();
					line = getHostNameAndPort(socket);
					index = 10000;
				}
			}
			// IPアドレスに基づいてエントリーソケットを格納するマップ
			Map<String, List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();
			Set<Integer> entryAgentIndex = new HashSet<>();
			// 指定された数のエージェントに接続
			for (int i = 0; i < config.getConnectAgentNum(); i++) {
				Socket socket = getSocketFromIndex(index, line, entryAgentIndex);
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				String ipAddress = socket.getInetAddress().getHostAddress();
				// IPアドレスに基づいてソケットをマップに追加
				entrySocketMap.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(pair);
				logger.debug(String.format("Socket connected: %s:%d", ipAddress, socket.getPort()));
				// 待機中のソケットマップにエントリーソケットマップを格納
				waitingSockets.put(ipAddress, entrySocketMap);
				index++;
			}
			// アイドルタイムアウトに基づいて無効な接続を削除
			removeInvalidConnection(config.getIdleConnectionTimeout());
			// アクティブな接続を表示
			try {
				printActiveConnection();
			} catch (Exception e) {
				logger.error(e);
				return;
			}
			// 接続キューを送信
			sendConnectionQueue(config.getConnectAgentNum(), config.isSingleAgentPerIp(), new HashSet<>());
		} catch (UnknownHostException e) {
			// 未知のホスト例外を処理
			logger.error(e);
			logger.error(String.format("Player%d host is not found.", index));
		} catch (ConnectException e) {
			// 接続拒否例外を処理
			logger.error(e);
			logger.error(String.format("Player%d connection refused.", index));
		} catch (NoRouteToHostException e) {
			// ホストへのルートがない例外を処理
			logger.error(e);
			logger.error(String.format("Player%d no route to host.", index));
		} catch (IOException e) {
			// その他のIO例外を処理
			logger.error(e);
			logger.error(String.format("Player%d connection failed.", index));
		}
	}

	private String getName(Socket socket) throws IOException, InterruptedException,
			ExecutionException, TimeoutException, SocketException {
		logger.info("Get name.");
		NLPAIWolfConnection connection = new NLPAIWolfConnection(socket, config);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		BufferedWriter bw = connection.getBufferedWriter();
		bw.append(DataConverter.convert(new Packet(Request.NAME)));
		bw.append("\n");
		bw.flush();
		BRCallable task = new BRCallable(connection.getBufferedReader());
		Future<String> future = pool.submit(task);
		String line = config.getResponseTimeout() > 0
				? future.get(config.getResponseTimeout(), TimeUnit.MILLISECONDS)
				: future.get();
		if (!task.isSuccess()) {
			throw task.getIOException();
		}
		pool.shutdown();
		return (line == null || line.isEmpty()) ? null : line;
	}

	private String getHostNameAndPort(Socket socket) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			String line = reader.readLine();
			logger.debug("---");
			logger.debug(line);
			return line;
		} finally {
			socket.close();
		}
	}

	private void removeInvalidConnection(int deleteTime) {
		Map<Pair<String, String>, Pair<Long, Socket>> lostMap = new HashMap<>();
		for (Entry<String, Map<String, List<Pair<Long, Socket>>>> sMapEntry : waitingSockets.entrySet()) {
			for (Entry<String, List<Pair<Long, Socket>>> entry : sMapEntry.getValue().entrySet()) {
				// ロストしているSocket・deleteTimeより長時間対戦が行われずに接続が継続しているSocketを削除リストに追加
				for (Pair<Long, Socket> socketPair : entry.getValue()) {
					long time = System.currentTimeMillis() / 3600000;
					try {
						if (getName(socketPair.getValue()) == null || time - socketPair.getKey() > deleteTime)
							lostMap.put(new Pair<>(sMapEntry.getKey(), entry.getKey()), socketPair);
					} catch (Exception e) {
						lostMap.put(new Pair<>(sMapEntry.getKey(), entry.getKey()), socketPair);
					}
				}
			}
		}
		// 問題のあるコネクションを削除
		for (Entry<Pair<String, String>, Pair<Long, Socket>> lostPair : lostMap.entrySet()) {
			Pair<String, String> keyPair = lostPair.getKey();
			waitingSockets.get(keyPair.getKey()).get(keyPair.getValue()).remove(lostPair.getValue());
			if (waitingSockets.get(keyPair.getKey()).get(keyPair.getValue()).isEmpty())
				waitingSockets.get(keyPair.getKey()).remove(keyPair.getValue());
			if (waitingSockets.get(keyPair.getKey()).isEmpty())
				waitingSockets.remove(keyPair.getKey());
		}
		removeEmptyMap();
	}

	private void removeEmptyMap() {
		waitingSockets.entrySet().removeIf(entry -> {
			if (entry.getValue().isEmpty()) {
				return true;
			} else {
				entry.getValue().entrySet().removeIf(insideEntry -> insideEntry.getValue().isEmpty());
				return false;
			}
		});
	}

	private void printActiveConnection() throws SocketException, IOException,
			InterruptedException, ExecutionException, TimeoutException {
		logger.info("Print active connection.");
		if (waitingSockets.isEmpty()) {
			logger.debug("connecting : connection is empty.");
			return;
		}
		for (Map<String, List<Pair<Long, Socket>>> map : waitingSockets.values()) {
			logger.debug("---");
			for (List<Pair<Long, Socket>> list : map.values()) {
				StringBuilder sb = new StringBuilder("connecting : ");
				for (Pair<Long, Socket> pair : list) {
					sb.append(getName(pair.getValue())).append(", ");
				}
				logger.debug(sb.toString());
			}
			logger.debug("");
		}
	}

	private void sendConnectionQueue(int connectAgentNum, boolean onlyConnection, Set<Socket> essentialSocketSet) {
		logger.info("Send connection queue.");
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> iterator = waitingSockets.entrySet()
				.iterator();
		Set<Socket> set = new HashSet<>(essentialSocketSet);
		while (iterator.hasNext()) {
			Entry<String, Map<String, List<Pair<Long, Socket>>>> entry = iterator.next();
			boolean canStartGame = false;
			for (Entry<String, List<Pair<Long, Socket>>> socketEntry : entry.getValue().entrySet()) {
				List<Socket> l = socketEntry.getValue().stream().map(Pair::getValue).toList();
				if (l.isEmpty())
					continue;
				if (onlyConnection) {
					set.add(l.getFirst());
					continue;
				} else {
					for (Socket s : l) {
						if (set.size() < connectAgentNum)
							set.add(s);
					}
				}
				if ((canStartGame = set.size() == connectAgentNum))
					break;
			}
			if (canStartGame) {
				synchronized (socketQue) {
					socketQue.add(new ArrayList<>(set));
				}
				iterator.remove();
			}
		}
	}

	public void start() {
		logger.info("Start.");
		if (isRunning)
			return;
		// ゲーム開始スレッドの起動
		GameStarter gameStarter = new GameStarter(socketQue, config);
		gameStarter.start();
		if (config.isServer()) {
			// サーバとして待ち受け
			acceptClients();
		} else if (config.isContinueOtherCombinations()) {
			for (int i = 0; i < config.getContinueCombinationsNum(); i++) {
				while (gameStarter.isGameRunning() || gameStarter.isWaitingGame()) {
					// continue; のみとかだと何故か上手く動かない
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						logger.error(e);
					}
				}
				// 2週目以降用
				try {
					logger.debug("Wait 20sec before connect to player server.");
					Thread.sleep(20000);
				} catch (Exception e) {
					logger.error(e);
				}
				connectToPlayerServer();
				// connectToPlayerServerの追加待ち
				try {
					logger.debug("Wait 20sec after connect to player server.");
					Thread.sleep(20000);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		} else if (!config.isListenPort()) {
			connectToPlayerServer();
		} else {
			// port listening
			while (true) {
				connectToPlayerServer();
			}
		}
		logger.info("End.");
	}
}