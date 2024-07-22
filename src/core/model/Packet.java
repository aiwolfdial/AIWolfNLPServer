
package core.model;

import java.util.List;

public record Packet(
		Request request,
		GameInfo gameInfo,
		GameSetting gameSetting,
		List<Talk> talkHistory,
		List<Talk> whisperHistory) {
	public Packet(Request request) {
		this(request, null, null, null, null);
	}

	public Packet(Request request, GameInfo gameInfo) {
		this(request, gameInfo, null, null, null);
	}

	public Packet(Request request, GameInfo gameInfo, GameSetting gameSetting) {
		this(request, gameInfo, gameSetting, null, null);
	}

	public Packet(Request request, List<Talk> talkHistory, List<Talk> whisperHistory) {
		this(request, null, null, talkHistory, whisperHistory);
	}
}
