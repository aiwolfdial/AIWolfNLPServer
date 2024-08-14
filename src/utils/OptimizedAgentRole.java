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
                    1-POSSESSED 2-VILLAGER 3-VILLAGER 4-SEER 0-WEREWOLF
                    1-VILLAGER 2-POSSESSED 3-WEREWOLF 4-VILLAGER 0-SEER
                    1-VILLAGER 2-SEER 3-POSSESSED 4-WEREWOLF 0-VILLAGER
                    1-WEREWOLF 2-VILLAGER 3-SEER 4-POSSESSED 0-VILLAGER
                    1-SEER 2-VILLAGER 3-WEREWOLF 4-POSSESSED 0-VILLAGER
                    1-VILLAGER 2-SEER 3-POSSESSED 4-VILLAGER 0-WEREWOLF
                    1-VILLAGER 2-POSSESSED 3-VILLAGER 4-WEREWOLF 0-SEER
                    1-WEREWOLF 2-VILLAGER 3-VILLAGER 4-SEER 0-POSSESSED
                    1-SEER 2-WEREWOLF 3-POSSESSED 4-VILLAGER 0-VILLAGER
                    1-POSSESSED 2-WEREWOLF 3-VILLAGER 4-VILLAGER 0-SEER
                    1-SEER 2-VILLAGER 3-WEREWOLF 4-VILLAGER 0-POSSESSED
                    1-POSSESSED 2-SEER 3-VILLAGER 4-VILLAGER 0-WEREWOLF
                    1-VILLAGER 2-POSSESSED 3-SEER 4-WEREWOLF 0-VILLAGER
                    1-WEREWOLF 2-VILLAGER 3-POSSESSED 4-SEER 0-VILLAGER
                    1-WEREWOLF 2-POSSESSED 3-VILLAGER 4-SEER 0-VILLAGER
                    1-VILLAGER 2-VILLAGER 3-SEER 4-POSSESSED 0-WEREWOLF
                    1-POSSESSED 2-SEER 3-VILLAGER 4-WEREWOLF 0-VILLAGER
                    1-VILLAGER 2-VILLAGER 3-WEREWOLF 4-POSSESSED 0-SEER
                    1-SEER 2-WEREWOLF 3-VILLAGER 4-VILLAGER 0-POSSESSED
                    """);
            put(6, """
                    1-SEER 2-VILLAGER 3-POSSESSED 4-WEREWOLF 0-VILLAGER
                    2-POSSESSED 3-VILLAGER 4-VILLAGER 5-WEREWOLF 0-SEER
                    1-WEREWOLF 2-VILLAGER 3-VILLAGER 4-POSSESSED 5-SEER
                    1-VILLAGER 3-SEER 4-VILLAGER 5-POSSESSED 0-WEREWOLF
                    1-POSSESSED 2-WEREWOLF 4-SEER 5-VILLAGER 0-VILLAGER
                    1-VILLAGER 2-SEER 3-WEREWOLF 5-VILLAGER 0-POSSESSED
                    1-POSSESSED 2-VILLAGER 4-SEER 5-WEREWOLF 0-VILLAGER
                    1-SEER 3-WEREWOLF 4-POSSESSED 5-VILLAGER 0-VILLAGER
                    1-VILLAGER 2-WEREWOLF 3-SEER 4-VILLAGER 5-POSSESSED
                    1-VILLAGER 2-SEER 3-POSSESSED 5-VILLAGER 0-WEREWOLF
                    1-VILLAGER 2-POSSESSED 3-VILLAGER 4-WEREWOLF 0-SEER
                    1-WEREWOLF 3-VILLAGER 4-VILLAGER 5-SEER 0-POSSESSED
                    2-WEREWOLF 3-VILLAGER 4-SEER 5-VILLAGER 0-POSSESSED
                    1-VILLAGER 2-SEER 4-POSSESSED 5-WEREWOLF 0-VILLAGER
                    1-SEER 2-POSSESSED 3-WEREWOLF 4-VILLAGER 5-VILLAGER
                    1-POSSESSED 2-VILLAGER 3-SEER 4-VILLAGER 0-WEREWOLF
                    2-VILLAGER 3-POSSESSED 4-WEREWOLF 5-VILLAGER 0-SEER
                    1-WEREWOLF 2-POSSESSED 3-VILLAGER 5-SEER 0-VILLAGER
                    1-WEREWOLF 2-VILLAGER 3-VILLAGER 5-POSSESSED 0-SEER
                    1-VILLAGER 2-VILLAGER 3-SEER 4-WEREWOLF 5-POSSESSED
                    1-POSSESSED 2-VILLAGER 4-SEER 5-VILLAGER 0-WEREWOLF
                    1-VILLAGER 2-SEER 3-WEREWOLF 4-POSSESSED 0-VILLAGER
                    1-SEER 3-POSSESSED 4-VILLAGER 5-WEREWOLF 0-VILLAGER
                    2-WEREWOLF 3-VILLAGER 4-VILLAGER 5-SEER 0-POSSESSED
                    """);
            put(7, """
                    1-VILLAGER 2-WEREWOLF 3-SEER 6-POSSESSED 0-VILLAGER
                    2-VILLAGER 3-POSSESSED 4-WEREWOLF 5-SEER 6-VILLAGER
                    1-VILLAGER 3-VILLAGER 4-SEER 5-WEREWOLF 0-POSSESSED
                    2-SEER 4-VILLAGER 5-POSSESSED 6-VILLAGER 0-WEREWOLF
                    1-WEREWOLF 3-VILLAGER 4-POSSESSED 5-VILLAGER 0-SEER
                    1-SEER 2-POSSESSED 4-VILLAGER 5-VILLAGER 6-WEREWOLF
                    1-POSSESSED 2-VILLAGER 3-WEREWOLF 6-SEER 0-VILLAGER
                    1-POSSESSED 2-SEER 3-VILLAGER 6-WEREWOLF 0-VILLAGER
                    1-VILLAGER 2-POSSESSED 4-SEER 5-WEREWOLF 6-VILLAGER
                    2-VILLAGER 3-VILLAGER 4-WEREWOLF 5-SEER 0-POSSESSED
                    3-WEREWOLF 4-VILLAGER 5-POSSESSED 6-SEER 0-VILLAGER
                    1-WEREWOLF 2-VILLAGER 3-SEER 5-VILLAGER 6-POSSESSED
                    1-SEER 4-POSSESSED 5-VILLAGER 6-VILLAGER 0-WEREWOLF
                    1-VILLAGER 2-WEREWOLF 3-POSSESSED 4-VILLAGER 0-SEER
                    1-VILLAGER 2-SEER 4-WEREWOLF 5-VILLAGER 6-POSSESSED
                    2-VILLAGER 3-SEER 4-POSSESSED 6-VILLAGER 0-WEREWOLF
                    1-POSSESSED 3-WEREWOLF 4-VILLAGER 5-VILLAGER 0-SEER
                    1-SEER 3-VILLAGER 5-POSSESSED 6-WEREWOLF 0-VILLAGER
                    2-WEREWOLF 3-VILLAGER 4-SEER 6-VILLAGER 0-POSSESSED
                    1-WEREWOLF 2-POSSESSED 4-VILLAGER 5-SEER 0-VILLAGER
                    1-VILLAGER 2-VILLAGER 3-POSSESSED 5-WEREWOLF 6-SEER
                    2-POSSESSED 3-VILLAGER 4-SEER 5-VILLAGER 0-WEREWOLF
                    1-SEER 2-VILLAGER 5-POSSESSED 6-WEREWOLF 0-VILLAGER
                    1-VILLAGER 3-WEREWOLF 4-POSSESSED 5-VILLAGER 6-SEER
                    1-WEREWOLF 2-VILLAGER 3-SEER 4-VILLAGER 6-POSSESSED
                    1-POSSESSED 2-WEREWOLF 3-VILLAGER 6-VILLAGER 0-SEER
                    1-VILLAGER 4-WEREWOLF 5-SEER 6-VILLAGER 0-POSSESSED
                    2-SEER 3-POSSESSED 4-VILLAGER 5-WEREWOLF 0-VILLAGER
                    """);
            put(8, """
                    1-POSSESSED 2-VILLAGER 4-SEER 5-VILLAGER 6-WEREWOLF
                    1-VILLAGER 3-WEREWOLF 4-VILLAGER 7-POSSESSED 0-SEER
                    2-POSSESSED 3-VILLAGER 5-SEER 7-VILLAGER 0-WEREWOLF
                    3-VILLAGER 4-VILLAGER 5-WEREWOLF 6-SEER 0-POSSESSED
                    1-SEER 2-VILLAGER 6-POSSESSED 7-WEREWOLF 0-VILLAGER
                    1-WEREWOLF 3-SEER 5-POSSESSED 6-VILLAGER 7-VILLAGER
                    1-VILLAGER 2-SEER 3-POSSESSED 4-WEREWOLF 0-VILLAGER
                    2-WEREWOLF 4-POSSESSED 5-VILLAGER 6-VILLAGER 7-SEER
                    1-VILLAGER 2-WEREWOLF 3-POSSESSED 5-SEER 7-VILLAGER
                    2-VILLAGER 4-POSSESSED 5-VILLAGER 6-WEREWOLF 0-SEER
                    1-WEREWOLF 3-VILLAGER 4-VILLAGER 6-SEER 7-POSSESSED
                    2-VILLAGER 3-SEER 5-VILLAGER 6-POSSESSED 0-WEREWOLF
                    1-SEER 4-WEREWOLF 6-VILLAGER 7-VILLAGER 0-POSSESSED
                    1-VILLAGER 2-POSSESSED 3-WEREWOLF 4-SEER 0-VILLAGER
                    1-POSSESSED 3-VILLAGER 4-VILLAGER 5-WEREWOLF 7-SEER
                    2-SEER 5-POSSESSED 6-VILLAGER 7-WEREWOLF 0-VILLAGER
                    1-WEREWOLF 2-POSSESSED 4-SEER 5-VILLAGER 7-VILLAGER
                    1-VILLAGER 3-POSSESSED 4-VILLAGER 6-SEER 0-WEREWOLF
                    2-VILLAGER 3-VILLAGER 6-WEREWOLF 7-POSSESSED 0-SEER
                    1-SEER 4-POSSESSED 5-WEREWOLF 6-VILLAGER 0-VILLAGER
                    2-VILLAGER 3-WEREWOLF 5-SEER 7-VILLAGER 0-POSSESSED
                    1-VILLAGER 3-SEER 4-WEREWOLF 5-VILLAGER 6-POSSESSED
                    1-POSSESSED 2-WEREWOLF 3-VILLAGER 7-SEER 0-VILLAGER
                    2-SEER 4-VILLAGER 5-POSSESSED 6-VILLAGER 7-WEREWOLF
                    3-VILLAGER 4-VILLAGER 6-WEREWOLF 7-POSSESSED 0-SEER
                    1-POSSESSED 2-WEREWOLF 5-SEER 7-VILLAGER 0-VILLAGER
                    1-WEREWOLF 2-SEER 4-VILLAGER 5-POSSESSED 6-VILLAGER
                    2-VILLAGER 3-WEREWOLF 4-POSSESSED 5-VILLAGER 7-SEER
                    1-VILLAGER 2-VILLAGER 3-POSSESSED 6-SEER 0-WEREWOLF
                    3-SEER 4-WEREWOLF 5-VILLAGER 7-VILLAGER 0-POSSESSED
                    1-VILLAGER 2-POSSESSED 4-SEER 6-VILLAGER 7-WEREWOLF
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