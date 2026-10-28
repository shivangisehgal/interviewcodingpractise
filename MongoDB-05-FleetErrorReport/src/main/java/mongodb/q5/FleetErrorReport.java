package mongodb.q5;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class FleetErrorReport {
    public record ServerLog(String name, List<String> errors) {
        public ServerLog {
            Objects.requireNonNull(name, "name");
            errors = List.copyOf(errors);
        }
    }
    public record ErrorRank(String code, int spread, long occurrences) { }
    public record BlastRank(String code, String worstReplicaSet, int nodesHit) { }
    private static final class Stats {
        private long occurrences;
        private final Set<String> nodes = new HashSet<>();
        private final Map<String, Set<String>> byReplicaSet = new HashMap<>();
    }
    private FleetErrorReport() { }

    public static List<ErrorRank> topErrors(List<ServerLog> fleet, int k) {
        requireK(k);
        List<ErrorRank> ranked = new ArrayList<>();
        summarize(fleet).forEach((code, stats) -> ranked.add(
                new ErrorRank(code, stats.nodes.size(), stats.occurrences)));
        ranked.sort(Comparator.comparingInt(ErrorRank::spread).reversed()
                .thenComparing(Comparator.comparingLong(ErrorRank::occurrences).reversed())
                .thenComparing(ErrorRank::code));
        return List.copyOf(ranked.subList(0, Math.min(k, ranked.size())));
    }

    public static List<BlastRank> topErrorsByBlastRadius(List<ServerLog> fleet, int k) {
        requireK(k);
        Map<String, Stats> stats = summarize(fleet);
        List<BlastRank> ranked = new ArrayList<>();
        stats.forEach((code, summary) -> {
            String worst = summary.byReplicaSet.keySet().stream().min(
                    Comparator.<String>comparingInt(set -> summary.byReplicaSet.get(set).size()).reversed()
                            .thenComparing(Comparator.naturalOrder())).orElseThrow();
            ranked.add(new BlastRank(code, worst, summary.byReplicaSet.get(worst).size()));
        });
        ranked.sort(Comparator.comparingInt(BlastRank::nodesHit).reversed()
                .thenComparing(Comparator.<BlastRank>comparingInt(row -> stats.get(row.code()).nodes.size()).reversed())
                .thenComparing(BlastRank::code));
        return List.copyOf(ranked.subList(0, Math.min(k, ranked.size())));
    }

    private static Map<String, Stats> summarize(List<ServerLog> fleet) {
        Objects.requireNonNull(fleet, "fleet");
        Map<String, Stats> result = new HashMap<>();
        Set<String> names = new HashSet<>();
        for (ServerLog log : fleet) {
            if (!names.add(log.name())) throw new IllegalArgumentException("Duplicate node: " + log.name());
            String[] parts = log.name().split("-", -1);
            if (parts.length != 4 || !parts[0].equals("atlas") || parts[1].isEmpty()
                    || !parts[2].equals("node") || !parts[3].matches("[0-9]+")) {
                throw new IllegalArgumentException("Expected atlas-<replicaSet>-node-<n>");
            }
            for (String code : log.errors()) {
                Stats stats = result.computeIfAbsent(code, ignored -> new Stats());
                stats.occurrences++;
                stats.nodes.add(log.name());
                stats.byReplicaSet.computeIfAbsent(parts[1], ignored -> new HashSet<>()).add(log.name());
            }
        }
        return result;
    }

    private static void requireK(int k) {
        if (k < 0) throw new IllegalArgumentException("k must be nonnegative");
    }

    public static void main(String[] args) {
        List<ServerLog> fleet = List.of(
                new ServerLog("atlas-rs0-node-1", List.of("CACHE", "LAG", "CACHE")),
                new ServerLog("atlas-rs0-node-2", List.of("CACHE")),
                new ServerLog("atlas-rs0-node-3", List.of("CACHE", "INDEX")),
                new ServerLog("atlas-rs1-node-1", List.of("LAG")),
                new ServerLog("atlas-rs1-node-2", List.of("LAG", "INDEX")),
                new ServerLog("atlas-rs2-node-1", List.of("LAG", "CURSOR", "CURSOR")));
        System.out.println(topErrors(fleet, 3));
        System.out.println(topErrorsByBlastRadius(fleet, 3));
    }
}
