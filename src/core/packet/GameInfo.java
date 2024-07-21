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
	public int day;
	public Agent agent;
	public Judge mediumResult;
	public Judge divineResult;
	public Agent executedAgent;
	public Agent latestExecutedAgent;
	public Agent attackedAgent;
	public Agent cursedFox;
	public Agent guardedAgent;
	public List<Vote> voteList = new ArrayList<>();
	public List<Vote> latestVoteList = new ArrayList<>();
	public List<Vote> attackVoteList = new ArrayList<>();
	public List<Vote> latestAttackVoteList = new ArrayList<>();
	public List<Talk> talkList = new ArrayList<>();
	public List<Talk> whisperList = new ArrayList<>();
	public Map<Agent, Status> statusMap = new HashMap<>();
	public Map<Agent, Role> roleMap = new HashMap<>();
	public Map<Agent, Integer> remainTalkMap = new HashMap<>();
	public Map<Agent, Integer> remainWhisperMap = new HashMap<>();
	public List<Role> existingRoleList = new ArrayList<>();
	public List<Agent> lastDeadAgentList = new ArrayList<>();

	public GameInfo(int day, Agent agent) {
		this.day = day;
		this.agent = agent;
	}
}
