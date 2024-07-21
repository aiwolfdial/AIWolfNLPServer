/**
 * AIWolfGame.java
 *
 * Copyright (c) 2014 人狼知能プロジェクト
 */
package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import client.Content;
import common.data.Agent;
import common.data.Guard;
import common.data.Judge;
import common.data.Role;
import common.data.Species;
import common.data.Status;
import common.data.Talk;
import common.data.Team;
import common.data.Vote;
import common.net.GameSetting;
import common.util.Counter;
import server.exception.IllegalPlayerNumException;
import server.exception.LostClientException;
import server.net.GameServer;
import server.utility.FileGameLogger;

/**
 * Game Class of AI Wolf Contest
 * 
 * @author tori and otsuki
 *
 */
public class AIWolfGame {
	private static final Logger logger = LogManager.getLogger(AIWolfGame.class);

	protected Random rand;

	/**
	 * Settings of the game
	 */
	protected GameSetting gameSetting;

	/**
	 * server to connect clients
	 */
	protected GameServer gameServer;

	/**
	 *
	 */
	protected Map<Integer, GameData> gameDataMap;

	/**
	 *
	 */
	protected GameData gameData;

	/**
	 * ログを記録するファイル
	 */
	// protected File logFile;

	/**
	 * Logger
	 */
	protected FileGameLogger gameLogger;

	/**
	 * Name of Agents
	 */
	protected Map<Agent, String> agentNameMap;

	/**
	 *
	 */
	public AIWolfGame(GameSetting gameSetting, GameServer gameServer) {
		rand = new Random();
		this.gameSetting = gameSetting;
		this.gameServer = gameServer;

		// gameLogger =
		// AiWolfLoggerFactory.getSimpleLogger(this.getClass().getSimpleName());
	}

	/**
	 * @return logFile
	 */
	// public File getLogFile() {
	// return logFile;
	// }

	/**
	 * @param logFile
	 *            セットする logFile
	 * @throws IOException
	 */
	public void setLogFile(File logFile) throws IOException {
		// this.logFile = logFile;
		gameLogger = new FileGameLogger(logFile);
	}

	/**
	 * set GameLogger
	 * 
	 * @param gameLogger
	 */
	public void setGameLogger(FileGameLogger gameLogger) {
		this.gameLogger = gameLogger;
	}

	/**
	 * get GameLogger
	 */
	public FileGameLogger getGameLogger() {
		return this.gameLogger;
	}

	/**
	 * Set Random Class
	 * 
	 * @param rand
	 */
	public void setRand(Random rand) {
		this.rand = rand;
	}

	/**
	 * Initialize Game
	 */
	protected void init() {
		gameDataMap = new TreeMap<>();
		gameData = new GameData(gameSetting);
		agentNameMap = new HashMap<>();
		gameServer.setGameData(gameData);

		List<Agent> agentList = gameServer.getConnectedAgentList();

		if (agentList.size() != gameSetting.getPlayerNum()) {
			throw new IllegalPlayerNumException(
					"Player num is " + gameSetting.getPlayerNum() + " but connected agent is " + agentList.size());
		}

		Collections.shuffle(agentList, rand);

		Map<Role, List<Agent>> requestRoleMap = new HashMap<>();
		for (Role role : Role.values()) {
			requestRoleMap.put(role, new ArrayList<>());
		}
		List<Agent> noRequestAgentList = new ArrayList<>();
		for (Agent agent : agentList) {
			if (gameSetting.isEnableRoleRequest()) {
				Role requestedRole = gameServer.requestRequestRole(agent);
				if (requestedRole != null) {
					if (requestRoleMap.get(requestedRole).size() < gameSetting.getRoleNum(requestedRole)) {
						requestRoleMap.get(requestedRole).add(agent);
					} else {
						noRequestAgentList.add(agent);
					}
				} else {
					noRequestAgentList.add(agent);
				}
			} else {
				noRequestAgentList.add(agent);
			}
		}

		for (Role role : Role.values()) {
			List<Agent> requestedAgentList = requestRoleMap.get(role);
			for (int i = 0; i < gameSetting.getRoleNum(role); i++) {
				if (requestedAgentList.isEmpty()) {
					gameData.addAgent(noRequestAgentList.removeFirst(), Status.ALIVE, role);
				} else {
					gameData.addAgent(requestedAgentList.removeFirst(), Status.ALIVE, role);
				}
			}
		}

		gameDataMap.put(gameData.getDay(), gameData);

		gameServer.setGameSetting(gameSetting);
		for (Agent agent : agentList) {
			gameServer.init(agent);
			String requestName = gameServer.requestName(agent);
			agentNameMap.put(agent, requestName);
		}
	}

	/**
	 * Start game
	 */
	public void start() {
		try {
			init();
			while (!isGameFinished()) {
				consoleLog();

				day();
				night();
				if (gameLogger != null) {
					gameLogger.flush();
				}
			}
			consoleLog();
			finish();
			logger.info(String.format("Winner: %s", getWinner()));
		} catch (LostClientException e) {
			if (gameLogger != null) {
				gameLogger.log("Lost Connection of " + e.getAgent());
			}
			throw e;
		}
	}

	public void finish() {
		if (gameLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgentList())) {
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.getAgentIdx(),
						gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
			gameLogger.log(String.format("%d,result,%d,%d,%s", gameData.getDay(), getAliveHumanList().size(),
					getAliveWolfList().size(), getWinner()));
			gameLogger.close();
		}

		for (Agent agent : gameData.getAgentList()) {
			gameServer.finish(agent);
		}
		/*
		 * try {
		 * Thread.sleep(1000);
		 * } catch (InterruptedException e) {
		 * e.printStackTrace();
		 * }
		 */
	}

	/**
	 * Get won team.
	 * if game not finished, return null
	 * 
	 * @return
	 */
	public Team getWinner() {
		int humanSide = 0;
		int wolfSide = 0;
		int otherSide = 0;
		for (Agent agent : gameData.getAgentList()) {
			if (gameData.getStatus(agent) == Status.DEAD) {
				continue;
			}

			if (gameData.getRole(agent).getTeam() == Team.OTHERS) {
				otherSide++;
			}
			if (gameData.getRole(agent).getSpecies() == Species.HUMAN) {
				humanSide++;
			} else {
				wolfSide++;
			}
		}
		if (wolfSide == 0) {
			if (otherSide > 0) {
				return Team.OTHERS;
			}
			return Team.VILLAGER;
		} else if (humanSide <= wolfSide) {
			if (otherSide > 0) {
				return Team.OTHERS;
			}
			return Team.WEREWOLF;
		} else {
			return null;
		}

	}

	private void consoleLog() {
		GameData yesterday = gameData.getDayBefore();

		logger.info("=============================================");
		if (yesterday != null) {
			logger.info(String.format("Day %02d", yesterday.getDay()));
			logger.info("========talk========");
			for (Talk talk : yesterday.getTalkList()) {
				logger.info(talk);
			}
			logger.info("========Whisper========");
			for (Talk whisper : yesterday.getWhisperList()) {
				logger.info(whisper);
			}
			logger.info("========Actions========");
			for (Vote vote : yesterday.getVoteList()) {
				logger.info(String.format("Vote: %s->%s", vote.getAgent(), vote.getTarget()));
			}
			for (Vote vote : yesterday.getAttackVoteList()) {
				logger.info(String.format("AttackVote: %s->%s", vote.getAgent(), vote.getTarget()));
			}
			logger.info(String.format("Executed: %s", yesterday.getExecuted()));
			Judge divine = yesterday.getDivine();
			if (divine != null) {
				logger.info(String.format("Divine: %s->%s", divine.getAgent(), divine.getTarget()));
			}
			Guard guard = yesterday.getGuard();
			if (guard != null) {
				logger.info(String.format("Guard: %s->%s", guard.getAgent(), guard.getTarget()));
			}
			if (yesterday.getAttackedDead() != null) {
				logger.info(String.format("Attacked: %s", yesterday.getAttackedDead()));
			}
			if (yesterday.getCursedFox() != null) {
				logger.info(String.format("Cursed: %s", yesterday.getCursedFox()));
			}
		}
		logger.info("======");
		List<Agent> agentList = gameData.getAgentList();
		agentList.sort((o1, o2) -> o1.getAgentIdx() - o2.getAgentIdx());
		for (Agent agent : agentList) {
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(String.format("%s\t%s\t%s\t%s", agent, agentNameMap.get(agent), gameData.getStatus(agent),
					gameData.getRole(agent)));
			if (yesterday != null) {
				if (yesterday.getExecuted() == agent) {
					logBuilder.append("\tExecuted");
				}
				if (agent == yesterday.getAttackedDead()) {
					logBuilder.append("\tAttacked");
				}
				Judge divine = yesterday.getDivine();
				if (divine != null && divine.getTarget() == agent) {
					logBuilder.append("\tDivined");
				}
				Guard guard = yesterday.getGuard();
				if (guard != null && guard.getTarget() == agent) {
					logBuilder.append("\tGuarded");
				}
				if (agent == yesterday.getCursedFox()) {
					logBuilder.append("\tCursed");
				}
			}
			logger.info(logBuilder.toString());
		}
		logger.info(String.format("Human: %d", getAliveHumanList().size()));
		logger.info(String.format("Werewolf: %d", getAliveWolfList().size()));
		if (gameSetting.getRoleNum(Role.FOX) != 0) {
			logger.info(String.format("Others: %d",
					gameData.getFilteredAgentList(getAliveAgentList(), Team.OTHERS).size()));
		}
		logger.info("=============================================");
	}

	protected void day() {
		dayStart();
		if (gameData.getDay() == 0) {
			if (gameSetting.isTalkOnFirstDay()) {
				whisper();
				talk();
			}
		} else {
			talk();
		}
	}

	/**
	 *
	 */
	protected void night() {

		// for (Agent agent : getAliveAgentList()) {
		// gameServer.dayFinish(agent);
		// }
		for (Agent agent : getGameData().getAgentList()) {
			gameServer.dayFinish(agent);
		}

		if (!gameSetting.isTalkOnFirstDay() && gameData.getDay() == 0) {
			whisper();
		}

		// Vote and execute except day 0
		Agent executed = null;
		List<Agent> candidates = null;
		if (gameData.getDay() != 0) {
			for (int i = 0; i <= gameSetting.getMaxRevote(); i++) {
				vote();
				candidates = getVotedCandidates(gameData.getVoteList());
				if (candidates.size() == 1) {
					executed = candidates.getFirst();
					break;
				}
			}

			if (executed == null && !gameSetting.isEnableNoExecution()) {
				Collections.shuffle(candidates, rand);
				executed = candidates.getFirst();
			}

			if (executed != null) {
				gameData.setExecutedTarget(executed);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,execute,%d,%s", gameData.getDay(), executed.getAgentIdx(),
							gameData.getRole(executed)));
				}
			}
		}

		// every day
		divine();

		if (gameData.getDay() != 0) {
			whisper();
			guard();

			// attackVote and attack except day 0
			Agent attacked = null;
			if (!getAliveWolfList().isEmpty()) {
				for (int i = 0; i <= gameSetting.getMaxAttackRevote(); i++) {
					if (i > 0 && gameSetting.isWhisperBeforeRevote()) {
						whisper();
					}
					attackVote();
					List<Vote> attackCandidateList = gameData.getAttackVoteList();
					Iterator<Vote> it = attackCandidateList.iterator();
					while (it.hasNext()) {
						Vote vote = it.next();
						if (vote.getAgent() == executed) {
							it.remove();
						}
					}
					candidates = getAttackVotedCandidates(attackCandidateList);
					if (candidates.size() == 1) {
						attacked = candidates.getFirst();
						break;
					}
				}

				if (attacked == null && !gameSetting.isEnableNoAttack()) {
					Collections.shuffle(candidates, rand);
					attacked = candidates.getFirst();
				}

				gameData.setAttackedTarget(attacked);

				boolean isGuarded = false;
				if (gameData.getGuard() != null) {
					if (gameData.getGuard().getTarget() == attacked && attacked != null) {
						if (gameData.getExecuted() == null
								|| !(gameData.getExecuted() == gameData.getGuard().getAgent())) {
							isGuarded = true;
						}
					}
				}
				if (!isGuarded && attacked != null && gameData.getRole(attacked) != Role.FOX) {
					gameData.setAttackedDead(attacked);
					gameData.addLastDeadAgent(attacked);

					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,true", gameData.getDay(), attacked.getAgentIdx()));
					}
				} else if (attacked != null) {
					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,false", gameData.getDay(), attacked.getAgentIdx()));
					}
				} else {
					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,-1,false", gameData.getDay()));
					}
				}
			}
		}

		gameData = gameData.nextDay();
		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameData(gameData);

	}

	/**
	 *
	 * @param voteList
	 * @return
	 */
	protected List<Agent> getVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.getTarget()) == Status.ALIVE) {
				counter.add(vote.getTarget());
			}
		}

		int max = counter.get(counter.getLargest());
		List<Agent> candidateList = new ArrayList<>();
		for (Agent agent : counter) {
			if (counter.get(agent) == max) {
				candidateList.add(agent);
			}
		}
		return candidateList;
	}

	/**
	 *
	 * @param voteList
	 * @return
	 */
	protected List<Agent> getAttackVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.getTarget()) == Status.ALIVE
					&& gameData.getRole(vote.getTarget()) != Role.WEREWOLF) {
				counter.add(vote.getTarget());
			}
		}
		if (!gameSetting.isEnableNoAttack()) {
			for (Agent agent : getAliveHumanList()) {
				counter.add(agent);
			}
		}

		int max = counter.get(counter.getLargest());
		List<Agent> candidateList = new ArrayList<>();
		for (Agent agent : counter) {
			if (counter.get(agent) == max) {
				candidateList.add(agent);
			}
		}
		return candidateList;
	}

	/**
	 *
	 */
	protected void dayStart() {
		if (gameLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgentList())) {
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.getAgentIdx(),
						gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
		}

		for (Agent agent : gameData.getAgentList()) {
			gameServer.dayStart(agent);
		}
	}

	/**
	 * 
	 */
	protected void talk() {
		List<Agent> aliveList = getAliveAgentList();
		for (Agent agent : aliveList) {
			gameData.remainTalkMap.put(agent, gameSetting.getMaxTalk());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for (int time = 0; time < gameSetting.getMaxTalkTurn(); time++) {
			Collections.shuffle(aliveList);

			boolean continueTalk = false;
			for (Agent agent : aliveList) {
				String talkText = Talk.OVER;
				if (gameData.getRemainTalkMap().get(agent) > 0) {
					talkText = gameServer.requestTalk(agent);
				}
				if (talkText == null || talkText.isEmpty()) {
					talkText = Talk.SKIP;
				}
				if (gameSetting.isValidateUtterance()) {
					if (!Content.validate(talkText)) {
						talkText = Talk.SKIP;
					}
				}
				if (talkText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if (skipCounter.get(agent) > gameSetting.getMaxSkip()) {
						talkText = Talk.OVER;
					}
				} else if (talkText.equals(Talk.FORCE_SKIP)) {
					talkText = Talk.SKIP;
				}
				Talk talk = new Talk(gameData.nextTalkIdx(), gameData.getDay(), time, agent, talkText);
				gameData.addTalk(talk.getAgent(), talk);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,talk,%d,%d,%d,%s", gameData.getDay(), talk.getIdx(),
							talk.getTurn(), talk.getAgent().getAgentIdx(), talk.getText()));
				}

				if (!talk.isOver() && !talk.isSkip()) {
					skipCounter.put(agent, 0);
				}
				if (!talk.isOver()) {
					continueTalk = true;
				}
			}

			if (!continueTalk) {
				break;
			}

		}
	}

	protected void whisper() {
		List<Agent> aliveWolfList = gameData.getFilteredAgentList(getAliveAgentList(), Role.WEREWOLF);
		// No whisper in case of lonely wolf.
		if (aliveWolfList.size() == 1) {
			return;
		}
		for (Agent agent : aliveWolfList) {
			gameData.remainWhisperMap.put(agent, gameSetting.getMaxWhisper());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for (int turn = 0; turn < gameSetting.getMaxWhisperTurn(); turn++) {
			Collections.shuffle(aliveWolfList);

			boolean continueWhisper = false;
			for (Agent agent : aliveWolfList) {
				String whisperText = Talk.OVER;
				if (gameData.getRemainWhisperMap().get(agent) > 0) {
					whisperText = gameServer.requestWhisper(agent);
				}
				if (whisperText == null || whisperText.isEmpty()) {
					whisperText = Talk.SKIP;
				}
				if (gameSetting.isValidateUtterance()) {
					if (!Content.validate(whisperText)) {
						whisperText = Talk.SKIP;
					}
				}
				if (whisperText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if (skipCounter.get(agent) > gameSetting.getMaxSkip()) {
						whisperText = Talk.OVER;
					}
				}
				Talk whisper = new Talk(gameData.nextWhisperIdx(), gameData.getDay(), turn, agent, whisperText);
				gameData.addWhisper(whisper.getAgent(), whisper);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,whisper,%d,%d,%d,%s", gameData.getDay(), whisper.getIdx(),
							whisper.getTurn(), whisper.getAgent().getAgentIdx(), whisper.getText()));
				}

				if (!whisper.isOver() && !whisper.isSkip()) {
					skipCounter.put(agent, 0);
				}
				if (!whisper.isOver()) {
					continueWhisper = true;
				}
			}

			if (!continueWhisper) {
				break;
			}
		}
	}

	/**
	 * <div lang="ja">投票</div>
	 *
	 * <div lang="en">Vote</div>
	 *
	 */
	protected void vote() {
		gameData.getVoteList().clear();
		List<Agent> voters = getAliveAgentList();
		List<Vote> latestVoteList = new ArrayList<>();
		for (Agent agent : voters) {
			Agent target = gameServer.requestVote(agent);
			if (target == null || gameData.getStatus(target) == null || gameData.getStatus(target) == Status.DEAD
					|| agent == target) {
				target = getRandomAgent(voters, agent);
			}
			Vote vote = new Vote(gameData.getDay(), agent, target);
			gameData.addVote(vote);
			latestVoteList.add(vote);
		}
		gameData.setLatestVoteList(latestVoteList);

		for (Vote vote : latestVoteList) {
			if (gameLogger != null) {
				gameLogger.log(String.format("%d,vote,%d,%d", gameData.getDay(), vote.getAgent().getAgentIdx(),
						vote.getTarget().getAgentIdx()));
			}
		}
	}

	/**
	 *
	 */
	protected void divine() {
		for (Agent agent : getAliveAgentList()) {
			if (gameData.getRole(agent) == Role.SEER) {
				Agent target = gameServer.requestDivineTarget(agent);
				Role targetRole = gameData.getRole(target);
				if (gameData.getStatus(target) == Status.DEAD || target == null || targetRole == null) {
					// target = getRandomAgent(agentList, agent);
				} else {
					Judge divine = new Judge(gameData.getDay(), agent, target, targetRole.getSpecies());
					gameData.addDivine(divine);

					// FOX
					if (gameData.getRole(target) == Role.FOX) {
						gameData.addLastDeadAgent(target);
						gameData.setCursedFox(target);
					}

					if (gameLogger != null) {
						gameLogger.log(String.format("%d,divine,%d,%d,%s", gameData.getDay(),
								divine.getAgent().getAgentIdx(), divine.getTarget().getAgentIdx(), divine.getResult()));
					}
				}
			}
		}
	}

	/**
	 *
	 */
	protected void guard() {
		for (Agent agent : getAliveAgentList()) {
			if (gameData.getRole(agent) == Role.BODYGUARD) {
				if (agent == gameData.getExecuted()) {
					continue;
				}
				Agent target = gameServer.requestGuardTarget(agent);
				if (target == null || gameData.getStatus(target) == null || agent == target) {
					// target = getRandomAgent(agentList, agent);
				} else {
					Guard guard = new Guard(gameData.getDay(), agent, target);
					gameData.addGuard(guard);

					if (gameLogger != null) {
						gameLogger.log(
								String.format("%d,guard,%d,%d,%s", gameData.getDay(), guard.getAgent().getAgentIdx(),
										guard.getTarget().getAgentIdx(), gameData.getRole(guard.getTarget())));
					}
				}
			}
		}
	}

	protected void attackVote() {
		gameData.getAttackVoteList().clear();
		for (Agent agent : getAliveWolfList()) {
			Agent target = gameServer.requestAttackTarget(agent);
			if (target == null || gameData.getStatus(target) == null || gameData.getStatus(target) == Status.DEAD
					|| gameData.getRole(target) == Role.WEREWOLF) {
				// target = getRandomAgent(candidateList, agent);
			} else {
				Vote attackVote = new Vote(gameData.getDay(), agent, target);
				gameData.addAttack(attackVote);

				if (gameLogger != null) {
					gameLogger.log(String.format("%d,attackVote,%d,%d", gameData.getDay(),
							attackVote.getAgent().getAgentIdx(), attackVote.getTarget().getAgentIdx()));
				}
			}
		}
		List<Vote> latestAttackVoteList = new ArrayList<>(gameData.getAttackVoteList());
		gameData.setLatestAttackVoteList(latestAttackVoteList);
	}

	/**
	 * ランダムなエージェントを獲得する．ただし，withoutを除く．
	 * 
	 * @param agentList
	 * @param without
	 * @return
	 */
	protected Agent getRandomAgent(List<Agent> agentList, Agent... without) {
		Agent target;
		List<Agent> list = new ArrayList<>(agentList);
		for (Agent agent : without) {
			list.remove(agent);
		}
		target = list.get(rand.nextInt(list.size()));
		return target;
	}

	/**
	 * get alive agents
	 * 
	 * @return
	 */
	protected List<Agent> getAliveAgentList() {
		List<Agent> agentList = new ArrayList<>();
		for (Agent agent : gameData.getAgentList()) {
			if (gameData.getStatus(agent) == Status.ALIVE) {
				agentList.add(agent);
			}
		}
		return agentList;
	}

	protected List<Agent> getAliveHumanList() {
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.HUMAN);
	}

	protected List<Agent> getAliveWolfList() {
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.WEREWOLF);
	}

	/**
	 * return is game finished
	 * 
	 * @return
	 */
	public boolean isGameFinished() {
		Team winner = getWinner();
		return winner != null;
	}

	/**
	 * get all data of the game
	 * 
	 * @return
	 */
	public GameData getGameData() {
		return gameData;
	}

	/**
	 * get setting of the game
	 * 
	 * @return
	 */
	public GameSetting getGameSetting() {
		return gameSetting;
	}

	/**
	 *
	 * @param agent
	 * @return
	 */
	public String getAgentName(Agent agent) {
		return agentNameMap.get(agent);
	}

}
