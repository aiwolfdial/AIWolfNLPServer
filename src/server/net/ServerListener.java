package server.net;

import java.net.Socket;

import common.data.Agent;

public interface ServerListener {
	void connected(Socket socket, Agent agent, String name);

	void unconnected(Socket socket, Agent agent, String name);

}
