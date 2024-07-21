package common;

import util.Configuration;

public class GameConfiguration {
    public enum HumanRole {
        VILLAGER, SEER, POSSESSED, WEREWOLF, NULL,
    }

    private String logDir = "./log/";
    private int port = 10000;
    private int battleAgentNum = 5;
    private int connectAgentNum = 5;
    private int gameNum = 1;
    private int maxTalkNum = 10;
    private int maxTalkTurn = 20;
    private int parallelRunningGameNum = 5;
    private long responseTimeout = 5000;
    private long actionTimeout = 3000;
    private boolean saveLog = false;
    private boolean continueExceptionAgent = false;
    private boolean matchSameIpOnly = false;
    private boolean talkOnFirstDay = true;
    private boolean singleAgentPerIp = true;
    private int idleConnectionTimeout = 24;
    private String requiredAgentName = "";
    private boolean prioritizeCombinations = true;
    private boolean debugMode = false;
    private boolean synchronousMode = true;
    private boolean joinHuman = false;
    private String humanName = "Human";
    private HumanRole humanRole = HumanRole.SEER;
    private int humanAgentNum = 0;
    private boolean isServer = true;
    private boolean listenPort = true;
    private boolean saveRoleCombination = false;
    private String roleCombinationDir = "./log/";
    private String roleCombinationFilename = "DoneCombinations";
    private int allParticipantNum = 6;
    private boolean continueOtherCombinations = false;
    private int continueCombinationsNum = 3;
    private String player1Ip = "127.0.0.1";
    private int player1Port = 10000;
    private String player2Ip = "127.0.0.1";
    private int player2Port = 10000;
    private String player3Ip = "127.0.0.1";
    private int player3Port = 10000;
    private String player4Ip = "127.0.0.1";
    private int player4Port = 10000;
    private String player5Ip = "127.0.0.1";
    private int player5Port = 10000;
    private String player6Ip = "127.0.0.1";
    private int player6Port = 10000;
    private String player7Ip = "127.0.0.1";
    private int player7Port = 10000;
    private String player8Ip = "127.0.0.1";
    private int player8Port = 10000;
    private String player9Ip = "127.0.0.1";
    private int player9Port = 10000;
    private String player10Ip = "127.0.0.1";
    private int player10Port = 10000;

    public static GameConfiguration load(String path) throws Exception {
        return (GameConfiguration) Configuration.loadFile(path, "game", new GameConfiguration());
    }

    public String getLogDir() {
        return logDir;
    }

    public int getPort() {
        return port;
    }

    public int getBattleAgentNum() {
        return battleAgentNum;
    }

    public int getConnectAgentNum() {
        return connectAgentNum;
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

    public int getParallelRunningGameNum() {
        return parallelRunningGameNum;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public long getActionTimeout() {
        return actionTimeout;
    }

    public boolean isSaveLog() {
        return saveLog;
    }

    public boolean isContinueExceptionAgent() {
        return continueExceptionAgent;
    }

    public boolean isMatchSameIpOnly() {
        return matchSameIpOnly;
    }

    public boolean isTalkOnFirstDay() {
        return talkOnFirstDay;
    }

    public boolean isSingleAgentPerIp() {
        return singleAgentPerIp;
    }

    public int getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    public String getRequiredAgentName() {
        return requiredAgentName;
    }

    public boolean isPrioritizeCombinations() {
        return prioritizeCombinations;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isSynchronousMode() {
        return synchronousMode;
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

    public boolean isServer() {
        return isServer;
    }

    public boolean isListenPort() {
        return listenPort;
    }

    public boolean isSaveRoleCombination() {
        return saveRoleCombination;
    }

    public String getRoleCombinationDir() {
        return roleCombinationDir;
    }

    public String getRoleCombinationFilename() {
        return roleCombinationFilename;
    }

    public int getAllParticipantNum() {
        return allParticipantNum;
    }

    public boolean isContinueOtherCombinations() {
        return continueOtherCombinations;
    }

    public int getContinueCombinationsNum() {
        return continueCombinationsNum;
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
}
