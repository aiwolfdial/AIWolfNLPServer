package core.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.model.Agent;
import core.model.Judge;
import core.model.Role;
import core.model.Status;
import core.model.Talk;
import core.model.Vote;

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
	List<Vote> voteList = new ArrayList<>();
	List<Vote> latestVoteList = new ArrayList<>();
	List<Vote> attackVoteList = new ArrayList<>();
	List<Vote> latestAttackVoteList = new ArrayList<>();

	List<Talk> talkList = new ArrayList<>();
	List<Talk> whisperList = new ArrayList<>();

	Map<Agent, Status> statusMap = new HashMap<>();
	Map<Agent, Role> roleMap = new HashMap<>();
	Map<Agent, Integer> remainTalkMap = new HashMap<>();
	Map<Agent, Integer> remainWhisperMap = new HashMap<>();

	List<Role> existingRoleList = new ArrayList<>();
	List<Agent> lastDeadAgentList = new ArrayList<>();

	public GameInfo(int day) {
		this.day = day;
	}

	public void setLatestVoteList(List<Vote> latestVoteList) {
		this.latestVoteList = latestVoteList;
	}

	public void setLatestExecutedAgent(Agent latestExecutedAgent) {
		this.latestExecutedAgent = latestExecutedAgent;
	}

	public void setLatestAttackVoteList(List<Vote> latestAttackVoteList) {
		this.latestAttackVoteList = latestAttackVoteList;
	}

	public void setExecutedAgent(Agent executedAgent) {
		this.executedAgent = executedAgent;
	}

	public void setLastDeadAgentList(List<Agent> lastDeadAgentList) {
		this.lastDeadAgentList = lastDeadAgentList;
	}

	public void setVoteList(List<Vote> voteList) {
		this.voteList = voteList;
	}

	public void setMediumResult(Judge mediumResult) {
		this.mediumResult = mediumResult;
	}

	public void setDivineResult(Judge divineResult) {
		this.divineResult = divineResult;
	}

	public void setAttackedAgent(Agent attackedAgent) {
		this.attackedAgent = attackedAgent;
	}

	public void setAttackVoteList(List<Vote> attackVoteList) {
		this.attackVoteList = attackVoteList;
	}

	public void setGuardedAgent(Agent guardedAgent) {
		this.guardedAgent = guardedAgent;
	}

	public void setCursedFox(Agent cursedFox) {
		this.cursedFox = cursedFox;
	}

	public void setTalkList(List<Talk> talkList) {
		this.talkList = talkList;
	}

	public void setStatusMap(Map<Agent, Status> statusMap) {
		this.statusMap = statusMap;
	}

	public void setExistingRoleList(List<Role> existingRoleList) {
		this.existingRoleList = existingRoleList;
	}

	public void setRemainTalkMap(Map<Agent, Integer> remainTalkMap) {
		this.remainTalkMap = remainTalkMap;
	}

	public void setRemainWhisperMap(Map<Agent, Integer> remainWhisperMap) {
		this.remainWhisperMap = remainWhisperMap;
	}

	public void setWhisperList(List<Talk> whisperList) {
		this.whisperList = whisperList;
	}

	public void setRoleMap(Map<Agent, Role> roleMap) {
		this.roleMap = roleMap;
	}

	public void setRemainTalkMap(HashMap<Agent, Integer> remainTalkMap) {
		this.remainTalkMap = remainTalkMap;
	}

	public List<Talk> getTalkList() {
		return talkList;
	}

	public List<Talk> getWhisperList() {
		return whisperList;
	}
}
