package client;

public enum Topic {
	ESTIMATE, COMINGOUT, DIVINATION, DIVINED, IDENTIFIED, GUARD, GUARDED, VOTE, VOTED, ATTACK, ATTACKED, AGREE, DISAGREE, OVER, SKIP, OPERATOR;

	public static Topic getTopic(String s) {
		for (Topic topic : Topic.values()) {
			if (topic.toString().equalsIgnoreCase(s)) {
				return topic;
			}
		}
		return switch (s) {
			case "REQUEST", "BECAUSE", "INQUIRE", "AND", "OR", "XOR", "NOT", "DAY" -> Topic.OPERATOR;
			default -> null;
		};
	}

}
