package test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import core.model.Role;
import libs.Pair;
import utils.OptimizedAgentRoleGenerator;

public class TestOptimizedAgentRoleGenerator {
    @Test
    public void testGenerateAgentRoleCombinations() {
        List<Pair<InetAddress, Integer>> sockets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            sockets.add(new Pair<>(InetAddress.getLoopbackAddress(), 10000 + i));
        }

        // Create an AgentRoleGenerator with 100 battles and 8 agents per battle
        OptimizedAgentRoleGenerator generator = new OptimizedAgentRoleGenerator(sockets, 20, 5);
        System.out.println(generator);

        // Write the optimized combinations to a file
        File optimizedFile = new File("optimized_combinations.txt");
        List<Map<Pair<InetAddress, Integer>, Role>> combinations = generator.toList();

        try (FileWriter fileWriter = new FileWriter(optimizedFile)) {
            for (Map<Pair<InetAddress, Integer>, Role> combination : combinations) {
                StringBuilder sb = new StringBuilder();
                for (Pair<InetAddress, Integer> socket : combination.keySet()) {
                    sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combination.get(socket)));
                }
                fileWriter.write(sb.toString().trim());
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
        }
    }

}
