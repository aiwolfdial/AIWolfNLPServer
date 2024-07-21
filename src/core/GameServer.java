package core;

import java.util.List;

import core.model.Agent;
import core.model.Role;
import core.packet.GameSetting;

public interface GameServer {
	List<Agent> getConnectedAgentList();

	void setGameSetting(GameSetting gameSetting);

	void init(Agent agent);

	String requestName(Agent agent);

	Role requestRequestRole(Agent agent);

	String requestTalk(Agent agent);

	String requestWhisper(Agent agent);

	Agent requestVote(Agent agent);

	Agent requestDivineTarget(Agent agent);

	Agent requestGuardTarget(Agent agent);

	Agent requestAttackTarget(Agent agent);

	void setGameData(GameData gameData);

	void dayStart(Agent agent);

	void dayFinish(Agent agent);

	void finish(Agent agent);

	void close();
}
