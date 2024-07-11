package client;

import org.aiwolf.common.net.TcpipClient;

/**
 * 自動接続用クライアントクラス（おそらくAIWolf標準のTCPIPClientでも良いが、デバッグなどを仕込むために一応分ける）
 */
public class NLPTcpipClient extends TcpipClient {

	public NLPTcpipClient(String hostname, int port) {
		super(hostname, port);
	}

}
