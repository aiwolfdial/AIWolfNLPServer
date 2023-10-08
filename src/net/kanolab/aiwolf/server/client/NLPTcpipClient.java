package net.kanolab.aiwolf.server.client;

import org.aiwolf.common.net.TcpipClient;

/**
 * 自動接続用クライアントクラス（おそらくAIWolf標準のTCPIPClientでも良いが、デバッグなどを仕込むために一応分ける）
 */
public class NLPTcpipClient extends TcpipClient {

	public NLPTcpipClient(String host, int port) {
		super(host, port);
		// TODO 自動生成されたコンストラクター・スタブ
	}

}
