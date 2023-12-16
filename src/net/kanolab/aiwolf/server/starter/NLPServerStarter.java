package net.kanolab.aiwolf.server.starter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
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

import net.kanolab.aiwolf.server.common.BRCallable;
import net.kanolab.aiwolf.server.common.GameConfiguration;
import net.kanolab.aiwolf.server.common.NLPAIWolfConnection;
import net.kanolab.aiwolf.server.common.Option;

// throw errors
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.io.IOException;

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
 * @author tminowa
 *
 * ・追記 2023/09/07
 * ゲームサーバをクライアント側に、プレイヤーをサーバ側としてゲームを実行する処理の記載
 * @author nwatanabe
 */
public class NLPServerStarter extends ServerStarter {
	// private static final String DEFAULT_INI_PATH = "../NLPServer/res/NLPAIWolfServerGUITest.ini";
	private static final String DEFAULT_INI_PATH  = "../res/NLPAIWolfServer_Client.ini";

	public static void main(String[] args){
		String initFileName = DEFAULT_INI_PATH;
		//オプションの読み込み
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			if(arg.startsWith("-f")){
				initFileName = args[++i];
			}
		}

		//iniファイルが指定されていなければ終了
		if(initFileName == null){
			System.out.println("Usage: NLPServerStarter -f initFileName");
			System.exit(0);
		}

		//NLPServerStarterの開始
		NLPServerStarter starter = new NLPServerStarter(initFileName);
		starter.start();
	}

	private GameConfiguration config;
	private Queue<List<Socket>> socketQue = new ArrayDeque<>();
	private GameStarter gameStarter;

	private boolean isActive = false;	// ゲームサーバが既に動いているかどうか

	private Map<String, Map<String,List<Pair<Long, Socket>>>> allWaitingSocketMap = new HashMap<>();

	public NLPServerStarter(){
		this.config = new GameConfiguration(DEFAULT_INI_PATH);
	}

	/**
	 * コンストラクタ
	 * iniファイルの読み込み
	 * @param fileName
	 */
	public NLPServerStarter(String fileName){
		this.config = new GameConfiguration(fileName);
	}

	/**
	 * クライアントを受け付ける
	 */
	private void acceptClients(){
		boolean isSingle = config.get(Option.RUN_SINGLE_PORT_GAME);
		int connectAgentNum = config.get(Option.CONNECT_AGENT_NUM);
		int deleteTime = config.get(Option.DELETE_WAITING_CONNECTION_TIME);//deleteTimeオプションは動作未検証
		boolean onlyConnection = config.get(Option.ONLY_1AGENT_BY_IP);
		String essentialAgentName = config.get(Option.ESSENTIAL_AGENT_NAME);
		boolean existEssentialAgent = !(essentialAgentName == null || essentialAgentName.isEmpty());

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(config.get(Option.PORT_NUM));
		} catch (IOException e2) {
			// TODO 自動生成された catch ブロック
			e2.printStackTrace();
		}
		Set<Socket> essentialSocketSet = new HashSet<>();
		System.out.println("NLPServerStarter start.");
		System.out.println("onlyConnection = " + onlyConnection + " " + Thread.currentThread().getStackTrace()[1]);
		this.isActive = true;
		while(true){
			try{

				if(existEssentialAgent)
					System.out.println("EssentialSocket : " + essentialSocketSet);

				//受け付けたソケットを格納するMap（ソケットのIPをkey、登録時間とソケットのペアをvalue）
				Map<String,List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();

				//受け付けたクライアント
				Socket socket = serverSocket.accept();

				//同一IP対戦ならIPをキーに利用し、それ以外はMapのハッシュコードをキーに使用する
				String key = String.valueOf(entrySocketMap.hashCode());
				if(isSingle)
					key = socket.getInetAddress().getHostAddress();
				else if(allWaitingSocketMap.size() != 0)
					key = new ArrayList<String>(allWaitingSocketMap.keySet()).get(0);
				if(allWaitingSocketMap.containsKey(key))
					entrySocketMap = allWaitingSocketMap.get(key);
				else
					allWaitingSocketMap.put(key, entrySocketMap);
				System.out.println("socket connected : " + key);
				//System.out.println("allWaitingSocketMap = " + allWaitingSocketMap + " " + Thread.currentThread().getStackTrace()[1]);
				//エージェント名が必須エージェントなら必須ソケットセットに加える
				String name = getName(socket);
				if(existEssentialAgent && name.contains(essentialAgentName))
					essentialSocketSet.add(socket);

				//Socketの追加
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				if(entrySocketMap.containsKey(socket.getInetAddress().getHostAddress()))
					entrySocketMap.get(socket.getInetAddress().getHostAddress()).add(pair);
				else{
					List<Pair<Long, Socket>> list = new ArrayList<>();
					list.add(pair);
					entrySocketMap.put(socket.getInetAddress().getHostAddress(), list);
				}



				removeInvalidConnection(deleteTime);
				printActiveConnection();

				//エージェントが人数に達していなければコネクションの受付へ戻る
				if(existEssentialAgent && essentialSocketSet.isEmpty())
					continue;

				sendConnectionQueue(connectAgentNum, onlyConnection, essentialSocketSet);
			}catch(Exception e){
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
	private Socket getServerSocket(int index, String line) throws UnknownHostException,ConnectException,NoRouteToHostException,IOException{
		Map<String, Object> serverInfo = new HashMap<String, Object>();

		System.out.println(index);

		switch(index){
			case 1:
				serverInfo.put("HOST",config.get(Option.PLAYER_HOST1));
				serverInfo.put("PORT",config.get(Option.PLAYER_PORT1));
				break;
			case 2:
				serverInfo.put("HOST",config.get(Option.PLAYER_HOST2));
				serverInfo.put("PORT",config.get(Option.PLAYER_PORT2));
				break;
			case 3:
				serverInfo.put("HOST",config.get(Option.PLAYER_HOST3));
				serverInfo.put("PORT",config.get(Option.PLAYER_PORT3));
				break;
			case 4:
				serverInfo.put("HOST",config.get(Option.PLAYER_HOST4));
				serverInfo.put("PORT",config.get(Option.PLAYER_PORT4));
				break;
			case 5:
				serverInfo.put("HOST",config.get(Option.PLAYER_HOST5));
				serverInfo.put("PORT",config.get(Option.PLAYER_PORT5));
				break;
			case 10000:
			case 10001:
			case 10002:
			case 10003:
			case 10004:
				String[] portList = line.split("\\s");
				// serverInfo.put("HOST","133.167.32.100");
				serverInfo.put("HOST","localhost");
				serverInfo.put("PORT",Integer.parseInt(portList[index%10000]));
				break;
		}

		System.out.println("HOST:" + serverInfo.get("HOST").toString() + " PORT:" + serverInfo.get("PORT"));
		Socket sock = new Socket(serverInfo.get("HOST").toString(),(int)serverInfo.get("PORT"));

		try{
			String name = getName(sock);
			System.out.println("NAME:" + name);
		}
		catch(Exception e){
			throw new UnknownHostException();
		}

		return sock;
	}

	private void waitGame(){
		while(true){
			continue;
		}
	}

	/**
	 * サーバとして待ち受ける時はclientがまばらに来るのでwhileで来た順に受け付けたが、こちらをclientとするときは全てのサーバは既に待ち受けしている物として進める。
	 * 
	 * @author nwatanabe
	 */
	private void connectToPlayerServer(){
		boolean isSingle = config.get(Option.RUN_SINGLE_PORT_GAME);
		int connectAgentNum = config.get(Option.CONNECT_AGENT_NUM);
		int deleteTime = config.get(Option.DELETE_WAITING_CONNECTION_TIME);	//deleteTimeオプションは動作未検証
		boolean onlyConnection = config.get(Option.ONLY_1AGENT_BY_IP);		// 同一IPからのみ接続があるかどうか

		Set<Socket> essentialSocketSet = new HashSet<>();	// essentialAgentはこちらから繋ぎに行く都合上いらない気がするので今は入れてない。(引数として必要なため定義)
		System.out.println("NLPServerStarter start.");
		// System.out.println("onlyConnection = " + onlyConnection + " " + Thread.currentThread().getStackTrace()[1]);
		this.isActive = true;
		int index = 1;
		String line = "";

		try{

			if(config.get(Option.IS_PORT_LISTENING_FLAG)){
				//接続先のポートを聞く。
				ServerSocket serverSocket = new ServerSocket(config.get(Option.PORT_NUM));
				Socket socket = serverSocket.accept();
				String line = getHostNameAndPort(socket);
				index = 10000;
			}		

			Map<String,List<Pair<Long, Socket>>> entrySocketMap = new HashMap<>();	// key:ip value: list[pair(entrytime, socket)]
			for(int i=0; i<connectAgentNum; i++){

				Socket socket = getServerSocket(index,line);

				//Socketの追加
				Pair<Long, Socket> pair = new Pair<>(System.currentTimeMillis() / 3600000, socket);
				if(entrySocketMap.containsKey(socket.getInetAddress().getHostAddress())){
					// 同一IPなら同じKeyの場所に格納する
					entrySocketMap.get(socket.getInetAddress().getHostAddress()).add(pair);
				}
				else{
					List<Pair<Long, Socket>> list = new ArrayList<>();
					list.add(pair);
					entrySocketMap.put(socket.getInetAddress().getHostAddress(), list);
					System.out.println("Address:" + socket.getInetAddress().getHostAddress());
				}

				// IPが異なる場合にIPをKeyとしない理由が今のところ分からないのでIPをkeyにする(グローバルIPとローカルIPがぶつかる可能性を考慮した？)
				String key = socket.getInetAddress().getHostAddress();

				allWaitingSocketMap.put(key,entrySocketMap);
				index++;
			}

			removeInvalidConnection(deleteTime);

			try{
				printActiveConnection();
			}
			catch(Exception e){
				System.out.println();
				return;
			}

			sendConnectionQueue(connectAgentNum, onlyConnection, essentialSocketSet);

		}
		catch (UnknownHostException e) {
			// check spelling of hostname
			System.out.println("Player" + index + " host is not found.\nPlease check spelling of hostname");
			return;
		} 
		catch (ConnectException e) {
			// connection refused - is server down? Try another port.
			System.out.println("Player" + index + " connection refused.");
			return;
		} 
		catch (NoRouteToHostException e) {
			// The connect attempt timed out.  Try connecting through a proxy
			System.out.println("Player" + index + "time out.");
			return;
		} 
		catch (IOException e) {
			// another error occurred
			System.out.println("Player" + index + "some error occured.");
			return;
		}


		waitGame();

	}

	/**
	 * ソケットが生きているかの確認も兼ねているため現在はとりあえずconnectionのgetNameと同じ内容をここでも再実装<br>
	 * getName自体はconnectionクラスに実装しているのでソケット通信が生きているかの確認はもっと簡略化した内容で実装し直してもよいと思う<br>
	 * @param socket
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws SocketException
	 */
	private String getName(Socket socket) throws IOException, InterruptedException,
		ExecutionException, TimeoutException, SocketException{
		NLPAIWolfConnection connection = new NLPAIWolfConnection(socket, config);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		//clientにrequestを送信し、結果を受け取る
		BufferedWriter bw = connection.getBufferedWriter();
		bw.append(DataConverter.getInstance().convert(new Packet(Request.NAME)));
		bw.append("\n");
		bw.flush();

		//結果の受け取りとタイムアウト
		BRCallable task = new BRCallable(connection.getBufferedReader());
		Future<String> future = pool.submit(task);
		long timeout = config.get(Option.TIMEOUT);
		String line = timeout > 0 ? future.get(timeout, TimeUnit.MILLISECONDS) : future.get();
		if(!task.isSuccess()){
			throw task.getIOException();
		}
		return (line == null || line.isEmpty() ) ? null : line;
	}

	private String getHostNameAndPort(Socket socket) throws IOException{
		BufferedReader reader = new BufferedReader(
				new InputStreamReader
				(socket.getInputStream()));
		
		String line = reader.readLine();

		// 通信の終了
		socket.close();
		reader.close();

		return line;
	}

	/**
	 * 無効なConnetionを削除する
	 * @param allWaitingSocketMap
	 * @param deleteTime
	 * @param entrySocketMap
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws SocketException
	 */
	private void removeInvalidConnection(int deleteTime){
		Map<Pair<String,String>,  Pair<Long,Socket>> lostMap = new HashMap<>();
		for(Entry<String, Map<String, List<Pair<Long, Socket>>>> sMapEntry : allWaitingSocketMap.entrySet()){
			for(Entry<String, List<Pair<Long, Socket>>> entry : sMapEntry.getValue().entrySet()){
				//ロストしているSocket・deleteTimeより長時間対戦が行われずに接続が継続しているSocketを削除リストに追加
				for(Pair<Long, Socket> socketPair : entry.getValue()){
					long time = System.currentTimeMillis() / 3600000;
					try {
						if(getName(socketPair.getValue()) == null || time - socketPair.getKey() > deleteTime )
							lostMap.put(new Pair<>(sMapEntry.getKey(), entry.getKey()),socketPair );
					} catch (Exception e) {
						lostMap.put(new Pair<>(sMapEntry.getKey(), entry.getKey()),socketPair );
					}
				}
			}
		}

		//問題のあるコネクションを削除
		for(Entry<Pair<String,String>, Pair<Long,Socket>> lostPair : lostMap.entrySet()){
			Pair<String,String> keyPair = lostPair.getKey();
			allWaitingSocketMap.get(keyPair.getKey()).get(keyPair.getValue()).remove(lostPair.getValue());
			if(allWaitingSocketMap.get(keyPair.getKey()).get(keyPair.getValue()).isEmpty())
				allWaitingSocketMap.get(keyPair.getKey()).remove(keyPair.getValue());
			if(allWaitingSocketMap.get(keyPair.getKey()).isEmpty())
				allWaitingSocketMap.remove(keyPair.getKey());
		}
		removeEmptyMap();

	}

	/**
	 * 空のMapを削除する
	 */
	private void removeEmptyMap() {
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> outsideIterator =
			allWaitingSocketMap.entrySet().iterator();
		while(outsideIterator.hasNext()){
			Entry<String, Map<String, List<Pair<Long, Socket>>>> entry = outsideIterator.next();
			if(entry.getValue().isEmpty())
				outsideIterator.remove();
			else{
				Iterator<Entry<String, List<Pair<Long, Socket>>>> insideIterator =
					entry.getValue().entrySet().iterator();
				while(insideIterator.hasNext()){
					Entry<String, List<Pair<Long, Socket>>> insideEntry = insideIterator.next();
					if(insideEntry.getValue().isEmpty())
						insideIterator.remove();
				}
			}
		}
	}



	/**
	 * 有効なConnectionの出力
	 * @throws SocketException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private void printActiveConnection() throws SocketException, IOException,
		InterruptedException, ExecutionException, TimeoutException{
		if(allWaitingSocketMap.isEmpty()){
			System.out.println("connecting : connection is empty.");
			return;
		}
		for(Map<String, List<Pair<Long, Socket>>> map : allWaitingSocketMap.values()){
			System.out.println("------------------------------------------------------------------");
			for(List<Pair<Long, Socket>> list : map.values()){
				System.out.print("connecting : ");
				for(Pair<Long, Socket> pair : list){
					System.out.print(getName(pair.getValue()) + ", ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	/**
	 * 人数が揃ったらGameStarterに送り、ゲームを開始する
	 * @param connectAgentNum
	 * @param onlyConnection
	 * @param essentialSocketSet
	 * @return
	 */
	private boolean sendConnectionQueue(int connectAgentNum, boolean onlyConnection, Set<Socket> essentialSocketSet) {
//		System.out.println("connectAgentNum = " + connectAgentNum + " " + Thread.currentThread().getStackTrace()[1]);
		//人数が揃っていればセット開始待機リストに追加
		boolean send = false;
		List<String> removeList = new ArrayList<>();
		Iterator<Entry<String, Map<String, List<Pair<Long, Socket>>>>> iterator =
			allWaitingSocketMap.entrySet().iterator();
		Set<Socket> set = new HashSet<>(essentialSocketSet);
		while(iterator.hasNext()){
			Entry<String, Map<String, List<Pair<Long, Socket>>>> entry = iterator.next();
//			Set<Socket> set = new HashSet<>(essentialSocketSet);
			boolean canStartGame = false;
			for(Entry<String, List<Pair<Long, Socket>>> socketEntry : entry.getValue().entrySet()){
				Pair<String, String> keyPair = new Pair<>(entry.getKey(), socketEntry.getKey());
				List<Socket> l = socketEntry.getValue().stream().map(p -> p.getValue()).collect(Collectors.toList());
				if(l.isEmpty())
					continue;
				if(onlyConnection){
					set.add(l.get(0));
					continue;
				}else{
					for(Socket s : l){
						if(set.size() < connectAgentNum)
							set.add(s);
					}
				}
				//System.out.println("set = " + set + " " + Thread.currentThread().getStackTrace()[1]);				
				if((canStartGame = set.size() == connectAgentNum))
					break;
			}
			if(canStartGame){
				synchronized(socketQue){
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
	public void start(){
		if(isActive)
			return;

		// ゲーム開始スレッドの起動
		gameStarter = new GameStarter(socketQue, config);
		gameStarter.start();

		if(config.get(Option.IS_SERVER_FLAG)){
			// サーバとして待ち受け
			acceptClients();
		}
		else{
			connectToPlayerServer();
		}

		System.err.println("server was dead.");
	}
}
