package net.kanolab.aiwolf.server.client;

import org.aiwolf.common.net.TcpipClient;

/**
 * 自動接続用クライアントクラス（おそらくAIWolf標準のTCPIPClientでも良いが、デバッグなどを仕込むために一応分ける）
 */
public class NLPTCPIPClient extends TcpipClient {

	public NLPTCPIPClient(String hostname, int port) {
		super(hostname, port);
	}

}
