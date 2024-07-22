package core.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import utils.IniLoader;

@JsonNaming(PropertyNamingStrategy.class)
public record Config(
        boolean saveLog,
        String logDir,
        boolean saveRoleCombination,
        String combinationsLogFilename,
        boolean isServer,
        int serverPort,
        boolean listenPort,
        int connectAgentNum,
        int idleConnectionTimeout,
        String player1Ip,
        int player1Port,
        String player2Ip,
        int player2Port,
        String player3Ip,
        int player3Port,
        String player4Ip,
        int player4Port,
        String player5Ip,
        int player5Port,
        String player6Ip,
        int player6Port,
        String player7Ip,
        int player7Port,
        String player8Ip,
        int player8Port,
        String player9Ip,
        int player9Port,
        String player10Ip,
        int player10Port,
        boolean continueCombinations,
        int continueCombinationsNum,
        int maxParallelExec,
        boolean prioritizeCombinations,
        boolean singleAgentPerIp,
        boolean joinHuman,
        String humanName,
        HumanRole humanRole,
        int humanAgentNum,
        int allParticipantNum,
        int battleAgentNum,
        int gameNum,
        int maxTalkNum,
        int maxTalkTurn,
        boolean talkOnFirstDay,
        int responseTimeout,
        int actionTimeout,
        boolean ignoreAgentException,
        String requiredAgentName) {
    public enum HumanRole {
        VILLAGER, SEER, POSSESSED, WEREWOLF, NULL,
    }

    public Config() {
        this(
                true,
                "./log/",
                true,
                "./log/combinations",
                false,
                10000,
                false,
                5,
                1800000,
                "127.0.0.1",
                50000,
                "127.0.0.1",
                50001,
                "127.0.0.1",
                50002,
                "127.0.0.1",
                50003,
                "127.0.0.1",
                50004,
                "127.0.0.1",
                50005,
                "127.0.0.1",
                50006,
                "127.0.0.1",
                50007,
                "127.0.0.1",
                50008,
                "127.0.0.1",
                50009,
                false,
                3,
                5,
                false,
                false,
                false,
                "Human",
                HumanRole.SEER,
                1,
                5,
                5,
                1,
                5,
                20,
                true,
                6000,
                3000,
                true,
                "");
    }

    public static Config load(String filename) throws IOException, ReflectiveOperationException {
        return IniLoader.load(filename, Config.class);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config {");
        for (var field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                sb.append(System.lineSeparator()).append("  ").append(field.getName()).append(": ")
                        .append(field.get(this));
            } catch (IllegalAccessException e) {
                sb.append(System.lineSeparator()).append("  ").append(field.getName()).append(": ")
                        .append("ACCESS ERROR");
            }
        }
        sb.append(System.lineSeparator()).append("}");
        return sb.toString();
    }
}
