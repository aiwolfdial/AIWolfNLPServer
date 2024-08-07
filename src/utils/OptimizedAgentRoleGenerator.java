package utils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.model.Role;

public class OptimizedAgentRoleGenerator implements Iterable<Map<Socket, Role>> {
    private final List<Socket> globalSockets;
    private final int globalBattleNum;
    private final Map<Role, Integer> roleMap;
    private final List<Map<Socket, Role>> agentRoleCombinations;

    public OptimizedAgentRoleGenerator(List<Socket> globalSockets, int globalBattleNum, int battleAgentNum) {
        this.globalSockets = new ArrayList<>(globalSockets);
        Collections.shuffle(this.globalSockets);
        this.globalBattleNum = globalBattleNum;
        this.roleMap = Role.DefaultMap(battleAgentNum).entrySet().stream()
                .filter(e -> e.getValue() != 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.agentRoleCombinations = generateAgentRoleCombinations();
    }

    private List<Map<Socket, Role>> generateAgentRoleCombinations() {
        List<Map<Socket, Role>> combinations = new ArrayList<>();
        Map<Socket, Map<Role, Integer>> socketRoleCounts = initializeSocketRoleCounts();
        List<Role> allRoles = createAllRolesList();

        for (int i = 0; i < globalBattleNum; i++) {
            Map<Socket, Role> combination = new HashMap<>();
            List<Socket> availableSockets = new ArrayList<>(globalSockets);
            Collections.shuffle(availableSockets);

            for (Role role : allRoles) {
                Socket bestSocket = findBestSocket(availableSockets, role, socketRoleCounts);
                combination.put(bestSocket, role);
                availableSockets.remove(bestSocket);
                socketRoleCounts.get(bestSocket).put(role, socketRoleCounts.get(bestSocket).get(role) + 1);
            }

            combinations.add(combination);
        }

        return combinations;
    }

    private Map<Socket, Map<Role, Integer>> initializeSocketRoleCounts() {
        Map<Socket, Map<Role, Integer>> counts = new HashMap<>();
        for (Socket socket : globalSockets) {
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

    private Socket findBestSocket(List<Socket> availableSockets, Role role,
            Map<Socket, Map<Role, Integer>> socketRoleCounts) {
        return availableSockets.stream()
                .min(Comparator.<Socket>comparingInt(socket -> socketRoleCounts.get(socket).get(role))
                        .thenComparingInt(socket -> socketRoleCounts.get(socket).values().stream()
                                .mapToInt(Integer::intValue).sum()))
                .orElseThrow(() -> new RuntimeException("No available socket found"));
    }

    @Override
    public String toString() {
        Map<Socket, Map<Role, Integer>> socketRoleCount = new HashMap<>();
        Map<Socket, Integer> socketMatchCount = new HashMap<>();
        for (Socket socket : globalSockets) {
            socketRoleCount.put(socket, new HashMap<>());
            socketMatchCount.put(socket, 0);
            for (Role role : Role.values()) {
                socketRoleCount.get(socket).put(role, 0);
            }
        }

        for (Map<Socket, Role> combination : agentRoleCombinations) {
            for (Map.Entry<Socket, Role> entry : combination.entrySet()) {
                Socket socket = entry.getKey();
                Role role = entry.getValue();
                socketRoleCount.get(socket).put(role, socketRoleCount.get(socket).get(role) + 1);
                socketMatchCount.put(socket, socketMatchCount.get(socket) + 1);
            }
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<Socket, Map<Role, Integer>> entry : socketRoleCount.entrySet()) {
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

    @Override
    public Iterator<Map<Socket, Role>> iterator() {
        return agentRoleCombinations.iterator();
    }
}