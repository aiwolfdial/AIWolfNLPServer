package core.model;

import java.util.Map;

public record GameSetting(
		Map<Role, Integer> roleNumMap,
		int maxTalk,
		int maxTalkTurn,
		int maxWhisper,
		int maxWhisperTurn,
		int maxSkip,
		boolean isEnableNoAttack,
		boolean isVoteVisible,
		boolean isTalkOnFirstDay,
		int responseTimeout,
		int actionTimeout,
		int maxRevote,
		int maxAttackRevote,
		boolean isEnableRoleRequest) {

	public GameSetting(Config config) {
		this(
				Role.DefaultMap(config.battleAgentNum()),
				config.maxTalkNum(),
				config.maxTalkTurn(),
				config.maxTalkNum(),
				config.maxTalkTurn(),
				0,
				false,
				false,
				config.talkOnFirstDay(),
				config.responseTimeout(),
				config.actionTimeout(),
				0,
				0,
				false);
	}

	public int getRoleNum(Role role) {
		return roleNumMap.getOrDefault(role, 0);
	}

	public int getPlayerNum() {
		return roleNumMap.values().stream().mapToInt(Integer::intValue).sum();
	}
}
