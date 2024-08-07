package test.utils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import utils.OptimizedAgentRoleGenerator;

public class TestOptimizedAgentRoleGenerator {
    @Test
    public void testGenerateAgentRoleCombinations() {
        List<Socket> sockets = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            sockets.add(new Socket());
        }

        // Create an AgentRoleGenerator with 100 battles and 8 agents per battle
        OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(sockets, 20, 5);
        System.out.println(generator);
    }

}
