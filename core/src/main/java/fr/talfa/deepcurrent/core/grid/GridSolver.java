package fr.talfa.deepcurrent.core.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the power balance of a network.
 *
 * <ol>
 *   <li>Producers serve consumers. Power is taken along the least-loss producer-consumer pairs first, so nearby
 *       producers feed nearby consumers. If producers fall short, every consumer gets the same fraction of its
 *       demand.</li>
 *   <li>What producers have left charges storage nodes, within their charge limits.</li>
 *   <li>A deficit is covered by discharging storage nodes, within their discharge limits.</li>
 *   <li>Overloaded cables cap the power through them: every flow crossing an overloaded cable is scaled down to
 *       fit (alpha rule: cables do not burn yet).</li>
 * </ol>
 */
final class GridSolver {
    private static final double EPSILON = 1e-9;
    /** Binary search steps for the served fraction: 2^-50 precision. */
    private static final int SEARCH_STEPS = 50;
    /** Efficiencies closer than this count as equal. */
    private static final double TIE = 1e-12;

    private GridSolver() {
    }

    /** Power injected at {@code source}, of which {@code efficiency} reaches {@code sink}. */
    private static final class Flow {
        final GridNode source;
        final GridNode sink;
        final int sourceIndex;
        final int sinkIndex;
        final double efficiency;
        double injectedKw;

        Flow(GridNode source, int sourceIndex, GridNode sink, int sinkIndex, double efficiency) {
            this.source = source;
            this.sink = sink;
            this.sourceIndex = sourceIndex;
            this.sinkIndex = sinkIndex;
            this.efficiency = efficiency;
        }
    }

    static GridSolution solve(GridNetwork network) {
        List<GridNode> producers = new ArrayList<>();
        List<GridNode> consumers = new ArrayList<>();
        List<GridNode> chargers = new ArrayList<>();
        List<GridNode> dischargers = new ArrayList<>();
        double offered = 0;
        double demanded = 0;
        for (GridNode node : network.members()) {
            switch (node.role) {
                case PRODUCER -> {
                    if (node.offerKw > 0) {
                        producers.add(node);
                        offered += node.offerKw;
                    }
                }
                case CONSUMER -> {
                    if (node.demandKw > 0) {
                        consumers.add(node);
                        demanded += node.demandKw;
                    }
                }
                case STORAGE -> {
                    if (node.chargeLimitKw > 0) {
                        chargers.add(node);
                    }
                    if (node.dischargeLimitKw > 0) {
                        dischargers.add(node);
                    }
                }
                case CONNECTOR -> {
                }
            }
        }

        List<Flow> flows = new ArrayList<>();
        double[] production = new double[producers.size()];
        for (int i = 0; i < producers.size(); i++) {
            production[i] = producers.get(i).offerKw;
        }

        // 1. Producers serve consumers.
        double[] demand = new double[consumers.size()];
        for (int j = 0; j < consumers.size(); j++) {
            demand[j] = consumers.get(j).demandKw;
        }
        double served = allocate(network, producers, production, consumers, demand, flows);

        // 2. What producers have left charges storage.
        double[] charge = new double[chargers.size()];
        for (int b = 0; b < chargers.size(); b++) {
            charge[b] = chargers.get(b).chargeLimitKw;
        }
        allocate(network, producers, production, chargers, charge, flows);

        // 3. Storage covers what consumers still lack.
        if (served < 1 - EPSILON) {
            double[] discharge = new double[dischargers.size()];
            for (int b = 0; b < dischargers.size(); b++) {
                discharge[b] = dischargers.get(b).dischargeLimitKw;
            }
            double[] unmet = new double[consumers.size()];
            for (int j = 0; j < consumers.size(); j++) {
                unmet[j] = demand[j] * (1 - served);
            }
            allocate(network, dischargers, discharge, consumers, unmet, flows);
        }

        Map<Cable, Double> load = new LinkedHashMap<>();
        Set<Cable> overloaded = capOverloadedCables(network, flows, load);
        return aggregate(flows, consumers, load, overloaded, offered, demanded);
    }

    /**
     * Serves every sink the same fraction of what it wants, as high as the sources allow, taking power along the
     * least-loss source-sink pairs first. Adds the flows, deducts the power used from {@code capacity} and returns
     * the fraction served.
     */
    private static double allocate(GridNetwork network, List<GridNode> sources, double[] capacity,
                                   List<GridNode> sinks, double[] wanted, List<Flow> flows) {
        double totalWanted = 0;
        for (double want : wanted) {
            totalWanted += want;
        }
        if (sinks.isEmpty() || totalWanted <= 0) {
            return 1;
        }
        if (sources.isEmpty()) {
            return 0;
        }
        List<Flow> order = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            for (int j = 0; j < sinks.size(); j++) {
                GridNode source = sources.get(i);
                GridNode sink = sinks.get(j);
                order.add(new Flow(source, i, sink, j, network.efficiency(source.id, sink.id)));
            }
        }
        // Stable sort: ties keep the deterministic member order.
        order.sort((x, y) -> Double.compare(y.efficiency, x.efficiency));

        double fraction = 1;
        double[] remaining = greedy(order, capacity, wanted, 1);
        if (remaining == null) {
            double low = 0;
            double high = 1;
            for (int step = 0; step < SEARCH_STEPS; step++) {
                double middle = (low + high) / 2;
                if (greedy(order, capacity, wanted, middle) != null) {
                    low = middle;
                } else {
                    high = middle;
                }
            }
            fraction = low;
            remaining = greedy(order, capacity, wanted, fraction);
        }
        System.arraycopy(remaining, 0, capacity, 0, capacity.length);
        for (Flow flow : order) {
            if (flow.injectedKw > 0) {
                flows.add(flow);
            }
        }
        return fraction;
    }

    /**
     * One greedy pass: walks the pairs from the most efficient and gives each sink {@code fraction * wanted}.
     * Sources equally efficient for a sink share its load in proportion to their remaining capacity, so identical
     * machines run at the same load. Writes the injected power into the pairs and returns the sources' remaining
     * capacity, or null if some sink could not be fully served.
     */
    private static double[] greedy(List<Flow> order, double[] capacity, double[] wanted, double fraction) {
        double[] left = capacity.clone();
        double[] missing = new double[wanted.length];
        for (int j = 0; j < wanted.length; j++) {
            missing[j] = wanted[j] * fraction;
        }
        for (Flow flow : order) {
            flow.injectedKw = 0;
        }
        int start = 0;
        while (start < order.size()) {
            int end = start + 1;
            while (end < order.size() && order.get(start).efficiency - order.get(end).efficiency <= TIE) {
                end++;
            }
            List<Flow> tied = order.subList(start, end);
            boolean[] done = new boolean[wanted.length];
            for (Flow first : tied) {
                int sink = first.sinkIndex;
                if (done[sink]) {
                    continue;
                }
                done[sink] = true;
                double available = 0;
                for (Flow flow : tied) {
                    if (flow.sinkIndex == sink) {
                        available += left[flow.sourceIndex];
                    }
                }
                double take = Math.min(available, missing[sink] / first.efficiency);
                if (take <= 0) {
                    continue;
                }
                double[] snapshot = left.clone();
                for (Flow flow : tied) {
                    if (flow.sinkIndex == sink) {
                        flow.injectedKw = take * snapshot[flow.sourceIndex] / available;
                        left[flow.sourceIndex] -= flow.injectedKw;
                        missing[sink] -= flow.injectedKw * flow.efficiency;
                    }
                }
            }
            start = end;
        }
        for (int j = 0; j < wanted.length; j++) {
            if (missing[j] > EPSILON * Math.max(1, wanted[j])) {
                return null;
            }
        }
        return left;
    }

    /** Step 4. Fills {@code load} with the final load of each cable and returns the overloaded ones. */
    private static Set<Cable> capOverloadedCables(GridNetwork network, List<Flow> flows, Map<Cable, Double> load) {
        Map<Flow, List<Cable>> paths = new HashMap<>();
        Map<Cable, Double> requested = new LinkedHashMap<>();
        for (Flow flow : flows) {
            List<Cable> path = network.path(flow.source.id, flow.sink.id);
            paths.put(flow, path);
            for (Cable cable : path) {
                requested.merge(cable, flow.injectedKw, Double::sum);
            }
        }
        Set<Cable> overloaded = new LinkedHashSet<>();
        Map<Cable, Double> scale = new HashMap<>();
        for (Map.Entry<Cable, Double> entry : requested.entrySet()) {
            double capacity = network.settings().spec(entry.getKey().voltage()).maxPowerKw();
            if (entry.getValue() > capacity * (1 + EPSILON)) {
                overloaded.add(entry.getKey());
                scale.put(entry.getKey(), capacity / entry.getValue());
            }
        }
        for (Flow flow : flows) {
            List<Cable> path = paths.get(flow);
            double factor = 1;
            for (Cable cable : path) {
                factor = Math.min(factor, scale.getOrDefault(cable, 1.0));
            }
            flow.injectedKw *= factor;
            for (Cable cable : path) {
                load.merge(cable, flow.injectedKw, Double::sum);
            }
        }
        return overloaded;
    }

    private static GridSolution aggregate(List<Flow> flows, List<GridNode> consumers, Map<Cable, Double> load,
                                          Set<Cable> overloaded, double offered, double demanded) {
        Map<Long, Double> flowKw = new LinkedHashMap<>();
        double produced = 0;
        double delivered = 0;
        double charged = 0;
        double discharged = 0;
        double losses = 0;
        for (Flow flow : flows) {
            double injected = flow.injectedKw;
            double received = injected * flow.efficiency;
            flowKw.merge(flow.source.id, injected, Double::sum);
            flowKw.merge(flow.sink.id, -received, Double::sum);
            losses += injected - received;
            if (flow.source.role == NodeRole.PRODUCER) {
                produced += injected;
            } else {
                discharged += injected;
            }
            if (flow.sink.role == NodeRole.CONSUMER) {
                delivered += received;
            } else {
                charged += received;
            }
        }
        Map<Long, Double> satisfaction = new LinkedHashMap<>();
        for (GridNode consumer : consumers) {
            double received = -flowKw.getOrDefault(consumer.id, 0.0);
            satisfaction.put(consumer.id, Math.min(1, received / consumer.demandKw));
        }
        return new GridSolution(flowKw, satisfaction, load, overloaded,
                offered, demanded, produced, delivered, charged, discharged, losses);
    }
}
