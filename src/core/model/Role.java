
package core.model;

public enum Role {
	BODYGUARD(Team.VILLAGER, Species.HUMAN), FREEMASON(Team.VILLAGER, Species.HUMAN), MEDIUM(Team.VILLAGER,
			Species.HUMAN), POSSESSED(Team.WEREWOLF,
					Species.HUMAN), SEER(Team.VILLAGER, Species.HUMAN), VILLAGER(Team.VILLAGER,
							Species.HUMAN), WEREWOLF(Team.WEREWOLF,
									Species.WEREWOLF), FOX(Team.OTHERS, Species.HUMAN), ANY(Team.ANY, Species.ANY);

	public final Team team;
	public final Species species;

	Role(Team teamType, Species species) {
		this.team = teamType;
		this.species = species;
	}
}
