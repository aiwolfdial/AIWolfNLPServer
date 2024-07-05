package net.kanolab.aiwolf.server.automatic;

import java.lang.reflect.InvocationTargetException;

import org.aiwolf.common.data.Player;

import net.kanolab.aiwolf.server.client.NLPTcpipClient;

public class AutomaticClientConnector {

	private static final String DEFAULT_PATH = "./res/AIWolfGameServer.ini";
	private AutomaticStarterConfiguration config;

	public static void main(String[] args) {
		String path = DEFAULT_PATH;
		// 引数としてパスが与えられていれば設定ファイルのパスとしてそれを利用する
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-f"))
				path = args[++i];

		// 接続先設定の読み込み
		AutomaticStarterConfiguration config = new AutomaticStarterConfiguration(path);

		// クライアントの接続
		AutomaticClientConnector connector = new AutomaticClientConnector(config);
		connector.connectClients();
	}

	public AutomaticClientConnector(AutomaticStarterConfiguration config) {
		this.config = config;
	}

	/**
	 * このインスタンスが持つhostのportに対してnum体のagentClassエージェントを接続する<br>
	 * エージェントの生成にはデフォルトコンストラクタを使用する<br>
	 * 現在はスレッド化して生成しているが、この実装だと1体落ちると全滅するので将来的にはそれぞれ別プロセスで実行するようにしたい
	 */
	public void connectClients() {
		for (int i = 0; i < config.getNum(); i++) {
			Runnable r = new Runnable() {
				public void run() {
					NLPTcpipClient client = new NLPTcpipClient(config.getHost(), config.getPort());
					try {
						Player player = (Player) config.getPlayerClass().getConstructor().newInstance();
						String playerInfo = player.getName() + " (" + config.getPlayerClass().getName() + ")";
						if (client.connect(player)) {
							System.out.println("Player connected to server : " + playerInfo);
						} else {
							System.err.println("This Player missed connecting to server : " + playerInfo);
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			};

			Thread thread = new Thread(r);
			thread.start();
		}
	}
}
