package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import common.data.Agent;
import common.data.Guard;
import common.data.Judge;
import common.data.Role;
import common.data.Species;
import common.data.Status;
import common.data.Talk;
import common.data.Team;
import common.data.Vote;
import common.net.GameInfo;
import common.net.GameSetting;
import server.exception.AIWolfException;

public class GameData {
	protected int day;
	protected Map<Agent, Status> agentStatusMap = new LinkedHashMap<>();
	protected Map<Agent, Role> agentRoleMap = new HashMap<>();
	protected List<Talk> talkList = new ArrayList<>();
	protected List<Talk> whisperList = new ArrayList<>();
	protected List<Vote> voteList = new ArrayList<>();
	protected List<Vote> latestVoteList = new ArrayList<>();
	protected List<Vote> attackVoteList = new ArrayList<>();
	protected List<Vote> latestAttackVoteList = new ArrayList<>();
	protected Map<Agent, Integer> remainTalkMap = new HashMap<>();
	protected Map<Agent, Integer> remainWhisperMap = new HashMap<>();
	protected Judge divine;
	protected Guard guard;
	protected Agent executed;
	protected Agent attackedDead;
	protected Agent attacked;
	protected Agent cursedFox;
	protected List<Agent> lastDeadAgentList = new ArrayList<>();
	protected List<Agent> suddenDeathList = new ArrayList<>();
	protected GameData dayBefore;
	protected int talkIdx;
	protected int whisperIdx;

	protected GameSetting gameSetting;

	public GameData(GameSetting gameSetting) {
		this.gameSetting = gameSetting;
	}

	public GameInfo getGameInfo(Agent agent) {
		GameData today = this;
		GameInfo gameInfo = new GameInfo(today.getDay());
		Role role = getRole(agent);

		if (gameSetting.isVoteVisible()) {
			gameInfo.setLatestVoteList(latestVoteList);
		}
		if (executed != null) {
			gameInfo.setLatestExecutedAgent(executed);
		}
		if (agent == null || role == Role.WEREWOLF) {
			gameInfo.setLatestAttackVoteList(latestAttackVoteList);
		}

		GameData yesterday = today.getDayBefore();
		if (yesterday != null) {
			if (yesterday.getExecuted() != null) {
				gameInfo.setExecutedAgent(yesterday.getExecuted());
			}
			gameInfo.setLastDeadAgentList(yesterday.getLastDeadAgentList());
			if (gameSetting.isVoteVisible()) {
				gameInfo.setVoteList(yesterday.getVoteList());
			}
			if (agent != null && today.getRole(agent) == Role.MEDIUM && executed != null) {
				gameInfo.setMediumResult(new Judge(day, agent, executed, yesterday.getRole(executed).getSpecies()));
			}
			if (agent == null || today.getRole(agent) == Role.SEER) {
				Judge divine = yesterday.getDivine();
				if (divine != null && divine.getTarget() != null) {
					gameInfo.setDivineResult(new Judge(day, divine.getAgent(), divine.getTarget(),
							yesterday.getRole(divine.getTarget()).getSpecies()));
				}
			}
			if (agent == null || today.getRole(agent) == Role.WEREWOLF) {
				if (yesterday.getAttacked() != null) {
					gameInfo.setAttackedAgent(yesterday.getAttacked());
				}
				gameInfo.setAttackVoteList(yesterday.getAttackVoteList());
			}
			if (agent == null || today.getRole(agent) == Role.BODYGUARD) {
				if (yesterday.getGuard() != null) {
					gameInfo.setGuardedAgent(yesterday.getGuard().getTarget());
				}
			}
			if (agent == null) {
				if (yesterday.cursedFox != null) {
					gameInfo.setCursedFox(yesterday.cursedFox);
				}
			}
		}
		gameInfo.setTalkList(today.getTalkList());
		gameInfo.setStatusMap(agentStatusMap);
		gameInfo.setExistingRoleList(new ArrayList<>(new TreeSet<>(agentRoleMap.values())));
		gameInfo.setRemainTalkMap(remainTalkMap);
		gameInfo.setRemainWhisperMap(remainWhisperMap);

		if (role == Role.WEREWOLF || agent == null) {
			gameInfo.setWhisperList(today.getWhisperList());
		}

		Map<Agent, Role> roleMap = new LinkedHashMap<>();
		if (role != null) {
			roleMap.put(agent, role);
			if (today.getRole(agent) == Role.WEREWOLF) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.WEREWOLF) {
						roleMap.put(target, Role.WEREWOLF);
					}
				}
			}
			if (today.getRole(agent) == Role.FREEMASON) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.FREEMASON) {
						roleMap.put(target, Role.FREEMASON);
					}
				}
			}
		}
		gameInfo.setRoleMap(roleMap);
		gameInfo.setRemainTalkMap(remainTalkMap);
		return gameInfo;
	}

	public GameInfo getFinalGameInfo(Agent agent) {
		GameInfo gameInfo = getGameInfo(agent);
		gameInfo.setRoleMap(agentRoleMap);
		return gameInfo;
	}

	public void addAgent(Agent agent, Status status, Role role) {
		agentRoleMap.put(agent, role);
		agentStatusMap.put(agent, status);
		remainTalkMap.put(agent, gameSetting.getMaxTalk());
		if (getRole(agent) == Role.WEREWOLF) {
			remainWhisperMap.put(agent, gameSetting.getMaxWhisper());
		}
	}

	public List<Agent> getAgentList() {
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
				throw new AIWolfException(
						"No remain talk but try to talk. #Contact to AIWolf Platform Developer");
			}
			remainTalkMap.put(agent, remainTalk - 1);
		}
		talkList.add(talk);
	}

	public void addWhisper(Agent agent, Talk whisper) {
		int remainWhisper = remainWhisperMap.get(agent);
		if (!whisper.isOver() && !whisper.isSkip()) {
			if (remainWhisper == 0) {
				throw new AIWolfException(
						"No remain whisper but try to whisper. #Contact to AIWolf Platform Developer");
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

	public List<Vote> getVoteList() {
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

	public List<Vote> getAttackVoteList() {
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

	public Agent getAttacked() {
		return attacked;
	}

	public void addLastDeadAgent(Agent agent) {
		if (!lastDeadAgentList.contains(agent)) {
			lastDeadAgentList.add(agent);
		}
	}

	public List<Agent> getLastDeadAgentList() {
		return lastDeadAgentList;
	}

	public List<Agent> getSuddenDeathList() {
		return suddenDeathList;
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

		for (Agent a : lastDeadAgentList) {
			gameData.agentStatusMap.put(a, Status.DEAD);
		}
		gameData.agentRoleMap = new HashMap<>(agentRoleMap);

		for (Agent a : gameData.getAgentList()) {
			if (gameData.getStatus(a) == Status.ALIVE) {
				gameData.remainTalkMap.put(a, gameSetting.getMaxTalk());
				if (gameData.getRole(a) == Role.WEREWOLF) {
					gameData.remainWhisperMap.put(a, gameSetting.getMaxWhisper());
				}
			}
		}

		gameData.dayBefore = this;

		return gameData;
	}

	public GameData getDayBefore() {
		return dayBefore;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Species species) {
		List<Agent> resultList = new ArrayList<>();
		for (Agent agent : agentList) {
			if (getRole(agent).getSpecies() == species) {
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Status status) {
		List<Agent> resultList = new ArrayList<>();
		for (Agent agent : agentList) {
			if (getStatus(agent) == status) {
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Role role) {
		List<Agent> resultList = new ArrayList<>();
		for (Agent agent : agentList) {
			if (getRole(agent) == role) {
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Team team) {
		List<Agent> resultList = new ArrayList<>();
		for (Agent agent : agentList) {
			if (getRole(agent).getTeam() == team) {
				resultList.add(agent);
			}
		}
		return resultList;
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

	public boolean contains(Agent target) {
		return this.agentRoleMap.containsKey(target);
	}
}
