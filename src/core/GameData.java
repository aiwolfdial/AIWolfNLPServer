package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import core.exception.AIWolfException;
import core.model.Agent;
import core.model.GameInfo;
import core.model.GameSetting;
import core.model.Guard;
import core.model.Judge;
import core.model.Role;
import core.model.Species;
import core.model.Status;
import core.model.Talk;
import core.model.Team;
import core.model.Vote;

public class GameData {
	protected int day;
	protected Map<Agent, Status> agentStatusMap = new LinkedHashMap<>();
	protected Map<Agent, Role> agentRoleMap = new HashMap<>();
	protected final List<Talk> talkList = new ArrayList<>();
	protected final List<Talk> whisperList = new ArrayList<>();
	protected final List<Vote> voteList = new ArrayList<>();
	protected List<Vote> latestVoteList = new ArrayList<>();
	protected final List<Vote> attackVoteList = new ArrayList<>();
	protected List<Vote> latestAttackVoteList = new ArrayList<>();
	protected final Map<Agent, Integer> remainTalkMap = new HashMap<>();
	protected final Map<Agent, Integer> remainWhisperMap = new HashMap<>();
	protected Judge divine;
	protected Guard guard;
	protected Agent executed;
	protected Agent attackedDead;
	protected Agent attacked;
	protected Agent cursedFox;
	protected final List<Agent> lastDeadAgentList = new ArrayList<>();
	protected List<Agent> suddenDeathList = new ArrayList<>();
	protected GameData dayBefore;
	protected int talkIdx;
	protected int whisperIdx;

	private final GameSetting gameSetting;

	public GameData(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}

	public GameInfo getGameInfo(Agent agent) {
		GameData today = this;
		GameInfo gameInfo = new GameInfo(today.day, agent);
		Role role = getRole(agent);

		if (gameSetting.isVoteVisible()) {
			gameInfo.latestVoteList = latestVoteList;
		}
		if (executed != null) {
			gameInfo.latestExecutedAgent = executed;
		}
		if (agent == null || role == Role.WEREWOLF) {
			gameInfo.latestAttackVoteList = latestAttackVoteList;
		}

		GameData yesterday = today.getDayBefore();
		if (yesterday != null) {
			if (yesterday.getExecuted() != null) {
				gameInfo.executedAgent = yesterday.getExecuted();
			}
			gameInfo.lastDeadAgentList = yesterday.lastDeadAgentList;
			if (gameSetting.isVoteVisible()) {
				gameInfo.voteList = yesterday.voteList;
			}
			if (agent != null && today.getRole(agent) == Role.MEDIUM && executed != null) {
				gameInfo.mediumResult = new Judge(day, agent, executed, yesterday.getRole(executed).species);
			}
			if (agent == null || today.getRole(agent) == Role.SEER) {
				Judge divine = yesterday.divine;
				if (divine != null && divine.target() != null) {
					gameInfo.divineResult = new Judge(day, divine.agent(), divine.target(),
							yesterday.getRole(divine.target()).species);
				}
			}
			if (agent == null || today.getRole(agent) == Role.WEREWOLF) {
				if (yesterday.attacked != null) {
					gameInfo.attackedAgent = yesterday.attacked;
				}
				gameInfo.attackVoteList = yesterday.attackVoteList;
			}
			if (agent == null || today.getRole(agent) == Role.BODYGUARD) {
				if (yesterday.guard != null) {
					gameInfo.guardedAgent = yesterday.guard.target();
				}
			}
			if (agent == null) {
				if (yesterday.cursedFox != null) {
					gameInfo.cursedFox = yesterday.cursedFox;
				}
			}
		}
		gameInfo.talkList = today.talkList;
		gameInfo.statusMap = agentStatusMap;
		gameInfo.existingRoleList = new ArrayList<>(new TreeSet<>(agentRoleMap.values()));
		gameInfo.remainTalkMap = remainTalkMap;
		gameInfo.remainWhisperMap = remainWhisperMap;

		if (role == Role.WEREWOLF || agent == null) {
			gameInfo.whisperList = today.whisperList;
		}

		Map<Agent, Role> roleMap = new LinkedHashMap<>();
		if (role != null) {
			roleMap.put(agent, role);
			if (today.getRole(agent) == Role.WEREWOLF) {
				for (Agent target : today.getAgents()) {
					if (today.getRole(target) == Role.WEREWOLF) {
						roleMap.put(target, Role.WEREWOLF);
					}
				}
			}
			if (today.getRole(agent) == Role.FREEMASON) {
				for (Agent target : today.getAgents()) {
					if (today.getRole(target) == Role.FREEMASON) {
						roleMap.put(target, Role.FREEMASON);
					}
				}
			}
		}
		gameInfo.roleMap = roleMap;
		gameInfo.remainTalkMap = remainTalkMap;
		return gameInfo;
	}

	public GameInfo getFinalGameInfo(Agent agent) {
		GameInfo gameInfo = getGameInfo(agent);
		gameInfo.roleMap = agentRoleMap;
		return gameInfo;
	}

	public void addAgent(Agent agent, Status status, Role role) {
		agentRoleMap.put(agent, role);
		agentStatusMap.put(agent, status);
		remainTalkMap.put(agent, gameSetting.maxTalk());
		if (getRole(agent) == Role.WEREWOLF) {
			remainWhisperMap.put(agent, gameSetting.maxWhisper());
		}
	}

	public List<Agent> getAgents() {
		return new ArrayList<>(agentRoleMap.keySet());
	}

	public Status getStatus(Agent agent) {
		return agentStatusMap.get(agent);
	}

	public Role getRole(Agent agent) {
		return agentRoleMap.get(agent);
	}

	public void addTalk(Agent agent, Talk talk) {
		int remainTalk = remainTalkMap.get(agent);
		if (!talk.isOver() && !talk.isSkip()) {
			if (remainTalk == 0) {
				throw new AIWolfException("Over the talk limit.");
			}
			remainTalkMap.put(agent, remainTalk - 1);
		}
		talkList.add(talk);
	}

	public void addWhisper(Agent agent, Talk whisper) {
		int remainWhisper = remainWhisperMap.get(agent);
		if (!whisper.isOver() && !whisper.isSkip()) {
			if (remainWhisper == 0) {
				throw new AIWolfException("Over the whisper limit.");
			}
			remainWhisperMap.put(agent, remainWhisper - 1);
		}
		whisperList.add(whisper);
	}

	public void addVote(Vote vote) {
		voteList.add(vote);
	}

	public void setDivine(Judge divine) {
		this.divine = divine;
	}

	public void setGuard(Guard guard) {
		this.guard = guard;
	}

	public void addAttack(Vote attack) {
		attackVoteList.add(attack);
	}

	public List<Vote> getVotes() {
		return voteList;
	}

	public void setExecutedTarget(Agent executed) {
		this.executed = executed;
		if (executed != null) {
			agentStatusMap.put(executed, Status.DEAD);
		}
	}

	public void setAttackedTarget(Agent attacked) {
		this.attacked = attacked;
	}

	public List<Vote> getAttackVotes() {
		return attackVoteList;
	}

	public Guard getGuard() {
		return guard;
	}

	public int getDay() {
		return day;
	}

	public List<Talk> getTalkList() {
		return talkList;
	}

	public List<Talk> getWhisperList() {
		return whisperList;
	}

	public Judge getDivine() {
		return divine;
	}

	public Agent getExecuted() {
		return executed;
	}

	public void addLastDeadAgent(Agent agent) {
		if (!lastDeadAgentList.contains(agent)) {
			lastDeadAgentList.add(agent);
		}
	}

	public Map<Agent, Integer> getRemainTalkMap() {
		return remainTalkMap;
	}

	public Map<Agent, Integer> getRemainWhisperMap() {
		return remainWhisperMap;
	}

	public GameData nextDay() {
		GameData gameData = new GameData(gameSetting);

		gameData.day = this.day + 1;
		gameData.agentStatusMap = new HashMap<>(agentStatusMap);

		for (Agent agent : lastDeadAgentList) {
			gameData.agentStatusMap.put(agent, Status.DEAD);
		}
		gameData.agentRoleMap = new HashMap<>(agentRoleMap);

		for (Agent agent : gameData.getAgents()) {
			if (gameData.getStatus(agent) == Status.ALIVE) {
				gameData.remainTalkMap.put(agent, gameSetting.maxTalk());
				if (gameData.getRole(agent) == Role.WEREWOLF) {
					gameData.remainWhisperMap.put(agent, gameSetting.maxWhisper());
				}
			}
		}

		gameData.dayBefore = this;
		return gameData;
	}

	public GameData getDayBefore() {
		return dayBefore;
	}

	protected List<Agent> getFilteredAgents(List<Agent> agentList, Species species) {
		return agentList.stream()
				.filter(agent -> getRole(agent).species == species)
				.collect(Collectors.toList());
	}

	protected List<Agent> getFilteredAgents(List<Agent> agentList, Role role) {
		return agentList.stream()
				.filter(agent -> getRole(agent) == role)
				.collect(Collectors.toList());
	}

	protected List<Agent> getFilteredAgents(List<Agent> agentList, Team team) {
		return agentList.stream()
				.filter(agent -> getRole(agent).team == team)
				.collect(Collectors.toList());
	}

	public int nextTalkIdx() {
		return talkIdx++;
	}

	public int nextWhisperIdx() {
		return whisperIdx++;
	}

	public Agent getAttackedDead() {
		return attackedDead;
	}

	public void setAttackedDead(Agent attackedDead) {
		this.attackedDead = attackedDead;
	}

	public Agent getCursedFox() {
		return cursedFox;
	}

	public void setCursedFox(Agent cursedFox) {
		this.cursedFox = cursedFox;
	}

	public List<Vote> getLatestVoteList() {
		return latestVoteList;
	}

	public void setLatestVoteList(List<Vote> latestVoteList) {
		this.latestVoteList = latestVoteList;
	}

	public List<Vote> getLatestAttackVoteList() {
		return latestAttackVoteList;
	}

	public void setLatestAttackVoteList(List<Vote> latestAttackVoteList) {
		this.latestAttackVoteList = latestAttackVoteList;
	}
}
