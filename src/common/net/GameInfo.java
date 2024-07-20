package common.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.data.Agent;
import common.data.Judge;
import common.data.Role;
import common.data.Status;
import common.data.Talk;
import common.data.Vote;

public class GameInfo {
	int day;
	Agent agent;

	Judge mediumResult;
	Judge divineResult;
	Agent executedAgent;
	Agent latestExecutedAgent;

	Agent attackedAgent;
	Agent cursedFox;
	Agent guardedAgent;
	List<Vote> voteList;
	List<Vote> latestVoteList;
	List<Vote> attackVoteList;
	List<Vote> latestAttackVoteList;

	List<Talk> talkList;
	List<Talk> whisperList;

	Map<Agent, Status> statusMap;
	Map<Agent, Role> roleMap;
	Map<Agent, Integer> remainTalkMap;
	Map<Agent, Integer> remainWhisperMap;

	List<Role> existingRoleList;
	List<Agent> lastDeadAgentList;

	public GameInfo() {
		voteList = new ArrayList<>();
		latestVoteList = new ArrayList<>();
		attackVoteList = new ArrayList<>();
		latestAttackVoteList = new ArrayList<>();
		talkList = new ArrayList<>();
		whisperList = new ArrayList<>();
		statusMap = new HashMap<>();
		roleMap = new HashMap<>();
		lastDeadAgentList = new ArrayList<>();
	}

	public int getDay() {
		return day;
	}

	public Role getRole() {
		return roleMap.get(agent);
	}

	public Agent getAgent() {
		return agent;
	}

	public List<Agent> getAgentList() {
		return new ArrayList<>(statusMap.keySet());
	}

	public Judge getMediumResult() {
		return mediumResult;
	}

	public Judge getDivineResult() {
		return divineResult;
	}

	public Agent getExecutedAgent() {
		return executedAgent;
	}

	public List<Vote> getVoteList() {
		return voteList;
	}

	public List<Talk> getTalkList() {
		return talkList;
	}

	public List<Talk> getWhisperList() {
		return whisperList;
	}

	public List<Agent> getAliveAgentList() {
		List<Agent> aliveAgentList = new ArrayList<>();
		if (getAgentList() != null) {
			for (Agent target : getAgentList()) {
				if (statusMap.get(target) == Status.ALIVE) {
					aliveAgentList.add(target);
				}
			}
		}
		return aliveAgentList;
	}

	public Map<Agent, Status> getStatusMap() {
		return statusMap;
	}

	public Map<Agent, Role> getRoleMap() {
		return roleMap;
	}

	public List<Agent> getLastDeadAgentList() {
		return lastDeadAgentList;
	}

	public List<Role> getExistingRoles() {
		return existingRoleList;
	}

	public List<Vote> getLatestVoteList() {
		return latestVoteList;
	}

	public Agent getLatestExecutedAgent() {
		return latestExecutedAgent;
	}
}
