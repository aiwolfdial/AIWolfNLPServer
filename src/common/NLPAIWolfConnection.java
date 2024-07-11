package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Request;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.DataConverter;
import org.aiwolf.common.net.Packet;
import org.aiwolf.common.util.Pair;
import org.aiwolf.server.LostClientException;
import org.aiwolf.server.util.FileGameLogger;

public class NLPAIWolfConnection {
	// エラーログ内のメッセージ
	private static final String LOST_CONNECTION_MESSAGE = "%s(%s:%s)_[Request:%s] lostConnection";

	private boolean isAlive;
	private boolean haveNewError;
	private Socket socket;
	private BufferedReader br;
	private BufferedWriter bw;
	private Pair<Exception, Request> exception = null;
	private Exception subException = null;
	private String name = null;
	private GameConfiguration config;
	private Agent agent;

	public String getName() {
		try {
			if (name != null)
				return name;

			ExecutorService pool = Executors.newSingleThreadExecutor();
			// clientにrequestを送信し、結果を受け取る
			BufferedWriter bw = getBufferedWriter();
			bw.append(DataConverter.getInstance().convert(new Packet(Request.NAME)));
			bw.append("\n");
			bw.flush();

			// 結果の受け取りとタイムアウト
			BRCallable task = new BRCallable(getBufferedReader());
			Future<String> future = pool.submit(task);
			String line = config.getResponseTimeout() > 0 ? future.get(
					config.getResponseTimeout(), TimeUnit.MILLISECONDS) : future.get();
			if (!task.isSuccess()) {
				throw task.getIOException();
			}

			this.name = (line == null || line.isEmpty()) ? null : line;
		} catch (Exception e) {
			if (isAlive)
				e.printStackTrace();
			catchException(agent, e, Request.NAME);
		}
		return name;
	}

	public NLPAIWolfConnection(Socket socket, GameConfiguration config) {
		try {
			this.isAlive = true;
			this.haveNewError = false;
			this.socket = socket;
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.config = config;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	/**
	 * エラー出力
	 */
	public void reportError(FileGameLogger logger, Agent agent, Role role) {
		// 未報告のエラーがなければ終了
		if (!haveNewError)
			return;

		logger.log(String.format(LOST_CONNECTION_MESSAGE, name, agent, role, exception.getValue()));
		for (StackTraceElement stackTraceElement : exception.getKey().getStackTrace()) {
			logger.log(stackTraceElement.toString());
			logger.flush();
		}
		if (subException == null)
			return;
		logger.log(subException.getClass() + " : " + subException.getMessage());
		for (StackTraceElement stackTraceElement : subException.getStackTrace()) {
			logger.log(stackTraceElement.toString());
			logger.flush();
		}

		haveNewError = false;
	}

	public boolean haveNewError() {
		return haveNewError;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public Socket getSocket() {
		return socket;
	}

	public Exception getSubException() {
		return subException;
	}

	public BufferedReader getBufferedReader() {
		return br;
	}

	public BufferedWriter getBufferedWriter() {
		return bw;
	}

	public Pair<Exception, Request> getException() {
		return exception;
	}

	public void close() {
		try {
			this.br.close();
			this.bw.close();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * エラー情報の保存、コネクションの終了処理
	 * 
	 * @param agent
	 * @param e
	 * @param request
	 */
	public void catchException(Agent agent, Exception e, Request request) {
		isAlive = false;
		haveNewError = true;
		this.subException = e;
		LostClientException exception = new LostClientException("Lost connection with " + agent + "\t" + name, e,
				agent);
		this.exception = new Pair<Exception, Request>(exception, request);

		close();
	}
}
