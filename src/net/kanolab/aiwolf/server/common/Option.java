package net.kanolab.aiwolf.server.common;

/**
 * iniファイルで設定できる内容の定義<br>
 * 値に列挙型を使用する場合はstringを引数に列挙型を返すstringToParam(String)メソッドを必ず実装しなければならない
 */
public enum Option{
	/**
	 * ログの保存先<br>
	 * String型<br>
	 * 初期値 : ./log/
	 */
	LOG_DIR("logDir","./log/", String.class),
	/**
	 * 使用するport<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PORT_NUM("port", "10000", Integer.class),
	/**
	 * 対戦するエージェントの人数<br>
	 * int型<br>
	 * 初期値 : 5
	 */
	BATTLE_AGENT_NUM("agentNum","5", Integer.class),
	/**
	 * 接続するエージェントの人数<br>
	 * int型<br>
	 * 初期値 : 5
	 */
	CONNECT_AGENT_NUM("connectAgentNum", "5", Integer.class),
	/**
	 * 同一の組み合わせで行うゲーム数<br>
	 * int型<br>
	 * 初期値 : 1
	 */
	GAME_NUM("gameNum","1", Integer.class),
	/**
	 * 1エージェントが1日に行える発話上限<br>
	 * int型<br>
	 * 初期値 : 10
	 */
	MAX_TALK_NUM("maxTalk", "10", Integer.class),
	/**
	 * 発話ターン上限<br>
	 * int型<br>
	 * 初期値 : 20
	 */
	MAX_TALK_TURN("maxTurn", "20", Integer.class),
	/**
	 * 並列対戦数<br>
	 * int型<br>
	 * 初期値 : 5
	 */
	PARALLEL_RUNNING_GAME_NUM("parallel","5", Integer.class),
	/**
	 * エージェントからの応答時間制限<br>
	 * long型<br>
	 * 初期値 : 5000
	 */
	TIMEOUT("timeout","5000", Long.class),
	/**
	 * ログを保存するかどうか<br>
	 * boolean型<br>
	 * 初期値 : false
	 */
	IS_SAVE_LOG("saveLog","false", Boolean.class),
	/**
	 * エージェントが落ちても試合を続行するかどうか<br>
	 * boolean型<br>
	 * 初期値 : false
	 */
	CONTINUE_EXCEPTION_AGENT("continueException","false", Boolean.class),
	/**
	 * 同一IPからの接続は同一IP同士で対戦する<br>
	 * boolean型<br>
	 * 初期値 : false
	 */
	RUN_SINGLE_PORT_GAME("isSingle","false", Boolean.class),
	/**
	 * 初日発話を行うかどうか<br>
	 * boolean型<br>
	 * 初期値 : true
	 */
	TALK_FIRST_DAY("talkOnFirstDay", "true", Boolean.class),
	/**
	 * 1IPから1エージェントまで<br>
	 * boolean型<br>
	 * 初期値 : true
	 */
	ONLY_1AGENT_BY_IP("only1AgentByIP","true",Boolean.class),
	/**
	 * 接続のまま放置しているコネクションを対戦に使用しなかった場合に削除する時間(h)<br>
	 * int型<br>
	 * 初期値 : 24
	 */
	DELETE_WAITING_CONNECTION_TIME("deleteConnectionTime", "24", Integer.class),
	/**
	 * 必ず含めるエージェント名(containsで判定)<br>
	 * String型<br>
	 * 初期値 : 空文字列
	 */
	ESSENTIAL_AGENT_NAME("essentialAgentName","", String.class),
	/**
	 * ゲーム数よりもコンビネーション数を優先する<br>
	 * boolean型<br>
	 * 初期値 : true
	 */
	PRIORITY_COMBINATION("isPriorCombination","true", Boolean.class),
	/**
	 * GUIモードで使用する接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	GUI_ADDRESS("GUIAddress", "127.0.0.1", String.class),
	/**
	 * GUIモードで使用するポート<br>
	 * int型<br>
	 * 初期値 : 9999
	 */
	GUI_PORT("GUIPort", "9999", Integer.class),
	/**
	 * CUIかGUIか<br>
	 * (cui/guiが有効、大文字小文字区別なし)<br>
	 * ViewerMode列挙型<br>
	 * 初期値 : CUI
	 */
	VIEWER_MODE("viewerMode", "cui", ViewerMode.class),
	/**
	 * デバッグモード（テスト出力等）を行うか<br>
	 * boolean型<br>
	 * 初期値 : false
	 */
	DEBUG_MODE("debugMode", "false", Boolean.class),
	/**
	 * 同期モードか非同期モードか<br>
	 * (synchronous/asynchronousが有効、大文字小文字区別なし)<br>
	 * GameMode列挙型<br>
	 * 初期値 : SYNCHRONOUS
	 */
	GAME_MODE("gameMode", "synchronous", GameMode.class),
	/**
	 * Humanが参加するかどうか<br>
	 * boolean方<br>
	 * 初期値：false
	 */
	PLAY_HUMAN("playHuman", "false", Boolean.class),
	HUMAN_NAME("humanName", "Human", String.class),
	/**
	 * 人間モードの際に人間の役職を何にするか<br>
	 * (villager/seer/possessed/werewolf/nullが有効、大文字小文字区別なし)<br>
	 * HumanRole型<br>
	 * 初期値 : null(指定無し)
	 */
	HUMAN_ROLE("humanRole", "null", HumanRole.class),
	/**
	 * 人間モードの際のHumanのエージェント番号<br>
	 * int型<br>
	 * 1以上、参加人数以下の値を指定する。
	 * 初期値：1（0以下は指定無し扱い）
	 */
	HUMAN_NUMBER("humanNumber", "0", Integer.class),
	//以下はあとで実装予定
	//対戦後もクライアントとの接続を再利用するかどうか
	//LOOP("isLoop","false", Boolean.class),
	//コンソールのログを出力するかどうか
	//CONSOLELOG("showConsoleLog","false", Boolean.class),
	//接続クライアントに人間が含まれるかどうか
	//HUMAN("containsHuman","false", Boolean.class),

	/**
	 * プレイヤー側がサーバとして振る舞うときの1人目の接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	PLAYER_HOST1("PlayerHost1", "127.0.0.1", String.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの1人目の接続先ポート<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PLAYER_PORT1("PlayerPort1", "10000", Integer.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの2人目の接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	PLAYER_HOST2("PlayerHost2", "127.0.0.1", String.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの2人目の接続先ポート<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PLAYER_PORT2("PlayerPort2", "10000", Integer.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの3人目の接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	PLAYER_HOST3("PlayerHost3", "127.0.0.1", String.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの3人目の接続先ポート<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PLAYER_PORT3("PlayerPort3", "10000", Integer.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの4人目の接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	PLAYER_HOST4("PlayerHost4", "127.0.0.1", String.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの4人目の接続先ポート<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PLAYER_PORT4("PlayerPort4", "10000", Integer.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの5人目の接続先アドレス<br>
	 * String型<br>
	 * 初期値 : 127.0.0.1 (localhost)
	 */
	PLAYER_HOST5("PlayerHost5", "127.0.0.1", String.class),
	/**
	 * プレイヤー側がサーバとして振る舞うときの5人目の接続先ポート<br>
	 * int型<br>
	 * 初期値 : 10000
	 */
	PLAYER_PORT5("PlayerPort5", "10000", Integer.class),
	;


	private String name;
	private String parameter;
	private Class<?> c;
	public static final Option[] ENUM_OPTIONS = {
			Option.VIEWER_MODE,
			Option.GAME_MODE,
			Option.HUMAN_ROLE,
	};



	private Option(String name, String parameter, Class<?> c){
		this.name= name;
		this.parameter =parameter;
		this.c = c;
	}

	public static Option get(String name){
		name = name.trim();
		for(Option o : Option.values()){
			if(o.name.equalsIgnoreCase(name)){
				return o;
			}
		}
		return null;
	}

	public Class<?> getParamClass(){
		return c;
	}

	public String getDefaultParam(){
		return parameter;
	}
}
