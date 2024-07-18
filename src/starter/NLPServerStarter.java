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
import java.util.stream.Collectors;

import common.BRCallable;
import common.GameConfiguration;
import common.NLPAIWolfConnection;
import common.data.Request;
import common.net.DataConverter;
import common.net.Packet;
import common.util.Pair;
import server.bin.ServerStarter;

/**
 * aiwolf server
 * https://github.com/aiwolf/AIWolfServer/blob/0.6.x/src/org/aiwolf/server/net/TcpipServer.java
 * 
 * 
 * aiwolf client
 * https://github.com/aiwolf/AIWolfCommon/blob/0.6.x/src/org/aiwolf/common/net/TcpipClient.java
 */

/**
 * 継続してクライアントからの接続を受け付ける人狼知能対戦用サーバ
 * 人狼知能プラットフォーム標準のままだとAgent.getAgentが並列対応しておらず、バグるためgetAgentメソッドを並列処理できるようにする必要がある
 */
public class NLPServerStarter extends ServerStarter {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";

	private final GameConfiguration config;
	private final Queue<List<Socket>> socketQue = new ArrayDeque<>();
	private GameStarter gameStarter;

	private final Map<String, Map<String, List<Pair<Long, Socket>>>> waitingSockets = new HashMap<>();

	private boolean isRunning = false;

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0)
			configPath = args[0];
		NLPServerStarter starter;
		try {
			starter = new NLPServerStarter(configPath);
			starter.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public NLPServerStarter() throws Exception {
		this.config = GameConfiguration.load(DEFAULT_CONFIG_PATH);
	}

	public NLPServerStarter(String path) throws Exception {
		this.config = GameConfiguration.load(path);
	}

	private void acceptClients() {
		// 必要なエージェント名が設定されているかどうかを確認
		boolean isSetRequiredAgentName = !config.getRequiredAgentName().isEmpty();
		ServerSocket serverSocket = null;
		try {
			// サーバーソケットを指定されたポートで作成
			serverSocket = new ServerSocket(config.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<Socket> requiredSockets = new HashSet<>();
		System.out.println("NLPServerStarter start.");
		isRunning = true;
		while (true) {
			try {
				if (isSetRequiredAgentName) {
					System.out.println("requiredSockets: " + requiredSockets);
				}

				// クライアントのIPアドレスをキーとし、そのIPアドレスに関連付けられたソケットのリストを値とするマップ
				Map<String, List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();
				// クライアントからの接続を受け入れる
				Socket socket = serverSocket.accept();

				// エントリーソケットマップのキーを生成
				String key = String.valueOf(entrySocketMap.hashCode());
				if (config.isSingleAgentPerIp()) {
					key = socket.getInetAddress().getHostAddress();
				} else if (!waitingSockets.isEmpty()) {
					key = new ArrayList<>(waitingSockets.keySet()).get(0);
				}

				// エントリーソケットマップを更新
				entrySocketMap = waitingSockets.getOrDefault(key, new HashMap<>());
				waitingSockets.putIfAbsent(key, entrySocketMap);

				System.out.println("socket connected: " + key);

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
				e.printStackTrace();
				continue;
			}
		}
	}

	// サーバーソケットを取得するメソッド
	private Socket getServerSocket(int index, String line, Set<Integer> entryAgentIndex)
			throws UnknownHostException, ConnectException, NoRouteToHostException, IOException {
		String host = null;
		int port = 0;

		// 他の組み合わせを続行する設定が有効な場合、ランダムにインデックスを選択
		if (config.isContinueOtherCombinations()) {
			Random rand = new Random();
			do {
				index = rand.nextInt(config.getAllParticipantNum()) + 1;
			} while (entryAgentIndex.contains(index));
			entryAgentIndex.add(index);
		}

		System.out.println("index: " + index);

		// インデックスに基づいてサーバー情報を設定
		switch (index) {
			case 1:
				host = config.getPlayer1Ip();
				port = config.getPlayer1Port();
				break;
			case 2:
				host = config.getPlayer2Ip();
				port = config.getPlayer2Port();
				break;
			case 3:
				host = config.getPlayer3Ip();
				port = config.getPlayer3Port();
				break;
			case 4:
				host = config.getPlayer4Ip();
				port = config.getPlayer4Port();
				break;
			case 5:
				host = config.getPlayer5Ip();
				port = config.getPlayer5Port();
				break;
			case 6:
				host = config.getPlayer6Ip();
				port = config.getPlayer6Port();
				break;
			case 7:
				host = config.getPlayer7Ip();
				port = config.getPlayer7Port();
				break;
			case 8:
				host = config.getPlayer8Ip();
				port = config.getPlayer8Port();
				break;
			case 9:
				host = config.getPlayer9Ip();
				port = config.getPlayer9Port();
				break;
			case 10:
				host = config.getPlayer10Ip();
				port = config.getPlayer10Port();
				break;
			case 10000:
			case 10001:
			case 10002:
			case 10003:
			case 10004:
				String[] portList = line.split("\\s");
				host = "localhost";
				port = Integer.parseInt(portList[index % 10000]);
				break;
			default:
				throw new IllegalArgumentException("Invalid index: " + index);
		}

		System.out.println("hostname: " + host + " port: " + port);
		Socket sock = new Socket(host, port);

		try {
			String name = getName(sock);
			System.out.println("name: " + name);
		} catch (Exception e) {
			throw new UnknownHostException();
		}
		return sock;
	}

	private void connectToPlayerServer() {
		// サーバースターターの初期化
		System.out.println("NLPServerStarter start.");
		isRunning = true;
		int index = 1;
		String line = "";

		try {
			// サーバーがポートをリッスンするかどうかを確認
			if (config.isListenPort()) {
				System.out.println("Port listening...");
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
				Socket socket = getServerSocket(index, line, entryAgentIndex);
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				String ipAddress = socket.getInetAddress().getHostAddress();

				// IPアドレスに基づいてソケットをマップに追加
				entrySocketMap.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(pair);
				System.out.println("address:" + ipAddress);

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
				e.printStackTrace();
				return;
			}

			// 接続キューを送信
			sendConnectionQueue(config.getConnectAgentNum(), config.isSingleAgentPerIp(), new HashSet<>());
		} catch (UnknownHostException e) {
			// 未知のホスト例外を処理
			System.out.println("Player" + index + " host is not found.\nPlease check spelling of hostname");
		} catch (ConnectException e) {
			// 接続拒否例外を処理
			System.out.println("Player" + index + " connection refused.");
		} catch (NoRouteToHostException e) {
			// ホストへのルートがない例外を処理
			System.out.println("Player" + index + " time out.");
		} catch (IOException e) {
			// その他のIO例外を処理
			System.out.println("Player" + index + " some error occurred.");
		}
	}

	private String getName(Socket socket) throws IOException, InterruptedException,
			ExecutionException, TimeoutException, SocketException {
		NLPAIWolfConnection connection = new NLPAIWolfConnection(socket, config);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		BufferedWriter bw = connection.getBufferedWriter();
		bw.append(DataConverter.getInstance().convert(new Packet(Request.NAME)));
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
			System.out.println("--------------------");
			System.out.println(line);
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

	/**
	 * 有効なConnectionの出力
	 * 
	 * @throws SocketException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private void printActiveConnection() throws SocketException, IOException,
			InterruptedException, ExecutionException, TimeoutException {
		if (waitingSockets.isEmpty()) {
			System.out.println("connecting : connection is empty.");
			return;
		}
		for (Map<String, List<Pair<Long, Socket>>> map : waitingSockets.values()) {
			System.out.println("------------------------------------------------------------------");
			for (List<Pair<Long, Socket>> list : map.values()) {
				System.out.print("connecting : ");
				for (Pair<Long, Socket> pair : list) {
					System.out.print(getName(pair.getValue()) + ", ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	/**
	 * 人数が揃ったらGameStarterに送り、ゲームを開始する
	 * 
	 * @param connectAgentNum
	 * @param onlyConnection
	 * @param essentialSocketSet
	 * @return
	 */
	private boolean sendConnectionQueue(int connectAgentNum, boolean onlyConnection, Set<Socket> essentialSocketSet) {
		// System.out.println("connectAgentNum = " + connectAgentNum + " " +
		// Thread.currentThread().getStackTrace()[1]);
		// 人数が揃っていればセット開始待機リストに追加
		boolean send = false;
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> iterator = waitingSockets.entrySet()
				.iterator();
		Set<Socket> set = new HashSet<>(essentialSocketSet);
		while (iterator.hasNext()) {
			Entry<String, Map<String, List<Pair<Long, Socket>>>> entry = iterator.next();
			// Set<Socket> set = new HashSet<>(essentialSocketSet);
			boolean canStartGame = false;
			for (Entry<String, List<Pair<Long, Socket>>> socketEntry : entry.getValue().entrySet()) {
				List<Socket> l = socketEntry.getValue().stream().map(p -> p.getValue()).collect(Collectors.toList());
				if (l.isEmpty())
					continue;
				if (onlyConnection) {
					set.add(l.get(0));
					continue;
				} else {
					for (Socket s : l) {
						if (set.size() < connectAgentNum)
							set.add(s);
					}
				}
				// System.out.println("set = " + set + " " +
				// Thread.currentThread().getStackTrace()[1]);
				if ((canStartGame = set.size() == connectAgentNum))
					break;
			}
			if (canStartGame) {
				synchronized (socketQue) {
					socketQue.add(new ArrayList<>(set));
				}
				iterator.remove();
				send = true;
			}
		}
		return send;
	}

	/**
	 * ServerStarterの起動
	 */
	public void start() {
		if (isRunning)
			return;

		// ゲーム開始スレッドの起動
		gameStarter = new GameStarter(socketQue, config);
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
						System.out.println(e);
					}
				}

				// 2週目以降用
				try {
					System.out.println("prev connectToPlayerServer wait 20sec");
					Thread.sleep(20000);
				} catch (Exception e) {
					System.out.println(e);
				}

				connectToPlayerServer();

				// connectToPlayerServerの追加待ち
				try {
					System.out.println("after connectToPlayerServer wait 20sec");
					Thread.sleep(20000);
				} catch (Exception e) {
					System.out.println(e);
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

		System.err.println("server was dead.");
	}
}
