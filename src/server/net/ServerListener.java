package server.net;

import java.net.Socket;

import common.data.Agent;

public interface ServerListener {
	public void connected(Socket socket, Agent agent, String name);

	public void unconnected(Socket socket, Agent agent, String name);

}
