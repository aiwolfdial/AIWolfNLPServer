package utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.model.Role;
import libs.Pair;

public class AgentRoleCombosGenerator {
    private final List<Pair<InetAddress, Integer>> globalSockets;
    private final int globalBattleNum;
    private final Map<Role, Integer> roleMap;
    private final List<Map<Pair<InetAddress, Integer>, Role>> agentRoleCombos;

    public AgentRoleCombosGenerator(List<Pair<InetAddress, Integer>> globalSockets, int globalBattleNum,
            int battleAgentNum) {
        this.globalSockets = new ArrayList<>(globalSockets);
        Collections.shuffle(this.globalSockets);
        this.globalBattleNum = globalBattleNum;
        this.roleMap = Role.DefaultMap(battleAgentNum).entrySet().stream()
                .filter(e -> e.getValue() != 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.agentRoleCombos = generateAgentRoleCombos();
    }

    public List<Map<Pair<InetAddress, Integer>, Role>> toList() {
        return new ArrayList<>(agentRoleCombos);
    }

    private List<Map<Pair<InetAddress, Integer>, Role>> generateAgentRoleCombos() {
        List<Map<Pair<InetAddress, Integer>, Role>> combos = new ArrayList<>();
        Map<Pair<InetAddress, Integer>, Map<Role, Integer>> socketRoleCounts = initializeSocketRoleCounts();
        List<Role> allRoles = createAllRolesList();

        for (int i = 0; i < globalBattleNum; i++) {
            Map<Pair<InetAddress, Integer>, Role> combo = new HashMap<>();
            List<Pair<InetAddress, Integer>> availableSockets = new ArrayList<>(globalSockets);
            Collections.shuffle(availableSockets);

            for (Role role : allRoles) {
                Pair<InetAddress, Integer> bestSocket = findBestSocket(availableSockets, role, socketRoleCounts);
                combo.put(bestSocket, role);
                availableSockets.remove(bestSocket);
                socketRoleCounts.get(bestSocket).put(role, socketRoleCounts.get(bestSocket).get(role) + 1);
            }

            combos.add(combo);
        }
        return combos;
    }

    private Map<Pair<InetAddress, Integer>, Map<Role, Integer>> initializeSocketRoleCounts() {
        Map<Pair<InetAddress, Integer>, Map<Role, Integer>> counts = new HashMap<>();
        for (Pair<InetAddress, Integer> socket : globalSockets) {
            counts.put(socket, new HashMap<>());
            for (Role role : roleMap.keySet()) {
                counts.get(socket).put(role, 0);
            }
        }
        return counts;
    }

    private List<Role> createAllRolesList() {
        List<Role> allRoles = new ArrayList<>();
        roleMap.forEach((role, count) -> {
            for (int i = 0; i < count; i++) {
                allRoles.add(role);
            }
        });
        return allRoles;
    }

    private Pair<InetAddress, Integer> findBestSocket(List<Pair<InetAddress, Integer>> availableSockets, Role role,
            Map<Pair<InetAddress, Integer>, Map<Role, Integer>> socketRoleCounts) {
        return availableSockets.stream()
                .min(Comparator
                        .<Pair<InetAddress, Integer>>comparingInt(socket -> socketRoleCounts.get(socket).get(role))
                        .thenComparingInt(socket -> socketRoleCounts.get(socket).values().stream()
                                .mapToInt(Integer::intValue).sum()))
                .orElseThrow(() -> new RuntimeException("No available socket found"));
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

        for (Map<Pair<InetAddress, Integer>, Role> combo : agentRoleCombos) {
            for (Map.Entry<Pair<InetAddress, Integer>, Role> entry : combo.entrySet()) {
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