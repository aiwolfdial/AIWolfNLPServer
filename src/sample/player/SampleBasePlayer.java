/**
 * SampleBasePlayer.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package sample.player;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import client.lib.AgreeContentBuilder;
import client.lib.AndContentBuilder;
import client.lib.AttackContentBuilder;
import client.lib.AttackedContentBuilder;
import client.lib.BecauseContentBuilder;
import client.lib.ComingoutContentBuilder;
import client.lib.Content;
import client.lib.DayContentBuilder;
import client.lib.DisagreeContentBuilder;
import client.lib.DivinationContentBuilder;
import client.lib.DivinedResultContentBuilder;
import client.lib.EstimateContentBuilder;
import client.lib.GuardCandidateContentBuilder;
import client.lib.GuardedAgentContentBuilder;
import client.lib.IdentContentBuilder;
import client.lib.InquiryContentBuilder;
import client.lib.NotContentBuilder;
import client.lib.OrContentBuilder;
import client.lib.RequestContentBuilder;
import client.lib.TalkType;
import client.lib.Topic;
import client.lib.VoteContentBuilder;
import client.lib.VotedContentBuilder;
import client.lib.XorContentBuilder;
import common.data.Agent;
import common.data.Judge;
import common.data.Player;
import common.data.Role;
import common.data.Species;
import common.data.Status;
import common.data.Talk;
import common.net.GameInfo;
import common.net.GameSetting;

/**
 * すべての役職のベースとなるクラス
 * 
 * @author otsuki
 */
public class SampleBasePlayer implements Player {

	/** このエージェント */
	Agent me;

	/** 日付 */
	int day;

	/** 再投票のときtrue */
	boolean isRevote;

	/** 最新のゲーム情報 */
	GameInfo currentGameInfo;

	/** 自分以外の生存エージェント */
	List<Agent> aliveOthers;

	/** 追放されたエージェント */
	List<Agent> executedAgents = new ArrayList<>();

	/** 殺されたエージェント */
	List<Agent> killedAgents = new ArrayList<>();

	/** 発言された占い結果報告のリスト */
	List<Judge> divinationList = new ArrayList<>();

	/** 発言された霊媒結果報告のリスト */
	List<Judge> identList = new ArrayList<>();

	/** 発言用待ち行列 */
	private final Deque<Content> talkQueue = new LinkedList<>();

	/** 投票先候補 */
	Agent voteCandidate;

	/** 宣言済み投票先候補 */
	Agent declaredVoteCandidate;

	/** カミングアウト状況 */
	Map<Agent, Role> comingoutMap = new HashMap<>();

	/** GameInfo.talkList読み込みのヘッド */
	int talkListHead;

	/** 推測理由マップ */
	EstimateReasonMap estimateReasonMap = new EstimateReasonMap();

	/** 投票理由マップ */
	VoteReasonMap voteReasonMap = new VoteReasonMap();

	/** 投票リクエストカウンタ */
	VoteRequestCounter voteRequestCounter = new VoteRequestCounter();

	/** talk()のターン */
	int talkTurn;

	/**
	 * エージェントが生きているかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isAlive(Agent agent) {
		return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/**
	 * エージェントが殺されたかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	/**
	 * エージェントがカミングアウトしたかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	/**
	 * 役職がカミングアウトされたかどうかを返す
	 * 
	 * @param role
	 * @return
	 */
	boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/**
	 * リストからランダムに選んで返す
	 * 
	 * @param list
	 * @return
	 */
	<T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	@Override
	public String getName() {
		return "SampleBasePlayer";
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		currentGameInfo = gameInfo;
		day = -1;
		me = currentGameInfo.getAgent();
		aliveOthers = new ArrayList<>(currentGameInfo.getAliveAgentList());
		aliveOthers.remove(me);
		executedAgents.clear();
		killedAgents.clear();
		divinationList.clear();
		identList.clear();
		comingoutMap.clear();
		estimateReasonMap.clear();
	}

	@Override
	public void update(GameInfo gameInfo) {
		currentGameInfo = gameInfo;
		// 1日の最初の呼び出しはdayStart()の前なので何もしない
		if (currentGameInfo.getDay() == day + 1) {
			day = currentGameInfo.getDay();
			return;
		}
		// 2回目の呼び出し以降
		// （夜限定）追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getLatestExecutedAgent());
		// GameInfo.talkListからカミングアウト・占い報告・霊媒報告を抽出
		for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
			Talk talk = currentGameInfo.getTalkList().get(i);
			Agent talker = talk.getAgent();
			if (talker == me) {
				continue;
			}
			Content content = new Content(talk.getText());

			// subjectがUNSPECの場合は発話者に入れ替える
			if (content.getSubject() == Content.UNSPEC) {
				content = replaceSubject(content, talker);
			}

			parseSentence(content);
		}

		talkListHead = currentGameInfo.getTalkList().size();
	}

	// 再帰的に文を解析する
	void parseSentence(Content content) {
		if (estimateReasonMap.put(content)) {
			return; // 推測文と解析できた
		}
		if (voteReasonMap.put(content)) {
			return; // 投票宣言と解析できた
		}
		switch (content.getTopic()) {
			case COMINGOUT:
				comingoutMap.put(content.getTarget(), content.getRole());
				return;
			case DIVINED:
				divinationList.add(new Judge(day, content.getSubject(), content.getTarget(), content.getResult()));
				return;
			case IDENTIFIED:
				identList.add(new Judge(day, content.getSubject(), content.getTarget(), content.getResult()));
				return;
			case OPERATOR:
				parseOperator(content);
				return;
			default:
				break;
		}
	}

	// 演算子文を解析する
	void parseOperator(Content content) {
		switch (content.getOperator()) {
			case BECAUSE:
				parseSentence(content.getContentList().get(1));
				break;
			case DAY:
				parseSentence(content.getContentList().get(0));
				break;
			case AND:
			case OR:
			case XOR:
				for (Content c : content.getContentList()) {
					parseSentence(c);
				}
				break;
			case REQUEST:
				if (voteRequestCounter.add(content)) {
					return; // 投票リクエストと解析できた
				}
				break;
			case INQUIRE:
				break;
			default:
				break;
		}
	}

	@Override
	public void dayStart() {
		isRevote = false;
		talkQueue.clear();
		declaredVoteCandidate = null;
		voteCandidate = null;
		talkListHead = 0;
		talkTurn = -1;
		voteReasonMap.clear();
		voteRequestCounter.clear();
		// 前日に追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		// 昨夜死亡した（襲撃された）エージェントを登録
		if (!currentGameInfo.getLastDeadAgentList().isEmpty()) {
			addKilledAgent(currentGameInfo.getLastDeadAgentList().get(0));
		}
	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			aliveOthers.remove(executedAgent);
			if (!executedAgents.contains(executedAgent)) {
				executedAgents.add(executedAgent);
			}
		}
	}

	private void addKilledAgent(Agent killedAgent) {
		if (killedAgent != null) {
			aliveOthers.remove(killedAgent);
			if (!killedAgents.contains(killedAgent)) {
				killedAgents.add(killedAgent);
			}
		}
	}

	/**
	 * 投票先候補を選びvoteCandidateにセットする
	 * 
	 * <blockquote>talk()から呼ばれる</blockquote>
	 */
	void chooseVoteCandidate() {
	}

	/**
	 * 投票先候補を選びvoteCandidateにセットする
	 * 
	 * <blockquote>vote()から呼ばれる</blockquote>
	 */
	void chooseFinalVoteCandidate() {
	}

	@Override
	public String talk() {
		talkTurn++;
		chooseVoteCandidate();
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			// 話すことがない場合は投票先を宣言
			if (talkQueue.isEmpty()) {
				Content vote = voteContent(me, voteCandidate);
				Content request = requestContent(me, Content.ANY, voteContent(Content.ANY, voteCandidate));
				Content reason = voteReasonMap.getReason(me, voteCandidate);
				Content and = andContent(me, vote, request);
				if (reason != null) {
					enqueueTalk(becauseContent(me, reason, and));
				}
				enqueueTalk(vote);
				enqueueTalk(request);
				declaredVoteCandidate = voteCandidate;
			}
		}
		return dequeueTalk();
	}

	void enqueueTalk(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			talkQueue.offer(replaceSubject(content, me));
		} else {
			talkQueue.offer(content);
		}
	}

	String dequeueTalk() {
		if (talkQueue.isEmpty()) {
			return Talk.SKIP;
		}
		Content content = talkQueue.poll();
		if (content.getSubject() == me) {
			return Content.stripSubject(content.getText());
		}
		return content.getText();
	}

	@Override
	public String whisper() {
		return null;
	}

	@Override
	public Agent vote() {
		chooseFinalVoteCandidate();
		isRevote = true;
		return voteCandidate;
	}

	@Override
	public Agent attack() {
		return null;
	}

	@Override
	public Agent divine() {
		return null;
	}

	@Override
	public Agent guard() {
		return null;
	}

	@Override
	public void finish() {
	}

	static Content replaceSubject(Content content, Agent newSubject) {
		if (content.getTopic() == Topic.SKIP || content.getTopic() == Topic.OVER) {
			return content;
		}
		if (newSubject == Content.UNSPEC) {
			return new Content(Content.stripSubject(content.getText()));
		} else {
			return new Content(newSubject + " " + Content.stripSubject(content.getText()));
		}
	}

	// 発話生成を簡略化するためのwrapper
	static Content agreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new AgreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	static Content disagreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new DisagreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	static Content voteContent(Agent subject, Agent target) {
		return new Content(new VoteContentBuilder(subject, target));
	}

	static Content votedContent(Agent subject, Agent target) {
		return new Content(new VotedContentBuilder(subject, target));
	}

	static Content attackContent(Agent subject, Agent target) {
		return new Content(new AttackContentBuilder(subject, target));
	}

	static Content attackedContent(Agent subject, Agent target) {
		return new Content(new AttackedContentBuilder(subject, target));
	}

	static Content guardContent(Agent subject, Agent target) {
		return new Content(new GuardCandidateContentBuilder(subject, target));
	}

	static Content guardedContent(Agent subject, Agent target) {
		return new Content(new GuardedAgentContentBuilder(subject, target));
	}

	static Content estimateContent(Agent subject, Agent target, Role role) {
		return new Content(new EstimateContentBuilder(subject, target, role));
	}

	static Content coContent(Agent subject, Agent target, Role role) {
		return new Content(new ComingoutContentBuilder(subject, target, role));
	}

	static Content requestContent(Agent subject, Agent target, Content content) {
		return new Content(new RequestContentBuilder(subject, target, content));
	}

	static Content inquiryContent(Agent subject, Agent target, Content content) {
		return new Content(new InquiryContentBuilder(subject, target, content));
	}

	static Content divinationContent(Agent subject, Agent target) {
		return new Content(new DivinationContentBuilder(subject, target));
	}

	static Content divinedContent(Agent subject, Agent target, Species result) {
		return new Content(new DivinedResultContentBuilder(subject, target, result));
	}

	static Content identContent(Agent subject, Agent target, Species result) {
		return new Content(new IdentContentBuilder(subject, target, result));
	}

	static Content andContent(Agent subject, Content... contents) {
		return new Content(new AndContentBuilder(subject, contents));
	}

	static Content orContent(Agent subject, Content... contents) {
		return new Content(new OrContentBuilder(subject, contents));
	}

	static Content xorContent(Agent subject, Content content1, Content content2) {
		return new Content(new XorContentBuilder(subject, content1, content2));
	}

	static Content notContent(Agent subject, Content content) {
		return new Content(new NotContentBuilder(subject, content));
	}

	static Content dayContent(Agent subject, int day, Content content) {
		return new Content(new DayContentBuilder(subject, day, content));
	}

	static Content becauseContent(Agent subject, Content reason, Content action) {
		return new Content(new BecauseContentBuilder(subject, reason, action));
	}

}
