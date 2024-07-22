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
import core.model.Packet;
import core.model.Request;
import core.model.Role;
import libs.CallableBufferedReader;
import libs.FileGameLogger;
import utils.JsonParser;

public class AIWolfConnection {
	private static final Logger logger = LogManager.getLogger(AIWolfConnection.class);

	private static final String LOST_CONNECTION_MESSAGE = "%s(%s:%s)_[Request:%s] lostConnection";

	private String name = null;
	private GameConfiguration gameConfiguration;
	private Agent agent;

	private boolean isAlive = true;
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;

	private boolean reportError = false;
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
			String line = gameConfiguration.getResponseTimeout() > 0 ? future.get(
					gameConfiguration.getResponseTimeout(), TimeUnit.MILLISECONDS) : future.get();
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

	public AIWolfConnection(Socket socket, GameConfiguration gameConfiguration, Set<Integer> usedNumberSet) {
		this.socket = socket;
		this.gameConfiguration = gameConfiguration;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			logger.error("Exception", e);
		}

		int agentNum = 1;
		int humanNum = gameConfiguration.isJoinHuman() ? gameConfiguration.getHumanAgentNum() : -1;
		String name = getName();
		if (name != null && name.equals(gameConfiguration.getHumanName()) && gameConfiguration.isJoinHuman()) {
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

	public void reportError(FileGameLogger logger, Agent agent, Role role) {
		if (!reportError)
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
		reportError = false;
	}

	public boolean getReportError() {
		return reportError;
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
		reportError = true;
		exception = new LostAgentConnectionException(e, agent);
		causeException = e;
		causeRequest = request;
		close();
	}
}
