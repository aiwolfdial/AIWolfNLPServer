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
import utils.AgentRoleCombosGenerator;

public class TestAgentRoleCombosGenerator {
    @Test
    public void testGenerateAgentRoleCombos() {
        List<Pair<InetAddress, Integer>> sockets = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            sockets.add(new Pair<>(InetAddress.getLoopbackAddress(), 10000 + i));
        }

        AgentRoleCombosGenerator generator = new AgentRoleCombosGenerator(sockets, 32, 5);
        System.out.println(generator);

        File file = new File("combos.txt");
        List<Map<Pair<InetAddress, Integer>, Role>> combos = generator.toList();

        try (FileWriter fileWriter = new FileWriter(file)) {
            for (Map<Pair<InetAddress, Integer>, Role> combo : combos) {
                StringBuilder sb = new StringBuilder();
                for (Pair<InetAddress, Integer> socket : combo.keySet()) {
                    sb.append(String.format("%s:%d-%s ", socket.key(), socket.value(), combo.get(socket)));
                }
                fileWriter.write(sb.toString().trim());
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
        }
    }

}
