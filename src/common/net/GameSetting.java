package common.net;

import java.util.Map;

import common.GameConfiguration;
import common.data.Role;

public class GameSetting implements Cloneable {
	static final private int[][] ROLES_NUM = {
			// {BODYGUARD (狩人), FREEMASON (共有者), MEDIUM (霊媒師), POSSESSED (狂人), SEER (占い師),
			// VILLAGER (村人), WEREWOLF (人狼), FOX (妖狐), ANY (役職不定)}
			{}, // 0
			{}, // 1
			{}, // 2
			{ 0, 0, 0, 0, 1, 1, 1, 0, 0 }, // 3
			{ 0, 0, 0, 0, 1, 2, 1, 0, 0 }, // 4
			{ 0, 0, 0, 1, 1, 2, 1, 0, 0 }, // 5
			{ 0, 0, 0, 1, 1, 3, 1, 0, 0 }, // 6
			{ 0, 0, 0, 0, 1, 4, 2, 0, 0 }, // 7
			{ 0, 0, 1, 0, 1, 4, 2, 0, 0 }, // 8
			{ 0, 0, 1, 0, 1, 5, 2, 0, 0 }, // 9
			{ 1, 0, 1, 1, 1, 4, 2, 0, 0 }, // 10
			{ 1, 0, 1, 1, 1, 5, 2, 0, 0 }, // 11
			{ 1, 0, 1, 1, 1, 5, 3, 0, 0 }, // 12
			{ 1, 0, 1, 1, 1, 6, 3, 0, 0 }, // 13
			{ 1, 0, 1, 1, 1, 7, 3, 0, 0 }, // 14
			{ 1, 0, 1, 1, 1, 8, 3, 0, 0 }, // 15
			{ 1, 0, 1, 1, 1, 9, 3, 0, 0 }, // 16
			{ 1, 0, 1, 1, 1, 10, 3, 0, 0 }, // 17
			{ 1, 0, 1, 1, 1, 11, 3, 0, 0 }, // 18
	};

	public void setRoleNumMap(int agentNum) {
		if (agentNum < 3 || agentNum > 18) {
			throw new IllegalArgumentException("The number of agents must be between 3 and 18.");
		}
		Role[] roles = Role.values();
		roleNumMap = new java.util.HashMap<>();
		for (int i = 0; i < roles.length; i++) {
			roleNumMap.put(roles[i], ROLES_NUM[agentNum][i]);
		}
	}

	static public GameSetting FromGameConfiguration(GameConfiguration config) {
		GameSetting setting = new GameSetting();
		setting.responseTimeout = (int) config.getResponseTimeout();
		setting.actionTimeout = (int) config.getActionTimeout();
		setting.maxTalk = config.getMaxTalkNum();
		setting.maxTalkTurn = config.getMaxTalkTurn();
		setting.maxWhisper = config.getMaxTalkNum();
		setting.maxWhisperTurn = config.getMaxTalkTurn();
		setting.isTalkOnFirstDay = config.isTalkOnFirstDay();
		setting.setRoleNumMap(config.getBattleAgentNum());
		return setting;
	}

	// 各役職が何人かを関連付けたマップ
	Map<Role, Integer> roleNumMap;
	// 1日あたりの発言の最大数
	int maxTalk;
	// 1日あたりの発言時間の最大数
	int maxTalkTurn;
	// 1日あたりの発言の最大数
	int maxWhisper;
	// 1日あたりの発言時間の最大数
	int maxWhisperTurn;
	// 連続Skipの最大数
	int maxSkip;
	// 誰も襲撃しないのを許すかどうか
	boolean isEnableNoAttack;
	// 誰が誰に投票したかをエージェントが確認できるかどうか
	boolean isVoteVisible;
	// 初日の投票をできるようにするかどうか
	private boolean isVotableInFirstDay;
	// 得票数同数で決まらなかった場合「追放なし」とするかどうか。falseの場合はランダム
	private boolean isEnableNoExecution;
	// Day 0にtalkがあるかどうか
	private boolean isTalkOnFirstDay;
	// 再襲撃投票前にwhisperするかどうか
	private boolean isWhisperBeforeRevote;
	// ランダムシード(乱数種)
	long randomSeed = System.currentTimeMillis();
	// リクエスト応答時間の上限
	int responseTimeout;
	int actionTimeout;
	// 最大再投票回数
	int maxRevote;
	// 最大再襲撃投票回数
	int maxAttackRevote;
	// 役職要求の可否
	boolean isEnableRoleRequest;

	// 指定された役職の人数を返します。
	public int getRoleNum(Role role) {
		return roleNumMap.getOrDefault(role, 0);
	}

	public int getMaxTalk() {
		return maxTalk;
	}

	public int getMaxTalkTurn() {
		return this.maxTalkTurn;
	}

	public int getMaxWhisper() {
		return maxWhisper;
	}

	public int getMaxWhisperTurn() {
		return this.maxWhisperTurn;
	}

	public int getMaxSkip() {
		return maxSkip;
	}

	/**
	 * <div lang="ja">
	 *
	 * 誰も襲撃しないのを許されているかどうかを返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Is the game permit to attack no one?
	 *
	 * </div>
	 *
	 * @return <div lang="ja">
	 *
	 *         誰も襲撃しないのを許されているかどうか
	 *
	 *         </div>
	 *
	 *         <div lang="en">
	 *
	 *         Permission for werewolfs attack no one.
	 *
	 *         </div>
	 */
	public boolean isEnableNoAttack() {
		return isEnableNoAttack;
	}

	/**
	 * <div lang="ja">
	 *
	 * 誰が誰に投票したかをエージェントが確認できるかを返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Can agents see who vote to who?
	 *
	 * </div>
	 *
	 * @return <div lang="ja">
	 *
	 *         誰が誰に投票したかをエージェントが確認できるかどうか
	 *
	 *         </div>
	 *
	 *         <div lang="en">
	 *
	 *         Permission for agents see who vote to who
	 *
	 *         </div>
	 */
	public boolean isVoteVisible() {
		return isVoteVisible;
	}

	/**
	 * <div lang="ja">
	 *
	 * 初日の投票ができるかどうかを返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Are there vote in first day?
	 *
	 * </div>
	 *
	 * @return <div lang="ja">
	 *
	 *         初日の投票ができるかどうか
	 *
	 *         </div>
	 *
	 *         <div lang="en">
	 *
	 *         Permission for there vote in first day
	 *
	 *         </div>
	 */
	public boolean isVotableInFirstDay() {
		return isVotableInFirstDay;
	}

	/**
	 * <div lang="ja">同票数の場合に追放なしとするかどうかを返します。</div>
	 *
	 * <div lang="en">Returns whether or not executing nobody is allowed.</div>
	 *
	 * @return <div lang="ja">同票数の場合に追放なしとするかどうか </div>
	 *
	 *         <div lang="en">whether or not executing nobody is allowed</div>
	 */
	public boolean isEnableNoExecution() {
		return isEnableNoExecution;
	}

	/**
	 * <div lang="ja">Day 0にtalkがあるかどうかを返します。</div>
	 *
	 * <div lang="en">Returns whether or not there are talks on day 0.</div>
	 *
	 * @return <div lang="ja">Day 0にtalkがあるかどうか</div>
	 *
	 *         <div lang="en">whether or not there are talks on day 0</div>
	 */
	public boolean isTalkOnFirstDay() {
		return isTalkOnFirstDay;
	}

	/**
	 *
	 * @return
	 */
	public boolean isEnableRoleRequest() {
		return isEnableRoleRequest;
	}

	/**
	 * <div lang="ja">再襲撃投票前にwhisperするかどうかを返します。</div>
	 *
	 * <div lang="en">Returns whether or not there is whisper before the revote for
	 * attack.</div> *
	 *
	 * @return <div lang="ja">再襲撃投票前にwhisperするかどうか</div>
	 *
	 *         <div lang="en">whether or not there is whisper before the revote for
	 *         attack</div>
	 */
	public boolean isWhisperBeforeRevote() {
		return isWhisperBeforeRevote;
	}

	/**
	 * <div lang="ja">
	 *
	 * プレイヤーの人数を返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Get the number of players.
	 *
	 * </div>
	 *
	 * @return
	 *
	 *         <div lang="ja">プレイヤーの人数</div>
	 *
	 *         <div lang="en">Number of players</div>
	 */
	public int getPlayerNum() {
		int num = 0;
		for (int value : roleNumMap.values()) {
			num += value;
		}
		return num;
	}

	/**
	 * <div lang="ja">
	 *
	 * 役職に対する人数を関連付けたマップを返します。
	 *
	 * </div>
	 *
	 * <div lang="en">
	 *
	 * Get number of each charactors.
	 *
	 * </div>
	 *
	 * @return
	 *
	 *         <div lang="ja">
	 *
	 *         役職に対する人数を関連付けたマップ
	 *
	 *         </div>
	 *
	 *         <div lang="en">
	 *
	 *         Number of each charactors
	 *
	 *         </div>
	 */
	public Map<Role, Integer> getRoleNumMap() {
		return roleNumMap;
	}

	/**
	 * <div lang="ja">ランダムシードを返します。</div>
	 *
	 * <div lang="en">Get the random seed.</div>
	 *
	 * @return
	 *
	 *         <div lang="ja">ランダムシード</div>
	 *
	 *         <div lang="en">Random seed</div>
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * <div lang="ja">ランダムシードを設定します。</div>
	 *
	 * <div lang="en">Set the random seed.</div>
	 *
	 * @param randomSeed
	 *
	 *            <div lang="ja">ランダムシード</div>
	 *
	 *            <div lang="en">Random seed</div>
	 */
	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	/**
	 * <div lang="ja">リクエスト応答時間の上限を返す．</div>
	 *
	 * <div lang="en">Returns the time limit for the response to the request.<div>
	 *
	 * @return <div lang="ja">制限時間(ms)あるいは未設定の場合-1</div>
	 *
	 *         <div lang="en">the time limit in millisecond or -1 if this is not set
	 *         yet<div>
	 */
	public int getResponseTimeout() {
		return responseTimeout;
	}

	public int getActionTimeout() {
		return actionTimeout;
	}

	/**
	 * <div lang="ja">リクエスト応答時間の上限をセットします。</div>
	 *
	 * <div lang="en">Sets the time limit for the response to the request.</div>
	 *
	 * @param responseTimeout
	 *            <div lang="ja">制限時間</div>
	 *
	 *            <div lang="en">the timeLimit to set</div>
	 */
	public void setResponseTimeout(int responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	public void setActionTimeout(int actionTimeout) {
		this.actionTimeout = actionTimeout;
	}

	/**
	 * <div lang="ja">最大再投票回数を返します。</div>
	 *
	 * <div lang="en">Returns the maximum number of revotes.<div>
	 *
	 * @return <div lang="ja">最大再投票回数</div>
	 *
	 *         <div lang="en">the maximum number of revotes<div>
	 */
	public int getMaxRevote() {
		return maxRevote;
	}

	/**
	 * <div lang="ja">最大再投票回数をセットします。</div>
	 *
	 * <div lang="en">Sets the maximum number of revotes.</div>
	 *
	 * @param maxRevote
	 *            <div lang="ja">最大再投票回数</div>
	 *
	 *            <div lang="en">the maximum number of revotes</div>
	 */
	public void setMaxRevote(int maxRevote) {
		this.maxRevote = maxRevote;
	}

	/**
	 * <div lang="ja">最大再襲撃投票回数を返します。</div>
	 *
	 * <div lang="en">Returns the maximum number of revotes for attack.<div>
	 *
	 * @return <div lang="ja">最大再襲撃投票回数</div>
	 *
	 *         <div lang="en">the maximum number of revotes for attack<div>
	 */
	public int getMaxAttackRevote() {
		return maxAttackRevote;
	}

	/**
	 * <div lang="ja">最大再襲撃投票回数をセットします。</div>
	 *
	 * <div lang="en">Sets the maximum number of revotes for attack.</div>
	 *
	 * @param maxRevote
	 *            <div lang="ja">最大再襲撃投票回数</div>
	 *
	 *            <div lang="en">the maximum number of revotes for
	 *            attack</div>
	 */
	public void setMaxAttackRevote(int maxAttackRevote) {
		this.maxAttackRevote = maxAttackRevote;
	}

	/**
	 *
	 * @param isEnable
	 */
	public void setEnableRoleRequest(boolean isEnable) {
		isEnableRoleRequest = isEnable;
	}

	/**
	 * <div lang="ja">ゲーム設定の複製を作成し、返します。</div>
	 *
	 * <div lang="en">Create copy.</div>
	 *
	 * @return <div lang="ja">ゲーム設定の複製</div>
	 *
	 *         <div lang="en">Copy of this object</div>
	 */
	@Override
	public GameSetting clone() {
		GameSetting gameSetting = new GameSetting();
		gameSetting.isEnableNoAttack = isEnableNoAttack;
		gameSetting.isVotableInFirstDay = isVotableInFirstDay;
		gameSetting.isVoteVisible = isVoteVisible;
		gameSetting.isEnableNoExecution = isEnableNoExecution;
		gameSetting.isTalkOnFirstDay = isTalkOnFirstDay;
		gameSetting.isWhisperBeforeRevote = isWhisperBeforeRevote;
		gameSetting.maxTalk = maxTalk;
		gameSetting.maxWhisper = maxWhisper;
		gameSetting.maxWhisperTurn = maxWhisperTurn;
		gameSetting.maxTalkTurn = maxTalkTurn;
		gameSetting.maxSkip = maxSkip;
		gameSetting.randomSeed = randomSeed;
		gameSetting.responseTimeout = responseTimeout;
		gameSetting.actionTimeout = actionTimeout;
		gameSetting.maxRevote = maxRevote;
		gameSetting.maxAttackRevote = maxAttackRevote;
		gameSetting.roleNumMap.putAll(roleNumMap);
		gameSetting.isEnableRoleRequest = isEnableRoleRequest;
		return gameSetting;
	}
}
