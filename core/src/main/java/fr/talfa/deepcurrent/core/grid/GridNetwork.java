package fr.talfa.deepcurrent.core.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A set of nodes joined by cables. Its topology is fixed: when it changes, {@link GridGraph} replaces the network
 * with new ones. The least-loss paths between nodes are computed once per network, the power balance whenever a
 * node's values change.
 */
public final class GridNetwork {
    private final long id;
    private final GridGraph graph;
    private List<GridNode> members = List.of();
    private List<Cable> cables = List.of();
    private long[] nodeIds = new long[0];
    /** Least-loss paths from each source node, computed on first use. */
    private final Map<Long, ShortestPaths> pathsFrom = new HashMap<>();
    private GridSolution solution;
    private boolean discarded;

    GridNetwork(long id, GridGraph graph) {
        this.id = id;
        this.graph = graph;
    }

    public long id() {
        return id;
    }

    /** Ids of the member nodes, in ascending order. */
    public long[] nodeIds() {
        return nodeIds.clone();
    }

    public int size() {
        return nodeIds.length;
    }

    public List<Cable> cables() {
        return cables;
    }

    /** Total cable length, in blocks. */
    public double cableLength() {
        double total = 0;
        for (Cable cable : cables) {
            total += cable.length();
        }
        return total;
    }

    /** Whether the topology changed since this network was built; a discarded network is never updated again. */
    public boolean isDiscarded() {
        return discarded;
    }

    /** Current power balance, solved again only if a node's values changed since the last call. */
    public GridSolution solution() {
        if (solution == null) {
            solution = GridSolver.solve(this);
            graph.countSolve();
        }
        return solution;
    }

    void setTopology(List<GridNode> members, List<Cable> cables) {
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.cables = Collections.unmodifiableList(cables);
        this.nodeIds = members.stream().mapToLong(node -> node.id).sorted().toArray();
    }

    List<GridNode> members() {
        return members;
    }

    GridSettings settings() {
        return graph.settings();
    }

    void markValuesDirty() {
        solution = null;
    }

    void discard() {
        discarded = true;
        solution = null;
        pathsFrom.clear();
    }

    /** Fraction of the power injected at {@code from} that reaches {@code to} along the least-loss path. */
    double efficiency(long from, long to) {
        return Math.exp(-paths(from).weight.get(to));
    }

    /** Cables on the least-loss path between two nodes. */
    List<Cable> path(long from, long to) {
        ShortestPaths paths = paths(from);
        List<Cable> path = new ArrayList<>();
        long current = to;
        while (current != from) {
            Cable cable = paths.via.get(current);
            path.add(cable);
            current = cable.other(current);
        }
        return path;
    }

    private ShortestPaths paths(long from) {
        return pathsFrom.computeIfAbsent(from, this::dijkstra);
    }

    /** Dijkstra with cable weights {@code length * lossPerBlock}: path efficiency is {@code exp(-weight)}. */
    private ShortestPaths dijkstra(long from) {
        ShortestPaths result = new ShortestPaths();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>((x, y) -> Double.compare(x.weight, y.weight));
        result.weight.put(from, 0.0);
        queue.add(new QueueEntry(0, from));
        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            long node = entry.node;
            if (entry.weight > result.weight.get(node)) {
                continue;
            }
            for (Cable cable : graph.nodeOrNull(node).cables.values()) {
                long next = cable.other(node);
                double weight = entry.weight + cable.length() * settings().spec(cable.voltage()).lossPerBlock();
                Double known = result.weight.get(next);
                if (known == null || weight < known) {
                    result.weight.put(next, weight);
                    result.via.put(next, cable);
                    queue.add(new QueueEntry(weight, next));
                }
            }
        }
        return result;
    }

    private record QueueEntry(double weight, long node) {
    }

    private static final class ShortestPaths {
        final Map<Long, Double> weight = new HashMap<>();
        final Map<Long, Cable> via = new HashMap<>();
    }
}
