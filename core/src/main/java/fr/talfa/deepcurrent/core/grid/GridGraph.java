package fr.talfa.deepcurrent.core.grid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The electrical grid as a graph of nodes joined by cables (GDD §4.5).
 *
 * <p>Connected nodes form a {@link GridNetwork}. Networks are rebuilt only when the topology changes (a node or
 * a cable is added or removed), and only the networks touched by that change. A network's power balance is
 * re-solved only when one of its nodes changes its offer, demand or storage limits. Nothing is walked per tick.
 *
 * <p>Node ids are chosen by the caller (in game: packed block positions). Not thread-safe: use it from the
 * server thread.
 */
public final class GridGraph {
    private final Map<Long, GridNode> nodes = new LinkedHashMap<>();
    /** Nodes whose network must be rebuilt before the next query. */
    private final Set<Long> dirtyTopology = new LinkedHashSet<>();
    private GridSettings settings;
    private long nextNetworkId = 1;
    private int topologyRebuilds;
    private int solves;

    public GridGraph(GridSettings settings) {
        this.settings = settings;
    }

    public GridSettings settings() {
        return settings;
    }

    /** Replaces the cable characteristics (config reload) and rebuilds every network. */
    public void setSettings(GridSettings settings) {
        this.settings = settings;
        for (GridNode node : nodes.values()) {
            node.network = null;
            dirtyTopology.add(node.id);
        }
    }

    // --- Topology ---------------------------------------------------------------

    /** Adds a node; returns false if the id is already used. */
    public boolean addNode(long id, NodeRole role) {
        if (nodes.containsKey(id)) {
            return false;
        }
        nodes.put(id, new GridNode(id, role));
        dirtyTopology.add(id);
        return true;
    }

    /** Removes a node and its cables; returns false if it did not exist. */
    public boolean removeNode(long id) {
        GridNode node = nodes.get(id);
        if (node == null) {
            return false;
        }
        invalidate(node);
        for (long neighbour : node.cables.keySet()) {
            nodes.get(neighbour).cables.remove(id);
        }
        nodes.remove(id);
        dirtyTopology.remove(id);
        return true;
    }

    public boolean hasNode(long id) {
        return nodes.containsKey(id);
    }

    public NodeRole role(long id) {
        return node(id).role;
    }

    /** Joins two existing nodes; returns false if they are already joined. */
    public boolean connect(long a, long b, double length, VoltageClass voltage) {
        GridNode nodeA = node(a);
        GridNode nodeB = node(b);
        if (nodeA.cables.containsKey(b)) {
            return false;
        }
        Cable cable = new Cable(a, b, length, voltage);
        invalidate(nodeA);
        invalidate(nodeB);
        nodeA.cables.put(b, cable);
        nodeB.cables.put(a, cable);
        return true;
    }

    /** Removes the cable between two nodes; returns false if there was none. */
    public boolean disconnect(long a, long b) {
        GridNode nodeA = node(a);
        GridNode nodeB = node(b);
        if (!nodeA.cables.containsKey(b)) {
            return false;
        }
        invalidate(nodeA);
        nodeA.cables.remove(b);
        nodeB.cables.remove(a);
        return true;
    }

    public Cable cable(long a, long b) {
        return node(a).cables.get(b);
    }

    // --- Node values ------------------------------------------------------------------

    /** Power a producer can inject, in kW. */
    public void setOffer(long id, double kw) {
        GridNode node = node(id);
        requireRole(node, NodeRole.PRODUCER);
        if (node.offerKw != kw) {
            node.offerKw = nonNegative(kw);
            valuesChanged(node);
        }
    }

    /** Power a consumer wants to draw, in kW. */
    public void setDemand(long id, double kw) {
        GridNode node = node(id);
        requireRole(node, NodeRole.CONSUMER);
        if (node.demandKw != kw) {
            node.demandKw = nonNegative(kw);
            valuesChanged(node);
        }
    }

    /**
     * Power a storage node can absorb and supply right now, in kW. Set the charge limit to zero when it is full
     * and the discharge limit to zero when it is empty; there is no need to update it as its charge moves.
     */
    public void setStorageLimits(long id, double chargeKw, double dischargeKw) {
        GridNode node = node(id);
        requireRole(node, NodeRole.STORAGE);
        if (node.chargeLimitKw != chargeKw || node.dischargeLimitKw != dischargeKw) {
            node.chargeLimitKw = nonNegative(chargeKw);
            node.dischargeLimitKw = nonNegative(dischargeKw);
            valuesChanged(node);
        }
    }

    // --- Networks -------------------------------------------------------------------------

    /** Network containing this node. */
    public GridNetwork networkOf(long id) {
        GridNode node = node(id);
        rebuildDirty();
        return node.network;
    }

    /** Every network, rebuilt where needed. */
    public Collection<GridNetwork> networks() {
        rebuildDirty();
        Set<GridNetwork> result = new LinkedHashSet<>();
        for (GridNode node : nodes.values()) {
            result.add(node.network);
        }
        return result;
    }

    /** Power balance of the network containing this node. */
    public GridSolution solutionOf(long id) {
        return networkOf(id).solution();
    }

    /** How many networks have been rebuilt since creation (for tests and profiling). */
    public int topologyRebuilds() {
        return topologyRebuilds;
    }

    /** How many power balances have been solved since creation (for tests and profiling). */
    public int solves() {
        return solves;
    }

    void countSolve() {
        solves++;
    }

    GridNode nodeOrNull(long id) {
        return nodes.get(id);
    }

    // --- Internals ------------------------------------------------------------------------------

    private GridNode node(long id) {
        GridNode node = nodes.get(id);
        if (node == null) {
            throw new IllegalArgumentException("unknown grid node " + id);
        }
        return node;
    }

    /** Drops the node's network: every member will be regrouped on the next query. */
    private void invalidate(GridNode node) {
        GridNetwork network = node.network;
        if (network == null) {
            dirtyTopology.add(node.id);
            return;
        }
        for (long member : network.nodeIds()) {
            GridNode memberNode = nodes.get(member);
            if (memberNode != null) {
                memberNode.network = null;
                dirtyTopology.add(member);
            }
        }
        network.discard();
    }

    private void valuesChanged(GridNode node) {
        if (node.network != null) {
            node.network.markValuesDirty();
        }
    }

    private void rebuildDirty() {
        if (dirtyTopology.isEmpty()) {
            return;
        }
        for (long start : dirtyTopology) {
            GridNode startNode = nodes.get(start);
            if (startNode == null || startNode.network != null) {
                continue;
            }
            List<GridNode> members = new ArrayList<>();
            Set<Cable> cables = new LinkedHashSet<>();
            Deque<GridNode> queue = new ArrayDeque<>();
            GridNetwork network = new GridNetwork(nextNetworkId++, this);
            startNode.network = network;
            queue.add(startNode);
            while (!queue.isEmpty()) {
                GridNode current = queue.poll();
                members.add(current);
                for (Cable cable : current.cables.values()) {
                    cables.add(cable);
                    GridNode next = nodes.get(cable.other(current.id));
                    if (next.network != network) {
                        next.network = network;
                        queue.add(next);
                    }
                }
            }
            network.setTopology(members, new ArrayList<>(cables));
            topologyRebuilds++;
        }
        dirtyTopology.clear();
    }

    private static void requireRole(GridNode node, NodeRole role) {
        if (node.role != role) {
            throw new IllegalStateException("node " + node.id + " is a " + node.role + ", not a " + role);
        }
    }

    private static double nonNegative(double kw) {
        if (!(kw >= 0) || Double.isInfinite(kw)) {
            throw new IllegalArgumentException("power must be finite and >= 0: " + kw);
        }
        return kw;
    }
}
