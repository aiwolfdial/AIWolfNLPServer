package core;

import utils.IniLoader;

public class Config {
    public enum HumanRole {
        VILLAGER, SEER, POSSESSED, WEREWOLF, NULL,
    }

    private boolean saveLog = true;
    private String logDir = "./log/";
    private boolean saveRoleCombination = true;
    private String combinationsLogFilename = "./log/combinations";

    private boolean isServer = false;
    private int serverPort = 10000;
    private boolean listenPort = false;
    private int connectAgentNum = 5;
    private int idleConnectionTimeout = 1800000;
    private String player1Ip = "127.0.0.1";
    private int player1Port = 50000;
    private String player2Ip = "127.0.0.1";
    private int player2Port = 50001;
    private String player3Ip = "127.0.0.1";
    private int player3Port = 50002;
    private String player4Ip = "127.0.0.1";
    private int player4Port = 50003;
    private String player5Ip = "127.0.0.1";
    private int player5Port = 50004;
    private String player6Ip = "127.0.0.1";
    private int player6Port = 50005;
    private String player7Ip = "127.0.0.1";
    private int player7Port = 50006;
    private String player8Ip = "127.0.0.1";
    private int player8Port = 50007;
    private String player9Ip = "127.0.0.1";
    private int player9Port = 50008;
    private String player10Ip = "127.0.0.1";
    private int player10Port = 50009;

    private boolean continueCombinations = false;
    private int continueCombinationsNum = 3;
    private int maxParallelExec = 5;
    private boolean prioritizeCombinations = false;
    private boolean matchSameIpOnly = false;
    private boolean singleAgentPerIp = false;
    private boolean joinHuman = false;
    private String humanName = "Human";
    private HumanRole humanRole = HumanRole.SEER;
    private int humanAgentNum = 1;

    private int allParticipantNum = 5;
    private int battleAgentNum = 5;
    private int gameNum = 1;
    private int maxTalkNum = 5;
    private int maxTalkTurn = 20;
    private boolean talkOnFirstDay = true;
    private long responseTimeout = 6000;
    private long actionTimeout = 3000;
    private boolean ignoreAgentException = true;
    private String requiredAgentName = "";

    public static Config load(String path) throws Exception {
        return (Config) IniLoader.loadFile(path, "game", new Config());
    }

    public boolean isSaveLog() {
        return saveLog;
    }

    public String getLogDir() {
        return logDir;
    }

    public boolean isSaveRoleCombination() {
        return saveRoleCombination;
    }

    public String getCombinationsLogFilename() {
        return combinationsLogFilename;
    }

    public boolean isServer() {
        return isServer;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean isListenPort() {
        return listenPort;
    }

    public int getConnectAgentNum() {
        return connectAgentNum;
    }

    public int getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    public String getPlayer1Ip() {
        return player1Ip;
    }

    public int getPlayer1Port() {
        return player1Port;
    }

    public String getPlayer2Ip() {
        return player2Ip;
    }

    public int getPlayer2Port() {
        return player2Port;
    }

    public String getPlayer3Ip() {
        return player3Ip;
    }

    public int getPlayer3Port() {
        return player3Port;
    }

    public String getPlayer4Ip() {
        return player4Ip;
    }

    public int getPlayer4Port() {
        return player4Port;
    }

    public String getPlayer5Ip() {
        return player5Ip;
    }

    public int getPlayer5Port() {
        return player5Port;
    }

    public String getPlayer6Ip() {
        return player6Ip;
    }

    public int getPlayer6Port() {
        return player6Port;
    }

    public String getPlayer7Ip() {
        return player7Ip;
    }

    public int getPlayer7Port() {
        return player7Port;
    }

    public String getPlayer8Ip() {
        return player8Ip;
    }

    public int getPlayer8Port() {
        return player8Port;
    }

    public String getPlayer9Ip() {
        return player9Ip;
    }

    public int getPlayer9Port() {
        return player9Port;
    }

    public String getPlayer10Ip() {
        return player10Ip;
    }

    public int getPlayer10Port() {
        return player10Port;
    }

    public boolean isContinueCombinations() {
        return continueCombinations;
    }

    public int getContinueCombinationsNum() {
        return continueCombinationsNum;
    }

    public int getMaxParallelExec() {
        return maxParallelExec;
    }

    public boolean isPrioritizeCombinations() {
        return prioritizeCombinations;
    }

    public boolean isMatchSameIpOnly() {
        return matchSameIpOnly;
    }

    public boolean isSingleAgentPerIp() {
        return singleAgentPerIp;
    }

    public boolean isJoinHuman() {
        return joinHuman;
    }

    public String getHumanName() {
        return humanName;
    }

    public HumanRole getHumanRole() {
        return humanRole;
    }

    public int getHumanAgentNum() {
        return humanAgentNum;
    }

    public int getAllParticipantNum() {
        return allParticipantNum;
    }

    public int getBattleAgentNum() {
        return battleAgentNum;
    }

    public int getGameNum() {
        return gameNum;
    }

    public int getMaxTalkNum() {
        return maxTalkNum;
    }

    public int getMaxTalkTurn() {
        return maxTalkTurn;
    }

    public boolean isTalkOnFirstDay() {
        return talkOnFirstDay;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public long getActionTimeout() {
        return actionTimeout;
    }

    public boolean isIgnoreAgentException() {
        return ignoreAgentException;
    }

    public String getRequiredAgentName() {
        return requiredAgentName;
    }
}
