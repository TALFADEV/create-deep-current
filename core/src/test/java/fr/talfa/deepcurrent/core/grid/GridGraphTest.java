package fr.talfa.deepcurrent.core.grid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GridGraphTest {
    private final GridGraph grid = new GridGraph(GridSettings.DEFAULT);

    @Test
    void connectedNodesShareANetwork() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONNECTOR);
        grid.addNode(3, NodeRole.CONSUMER);
        grid.addNode(4, NodeRole.CONSUMER);
        grid.connect(1, 2, 10, VoltageClass.LV);
        grid.connect(2, 3, 10, VoltageClass.LV);

        assertSame(grid.networkOf(1), grid.networkOf(3));
        assertNotSame(grid.networkOf(1), grid.networkOf(4));
        assertArrayEquals(new long[] {1, 2, 3}, grid.networkOf(1).nodeIds());
        assertEquals(2, grid.networks().size());
        assertEquals(20, grid.networkOf(2).cableLength(), 1e-12);
    }

    @Test
    void disconnectingSplitsAndReconnectingMerges() {
        grid.addNode(1, NodeRole.CONNECTOR);
        grid.addNode(2, NodeRole.CONNECTOR);
        grid.addNode(3, NodeRole.CONNECTOR);
        grid.connect(1, 2, 5, VoltageClass.LV);
        grid.connect(2, 3, 5, VoltageClass.LV);
        GridNetwork before = grid.networkOf(1);

        assertTrue(grid.disconnect(2, 3));
        assertTrue(before.isDiscarded());
        assertNotSame(grid.networkOf(1), grid.networkOf(3));

        grid.connect(3, 1, 5, VoltageClass.LV);
        assertSame(grid.networkOf(1), grid.networkOf(2));
        assertSame(grid.networkOf(1), grid.networkOf(3));
    }

    @Test
    void removingANodeRemovesItsCables() {
        grid.addNode(1, NodeRole.CONNECTOR);
        grid.addNode(2, NodeRole.CONNECTOR);
        grid.addNode(3, NodeRole.CONNECTOR);
        grid.connect(1, 2, 5, VoltageClass.LV);
        grid.connect(2, 3, 5, VoltageClass.LV);

        assertTrue(grid.removeNode(2));
        assertFalse(grid.hasNode(2));
        assertNotSame(grid.networkOf(1), grid.networkOf(3));
        assertEquals(1, grid.networkOf(1).size());
        assertTrue(grid.networkOf(1).cables().isEmpty());
        assertFalse(grid.removeNode(2));
    }

    @Test
    void onlyOneCablePerPairOfNodes() {
        grid.addNode(1, NodeRole.CONNECTOR);
        grid.addNode(2, NodeRole.CONNECTOR);
        assertTrue(grid.connect(1, 2, 5, VoltageClass.LV));
        assertFalse(grid.connect(2, 1, 8, VoltageClass.HV));
        assertEquals(5, grid.cable(2, 1).length());
        assertFalse(grid.addNode(1, NodeRole.PRODUCER));
    }

    @Test
    void topologyIsRebuiltOnlyWhenItChanges() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 10, VoltageClass.LV);
        grid.networkOf(1);
        int rebuilds = grid.topologyRebuilds();

        for (int i = 0; i < 100; i++) {
            grid.setOffer(1, i);
            grid.setDemand(2, 50);
            grid.solutionOf(1);
        }
        assertEquals(rebuilds, grid.topologyRebuilds());
    }

    @Test
    void untouchedNetworksAreNotRebuilt() {
        for (long id = 1; id <= 4; id++) {
            grid.addNode(id, NodeRole.CONNECTOR);
        }
        grid.connect(1, 2, 1, VoltageClass.LV);
        grid.connect(3, 4, 1, VoltageClass.LV);
        GridNetwork untouched = grid.networkOf(3);
        int rebuilds = grid.topologyRebuilds();

        grid.addNode(5, NodeRole.CONNECTOR);
        grid.connect(2, 5, 1, VoltageClass.LV);
        grid.networks();

        assertSame(untouched, grid.networkOf(3));
        assertEquals(rebuilds + 1, grid.topologyRebuilds());
    }

    @Test
    void balanceIsSolvedOnlyWhenValuesChange() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 10, VoltageClass.LV);
        grid.setOffer(1, 100);
        grid.setDemand(2, 50);
        grid.solutionOf(1);
        int solves = grid.solves();

        grid.solutionOf(2);
        grid.setOffer(1, 100);
        grid.solutionOf(1);
        assertEquals(solves, grid.solves());

        grid.setOffer(1, 90);
        grid.solutionOf(1);
        assertEquals(solves + 1, grid.solves());
    }

    @Test
    void rejectsInvalidUse() {
        grid.addNode(1, NodeRole.PRODUCER);
        assertThrows(IllegalArgumentException.class, () -> grid.networkOf(99));
        assertThrows(IllegalArgumentException.class, () -> grid.connect(1, 99, 1, VoltageClass.LV));
        assertThrows(IllegalArgumentException.class, () -> grid.connect(1, 1, 1, VoltageClass.LV));
        assertThrows(IllegalStateException.class, () -> grid.setDemand(1, 10));
        assertThrows(IllegalArgumentException.class, () -> grid.setOffer(1, -5));
        assertThrows(IllegalArgumentException.class, () -> grid.setOffer(1, Double.NaN));
    }
}
