package core;

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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.Packet;
import core.model.Request;
import core.model.Role;
import libs.CallableBufferedReader;
import libs.FileGameLogger;
import libs.Pair;
import utils.JsonParser;

public class AIWolfConnection {
	private static final Logger logger = LogManager.getLogger(AIWolfConnection.class);

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
			bw.append(JsonParser.encode(new Packet(Request.NAME)));
			bw.append("\n");
			bw.flush();

			// 結果の受け取りとタイムアウト
			CallableBufferedReader task = new CallableBufferedReader(getBufferedReader());
			Future<String> future = pool.submit(task);
			String line = config.getResponseTimeout() > 0 ? future.get(
					config.getResponseTimeout(), TimeUnit.MILLISECONDS) : future.get();
			if (!task.isSuccess()) {
				throw task.getIOException();
			}

			this.name = (line == null || line.isEmpty()) ? null : line;
		} catch (Exception e) {
			if (isAlive) {
				logger.error("Exception", e);
			}
			catchException(agent, e, Request.NAME);
		}
		return name;
	}

	public AIWolfConnection(Socket socket, GameConfiguration config) {
		try {
			this.isAlive = true;
			this.haveNewError = false;
			this.socket = socket;
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.config = config;
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public void reportError(FileGameLogger logger, Agent agent, Role role) {
		// 未報告のエラーがなければ終了
		if (!haveNewError)
			return;

		logger.log(String.format(LOST_CONNECTION_MESSAGE, name, agent, role, exception.value()));
		for (StackTraceElement stackTraceElement : exception.key().getStackTrace()) {
			logger.log(stackTraceElement.toString());
			logger.flush();
		}
		if (subException == null)
			return;
		logger.log(subException.getClass() + ": " + subException.getMessage());
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
			logger.error("Exception", e);
		}
	}

	public void catchException(Agent agent, Exception e, Request request) {
		isAlive = false;
		haveNewError = true;
		this.subException = e;
		this.exception = new Pair<>(new LostAgentConnectionException(e,
				agent), request);
		close();
	}
}
