package client;

public enum TopicType {
	ESTIMATE, COMINGOUT, DIVINATION, DIVINED, IDENTIFIED, GUARD, GUARDED, VOTE, VOTED, ATTACK, ATTACKED, AGREE, DISAGREE, OVER, SKIP, OPERATOR;

	public static TopicType getTopic(String s) {
		TopicType topic = switch (s.toUpperCase()) {
			case "REQUEST", "BECAUSE", "INQUIRE", "AND", "OR", "XOR", "NOT", "DAY" -> TopicType.OPERATOR;
			default -> {
				try {
					yield TopicType.valueOf(s.toUpperCase());
				} catch (IllegalArgumentException e) {
					yield null;
				}
			}
		};
		return topic;
	}
}
