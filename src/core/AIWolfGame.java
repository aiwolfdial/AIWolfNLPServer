package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import core.exception.IllegalPlayerNumberException;
import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.GameSetting;
import core.model.Guard;
import core.model.Judge;
import core.model.Role;
import core.model.Species;
import core.model.Status;
import core.model.Talk;
import core.model.Team;
import core.model.Vote;
import libs.Counter;
import libs.FileGameLogger;

public class AIWolfGame {
	private static final Logger logger = LogManager.getLogger(AIWolfGame.class);

	protected GameConfiguration gameConfiguration;
	protected GameSetting gameSetting;
	protected GameServer gameServer;
	protected Map<Integer, GameData> gameDataMap;
	protected GameData gameData;
	protected FileGameLogger gameLogger;
	protected Map<Agent, String> agentNameMap;

	public AIWolfGame(GameConfiguration gameConfiguration, GameSetting gameSetting, GameServer gameServer) {
		this.gameConfiguration = gameConfiguration;
		this.gameSetting = gameSetting;
		this.gameServer = gameServer;
	}

	public void setGameLogger(FileGameLogger gameLogger) {
		this.gameLogger = gameLogger;
	}

	protected void initialize() {
		gameDataMap = new TreeMap<>();
		agentNameMap = new HashMap<>();
		gameServer.setGameData(gameData);

		List<Agent> agentList = gameServer.getConnectedAgentList();

		if (agentList.size() != gameSetting.getPlayerNum()) {
			throw new IllegalPlayerNumberException(
					"Player num is " + gameSetting.getPlayerNum() + " but connected agent is " + agentList.size());
		}

		Collections.shuffle(agentList);

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
			String requestName = gameServer.getName(agent);
			agentNameMap.put(agent, requestName);
		}
	}

	private boolean existsCombinationsText(GameConfiguration config, String text) {
		File file = new File(config.getRoleCombinationDir() + config.getRoleCombinationFilename());
		if (!file.exists()) {
			return false;
		}
		try (BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
			String doneCombinationText;
			while ((doneCombinationText = bufferReader.readLine()) != null) {
				if (doneCombinationText.equals(text)) {
					return true;
				}
			}
		} catch (IOException e) {
			logger.error("Exception", e);
		}
		return false;
	}

	public void start(GameData gameData) {
		this.gameData = gameData;
		gameDataMap = new TreeMap<>();
		agentNameMap = new HashMap<>();
		gameServer.setGameData(gameData);

		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameSetting(gameSetting);
		for (Agent agent : gameServer.getConnectedAgentList()) {
			gameServer.init(agent);
			String requestName = gameServer.getName(agent);
			agentNameMap.put(agent, requestName);
		}

		if (gameConfiguration.isSaveRoleCombination()) {
			if (existsCombinationsText(gameConfiguration, getCombinationsText())) {
				finish();
				return;
			}
		}

		try {
			initialize();
			while (!isGameFinished()) {
				consoleLog();

				day();
				night();
				if (gameLogger != null) {
					gameLogger.flush();
				}
			}
			consoleLog();

			if (gameConfiguration.isSaveRoleCombination()) {
				try {
					File file = new File(
							gameConfiguration.getRoleCombinationDir() + gameConfiguration.getRoleCombinationFilename());
					if (!file.canWrite()) {
						file.setWritable(true);
					}
					FileWriter fileWriter = new FileWriter(file, true);
					fileWriter.write(getCombinationsText());
					fileWriter.write("\r\n");
					fileWriter.close();
				} catch (Exception e) {
					logger.error("Exception", e);
				}
			}

			finish();
			logger.info(String.format("Winner: %s", getWinner()));
		} catch (LostAgentConnectionException e) {
			if (gameLogger != null) {
				gameLogger.log("Lost Connection of " + e.agent);
			}
			throw e;
		}
	}

	private String getCombinationsText() {
		List<String> combinationText = new ArrayList<>();
		gameData.getAgentList().stream()
				.sorted()
				.forEach(agent -> {
					String agentName = agentNameMap.get(agent).replaceAll("[0-9]", "");
					combinationText.add(String.format("%s,%s", gameData.getRole(agent), agentName));
				});
		Collections.sort(combinationText);
		return String.join(",", combinationText);
	}

	public void finish() {
		if (gameLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgentList())) {
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.agentIdx,
						gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
			gameLogger.log(String.format("%d,result,%d,%d,%s", gameData.getDay(), getAliveHumanList().size(),
					getAliveWolfList().size(), getWinner()));
			gameLogger.close();
		}
		for (Agent agent : gameData.getAgentList()) {
			gameServer.finish(agent);
		}
	}

	private Team getWinner() {
		int humanSide = 0;
		int wolfSide = 0;
		int otherSide = 0;
		for (Agent agent : gameData.getAgentList()) {
			if (gameData.getStatus(agent) == Status.DEAD) {
				continue;
			}

			if (gameData.getRole(agent).team == Team.OTHERS) {
				otherSide++;
			}
			if (gameData.getRole(agent).species == Species.HUMAN) {
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
				logger.info(String.format("Vote: %s->%s", vote.agent(), vote.target()));
			}
			for (Vote vote : yesterday.getAttackVoteList()) {
				logger.info(String.format("AttackVote: %s->%s", vote.agent(), vote.target()));
			}
			logger.info(String.format("Executed: %s", yesterday.getExecuted()));
			Judge divine = yesterday.getDivine();
			if (divine != null) {
				logger.info(String.format("Divine: %s->%s", divine.agent(), divine.target()));
			}
			Guard guard = yesterday.getGuard();
			if (guard != null) {
				logger.info(String.format("Guard: %s->%s", guard.agent(), guard.target()));
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
		agentList.sort(Comparator.comparingInt(o -> o.agentIdx));
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
				if (divine != null && divine.target() == agent) {
					logBuilder.append("\tDivined");
				}
				Guard guard = yesterday.getGuard();
				if (guard != null && guard.target() == agent) {
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

	protected void night() {
		for (Agent agent : gameData.getAgentList()) {
			gameServer.dayFinish(agent);
		}

		if (!gameSetting.isTalkOnFirstDay() && gameData.getDay() == 0) {
			whisper();
		}

		Agent executed = null;
		List<Agent> candidates = null;
		if (gameData.getDay() != 0) {
			for (int i = 0; i <= gameSetting.maxRevote(); i++) {
				vote();
				candidates = getVotedCandidates(gameData.getVoteList());
				if (candidates.size() == 1) {
					executed = candidates.getFirst();
					break;
				}
			}

			if (executed == null) {
				Collections.shuffle(candidates);
				executed = candidates.getFirst();
			}

			if (executed != null) {
				gameData.setExecutedTarget(executed);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,execute,%d,%s", gameData.getDay(), executed.agentIdx,
							gameData.getRole(executed)));
				}
			}
		}

		divine();

		if (gameData.getDay() != 0) {
			whisper();
			guard();

			Agent attacked = null;
			if (!getAliveWolfList().isEmpty()) {
				for (int i = 0; i <= gameSetting.maxAttackRevote(); i++) {
					attackVote();
					List<Vote> attackCandidateList = gameData.getAttackVoteList();
					Iterator<Vote> it = attackCandidateList.iterator();
					while (it.hasNext()) {
						Vote vote = it.next();
						if (vote.agent() == executed) {
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
					Collections.shuffle(candidates);
					attacked = candidates.getFirst();
				}

				gameData.setAttackedTarget(attacked);

				boolean isGuarded = false;
				if (gameData.getGuard() != null) {
					if (gameData.getGuard().target() == attacked && attacked != null) {
						if (gameData.getExecuted() == null
								|| !(gameData.getExecuted() == gameData.getGuard().agent())) {
							isGuarded = true;
						}
					}
				}
				if (!isGuarded && attacked != null && gameData.getRole(attacked) != Role.FOX) {
					gameData.setAttackedDead(attacked);
					gameData.addLastDeadAgent(attacked);

					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,true", gameData.getDay(), attacked.agentIdx));
					}
				} else if (attacked != null) {
					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,false", gameData.getDay(), attacked.agentIdx));
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

	protected List<Agent> getVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.target()) == Status.ALIVE) {
				counter.add(vote.target());
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

	protected List<Agent> getAttackVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.target()) == Status.ALIVE
					&& gameData.getRole(vote.target()) != Role.WEREWOLF) {
				counter.add(vote.target());
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

	protected void dayStart() {
		if (gameLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgentList())) {
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.agentIdx,
						gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
		}

		for (Agent agent : gameData.getAgentList()) {
			gameServer.dayStart(agent);
		}
	}

	protected void talk() {
		List<Agent> aliveList = getAliveAgentList();
		for (Agent agent : aliveList) {
			gameData.remainTalkMap.put(agent, gameSetting.maxTalk());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for (int time = 0; time < gameSetting.maxTalkTurn(); time++) {
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
				if (talkText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if (skipCounter.get(agent) > gameSetting.maxSkip()) {
						talkText = Talk.OVER;
					}
				} else if (talkText.equals(Talk.FORCE_SKIP)) {
					talkText = Talk.SKIP;
				}
				Talk talk = new Talk(gameData.nextTalkIdx(), gameData.getDay(), time, agent, talkText);
				gameData.addTalk(talk.agent(), talk);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,talk,%d,%d,%d,%s", gameData.getDay(), talk.idx(),
							talk.turn(), talk.agent().agentIdx, talk.text()));
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
		if (aliveWolfList.size() == 1) {
			return;
		}
		for (Agent agent : aliveWolfList) {
			gameData.remainWhisperMap.put(agent, gameSetting.maxWhisper());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for (int turn = 0; turn < gameSetting.maxWhisperTurn(); turn++) {
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
				if (whisperText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if (skipCounter.get(agent) > gameSetting.maxSkip()) {
						whisperText = Talk.OVER;
					}
				}
				Talk whisper = new Talk(gameData.nextWhisperIdx(), gameData.getDay(), turn, agent, whisperText);
				gameData.addWhisper(whisper.agent(), whisper);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,whisper,%d,%d,%d,%s", gameData.getDay(), whisper.idx(),
							whisper.turn(), whisper.agent().agentIdx, whisper.text()));
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
				gameLogger.log(String.format("%d,vote,%d,%d", gameData.getDay(), vote.agent().agentIdx,
						vote.target().agentIdx));
			}
		}
	}

	protected void divine() {
		for (Agent agent : getAliveAgentList()) {
			if (gameData.getRole(agent) == Role.SEER) {
				Agent target = gameServer.requestDivineTarget(agent);
				Role targetRole = gameData.getRole(target);
				if (gameData.getStatus(target) == Status.DEAD || target == null || targetRole == null) {
				} else {
					Judge divine = new Judge(gameData.getDay(), agent, target, targetRole.species);
					gameData.setDivine(divine);

					if (gameData.getRole(target) == Role.FOX) {
						gameData.addLastDeadAgent(target);
						gameData.setCursedFox(target);
					}

					if (gameLogger != null) {
						gameLogger.log(String.format("%d,divine,%d,%d,%s", gameData.getDay(),
								divine.agent().agentIdx, divine.target().agentIdx, divine.result()));
					}
				}
			}
		}
	}

	protected void guard() {
		for (Agent agent : getAliveAgentList()) {
			if (gameData.getRole(agent) == Role.BODYGUARD) {
				if (agent == gameData.getExecuted()) {
					continue;
				}
				Agent target = gameServer.requestGuardTarget(agent);
				if (target == null || gameData.getStatus(target) == null || agent == target) {
				} else {
					Guard guard = new Guard(gameData.getDay(), agent, target);
					gameData.setGuard(guard);

					if (gameLogger != null) {
						gameLogger.log(
								String.format("%d,guard,%d,%d,%s", gameData.getDay(), guard.agent().agentIdx,
										guard.target().agentIdx, gameData.getRole(guard.target())));
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
			} else {
				Vote attackVote = new Vote(gameData.getDay(), agent, target);
				gameData.addAttack(attackVote);

				if (gameLogger != null) {
					gameLogger.log(String.format("%d,attackVote,%d,%d", gameData.getDay(),
							attackVote.agent().agentIdx, attackVote.target().agentIdx));
				}
			}
		}
		List<Vote> latestAttackVoteList = new ArrayList<>(gameData.getAttackVoteList());
		gameData.setLatestAttackVoteList(latestAttackVoteList);
	}

	protected Agent getRandomAgent(List<Agent> agentList, Agent... without) {
		List<Agent> list = new ArrayList<>(agentList);
		list.removeAll(Arrays.asList(without));
		return list.get(new Random().nextInt(list.size()));
	}

	protected List<Agent> getAliveAgentList() {
		return gameData.getAgentList().stream()
				.filter(agent -> gameData.getStatus(agent) == Status.ALIVE)
				.collect(Collectors.toList());
	}

	protected List<Agent> getAliveHumanList() {
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.HUMAN);
	}

	protected List<Agent> getAliveWolfList() {
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.WEREWOLF);
	}

	public boolean isGameFinished() {
		return getWinner() != null;
	}
}
