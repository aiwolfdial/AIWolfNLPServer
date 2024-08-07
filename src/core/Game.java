package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import core.exception.DuplicateCombinationException;
import core.exception.IllegalPlayerNumberException;
import core.exception.LostAgentConnectionException;
import core.model.Agent;
import core.model.Config;
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
import libs.RawFileLogger;

public class Game {
	private static final Logger logger = LogManager.getLogger(Game.class);

	private final Config config;
	private final GameSetting gameSetting;
	private final GameServer gameServer;
	private GameData gameData;
	private RawFileLogger rawFileLogger;

	private final Map<Integer, GameData> gameDataMap;

	public Game(Config config, GameSetting gameSetting, GameServer gameServer, GameData gameData,
			Map<Agent, Role> roleMap,
			RawFileLogger rawFileLogger) throws IllegalPlayerNumberException, DuplicateCombinationException {
		this.config = config;
		this.gameSetting = gameSetting;
		this.gameServer = gameServer;
		this.gameData = gameData;
		this.rawFileLogger = rawFileLogger;
		this.gameDataMap = new TreeMap<>();

		logger.info("Initialize game.");
		gameServer.setGameData(gameData);
		gameServer.setGameSetting(gameSetting);

		List<Agent> agents = roleMap.keySet().stream().collect(Collectors.toList());
		if (agents.size() != gameSetting.getPlayerNum()) {
			throw new IllegalPlayerNumberException(
					String.format("Player num is %d but connected agent is %d", gameSetting.getPlayerNum(),
							agents.size()));
		}

		if (gameSetting.isEnableRoleRequest()) {
			Collections.shuffle(agents);
			Map<Role, List<Agent>> requestRoleAgents = new HashMap<>();
			for (Role role : Role.values()) {
				requestRoleAgents.put(role, new ArrayList<>());
			}
			List<Agent> noRequestAgents = new ArrayList<>();
			for (Agent agent : agents) {
				Role role = gameServer.requestRequestRole(agent);
				if (role != null && requestRoleAgents.get(role).size() < gameSetting.getRoleNum(role)) {
					requestRoleAgents.get(role).add(agent);
				} else {
					noRequestAgents.add(agent);
				}
			}
			for (Role role : Role.values()) {
				List<Agent> requestedAgents = requestRoleAgents.get(role);
				int roleNum = gameSetting.getRoleNum(role);
				for (int i = 0; i < roleNum; i++) {
					Agent agent = requestedAgents.isEmpty() ? noRequestAgents.removeFirst()
							: requestedAgents.removeFirst();
					gameData.addAgent(agent, Status.ALIVE, role);
					logger.info(String.format("Set role %s to %s", role, agent));
				}
			}
		} else {
			for (Agent agent : agents) {
				Role role = roleMap.get(agent);
				gameData.addAgent(agent, Status.ALIVE, role);
				logger.info(String.format("Set role %s to %s", role, agent));
			}
		}

		if (config.saveRoleCombination()) {
			if (existsCombinationsText(config, getCombinationsText())) {
				throw new DuplicateCombinationException(getCombinationsText());
			}
		}

		gameServer.setAgents(agents);

		gameDataMap.put(gameData.getDay(), gameData);
		for (Agent agent : agents) {
			gameServer.init(agent);
		}
	}

	private boolean existsCombinationsText(Config config, String text) {
		File file = new File(config.combinationsLogFilename());
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

	private void appendCombinationsText(Config config, String text) {
		File file = new File(config.combinationsLogFilename());
		try (RawFileLogger rawFileLogger = new RawFileLogger(file)) {
			rawFileLogger.log(text);
		} catch (IOException e) {
			logger.error("Exception", e);
		}
	}

	public void start() {
		try {
			while (!isFinished()) {
				logGameData();

				day();
				night();
				if (rawFileLogger != null) {
					rawFileLogger.flush();
				}
			}
			logGameData();
			if (config.saveRoleCombination()) {
				appendCombinationsText(config, getCombinationsText());
			}
			finish();
			logger.info("Finish game.");
			logger.info(String.format("Winner: %s", getWinner()));
		} catch (LostAgentConnectionException e) {
			if (rawFileLogger != null) {
				rawFileLogger.log("LostAgentConnectionException: " + e.agent);
			}
			throw e;
		}
	}

	private String getCombinationsText() {
		List<String> combinationText = new ArrayList<>();
		gameData.getAgents().stream()
				.sorted()
				.forEach(agent -> {
					String agentName = agent.name.replaceAll("[0-9]", "");
					combinationText.add(String.format("%s:%s", agentName, gameData.getRole(agent)));
				});
		Collections.sort(combinationText);
		return String.join("-", combinationText);
	}

	private void finish() {
		if (rawFileLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgents())) {
				rawFileLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.idx,
						gameData.getRole(agent), gameData.getStatus(agent), agent.name));
			}
			rawFileLogger.log(String.format("%d,result,%d,%d,%s", gameData.getDay(), gameData.getAliveHumans().size(),
					gameData.getAliveWolfs().size(), getWinner()));
			rawFileLogger.close();
		}
		for (Agent agent : gameData.getAgents()) {
			gameServer.finish(agent);
		}
	}

	private Team getWinner() {
		int humanSide = 0;
		int wolfSide = 0;
		int otherSide = 0;
		for (Agent agent : gameData.getAgents()) {
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

	private void logGameData() {
		GameData yesterday = gameData.getDayBefore();
		logger.info("### START GAME INFO ###");
		if (yesterday != null) {
			logger.info(String.format("Day%02d", yesterday.getDay()));
			logger.info("### Talk ###");
			for (Talk talk : yesterday.getTalkList()) {
				logger.info(talk);
			}
			logger.info("### Whisper ###");
			for (Talk whisper : yesterday.getWhisperList()) {
				logger.info(whisper);
			}
			logger.info("### Vote ###");
			for (Vote vote : yesterday.getVotes()) {
				logger.info(vote);
			}
			logger.info("### Attack Vote ###");
			for (Vote vote : yesterday.getAttackVotes()) {
				logger.info(vote);
			}
			logger.info("### Result ###");
			logger.info(String.format("Executed: %s", yesterday.getExecuted()));
			if (yesterday.getAttackedDead() != null) {
				logger.info(String.format("Attacked: %s", yesterday.getAttackedDead()));
			}
			if (yesterday.getCursedFox() != null) {
				logger.info(String.format("Cursed: %s", yesterday.getCursedFox()));
			}
			if (yesterday.getDivine() != null) {
				logger.info("### Divine ###");
				logger.info(yesterday.getDivine());
			}
			if (yesterday.getGuard() != null) {
				logger.info("### Guard ###");
				logger.info(yesterday.getGuard());
			}
		}
		logger.info("### Agent ###");
		List<Agent> agentList = gameData.getAgents();
		agentList.sort(Comparator.comparingInt(o -> o.idx));
		for (Agent agent : agentList) {
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append(String.format("%s\t%s\t%s\t%s", agent, agent.name, gameData.getStatus(agent),
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
		logger.info(String.format("Human: %d", gameData.getAliveHumans().size()));
		logger.info(String.format("Werewolf: %d", gameData.getAliveWolfs().size()));
		if (gameSetting.getRoleNum(Role.FOX) != 0) {
			logger.info(String.format("Others: %d",
					gameData.getAliveOthers().size()));
		}
		logger.info("### END GAME INFO ###");
	}

	private void day() {
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

	private void night() {
		for (Agent agent : gameData.getAgents()) {
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
				candidates = getVotedCandidates(gameData.getVotes());
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
				if (rawFileLogger != null) {
					rawFileLogger.log(String.format("%d,execute,%d,%s", gameData.getDay(), executed.idx,
							gameData.getRole(executed)));
				}
			}
		}
		divine();
		if (gameData.getDay() != 0) {
			whisper();
			guard();
			Agent attacked = null;
			if (!gameData.getAliveWolfs().isEmpty()) {
				for (int i = 0; i <= gameSetting.maxAttackRevote(); i++) {
					attackVote();
					List<Vote> attackCandidateList = gameData.getAttackVotes();
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

					if (rawFileLogger != null) {
						rawFileLogger.log(String.format("%d,attack,%d,true", gameData.getDay(), attacked.idx));
					}
				} else if (attacked != null) {
					if (rawFileLogger != null) {
						rawFileLogger.log(String.format("%d,attack,%d,false", gameData.getDay(), attacked.idx));
					}
				} else {
					if (rawFileLogger != null) {
						rawFileLogger.log(String.format("%d,attack,-1,false", gameData.getDay()));
					}
				}
			}
		}
		gameData = gameData.nextDay();
		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameData(gameData);
	}

	private List<Agent> getVotedCandidates(List<Vote> voteList) {
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

	private List<Agent> getAttackVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.target()) == Status.ALIVE
					&& gameData.getRole(vote.target()) != Role.WEREWOLF) {
				counter.add(vote.target());
			}
		}
		if (!gameSetting.isEnableNoAttack()) {
			for (Agent agent : gameData.getAliveHumans()) {
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

	private void dayStart() {
		if (rawFileLogger != null) {
			for (Agent agent : new TreeSet<>(gameData.getAgents())) {
				rawFileLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.idx,
						gameData.getRole(agent), gameData.getStatus(agent), agent.name));
			}
		}
		gameData.getAgents().forEach(gameServer::dayStart);
	}

	private void talk() {
		gameData.resetRemainTalkMap();
		Counter<Agent> skipCounter = new Counter<>();
		for (int time = 0; time < gameSetting.maxTalkTurn(); time++) {
			List<Agent> aliveAgents = new ArrayList<>(gameData.getAliveAgents());
			Collections.shuffle(aliveAgents);
			boolean continueTalk = false;
			for (Agent agent : aliveAgents) {
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
				if (rawFileLogger != null) {
					rawFileLogger.log(String.format("%d,talk,%d,%d,%d,%s", gameData.getDay(), talk.idx(),
							talk.turn(), talk.agent().idx, talk.text()));
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

	private void whisper() {
		if (!gameData.resetRemainWhisperMap()) {
			return;
		}
		Counter<Agent> skipCounter = new Counter<>();
		for (int turn = 0; turn < gameSetting.maxWhisperTurn(); turn++) {
			List<Agent> aliveWolfs = new ArrayList<>(gameData.getAliveWolfs());
			Collections.shuffle(aliveWolfs);
			boolean continueWhisper = false;
			for (Agent agent : aliveWolfs) {
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
				if (rawFileLogger != null) {
					rawFileLogger.log(String.format("%d,whisper,%d,%d,%d,%s", gameData.getDay(), whisper.idx(),
							whisper.turn(), whisper.agent().idx, whisper.text()));
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

	private void vote() {
		gameData.getVotes().clear();
		List<Agent> voters = gameData.getAliveAgents();
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
		if (rawFileLogger != null) {
			latestVoteList.forEach(vote -> rawFileLogger.log(String.format("%d,vote,%d,%d",
					gameData.getDay(), vote.agent().idx, vote.target().idx)));
		}
	}

	private void divine() {
		for (Agent agent : gameData.getAliveAgents()) {
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
					if (rawFileLogger != null) {
						rawFileLogger.log(String.format("%d,divine,%d,%d,%s", gameData.getDay(),
								divine.agent().idx, divine.target().idx, divine.result()));
					}
				}
			}
		}
	}

	private void guard() {
		for (Agent agent : gameData.getAliveAgents()) {
			if (gameData.getRole(agent) == Role.BODYGUARD) {
				if (agent == gameData.getExecuted()) {
					continue;
				}
				Agent target = gameServer.requestGuardTarget(agent);
				if (target == null || gameData.getStatus(target) == null || agent == target) {
				} else {
					Guard guard = new Guard(gameData.getDay(), agent, target);
					gameData.setGuard(guard);
					if (rawFileLogger != null) {
						rawFileLogger.log(
								String.format("%d,guard,%d,%d,%s", gameData.getDay(), guard.agent().idx,
										guard.target().idx, gameData.getRole(guard.target())));
					}
				}
			}
		}
	}

	private void attackVote() {
		gameData.getAttackVotes().clear();
		for (Agent agent : gameData.getAliveWolfs()) {
			Agent target = gameServer.requestAttackTarget(agent);
			if (target == null || gameData.getStatus(target) == null || gameData.getStatus(target) == Status.DEAD
					|| gameData.getRole(target) == Role.WEREWOLF) {
			} else {
				Vote attackVote = new Vote(gameData.getDay(), agent, target);
				gameData.addAttack(attackVote);
				if (rawFileLogger != null) {
					rawFileLogger.log(String.format("%d,attackVote,%d,%d", gameData.getDay(),
							attackVote.agent().idx, attackVote.target().idx));
				}
			}
		}
		List<Vote> latestAttackVoteList = new ArrayList<>(gameData.getAttackVotes());
		gameData.setLatestAttackVoteList(latestAttackVoteList);
	}

	private Agent getRandomAgent(List<Agent> agentList, Agent... without) {
		List<Agent> list = new ArrayList<>(agentList);
		list.removeAll(Arrays.asList(without));
		return list.get(new Random().nextInt(list.size()));
	}

	private boolean isFinished() {
		return getWinner() != null;
	}
}
