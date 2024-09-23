package utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.model.Role;
import libs.Pair;

public class OptimizedAgentRole {
    private final List<Pair<InetAddress, Integer>> globalSockets;
    private final List<Map<Pair<InetAddress, Integer>, Role>> agentRoleCombinations;

    private final Map<Integer, String> JP_TRACKS = new HashMap<Integer, String>() {
        {
            put(5, """
                    1-VILLAGER 2-WEREWOLF 3-SEER 4-VILLAGER 0-POSSESSED
                    """);
            put(6, """
                    1-VILLAGER 2-WEREWOLF 3-SEER 4-VILLAGER 0-POSSESSED
                    """);
            put(7, """
                    1-VILLAGER 2-WEREWOLF 3-SEER 4-VILLAGER 0-POSSESSED
                    """);
            put(8, """
                    1-VILLAGER 2-WEREWOLF 3-SEER 4-VILLAGER 0-POSSESSED
                    """);
        }
    };

    public OptimizedAgentRole(List<Pair<InetAddress, Integer>> globalSockets) {
        this.globalSockets = new ArrayList<>(globalSockets);
        this.agentRoleCombinations = new ArrayList<>();
        for (String line : JP_TRACKS.get(globalSockets.size()).split("\n")) {
            Map<Pair<InetAddress, Integer>, Role> combination = new HashMap<>();
            String[] parts = line.trim().split(" ");
            for (String part : parts) {
                String[] socketRole = part.split("-");
                int index = Integer.parseInt(socketRole[0]);
                Role role = Role.valueOf(socketRole[1]);
                combination.put(new Pair<>(globalSockets.get(index).key(), globalSockets.get(index).value()), role);
            }
            agentRoleCombinations.add(combination);
        }
    }

    public List<Map<Pair<InetAddress, Integer>, Role>> toList() {
        return new ArrayList<>(agentRoleCombinations);
    }

    @Override
    public String toString() {
        Map<Pair<InetAddress, Integer>, Map<Role, Integer>> socketRoleCount = new HashMap<>();
        Map<Pair<InetAddress, Integer>, Integer> socketMatchCount = new HashMap<>();
        for (Pair<InetAddress, Integer> socket : globalSockets) {
            socketRoleCount.put(socket, new HashMap<>());
            socketMatchCount.put(socket, 0);
            for (Role role : Role.values()) {
                socketRoleCount.get(socket).put(role, 0);
            }
        }

        for (Map<Pair<InetAddress, Integer>, Role> combination : agentRoleCombinations) {
            for (Map.Entry<Pair<InetAddress, Integer>, Role> entry : combination.entrySet()) {
                Pair<InetAddress, Integer> socket = entry.getKey();
                Role role = entry.getValue();
                socketRoleCount.get(socket).put(role, socketRoleCount.get(socket).get(role) + 1);
                socketMatchCount.put(socket, socketMatchCount.get(socket) + 1);
            }
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<Pair<InetAddress, Integer>, Map<Role, Integer>> entry : socketRoleCount.entrySet()) {
            StringBuilder rolesLine = new StringBuilder(entry.getKey() + "\t");
            for (Map.Entry<Role, Integer> roleCount : entry.getValue().entrySet()) {
                if (roleCount.getValue() > 0) {
                    rolesLine.append(roleCount.getKey()).append(":").append(roleCount.getValue()).append("\t");
                }
            }
            rolesLine.append("\t").append("Matches:").append(socketMatchCount.get(entry.getKey()));
            result.append(rolesLine.toString().trim()).append("\n");
        }
        return result.toString();
    }
}