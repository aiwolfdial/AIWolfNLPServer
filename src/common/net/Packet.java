
package common.net;

import java.util.List;

import common.data.Request;
import common.data.Talk;

public class Packet {
	Request request;
	GameInfo gameInfo;
	GameSetting gameSetting;
	List<Talk> talkHistory;
	List<Talk> whisperHistory;

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

	public Request getRequest() {
		return request;
	}

	public GameInfo getGameInfo() {
		return gameInfo;
	}

	public GameSetting getGameSetting() {
		return gameSetting;
	}

	public List<Talk> getTalkHistory() {
		return talkHistory;
	}

	public List<Talk> getWhisperHistory() {
		return whisperHistory;
	}
}
