package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.Config;
import core.model.Packet;
import core.model.Request;
import core.model.Role;
import libs.CallableBufferedReader;
import libs.RawFileLogger;
import utils.JsonParser;

public class Connection {
	private static final Logger logger = LogManager.getLogger(Connection.class);

	private final Config config;
	private final Agent agent;
	private final Socket socket;
	private final BufferedReader bufferedReader;
	private final BufferedWriter bufferedWriter;
	private final String name;

	private boolean isAlive = true;
	private boolean hasException = false;
	private Exception exception = null;
	private Exception causeException = null;
	private Request causeRequest = null;

	private String requestName() {
		try {
			ExecutorService pool = Executors.newSingleThreadExecutor();
			bufferedWriter.append(JsonParser.encode(new Packet(Request.NAME)));
			bufferedWriter.append("\n");
			bufferedWriter.flush();

			CallableBufferedReader task = new CallableBufferedReader(getBufferedReader());
			Future<String> future = pool.submit(task);
			String line = config.responseTimeout() > 0 ? future.get(
					config.responseTimeout(), TimeUnit.MILLISECONDS) : future.get();
			if (!task.isSuccess()) {
				throw task.getIOException();
			}
			pool.shutdown();
			return line.isEmpty() ? null : line;
		} catch (Exception e) {
			logger.error("Exception", e);
			throwException(agent, e, Request.NAME);
		}
		return null;
	}

	public Connection(Socket socket, Config config, Set<Integer> usedNumberSet) throws IOException {
		this.socket = socket;
		this.config = config;
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		int agentNum = 1;
		int humanNum = config.joinHuman() ? config.humanAgentNum() : -1;
		name = requestName();
		if (name != null && name.equals(config.humanName()) && config.joinHuman()) {
			agentNum = humanNum;
		} else {
			while (usedNumberSet.contains(agentNum) || agentNum == humanNum)
				agentNum++;
		}
		agent = Agent.setAgent(agentNum, name);
	}

	public Agent getAgent() {
		return agent;
	}

	public void printException(RawFileLogger logger, Agent agent, Role role) {
		if (!hasException)
			return;
		logger.log(String.format("%s(%s:%s)_[Request:%s] lostConnection", name, agent, role, causeRequest));
		for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
			logger.log(stackTraceElement.toString());
			logger.flush();
		}
		if (causeException == null)
			return;
		logger.log(causeException.getClass() + ": " + causeException.getMessage());
		for (StackTraceElement stackTraceElement : causeException.getStackTrace()) {
			logger.log(stackTraceElement.toString());
			logger.flush();
		}
		hasException = false;
	}

	public boolean getHasException() {
		return hasException;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public Socket getSocket() {
		return socket;
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}

	public BufferedWriter getBufferedWriter() {
		return bufferedWriter;
	}

	public void close() {
		try {
			bufferedReader.close();
			bufferedWriter.close();
			socket.close();
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void throwException(Agent agent, Exception e, Request request) {
		isAlive = false;
		hasException = true;
		exception = new LostAgentConnectionException(e, agent);
		causeException = e;
		causeRequest = request;
		close();
	}
}
