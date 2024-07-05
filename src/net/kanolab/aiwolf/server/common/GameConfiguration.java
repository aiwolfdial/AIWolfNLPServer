package net.kanolab.aiwolf.server.common;

import java.io.File;

import org.aiwolf.ui.HumanPlayer;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class GameConfiguration {
    private String logDir = "./log/";
    private int port = 10000;
    private int battleAgentNum = 5;
    private int connectAgentNum = 5;
    private int gameNum = 1;
    private int maxTalkNum = 10;
    private int maxTalkTurn = 20;
    private int parallelRunningGameNum = 5;
    private long responseTimeout = 5000;
    private boolean saveLog = false;
    private boolean continueExceptionAgent = false;
    private boolean matchSameIpOnly = false;
    private boolean talkOnFirstDay = true;
    private boolean singleAgentPerIp = true;
    private int idleConnectionTimeout = 24;
    private String requiredAgentName = "";
    private boolean prioritizeCombinations = true;
    private String guiIp = "127.0.0.1";
    private int guiPort = 9999;
    private boolean useGui = false;
    private boolean debugMode = false;
    private boolean synchronousMode = true;
    private boolean joinHuman = false;
    private String humanName = "Human";
    private Class<?> humanClass = HumanPlayer.class;
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

    public GameConfiguration(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File not found: " + path);
            return;
        }
        try {
            Ini ini = new Ini(file);
            Section section = ini.get("game");
            if (section.containsKey("logDir")) {
                logDir = section.get("logDir");
            }
            if (section.containsKey("port")) {
                port = Integer.parseInt(section.get("port"));
            }
            if (section.containsKey("battleAgentNum")) {
                battleAgentNum = Integer.parseInt(section.get("battleAgentNum"));
            }
            if (section.containsKey("connectAgentNum")) {
                connectAgentNum = Integer.parseInt(section.get("connectAgentNum"));
            }
            if (section.containsKey("gameNum")) {
                gameNum = Integer.parseInt(section.get("gameNum"));
            }
            if (section.containsKey("maxTalkNum")) {
                maxTalkNum = Integer.parseInt(section.get("maxTalkNum"));
            }
            if (section.containsKey("maxTalkTurn")) {
                maxTalkTurn = Integer.parseInt(section.get("maxTalkTurn"));
            }
            if (section.containsKey("parallelRunningGameNum")) {
                parallelRunningGameNum = Integer.parseInt(section.get("parallelRunningGameNum"));
            }
            if (section.containsKey("responseTimeout")) {
                responseTimeout = Long.parseLong(section.get("responseTimeout"));
            }
            if (section.containsKey("saveLog")) {
                saveLog = Boolean.parseBoolean(section.get("saveLog"));
            }
            if (section.containsKey("continueExceptionAgent")) {
                continueExceptionAgent = Boolean.parseBoolean(section.get("continueExceptionAgent"));
            }
            if (section.containsKey("matchSameIpOnly")) {
                matchSameIpOnly = Boolean.parseBoolean(section.get("matchSameIpOnly"));
            }
            if (section.containsKey("talkOnFirstDay")) {
                talkOnFirstDay = Boolean.parseBoolean(section.get("talkOnFirstDay"));
            }
            if (section.containsKey("singleAgentPerIp")) {
                singleAgentPerIp = Boolean.parseBoolean(section.get("singleAgentPerIp"));
            }
            if (section.containsKey("idleConnectionTimeout")) {
                idleConnectionTimeout = Integer.parseInt(section.get("idleConnectionTimeout"));
            }
            if (section.containsKey("requiredAgentName")) {
                requiredAgentName = section.get("requiredAgentName");
            }
            if (section.containsKey("prioritizeCombinations")) {
                prioritizeCombinations = Boolean.parseBoolean(section.get("prioritizeCombinations"));
            }
            if (section.containsKey("guiIp")) {
                guiIp = section.get("guiIp");
            }
            if (section.containsKey("guiPort")) {
                guiPort = Integer.parseInt(section.get("guiPort"));
            }
            if (section.containsKey("useGui")) {
                useGui = Boolean.parseBoolean(section.get("useGui"));
            }
            if (section.containsKey("debugMode")) {
                debugMode = Boolean.parseBoolean(section.get("debugMode"));
            }
            if (section.containsKey("synchronousMode")) {
                synchronousMode = Boolean.parseBoolean(section.get("synchronousMode"));
            }
            if (section.containsKey("joinHuman")) {
                joinHuman = Boolean.parseBoolean(section.get("joinHuman"));
            }
            if (section.containsKey("humanName")) {
                humanName = section.get("humanName");
            }
            if (section.containsKey("humanClass")) {
                try {
                    humanClass = Class.forName(section.get("humanClass"));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (section.containsKey("humanAgentNum")) {
                humanAgentNum = Integer.parseInt(section.get("humanAgentNum"));
            }
            if (section.containsKey("isServer")) {
                isServer = Boolean.parseBoolean(section.get("isServer"));
            }
            if (section.containsKey("listenPort")) {
                listenPort = Boolean.parseBoolean(section.get("listenPort"));
            }
            if (section.containsKey("saveRoleCombination")) {
                saveRoleCombination = Boolean.parseBoolean(section.get("saveRoleCombination"));
            }
            if (section.containsKey("roleCombinationDir")) {
                roleCombinationDir = section.get("roleCombinationDir");
            }
            if (section.containsKey("roleCombinationFilename")) {
                roleCombinationFilename = section.get("roleCombinationFilename");
            }
            if (section.containsKey("allParticipantNum")) {
                allParticipantNum = Integer.parseInt(section.get("allParticipantNum"));
            }
            if (section.containsKey("continueOtherCombinations")) {
                continueOtherCombinations = Boolean.parseBoolean(section.get("continueOtherCombinations"));
            }
            if (section.containsKey("continueCombinationsNum")) {
                continueCombinationsNum = Integer.parseInt(section.get("continueCombinationsNum"));
            }
            if (section.containsKey("player1Ip")) {
                player1Ip = section.get("player1Ip");
            }
            if (section.containsKey("player1Port")) {
                player1Port = Integer.parseInt(section.get("player1Port"));
            }
            if (section.containsKey("player2Ip")) {
                player2Ip = section.get("player2Ip");
            }
            if (section.containsKey("player2Port")) {
                player2Port = Integer.parseInt(section.get("player2Port"));
            }
            if (section.containsKey("player3Ip")) {
                player3Ip = section.get("player3Ip");
            }
            if (section.containsKey("player3Port")) {
                player3Port = Integer.parseInt(section.get("player3Port"));
            }
            if (section.containsKey("player4Ip")) {
                player4Ip = section.get("player4Ip");
            }
            if (section.containsKey("player4Port")) {
                player4Port = Integer.parseInt(section.get("player4Port"));
            }
            if (section.containsKey("player5Ip")) {
                player5Ip = section.get("player5Ip");
            }
            if (section.containsKey("player5Port")) {
                player5Port = Integer.parseInt(section.get("player5Port"));
            }
            if (section.containsKey("player6Ip")) {
                player6Ip = section.get("player6Ip");
            }
            if (section.containsKey("player6Port")) {
                player6Port = Integer.parseInt(section.get("player6Port"));
            }
            if (section.containsKey("player7Ip")) {
                player7Ip = section.get("player7Ip");
            }
            if (section.containsKey("player7Port")) {
                player7Port = Integer.parseInt(section.get("player7Port"));
            }
            if (section.containsKey("player8Ip")) {
                player8Ip = section.get("player8Ip");
            }
            if (section.containsKey("player8Port")) {
                player8Port = Integer.parseInt(section.get("player8Port"));
            }
            if (section.containsKey("player9Ip")) {
                player9Ip = section.get("player9Ip");
            }
            if (section.containsKey("player9Port")) {
                player9Port = Integer.parseInt(section.get("player9Port"));
            }
            if (section.containsKey("player10Ip")) {
                player10Ip = section.get("player10Ip");
            }
            if (section.containsKey("player10Port")) {
                player10Port = Integer.parseInt(section.get("player10Port"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public String getGuiIp() {
        return guiIp;
    }

    public int getGuiPort() {
        return guiPort;
    }

    public boolean isUseGui() {
        return useGui;
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

    public Class<?> getHumanClass() {
        return humanClass;
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
