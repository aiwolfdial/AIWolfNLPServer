package net.kanolab.aiwolf.server.automatic;

import java.lang.reflect.InvocationTargetException;

import org.aiwolf.common.data.Player;

import net.kanolab.aiwolf.server.client.NLPTCPIPClient;

public class AutomaticClientConnector {
	private static final String DEFAULT_CONFIG_PATH = "./res/AIWolfGameServer.ini";
	private AutomaticStarterConfiguration config;

	public static void main(String[] args) {
		String configPath = DEFAULT_CONFIG_PATH;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-f") && i + 1 < args.length) {
				configPath = args[++i];
			}
		}

		AutomaticStarterConfiguration config = new AutomaticStarterConfiguration(configPath);
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
		for (int i = 0; i < config.getPlayerNum(); i++) {
			Runnable r = new Runnable() {
				public void run() {
					NLPTCPIPClient client = new NLPTCPIPClient(config.getHostname(), config.getPort());
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
				}
			};

			Thread thread = new Thread(r);
			thread.start();
		}
	}
}
