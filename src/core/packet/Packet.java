
package core.packet;

import java.util.List;

import core.model.Request;
import core.model.Talk;

public class Packet {
	public Request request;
	public GameInfo gameInfo;
	public GameSetting gameSetting;
	public List<Talk> talkHistory;
	public List<Talk> whisperHistory;

	public Packet(Request request) {
		this.request = request;
	}

	public Packet(Request request, GameInfo gameInfo) {
		this.request = request;
		this.gameInfo = gameInfo;
	}

	public Packet(Request request, GameInfo gameInfo, GameSetting gameSetting) {
		this.request = request;
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
	}

	public Packet(Request request, List<Talk> talkHistory, List<Talk> whisperHistory) {
		this.request = request;
		this.talkHistory = talkHistory;
		this.whisperHistory = whisperHistory;
	}
}
