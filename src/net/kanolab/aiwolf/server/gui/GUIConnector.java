package net.kanolab.aiwolf.server.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * GUIとの接続
 */
public class GUIConnector {

	// private Socket socket;
	private int messageIdx = 0;
	private String address;
	private int port;

	public int getMessageIdx() {
		return messageIdx;
	}

	public void setMessageIdx(int messageIdx) {
		this.messageIdx = messageIdx;
	}

	public GUIConnector(String address, int port) {
		// try {
		this.address = address;
		this.port = port;
		// this.socket = new Socket(address, port);
		// System.out.println("guiConnector : " + socket);
		// } catch (IOException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
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
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void destroy() {
		// try {
		// this.socket.close();
		// } catch (IOException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
	}
}
