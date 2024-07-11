package gui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * GUIとの接続
 */
public class GUIConnector {
	private int messageIdx = 0;
	private final String address;
	private final int port;

	public int getMessageIdx() {
		return messageIdx;
	}

	public void setMessageIdx(int messageIdx) {
		this.messageIdx = messageIdx;
	}

	public GUIConnector(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public void send(String str) {
		str = String.format("%04d", messageIdx++) + str;
		try {
			Socket socket = new Socket(address, port);
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(str.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
	}
}
