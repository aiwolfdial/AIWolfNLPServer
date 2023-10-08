package net.kanolab.aiwolf.server.automatic;

// import net.kanolab.aiwolf.agent.KanolabPlayer;
import net.kanolab.tminowa.util.DataReader;

import org.aiwolf.common.data.Agent;

public class AutomaticStarterConfiguration {
	// private static final Class<?> DEFAULT_CLASS = KanolabPlayer.class;
	private static final Class<?> DEFAULT_CLASS = Agent.class;
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_NUM = 5;
	private static final int DEFAULT_PORT = 10000;
	private static final boolean DEFAULT_START_SERVER = false;

	private String host;
	private Class<?> playerClass;
	private int port;
	private int num;
	private boolean startServer;

	public AutomaticStarterConfiguration(){
		this.host = DEFAULT_HOST;
		this.playerClass = DEFAULT_CLASS;
		this.port = DEFAULT_PORT;
		this.num = DEFAULT_NUM;
		this.startServer =DEFAULT_START_SERVER;
	}

	public AutomaticStarterConfiguration(String path){
		this();
		DataReader reader = new DataReader(path,"=");
		for(String[] array : reader.getSplitLines()){
			System.out.println(array[0] + " : "+ array[1]);
			switch(array[0]){
			case "port":
				port = Integer.parseInt(array[1]);
				break;
			case "player":
				num = Integer.parseInt(array[1]);
				break;
			case "host":
				host = array[1];
				break;
			case "className":
				try {
					playerClass = Class.forName(array[1]);
				} catch (ClassNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				break;
			case "server":
				startServer = Boolean.parseBoolean(array[1]);
				break;
			}
		}
	}

	public String getHost() {
		return host;
	}

	public Class<?> getPlayerClass() {
		return playerClass;
	}

	public int getPort() {
		return port;
	}

	public int getNum() {
		return num;
	}

	public boolean isStartServer() {
		return startServer;
	}


}
