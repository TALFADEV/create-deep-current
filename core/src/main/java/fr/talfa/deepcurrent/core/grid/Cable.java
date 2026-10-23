package fr.talfa.deepcurrent.core.grid;

/**
 * A cable between two nodes. Endpoints are stored in ascending order, so a pair of nodes has one cable at most.
 */
public record Cable(long a, long b, double length, VoltageClass voltage) {
    public Cable {
        if (a == b) {
            throw new IllegalArgumentException("a cable needs two distinct nodes");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        if (a > b) {
            long swap = a;
            a = b;
            b = swap;
        }
    }

    public long other(long node) {
        return node == a ? b : a;
    }
}
