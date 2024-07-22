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

	private static final String LOST_CONNECTION_MESSAGE = "%s(%s:%s)_[Request:%s] lostConnection";

	private String name = null;
	private Config config;
	private Agent agent;

	private boolean isAlive = true;
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;

	private boolean hasException = false;
	private Exception exception = null;
	private Exception causeException = null;
	private Request causeRequest = null;

	public String getName() {
		if (name != null)
			return name;
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
			name = line.isEmpty() ? null : line;
		} catch (Exception e) {
			if (isAlive) {
				logger.error("Exception", e);
			}
			throwException(agent, e, Request.NAME);
		}
		return name;
	}

	public Connection(Socket socket, Config gameConfiguration, Set<Integer> usedNumberSet) {
		this.socket = socket;
		this.config = gameConfiguration;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			logger.error("Exception", e);
		}

		int agentNum = 1;
		int humanNum = gameConfiguration.joinHuman() ? gameConfiguration.humanAgentNum() : -1;
		String name = getName();
		if (name != null && name.equals(gameConfiguration.humanName()) && gameConfiguration.joinHuman()) {
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
		logger.log(String.format(LOST_CONNECTION_MESSAGE, name, agent, role, causeRequest));
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
