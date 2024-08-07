
package core.model;

import java.util.Map;

public enum Role {
	BODYGUARD(Team.VILLAGER, Species.HUMAN), FREEMASON(Team.VILLAGER, Species.HUMAN), MEDIUM(Team.VILLAGER,
			Species.HUMAN), POSSESSED(Team.WEREWOLF,
					Species.HUMAN), SEER(Team.VILLAGER, Species.HUMAN), VILLAGER(Team.VILLAGER,
							Species.HUMAN), WEREWOLF(Team.WEREWOLF,
									Species.WEREWOLF), FOX(Team.OTHERS, Species.HUMAN), ANY(Team.ANY, Species.ANY);

	public final Team team;
	public final Species species;

	Role(Team team, Species species) {
		this.team = team;
		this.species = species;
	}

	public static final int[][] ROLES_NUM = {
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

	public static Map<Role, Integer> DefaultMap(int agentNum) {
		Map<Role, Integer> roleNumMap = new java.util.HashMap<>();
		if (agentNum < 3 || agentNum > 18) {
			throw new IllegalArgumentException("The number of agents must be between 3 and 18.");
		}
		Role[] roles = Role.values();
		for (int i = 0; i < roles.length; i++) {
			roleNumMap.put(roles[i], ROLES_NUM[agentNum][i]);
		}
		return roleNumMap;
	}
}
