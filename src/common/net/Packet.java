
package common.net;

import java.util.List;

import common.data.Request;

public class Packet {
	Request request;
	GameInfoToSend gameInfo;
	GameSetting gameSetting;
	List<TalkToSend> talkHistory;
	List<TalkToSend> whisperHistory;

	public Packet(Request request) {
		this.request = request;
	}

	public Packet(Request request, GameInfoToSend gameInfo) {
		this.request = request;
		this.gameInfo = gameInfo;
	}

	public Packet(Request request, GameInfoToSend gameInfo, GameSetting gameSetting) {
		this.request = request;
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
	}

	public Packet(Request request, List<TalkToSend> talkHistory, List<TalkToSend> whisperHistory) {
		this.request = request;
		this.talkHistory = talkHistory;
		this.whisperHistory = whisperHistory;
	}

	public Request getRequest() {
		return request;
	}

	public GameInfoToSend getGameInfo() {
		return gameInfo;
	}

	public GameSetting getGameSetting() {
		return gameSetting;
	}

	public List<TalkToSend> getTalkHistory() {
		return talkHistory;
	}

	public List<TalkToSend> getWhisperHistory() {
		return whisperHistory;
	}
}
