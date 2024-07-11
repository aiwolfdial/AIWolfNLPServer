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

import org.aiwolf.common.data.Request;
import org.aiwolf.common.net.DataConverter;
import org.aiwolf.common.net.Packet;
import org.aiwolf.common.util.Pair;
import org.aiwolf.server.bin.ServerStarter;

import common.BRCallable;
import common.GameConfiguration;
import common.NLPAIWolfConnection;

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

	private GameConfiguration config;
	private Queue<List<Socket>> socketQue = new ArrayDeque<>();
	private GameStarter gameStarter;

	private Map<String, Map<String, List<Pair<Long, Socket>>>> allWaitingSocketMap = new HashMap<>();

	private boolean isActive = false;

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0)
			configPath = args[0];
		NLPServerStarter starter = new NLPServerStarter(configPath);
		starter.start();
	}

	public NLPServerStarter() {
		this.config = new GameConfiguration(DEFAULT_CONFIG_PATH);
	}

	public NLPServerStarter(String path) {
		this.config = new GameConfiguration(path);
	}

	private void acceptClients() {
		boolean existEssentialAgent = !(config.getRequiredAgentName() == null
				|| config.getRequiredAgentName().isEmpty());

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(config.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<Socket> essentialSocketSet = new HashSet<>();
		System.out.println("NLPServerStarter start.");
		System.out.println(
				"SingleAgentPerIp = " + config.isSingleAgentPerIp() + " " + Thread.currentThread().getStackTrace()[1]);
		this.isActive = true;
		while (true) {
			try {
				if (existEssentialAgent)
					System.out.println("EssentialSocket : " + essentialSocketSet);

				// 受け付けたソケットを格納するMap（ソケットのIPをkey、登録時間とソケットのペアをvalue）
				Map<String, List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();

				// 受け付けたクライアント
				Socket socket = serverSocket.accept();

				// 同一IP対戦ならIPをキーに利用し、それ以外はMapのハッシュコードをキーに使用する
				String key = String.valueOf(entrySocketMap.hashCode());
				if (config.isSingleAgentPerIp())
					key = socket.getInetAddress().getHostAddress();
				else if (allWaitingSocketMap.size() != 0)
					key = new ArrayList<String>(allWaitingSocketMap.keySet()).get(0);
				if (allWaitingSocketMap.containsKey(key))
					entrySocketMap = allWaitingSocketMap.get(key);
				else
					allWaitingSocketMap.put(key, entrySocketMap);
				System.out.println("socket connected : " + key);
				// System.out.println("allWaitingSocketMap = " + allWaitingSocketMap + " " +
				// Thread.currentThread().getStackTrace()[1]);
				// エージェント名が必須エージェントなら必須ソケットセットに加える
				String name = getName(socket);
				if (existEssentialAgent && name.contains(config.getRequiredAgentName()))
					essentialSocketSet.add(socket);

				// Socketの追加
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				if (entrySocketMap.containsKey(socket.getInetAddress().getHostAddress()))
					entrySocketMap.get(socket.getInetAddress().getHostAddress()).add(pair);
				else {
					List<Pair<Long, Socket>> list = new ArrayList<>();
					list.add(pair);
					entrySocketMap.put(socket.getInetAddress().getHostAddress(), list);
				}

				removeInvalidConnection(config.getIdleConnectionTimeout());
				printActiveConnection();

				// エージェントが人数に達していなければコネクションの受付へ戻る
				if (existEssentialAgent && essentialSocketSet.isEmpty())
					continue;

				sendConnectionQueue(config.getConnectAgentNum(), config.isSingleAgentPerIp(), essentialSocketSet);
			} catch (Exception e) {
				System.out.println();
				continue;
			}
		}
	}

	/**
	 * @author nwatanabe
	 * @param index 何番目に接続するプレイヤーかのインデックス
	 * @return プレイヤーサーバに接続したソケット
	 * @throws UnknownHostException
	 * @throws ConnectException
	 * @throws NoRouteToHostException
	 * @throws IOException
	 */
	private Socket getServerSocket(int index, String line, Set<Integer> entryAgentIndex)
			throws UnknownHostException, ConnectException, NoRouteToHostException, IOException {
		Map<String, Object> serverInfo = new HashMap<String, Object>();
		if (config.isContinueOtherCombinations()) {
			while (true) {
				Random rand = new Random();
				index = rand.nextInt((Integer) config.getAllParticipantNum()) + 1;

				if (!entryAgentIndex.contains(index)) {
					entryAgentIndex.add(index);
					break;
				}
			}
		}

		System.out.println("index:" + Integer.toString(index));

		switch (index) {
			case 1:
				serverInfo.put("HOST", config.getPlayer1Ip());
				serverInfo.put("PORT", config.getPlayer1Port());
				break;
			case 2:
				serverInfo.put("HOST", config.getPlayer2Ip());
				serverInfo.put("PORT", config.getPlayer2Port());
				break;
			case 3:
				serverInfo.put("HOST", config.getPlayer3Ip());
				serverInfo.put("PORT", config.getPlayer3Port());
				break;
			case 4:
				serverInfo.put("HOST", config.getPlayer4Ip());
				serverInfo.put("PORT", config.getPlayer4Port());
				break;
			case 5:
				serverInfo.put("HOST", config.getPlayer5Ip());
				serverInfo.put("PORT", config.getPlayer5Port());
				break;
			case 6:
				serverInfo.put("HOST", config.getPlayer6Ip());
				serverInfo.put("PORT", config.getPlayer6Port());
				break;
			case 7:
				serverInfo.put("HOST", config.getPlayer7Ip());
				serverInfo.put("PORT", config.getPlayer7Port());
				break;
			case 8:
				serverInfo.put("HOST", config.getPlayer8Ip());
				serverInfo.put("PORT", config.getPlayer8Port());
				break;
			case 9:
				serverInfo.put("HOST", config.getPlayer9Ip());
				serverInfo.put("PORT", config.getPlayer9Port());
				break;
			case 10:
				serverInfo.put("HOST", config.getPlayer10Ip());
				serverInfo.put("PORT", config.getPlayer10Port());
				break;
			case 10000:
			case 10001:
			case 10002:
			case 10003:
			case 10004:
				String[] portList = line.split("\\s");
				// serverInfo.put("HOST","133.167.32.100");
				serverInfo.put("HOST", "localhost");
				serverInfo.put("PORT", Integer.parseInt(portList[index % 10000]));
				break;
		}

		System.out.println("HOST:" + serverInfo.get("HOST").toString() + " PORT:" + serverInfo.get("PORT"));
		Socket sock = new Socket(serverInfo.get("HOST").toString(), (int) serverInfo.get("PORT"));

		try {
			String name = getName(sock);
			System.out.println("NAME:" + name);
		} catch (Exception e) {
			throw new UnknownHostException();
		}
		return sock;
	}

	/**
	 * サーバとして待ち受ける時はclientがまばらに来るのでwhileで来た順に受け付けたが、こちらをclientとするときは全てのサーバは既に待ち受けしている物として進める。
	 * 
	 * @author nwatanabe
	 */
	private void connectToPlayerServer() {
		Set<Socket> essentialSocketSet = new HashSet<>(); // essentialAgentはこちらから繋ぎに行く都合上いらない気がするので今は入れてない。(引数として必要なため定義)
		System.out.println("NLPServerStarter start.");
		// System.out.println("onlyConnection = " + onlyConnection + " " +
		// Thread.currentThread().getStackTrace()[1]);
		this.isActive = true;
		int index = 1;
		String line = "";

		try {

			if (config.isListenPort()) {
				// 接続先のポートを聞く。
				System.out.println("Port Listening...");
				ServerSocket serverSocket = new ServerSocket(config.getPort());
				Socket socket = serverSocket.accept();
				line = getHostNameAndPort(socket);
				index = 10000;
			}

			Map<String, List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();
			// key:ip value:
			// list[pair(entrytime, socket)]
			Set<Integer> entryAgentIndex = new HashSet<>();
			for (int i = 0; i < config.getConnectAgentNum(); i++) {
				Socket socket = getServerSocket(index, line, entryAgentIndex);

				// Socketの追加
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				if (entrySocketMap.containsKey(socket.getInetAddress().getHostAddress())) {
					// 同一IPなら同じKeyの場所に格納する
					entrySocketMap.get(socket.getInetAddress().getHostAddress()).add(pair);
				} else {
					List<Pair<Long, Socket>> list = new ArrayList<>();
					list.add(pair);
					entrySocketMap.put(socket.getInetAddress().getHostAddress(), list);
					System.out.println("Address:" + socket.getInetAddress().getHostAddress());
				}

				// IPが異なる場合にIPをKeyとしない理由が今のところ分からないのでIPをkeyにする(グローバルIPとローカルIPがぶつかる可能性を考慮した？)
				String key = socket.getInetAddress().getHostAddress();

				allWaitingSocketMap.put(key, entrySocketMap);
				index++;
			}

			removeInvalidConnection(config.getIdleConnectionTimeout());

			try {
				printActiveConnection();
			} catch (Exception e) {
				System.out.println();
				return;
			}

			sendConnectionQueue(config.getConnectAgentNum(), config.isSingleAgentPerIp(), essentialSocketSet);

		} catch (UnknownHostException e) {
			// check spelling of hostname
			System.out.println("Player" + index + " host is not found.\nPlease check spelling of hostname");
			return;
		} catch (ConnectException e) {
			// connection refused - is server down? Try another port.
			System.out.println("Player" + index + " connection refused.");
			return;
		} catch (NoRouteToHostException e) {
			// The connect attempt timed out. Try connecting through a proxy
			System.out.println("Player" + index + "time out.");
			return;
		} catch (IOException e) {
			// another error occurred
			System.out.println("Player" + index + "some error occured.");
			return;
		}
	}

	/**
	 * ソケットが生きているかの確認も兼ねているため現在はとりあえずconnectionのgetNameと同じ内容をここでも再実装<br>
	 * getName自体はconnectionクラスに実装しているのでソケット通信が生きているかの確認はもっと簡略化した内容で実装し直してもよいと思う<br>
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws SocketException
	 */
	private String getName(Socket socket) throws IOException, InterruptedException,
			ExecutionException, TimeoutException, SocketException {
		NLPAIWolfConnection connection = new NLPAIWolfConnection(socket, config);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		// clientにrequestを送信し、結果を受け取る
		BufferedWriter bw = connection.getBufferedWriter();
		bw.append(DataConverter.getInstance().convert(new Packet(Request.NAME)));
		bw.append("\n");
		bw.flush();

		System.out.println("Send getName");

		// 結果の受け取りとタイムアウト
		BRCallable task = new BRCallable(connection.getBufferedReader());
		Future<String> future = pool.submit(task);
		String line = config.getResponseTimeout() > 0 ? future.get(config.getResponseTimeout(), TimeUnit.MILLISECONDS)
				: future.get();
		if (!task.isSuccess()) {
			throw task.getIOException();
		}
		return (line == null || line.isEmpty()) ? null : line;
	}

	private String getHostNameAndPort(Socket socket) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));

		String line = reader.readLine();

		System.out.println("--------------------");
		System.out.println(line);

		// 通信の終了
		socket.close();
		reader.close();

		return line;
	}

	/**
	 * 無効なConnetionを削除する
	 * 
	 * @param allWaitingSocketMap
	 * @param deleteTime
	 * @param entrySocketMap
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws SocketException
	 */
	private void removeInvalidConnection(int deleteTime) {
		Map<Pair<String, String>, Pair<Long, Socket>> lostMap = new HashMap<>();
		for (Entry<String, Map<String, List<Pair<Long, Socket>>>> sMapEntry : allWaitingSocketMap.entrySet()) {
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
			allWaitingSocketMap.get(keyPair.getKey()).get(keyPair.getValue()).remove(lostPair.getValue());
			if (allWaitingSocketMap.get(keyPair.getKey()).get(keyPair.getValue()).isEmpty())
				allWaitingSocketMap.get(keyPair.getKey()).remove(keyPair.getValue());
			if (allWaitingSocketMap.get(keyPair.getKey()).isEmpty())
				allWaitingSocketMap.remove(keyPair.getKey());
		}
		removeEmptyMap();
	}

	/**
	 * 空のMapを削除する
	 */
	private void removeEmptyMap() {
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> outsideIterator = allWaitingSocketMap.entrySet()
				.iterator();
		while (outsideIterator.hasNext()) {
			Entry<String, Map<String, List<Pair<Long, Socket>>>> entry = outsideIterator.next();
			if (entry.getValue().isEmpty())
				outsideIterator.remove();
			else {
				Iterator<Entry<String, List<Pair<Long, Socket>>>> insideIterator = entry.getValue().entrySet()
						.iterator();
				while (insideIterator.hasNext()) {
					Entry<String, List<Pair<Long, Socket>>> insideEntry = insideIterator.next();
					if (insideEntry.getValue().isEmpty())
						insideIterator.remove();
				}
			}
		}
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
		if (allWaitingSocketMap.isEmpty()) {
			System.out.println("connecting : connection is empty.");
			return;
		}
		for (Map<String, List<Pair<Long, Socket>>> map : allWaitingSocketMap.values()) {
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
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> iterator = allWaitingSocketMap.entrySet()
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
		if (isActive)
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
