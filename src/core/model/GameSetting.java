package core.model;

import java.util.Map;

import core.Config;

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

	static final private int[][] ROLES_NUM = {
			// {BODYGUARD (狩人), FREEMASON (共有者), MEDIUM (霊媒師), POSSESSED (狂人), SEER (占い師),
			// VILLAGER (村人), WEREWOLF (人狼), FOX (妖狐), ANY (役職不定)}
			{}, // 0
			{}, // 1
			{}, // 2
			{ 0, 0, 0, 0, 1, 1, 1, 0, 0 }, // 3
			{ 0, 0, 0, 0, 1, 2, 1, 0, 0 }, // 4
			{ 0, 0, 0, 1, 1, 2, 1, 0, 0 }, // 5
			{ 0, 0, 0, 1, 1, 3, 1, 0, 0 }, // 6
			{ 0, 0, 0, 0, 1, 4, 2, 0, 0 }, // 7
			{ 0, 0, 1, 0, 1, 4, 2, 0, 0 }, // 8
			{ 0, 0, 1, 0, 1, 5, 2, 0, 0 }, // 9
			{ 1, 0, 1, 1, 1, 4, 2, 0, 0 }, // 10
			{ 1, 0, 1, 1, 1, 5, 2, 0, 0 }, // 11
			{ 1, 0, 1, 1, 1, 5, 3, 0, 0 }, // 12
			{ 1, 0, 1, 1, 1, 6, 3, 0, 0 }, // 13
			{ 1, 0, 1, 1, 1, 7, 3, 0, 0 }, // 14
			{ 1, 0, 1, 1, 1, 8, 3, 0, 0 }, // 15
			{ 1, 0, 1, 1, 1, 9, 3, 0, 0 }, // 16
			{ 1, 0, 1, 1, 1, 10, 3, 0, 0 }, // 17
			{ 1, 0, 1, 1, 1, 11, 3, 0, 0 }, // 18
	};

	public GameSetting(Config config) {
		this(
				new java.util.HashMap<>(),
				config.getMaxTalkNum(),
				config.getMaxTalkTurn(),
				config.getMaxTalkNum(),
				config.getMaxTalkTurn(),
				0,
				false,
				false,
				config.isTalkOnFirstDay(),
				(int) config.getResponseTimeout(),
				(int) config.getActionTimeout(),
				0,
				0,
				false);
		setRoleNumMap(config.getBattleAgentNum());
	}

	public void setRoleNumMap(int agentNum) {
		if (agentNum < 3 || agentNum > 18) {
			throw new IllegalArgumentException("The number of agents must be between 3 and 18.");
		}
		Role[] roles = Role.values();
		for (int i = 0; i < roles.length; i++) {
			roleNumMap.put(roles[i], ROLES_NUM[agentNum][i]);
		}
	}

	public int getRoleNum(Role role) {
		return roleNumMap.getOrDefault(role, 0);
	}

	public int getPlayerNum() {
		return roleNumMap.values().stream().mapToInt(Integer::intValue).sum();
	}
}
