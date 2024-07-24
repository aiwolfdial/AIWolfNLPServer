
package core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record Packet(
		Request request,
		@JsonInclude(JsonInclude.Include.NON_NULL) GameInfo gameInfo,
		@JsonInclude(JsonInclude.Include.NON_NULL) GameSetting gameSetting,
		@JsonInclude(JsonInclude.Include.NON_NULL) List<Talk> talkHistory,
		@JsonInclude(JsonInclude.Include.NON_NULL) List<Talk> whisperHistory) {
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
