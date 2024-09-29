package core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import utils.IniLoader;

@JsonNaming(PropertyNamingStrategy.class)
public record Config(
        boolean saveGameLog,
        String gameLogDir,
        String comboLogDir,
        String agentAddresses,
        String resumeComboLogPath,
        int battleAgentNum,
        int maxTalkNum,
        int maxTalkTurn,
        boolean talkOnFirstDay,
        int responseTimeout,
        int actionTimeout,
        boolean ignoreAgentException) {
    public enum HumanRole {
        VILLAGER, SEER, POSSESSED, WEREWOLF, NULL,
    }

    public Config() {
        this(
                true,
                "./log/game/",
                "./log/combo/",
                "[127.0.0.1:50000, 127.0.0.1:50001, 127.0.0.1:50002, 127.0.0.1:50003, 127.0.0.1:50004]",
                "",
                5,
                5,
                20,
                true,
                6000,
                3000,
                true);
    }

    public static Config load(String filename) throws IOException, ReflectiveOperationException {
        Config config = IniLoader.load(filename, Config.class);
        Path gameLogDirPath = Paths.get(config.gameLogDir());
        Path comboLogDirPath = Paths.get(config.comboLogDir());
        if (!Files.exists(gameLogDirPath)) {
            Files.createDirectories(gameLogDirPath);
        }
        if (!Files.exists(comboLogDirPath)) {
            Files.createDirectories(comboLogDirPath);
        }
        return config;
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
