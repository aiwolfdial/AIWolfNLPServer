package core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public class GameInfo {
	public final int day;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public final Agent agent;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Judge mediumResult;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Judge divineResult;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Agent executedAgent;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Agent latestExecutedAgent;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Agent attackedAgent;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Agent cursedFox;

	@JsonInclude(JsonInclude.Include.NON_NULL)
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
