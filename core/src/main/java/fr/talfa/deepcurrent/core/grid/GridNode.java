package fr.talfa.deepcurrent.core.grid;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mutable state of one node, owned by {@link GridGraph}. */
final class GridNode {
    final long id;
    final NodeRole role;
    /** Neighbour id to the cable leading to it. */
    final Map<Long, Cable> cables = new LinkedHashMap<>();
    double offerKw;
    double demandKw;
    double chargeLimitKw;
    double dischargeLimitKw;
    GridNetwork network;

    GridNode(long id, NodeRole role) {
        this.id = id;
        this.role = role;
    }
}
