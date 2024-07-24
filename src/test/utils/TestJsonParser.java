package test.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import core.model.Agent;
import core.model.GameInfo;
import utils.JsonParser;

public class TestJsonParser {
    @Test
    public void testEncodeAgent() {
        Agent.setAgent(1, "agent1");
        String json = JsonParser.encode(Agent.getAgent(1));
        assertEquals("\"Agent[01]\"", json);
    }

    @Test
    public void testDecodeAgent() {
        Agent.setAgent(1, "agent1");
        Agent agent = JsonParser.decode("\"Agent[01]\"", Agent.class);
        assertEquals(Agent.getAgent(1), agent);
    }

    @Test
    public void testEncodeGameInfo() {
        Agent.setAgent(1, "agent1");
        GameInfo gameInfo = new GameInfo(1, Agent.getAgent(1));
        String json = JsonParser.encode(gameInfo);
        assertEquals(
                """
                        {"day":1,"agent":"Agent[01]","voteList":[],"latestVoteList":[],"attackVoteList":[],"latestAttackVoteList":[],"talkList":[],"whisperList":[],"statusMap":{},"roleMap":{},"remainTalkMap":{},"remainWhisperMap":{},"existingRoleList":[],"lastDeadAgentList":[]}""",
                json);
    }
}
