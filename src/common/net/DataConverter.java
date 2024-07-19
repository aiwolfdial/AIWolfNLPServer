package common.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.data.Agent;
import common.data.Request;
import net.arnx.jsonic.JSON;

public class DataConverter {
	public static String convert(Object obj) {
		return JSON.encode(obj);
	}

	public static Packet toPacket(String line) {
		Map<String, Object> map = JSON.decode(line);
		Request request = Request.valueOf((String) map.get("request"));
		GameInfoToSend gameInfoToSend = map.containsKey("gameInfo")
				? JSON.decode(JSON.encode(map.get("gameInfo")), GameInfoToSend.class)
				: null;

		if (gameInfoToSend != null) {
			if (map.containsKey("gameSetting")) {
				GameSetting gameSetting = JSON.decode(JSON.encode(map.get("gameSetting")), GameSetting.class);
				return new Packet(request, gameInfoToSend, gameSetting);
			} else {
				return new Packet(request, gameInfoToSend);
			}
		} else if (map.containsKey("talkHistory")) {
			List<TalkToSend> talkHistoryList = toTalkList(map.get("talkHistory"));
			List<TalkToSend> whisperHistoryList = toTalkList(map.get("whisperHistory"));
			return new Packet(request, talkHistoryList, whisperHistoryList);
		} else {
			return new Packet(request);
		}
	}

	private static List<TalkToSend> toTalkList(Object obj) {
		List<TalkToSend> list = new ArrayList<>();
		if (obj instanceof List<?>) {
			for (Object item : (List<?>) obj) {
				if (item instanceof Map<?, ?>) {
					TalkToSend talk = JSON.decode(JSON.encode(item), TalkToSend.class);
					list.add(talk);
				}
			}
		}
		return list;
	}

	public static Agent toAgent(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof String) {
			Matcher m = Pattern.compile("\\{\"agentIdx\":(\\d+)\\}").matcher((String) obj);
			if (m.find()) {
				return Agent.getAgent(Integer.parseInt(m.group(1)));
			}
		} else if (obj instanceof Agent) {
			return (Agent) obj;
		} else if (obj instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) obj;
			Object agentIdx = map.get("agentIdx");
			if (agentIdx instanceof Number) {
				return Agent.getAgent(((Number) agentIdx).intValue());
			}
		}
		return null;
	}
}
