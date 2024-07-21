package common.net;

import java.util.Map;

import common.GameConfiguration;
import common.data.Role;

public class GameSetting {
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
	// 得票数同数で決まらなかった場合「追放なし」とするかどうか。falseの場合はランダム
	private boolean isEnableNoExecution;
	// Day 0にtalkがあるかどうか
	private boolean isTalkOnFirstDay;
	// 再襲撃投票前にwhisperするかどうか
	private boolean isWhisperBeforeRevote;
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

	public boolean isEnableNoAttack() {
		return isEnableNoAttack;
	}

	public boolean isVoteVisible() {
		return isVoteVisible;
	}

	public boolean isEnableNoExecution() {
		return isEnableNoExecution;
	}

	public boolean isTalkOnFirstDay() {
		return isTalkOnFirstDay;
	}

	public boolean isEnableRoleRequest() {
		return isEnableRoleRequest;
	}

	public boolean isWhisperBeforeRevote() {
		return isWhisperBeforeRevote;
	}

	public int getPlayerNum() {
		return roleNumMap.values().stream().mapToInt(Integer::intValue).sum();
	}

	public int getResponseTimeout() {
		return responseTimeout;
	}

	public int getActionTimeout() {
		return actionTimeout;
	}

	public int getMaxRevote() {
		return maxRevote;
	}

	public int getMaxAttackRevote() {
		return maxAttackRevote;
	}
}
