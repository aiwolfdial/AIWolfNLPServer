package test.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import core.model.Agent;
import core.packet.GameInfo;
import utils.JsonParser;

public class TestJsonParser {
    @Test
    public void testEncodeGameInfo() {
        Agent.getAgent(1, "agent1");
        GameInfo gameInfo = new GameInfo(1, Agent.getAgent(1));
        String json = JsonParser.encode(gameInfo);
        assertEquals(
                """
                        {"day":1,"agent":1,"mediumResult":null,"divineResult":null,"executedAgent":null,"latestExecutedAgent":null,"attackedAgent":null,"cursedFox":null,"guardedAgent":null,"voteList":[],"latestVoteList":[],"attackVoteList":[],"latestAttackVoteList":[],"talkList":[],"whisperList":[],"statusMap":{},"roleMap":{},"remainTalkMap":{},"remainWhisperMap":{},"existingRoleList":[],"lastDeadAgentList":[]}""",
                json);
    }
}
