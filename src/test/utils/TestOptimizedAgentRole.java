package test.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import libs.Pair;
import utils.OptimizedAgentRole;

public class TestOptimizedAgentRole {
    @Test
    public void testGenerateAgentRoleCombinations() {
        List<Pair<InetAddress, Integer>> sockets = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            sockets.add(new Pair<>(InetAddress.getLoopbackAddress(), 10000 + i));
        }

        OptimizedAgentRole role = new OptimizedAgentRole(sockets);
        System.out.println(role);
        System.out.println(role.toList());
    }

}
