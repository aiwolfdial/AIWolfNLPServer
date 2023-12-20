package net.kanolab.aiwolf.server.automatic;

import net.kanolab.aiwolf.server.starter.NLPServerStarter;

/**
 * 自動対戦開始プログラム
 * あとで別プロセスに変更したい
 */
public class AutoGameStarter {
	public static void main(String[] args){
		
		String configIniPath = "../res/AIWolfGameServer.ini";

		if (args.length > 0) configIniPath = args[0];
		AutomaticStarterConfiguration config = new AutomaticStarterConfiguration(configIniPath);
		if(config.isStartServer()){

			Runnable r = new Runnable() {

				@Override
				public void run() {
					NLPServerStarter starter = new NLPServerStarter();
					starter.start();
				}
			};

			Thread thread = new Thread(r);
			thread.start();
		}

		// AutomaticClientConnector connector = new AutomaticClientConnector(config);
		// connector.connectClients();
	}
}
