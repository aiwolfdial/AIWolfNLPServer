package net.kanolab.aiwolf.server.common;

public enum GameMode{
	SYNCHRONOUS,
	ASYNCHRONOUS,
	;
	public static GameMode stringToParam(String s){
		for(GameMode g : values()){
			System.out.println("gameMode -> " + g.toString() + " : " + s);
			if(g.toString().equalsIgnoreCase(s)) return g;
		}
		return null;
	}
}
