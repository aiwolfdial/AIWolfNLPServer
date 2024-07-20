package automatic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import common.data.Player;
import common.net.TcpClient;

public class AutomaticClientConnector {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";
	private final AutomaticStarterConfiguration config;

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		if (args.length > 0)
			configPath = args[0];
		AutomaticStarterConfiguration config;
		try {
			config = AutomaticStarterConfiguration.load(configPath);
			AutomaticClientConnector connector = new AutomaticClientConnector(config);
			connector.connectClients();
		} catch (NoSuchFieldException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}

	public AutomaticClientConnector(AutomaticStarterConfiguration config) {
		this.config = config;
	}

	/**
	 * このインスタンスが持つhostのportに対してnum体のagentClassエージェントを接続する
	 * エージェントの生成にはデフォルトコンストラクタを使用する
	 * 現在はスレッド化して生成しているが、この実装だと1体落ちると全滅するので将来的にはそれぞれ別プロセスで実行するようにしたい
	 */
	public void connectClients() {
		for (int i = 0; i < config.getPlayerNum(); i++) {
			Runnable r = () -> {
				TcpClient client = new TcpClient(config.getHostname(), config.getPort());
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
					e.printStackTrace();
				}
			};

			Thread thread = new Thread(r);
			thread.start();
		}
	}
}
