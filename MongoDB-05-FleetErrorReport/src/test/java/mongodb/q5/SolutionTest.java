package mongodb.q5;

import java.util.*;
import mongodb.q5.FleetErrorReport.*;

public final class SolutionTest {
    public static void main(String[] args) {
        List<String> noisy = new ArrayList<>(Collections.nCopies(50, "CURSOR")); noisy.add("LAG");
        List<ServerLog> fleet = List.of(new ServerLog("atlas-rs0-node-1", List.of("CACHE", "LAG", "CACHE")),
                new ServerLog("atlas-rs0-node-2", List.of("CACHE")),
                new ServerLog("atlas-rs0-node-3", List.of("CACHE", "INDEX")),
                new ServerLog("atlas-rs1-node-1", List.of("LAG")),
                new ServerLog("atlas-rs1-node-2", List.of("LAG", "INDEX")),
                new ServerLog("atlas-rs2-node-1", noisy));
        check(FleetErrorReport.topErrors(fleet, 10).equals(List.of(new ErrorRank("LAG", 4, 4),
                new ErrorRank("CACHE", 3, 4), new ErrorRank("INDEX", 2, 2), new ErrorRank("CURSOR", 1, 50))),
                "Spread must outrank a noisy node");
        check(FleetErrorReport.topErrorsByBlastRadius(fleet, 3).equals(List.of(new BlastRank("CACHE", "rs0", 3),
                new BlastRank("LAG", "rs1", 2), new BlastRank("INDEX", "rs0", 1))), "Blast ranking");
        List<ServerLog> tied = List.of(new ServerLog("atlas-rs0-node-1", List.of("DISK", "ELECTION", "DISK")),
                new ServerLog("atlas-rs1-node-1", List.of("DISK", "ELECTION", "AUTH")),
                new ServerLog("atlas-rs2-node-1", List.of("AUTH")));
        check(FleetErrorReport.topErrors(tied, 3).equals(List.of(new ErrorRank("DISK", 2, 3),
                new ErrorRank("AUTH", 2, 2), new ErrorRank("ELECTION", 2, 2))), "Occurrence and alphabetic ties");
        check(FleetErrorReport.topErrorsByBlastRadius(tied, 3).equals(List.of(new BlastRank("AUTH", "rs1", 1),
                new BlastRank("DISK", "rs0", 1), new BlastRank("ELECTION", "rs0", 1))), "Blast ties and worst-set ties");
        check(FleetErrorReport.topErrors(fleet, 0).isEmpty(), "Zero k");
        check(FleetErrorReport.topErrors(List.of(), 5).isEmpty(), "Empty fleet");
        check(FleetErrorReport.topErrorsByBlastRadius(List.of(new ServerLog("atlas-rs0-node-1", List.of())), 5).isEmpty(), "No errors");
        expect(IllegalArgumentException.class, () -> FleetErrorReport.topErrors(fleet, -1));
        expect(IllegalArgumentException.class, () -> FleetErrorReport.topErrors(List.of(fleet.get(0), fleet.get(0)), 3));
        expect(IllegalArgumentException.class, () -> FleetErrorReport.topErrors(List.of(new ServerLog("bad", List.of("X"))), 3));
        Random random = new Random(5);
        for (int round = 0; round < 100; round++) {
            List<ServerLog> shuffled = new ArrayList<>(fleet);
            Collections.shuffle(shuffled, random);
            check(FleetErrorReport.topErrors(shuffled, 10).equals(FleetErrorReport.topErrors(fleet, 10)), "Input order changed board");
            check(FleetErrorReport.topErrorsByBlastRadius(shuffled, 10).equals(FleetErrorReport.topErrorsByBlastRadius(fleet, 10)), "Input order changed blast board");
        }
        System.out.println("MongoDB-05 tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Unexpected exception", failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
