package client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import common.data.Agent;
import common.data.Role;
import common.data.Species;
import common.data.Talk;

public class Content implements Cloneable {
	public static final Content SKIP = new Content(TopicType.SKIP);
	public static final Content OVER = new Content(TopicType.OVER);

	public static final Agent ANY = Agent.getAgent(0);
	public static final Agent UNSPEC = null;

	private String text = null;
	private Operator operator = null;
	private TopicType topic = null;
	private Agent subject = UNSPEC;
	private Agent target = ANY;
	private Role role = null;
	private Species result = null;
	private TalkType talkType = null;
	private int talkDay = -1;
	private int talkID = -1;
	private List<Content> contentList = null;
	private int day = -1;

	// かっこで囲んだContent文字列の並びをContentのリストに変換する
	private static List<Content> getContents(String input, boolean isForValidation) {
		List<Content> contents = new ArrayList<>();
		for (String s : getContentStrings(input)) {
			contents.add(new Content(s, isForValidation));
		}
		return contents;
	}

	// かっこで囲んだContent文字列の並びをContent文字列のリストに変換する
	private static List<String> getContentStrings(String input) {
		List<String> strings = new ArrayList<>();
		int length = input.length();
		int stackPtr = 0;
		int start = 0;
		for (int i = 0; i < length; i++) {
			if (input.charAt(i) == '(') {
				if (stackPtr == 0) {
					start = i;
				}
				stackPtr++;
			} else if (input.charAt(i) == ')') {
				stackPtr--;
				if (stackPtr == 0) {
					strings.add(input.substring(start + 1, i));
				}
			}
		}
		return strings;
	}

	// 内側の文のsubjectを補完する
	private void completeInnerSubject() {
		if (contentList == null) {
			return;
		}
		contentList = contentList.stream().map(c -> {
			if (c.subject == UNSPEC) {
				// INQUIREとREQUESTでsubjectが省略された場合は外の文のtarget
				if (operator == Operator.INQUIRE || operator == Operator.REQUEST) {
					return c.cloneAndReplaceSubject(target);
				}
				// それ以外は外の文のsubject
				if (UNSPEC != subject) { // 未指定の場合は何もしない
					return c.cloneAndReplaceSubject(subject);
				}
			}
			c.completeInnerSubject();
			return c;
		}).collect(Collectors.toList());
	}

	// 複製したContentのsubjectを入れ替えて返す
	// Clone this and replace subject with given subject.
	private Content cloneAndReplaceSubject(Agent newSubject) {
		Content c = clone();
		c.subject = newSubject;
		c.completeInnerSubject();
		c.normalizeText(); // subjectを入れ替えると簡潔にできる場合がある
		return c;
	}

	public Content(TopicType topic) {
		this.topic = topic;
		completeInnerSubject();
		normalizeText();
	}

	private static final String regAgent = "\\s+(Agent\\[\\d+\\]|ANY)";
	private static final String regSubject = "^(Agent\\[\\d+\\]|ANY|)\\s*";
	private static final String regTalk = "\\s+(\\p{Upper}+)\\s+day(\\d+)\\s+ID:(\\d+)";
	private static final String regRoleResult = "\\s+(\\p{Upper}+)";
	private static final String regParen = "(\\(.*\\))";
	private static final String regDigit = "(\\d+)";
	private static final String TERM = "$";
	private static final String SP = "\\s+";
	private static final Pattern agreePattern = Pattern.compile(regSubject + "(AGREE|DISAGREE)" + regTalk + TERM);
	private static final Pattern estimatePattern = Pattern
			.compile(regSubject + "(ESTIMATE|COMINGOUT)" + regAgent + regRoleResult + TERM);
	private static final Pattern divinedPattern = Pattern
			.compile(regSubject + "(DIVINED|IDENTIFIED)" + regAgent + regRoleResult + TERM);
	private static final Pattern attackPattern = Pattern
			.compile(regSubject + "(ATTACK|ATTACKED|DIVINATION|GUARD|GUARDED|VOTE|VOTED)" + regAgent + TERM);
	private static final Pattern requestPattern = Pattern
			.compile(regSubject + "(REQUEST|INQUIRE)" + regAgent + SP + regParen + TERM);
	private static final Pattern becausePattern = Pattern
			.compile(regSubject + "(BECAUSE|AND|OR|XOR|NOT|REQUEST)" + SP + regParen + TERM);
	private static final Pattern dayPattern = Pattern
			.compile(regSubject + "DAY" + SP + regDigit + SP + regParen + TERM);

	/**
	 * <div lang="ja">発話テキストによりContentを構築する</div>
	 *
	 * <div lang="en">Constructs a Content from the uttered text.</div>
	 * 
	 * @param input
	 *            <div lang="ja">発話テキスト</div>
	 *
	 *            <div lang="en">The uttered text.</div>
	 */
	public Content(String input) {
		this(input, false);
	}

	private Content(String input, boolean isForValidation) {
		String trimmed = input.trim();
		Matcher m;
		try {
			// SKIP
			if (trimmed.equals(Talk.SKIP)) {
				topic = TopicType.SKIP;
			}
			// OVER
			else if (trimmed.equals(Talk.OVER)) {
				topic = TopicType.OVER;
			}
			// AGREE,DISAGREE
			else if ((m = agreePattern.matcher(trimmed)).find()) {
				subject = toAgent(m.group(1));
				topic = TopicType.valueOf(m.group(2));
				talkType = TalkType.valueOf(m.group(3));
				talkDay = Integer.parseInt(m.group(4));
				talkID = Integer.parseInt(m.group(5));
			}
			// ESTIMATE,COMINGOUT
			else if ((m = estimatePattern.matcher(trimmed)).find()) {
				subject = toAgent(m.group(1));
				topic = TopicType.valueOf(m.group(2));
				target = toAgent(m.group(3));
				role = Role.valueOf(m.group(4));
			}
			// DIVINED,IDENTIFIED
			else if ((m = divinedPattern.matcher(trimmed)).find()) {
				subject = toAgent(m.group(1));
				topic = TopicType.valueOf(m.group(2));
				target = toAgent(m.group(3));
				result = Species.valueOf(m.group(4));
			}
			// ATTACK,ATTACKED,DIVINATION,GUARD,GUARDED,VOTE,VOTED
			else if ((m = attackPattern.matcher(trimmed)).find()) {
				subject = toAgent(m.group(1));
				topic = TopicType.valueOf(m.group(2));
				target = toAgent(m.group(3));
			}
			// REQUEST,INQUIRE
			else if ((m = requestPattern.matcher(trimmed)).find()) {
				topic = TopicType.OPERATOR;
				subject = toAgent(m.group(1));
				operator = Operator.valueOf(m.group(2));
				target = toAgent(m.group(3));
				contentList = getContents(m.group(4), true);
			}
			// BECAUSE,AND,OR,XOR,NOT,REQUEST(ver.2)
			else if ((m = becausePattern.matcher(trimmed)).find()) {
				topic = TopicType.OPERATOR;
				subject = toAgent(m.group(1));
				operator = Operator.valueOf(m.group(2));
				contentList = getContents(m.group(3), true);
				if (operator == Operator.REQUEST) {
					target = contentList.getFirst().subject == UNSPEC ? ANY : contentList.getFirst().subject;
				}
			}
			// DAY
			else if ((m = dayPattern.matcher(trimmed)).find()) {
				topic = TopicType.OPERATOR;
				operator = Operator.DAY;
				subject = toAgent(m.group(1));
				day = Integer.parseInt(m.group(2));
				contentList = getContents(m.group(3), true);
			}
			// Unknown string pattern.
			else {
				throw new IllegalContentStringException();
			}
		} catch (IllegalArgumentException e) {
			if (isForValidation) {
				throw new IllegalContentStringException(input);
			} else {
				topic = TopicType.SKIP;
			}
		}
		completeInnerSubject();
		normalizeText();
	}

	/**
	 * <div lang="ja">発話内容の主語を返す</div>
	 *
	 * <div lang="en">Returns the subject of this content.</div>
	 * 
	 * @return <div lang="ja">主語</div>
	 *
	 *         <div lang="en">The subject.</div>
	 */
	public Agent getSubject() {
		return subject;
	}

	/**
	 * <div lang="ja">発話テキストが有効かどうかを返す．</div>
	 * 
	 * <div lang="en">Returns whether or not the uttered text is valid.</div>
	 * 
	 * @param input
	 *            <div lang="ja">被チェックテキスト</div>
	 *
	 *            <div lang="en">The text to be checked.</div>
	 * 
	 * @return <div lang="ja">有効である場合{@code true}，そうでなければ{@code false}</div>
	 *
	 *         <div lang="en">{@code true} if the text is valid, otherwise
	 *         {@code false}.</div>
	 */
	public static boolean validate(String input) {
		try {
			new Content(input, true);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	// textを正規化する
	private void normalizeText() {
		switch (topic) {
			case SKIP:
				text = Talk.SKIP;
				break;
			case OVER:
				text = Talk.OVER;
				break;
			case AGREE:
			case DISAGREE:
				text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
						+ topic
						+ " " + talkType.toString() + " day" + talkDay + " ID:" + talkID;
				break;
			case ESTIMATE:
			case COMINGOUT:
				text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
						+ topic
						+ " " + (target == ANY ? "ANY" : target.toString())
						+ " " + role.toString();
				break;
			case DIVINED:
			case IDENTIFIED:
				text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
						+ topic
						+ " " + (target == ANY ? "ANY" : target.toString())
						+ " " + result.toString();
				break;
			case ATTACK:
			case ATTACKED:
			case DIVINATION:
			case GUARD:
			case GUARDED:
			case VOTE:
			case VOTED:
				text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
						+ topic
						+ " " + (target == ANY ? "ANY" : target.toString());
				break;
			case OPERATOR:
				switch (operator) {
					case REQUEST:
					case INQUIRE:
						text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
								+ operator
								+ " " + (target == ANY ? "ANY" : target.toString())
								+ " ("
								+ (contentList.getFirst().getSubject() == target
										? stripSubject(contentList.getFirst().text)
										: contentList.getFirst().text)
								+ ")";
						break;
					case BECAUSE:
					case XOR:
						text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
								+ operator
								+ " ("
								+ (contentList.get(0).getSubject() == subject
										? stripSubject(contentList.get(0).text)
										: contentList.get(0).text)
								+ ") ("
								+ (contentList.get(1).getSubject() == subject
										? stripSubject(contentList.get(1).text)
										: contentList.get(1).text)
								+ ")";
						break;
					case AND:
					case OR:
						text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
								+ operator
								+ " " + contentList.stream().map(c -> "(" +
										(c.getSubject() == subject ? stripSubject(c.text) : c.text)
										+ ")").collect(Collectors.joining(" "));
						break;
					case NOT:
						text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
								+ operator
								+ " ("
								+ (contentList.getFirst().getSubject() == subject
										? stripSubject(contentList.getFirst().text)
										: contentList.getFirst().text)
								+ ")";
						break;
					case DAY:
						text = (subject == UNSPEC ? "" : subject == ANY ? "ANY " : subject + " ")
								+ operator
								+ " " + day
								+ " ("
								+ (contentList.getFirst().getSubject() == subject
										? stripSubject(contentList.getFirst().text)
										: contentList.getFirst().text)
								+ ")";
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
	}

	private static final Pattern agentPattern = Pattern.compile("\\s?(Agent\\[(\\d+)\\]|ANY)\\s?");

	private static Agent toAgent(String s) {
		if (s.isEmpty()) {
			return UNSPEC;
		}
		Matcher m = agentPattern.matcher(s);
		if (m.find()) {
			if (m.group(1).equals("ANY")) {
				return ANY;
			} else {
				return Agent.getAgent(Integer.parseInt(m.group(2)));
			}
		}
		throw new IllegalContentStringException();
	}

	@Override
	public Content clone() {
		Content clone = null;
		try {
			clone = (Content) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	@Override
	public boolean equals(Object content) {
		if (content instanceof Content && text != null) {
			return text.equals(((Content) content).text);
		}
		return false;
	}

	private static final Pattern stripPattern = Pattern.compile("^(Agent\\[\\d+\\]|ANY|)\\s*(\\p{Upper}+)(.*)$");

	/**
	 * <div lang="ja">発話文字列からsubjectの部分を除いた文字列を返す</div>
	 *
	 * <div lang="en">Strips subject off the given string and returns it.</div>
	 * 
	 * @param input
	 *            <div lang="ja">入力文字列</div>
	 *
	 *            <div lang="en">Input string.</div>
	 * @return <div lang="ja">発話文字列からsubjectの部分を除いた文字列</div>
	 *
	 *         <div lang="en">String with no subject prefix.</div>
	 * 
	 */
	public static String stripSubject(String input) {
		Matcher m = stripPattern.matcher(input);
		if (m.find()) {
			return m.group(2) + m.group(3);
		}
		return input;
	}

	@Override
	public String toString() {
		return text;
	}
}
