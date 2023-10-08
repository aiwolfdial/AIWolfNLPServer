package net.kanolab.aiwolf.server.common;

public enum HumanRole {
	VILLAGER,
	SEER,
	POSSESSED,
	WEREWOLF,
	NULL,
	;
	public static HumanRole stringToParam(String s){
		for(HumanRole e : values()){
			if(e.toString().equalsIgnoreCase(s)) return e;
		}
		return null;
	}
}
