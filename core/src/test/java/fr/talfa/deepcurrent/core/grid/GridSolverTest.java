package fr.talfa.deepcurrent.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class GridSolverTest {
    private static final double EPS = 1e-6;
    /** LV attenuation per block with default settings. */
    private static final double LV_LOSS = GridSettings.DEFAULT.spec(VoltageClass.LV).lossPerBlock();
    private static final double LV_MAX = GridSettings.DEFAULT.spec(VoltageClass.LV).maxPowerKw();

    private final GridGraph grid = new GridGraph(GridSettings.DEFAULT);

    private static void assertConservesEnergy(GridSolution s) {
        assertEquals(s.producedKw() + s.dischargedKw(), s.deliveredKw() + s.chargedKw() + s.lossesKw(), EPS);
    }

    @Test
    void producerCoversDemandAndPaysTheLosses() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 100, VoltageClass.LV);
        grid.setOffer(1, 100);
        grid.setDemand(2, 50);

        GridSolution s = grid.solutionOf(1);
        double efficiency = Math.exp(-100 * LV_LOSS);
        assertEquals(1, s.satisfaction(2), EPS);
        assertEquals(50, s.deliveredKw(), EPS);
        assertEquals(50 / efficiency, s.producedKw(), EPS);
        assertEquals(50 / efficiency, s.flowKw(1), EPS);
        assertEquals(-50, s.flowKw(2), EPS);
        assertEquals(50 / efficiency - 50, s.lossesKw(), EPS);
        assertConservesEnergy(s);
    }

    @Test
    void longerCablesLoseMore() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.PRODUCER);
        grid.addNode(4, NodeRole.CONSUMER);
        grid.connect(1, 2, 50, VoltageClass.LV);
        grid.connect(3, 4, 500, VoltageClass.LV);
        for (long producer : new long[] {1, 3}) {
            grid.setOffer(producer, 1000);
        }
        grid.setDemand(2, 100);
        grid.setDemand(4, 100);

        assertTrue(grid.solutionOf(4).lossRatio() > grid.solutionOf(2).lossRatio());
    }

    @Test
    void higherVoltageLosesLess() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.PRODUCER);
        grid.addNode(4, NodeRole.CONSUMER);
        grid.connect(1, 2, 300, VoltageClass.LV);
        grid.connect(3, 4, 300, VoltageClass.HV);
        grid.setOffer(1, 1000);
        grid.setOffer(3, 1000);
        grid.setDemand(2, 100);
        grid.setDemand(4, 100);

        assertTrue(grid.solutionOf(4).lossRatio() < grid.solutionOf(2).lossRatio());
    }

    @Test
    void shortfallIsSharedEquallyAmongConsumers() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.CONSUMER);
        grid.connect(1, 2, 10, VoltageClass.LV);
        grid.connect(1, 3, 200, VoltageClass.LV);
        grid.setOffer(1, 60);
        grid.setDemand(2, 50);
        grid.setDemand(3, 50);

        GridSolution s = grid.solutionOf(1);
        assertTrue(s.satisfaction(2) < 1);
        assertEquals(s.satisfaction(2), s.satisfaction(3), EPS);
        assertEquals(60, s.producedKw(), EPS);
        assertConservesEnergy(s);
    }

    @Test
    void producersShareTheLoadByOffer() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.PRODUCER);
        grid.addNode(3, NodeRole.CONSUMER);
        grid.connect(1, 3, 0, VoltageClass.LV);
        grid.connect(2, 3, 0, VoltageClass.LV);
        grid.setOffer(1, 300);
        grid.setOffer(2, 100);
        grid.setDemand(3, 200);

        GridSolution s = grid.solutionOf(3);
        assertEquals(150, s.flowKw(1), EPS);
        assertEquals(50, s.flowKw(2), EPS);
    }

    @Test
    void nearbyProducerFeedsNearbyConsumer() {
        // Two plants 1000 blocks apart, each with its own consumer next to it.
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.PRODUCER);
        grid.addNode(4, NodeRole.CONSUMER);
        grid.connect(1, 2, 5, VoltageClass.HV);
        grid.connect(3, 4, 5, VoltageClass.HV);
        grid.connect(1, 3, 1000, VoltageClass.HV);
        grid.setOffer(1, 100);
        grid.setOffer(3, 100);
        grid.setDemand(2, 80);
        grid.setDemand(4, 80);

        GridSolution s = grid.solutionOf(1);
        assertEquals(0, s.cableLoadKw(grid.cable(1, 3)), EPS);
        assertEquals(1, s.satisfaction(2), EPS);
        assertEquals(1, s.satisfaction(4), EPS);
    }

    @Test
    void distantProducerHelpsWhenTheLocalOneFallsShort() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.PRODUCER);
        grid.connect(1, 2, 5, VoltageClass.HV);
        grid.connect(1, 3, 1000, VoltageClass.HV);
        grid.setOffer(1, 50);
        grid.setOffer(3, 100);
        grid.setDemand(2, 80);

        GridSolution s = grid.solutionOf(1);
        assertEquals(1, s.satisfaction(2), EPS);
        assertEquals(50, s.flowKw(1), EPS);
        assertTrue(s.cableLoadKw(grid.cable(1, 3)) > 30);
        assertConservesEnergy(s);
    }

    @Test
    void idleProducerInjectsNothing() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 10, VoltageClass.LV);
        grid.setOffer(1, 100);

        GridSolution s = grid.solutionOf(1);
        assertEquals(0, s.producedKw(), EPS);
        assertEquals(1, s.satisfaction(2), EPS);
    }

    @Test
    void consumerWithoutProducerGetsNothing() {
        grid.addNode(1, NodeRole.CONSUMER);
        grid.setDemand(1, 10);
        assertEquals(0, grid.solutionOf(1).satisfaction(1), EPS);
    }

    @Test
    void surplusChargesStorageWithinItsLimit() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.STORAGE);
        grid.connect(1, 2, 0, VoltageClass.LV);
        grid.connect(1, 3, 0, VoltageClass.LV);
        grid.setOffer(1, 100);
        grid.setDemand(2, 40);
        grid.setStorageLimits(3, 30, 30);

        GridSolution s = grid.solutionOf(1);
        assertEquals(30, s.chargedKw(), EPS);
        assertEquals(-30, s.flowKw(3), EPS);
        assertEquals(70, s.producedKw(), EPS);

        grid.setStorageLimits(3, 500, 30);
        s = grid.solutionOf(1);
        assertEquals(60, s.chargedKw(), EPS);
        assertEquals(100, s.producedKw(), EPS);
    }

    @Test
    void fullStorageTakesNothing() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.STORAGE);
        grid.connect(1, 2, 0, VoltageClass.LV);
        grid.setOffer(1, 100);
        grid.setStorageLimits(2, 0, 50);

        GridSolution s = grid.solutionOf(1);
        assertEquals(0, s.chargedKw(), EPS);
        assertEquals(0, s.producedKw(), EPS);
    }

    @Test
    void storageCoversADeficit() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.STORAGE);
        grid.connect(1, 2, 0, VoltageClass.LV);
        grid.connect(3, 2, 0, VoltageClass.LV);
        grid.setOffer(1, 30);
        grid.setDemand(2, 50);
        grid.setStorageLimits(3, 100, 100);

        GridSolution s = grid.solutionOf(2);
        assertEquals(1, s.satisfaction(2), EPS);
        assertEquals(20, s.dischargedKw(), EPS);
        assertEquals(20, s.flowKw(3), EPS);
        assertEquals(0, s.chargedKw(), EPS);
        assertConservesEnergy(s);
    }

    @Test
    void storageAloneCoversWhatItCan() {
        grid.addNode(1, NodeRole.STORAGE);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.CONSUMER);
        grid.connect(1, 2, 0, VoltageClass.LV);
        grid.connect(1, 3, 300, VoltageClass.LV);
        grid.setStorageLimits(1, 0, 40);
        grid.setDemand(2, 50);
        grid.setDemand(3, 50);

        GridSolution s = grid.solutionOf(1);
        assertEquals(40, s.dischargedKw(), EPS);
        assertTrue(s.satisfaction(2) <= 1 && s.satisfaction(3) <= 1);
        assertTrue(s.deliveredKw() < 40);
        assertConservesEnergy(s);
    }

    @Test
    void overloadedCableCapsThePower() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 0, VoltageClass.LV);
        grid.setOffer(1, 1000);
        grid.setDemand(2, 500);

        GridSolution s = grid.solutionOf(1);
        Cable cable = grid.cable(1, 2);
        assertTrue(s.isOverloaded());
        assertTrue(s.overloaded().contains(cable));
        assertEquals(LV_MAX, s.cableLoadKw(cable), EPS);
        assertEquals(LV_MAX, s.deliveredKw(), EPS);
        assertEquals(LV_MAX / 500, s.satisfaction(2), EPS);
    }

    @Test
    void cableWithinCapacityIsNotOverloaded() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 0, VoltageClass.HV);
        grid.setOffer(1, 1000);
        grid.setDemand(2, 500);

        GridSolution s = grid.solutionOf(1);
        assertFalse(s.isOverloaded());
        assertEquals(1, s.satisfaction(2), EPS);
    }

    @Test
    void powerTakesTheLeastLossPath() {
        // 1 -> 2 directly over 400 blocks of LV, or 1 -> 3 -> 2 over 600 blocks of HV.
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.addNode(3, NodeRole.CONNECTOR);
        grid.connect(1, 2, 400, VoltageClass.LV);
        grid.connect(1, 3, 300, VoltageClass.HV);
        grid.connect(3, 2, 300, VoltageClass.HV);
        grid.setOffer(1, 100);
        grid.setDemand(2, 10);

        GridSolution s = grid.solutionOf(1);
        assertEquals(0, s.cableLoadKw(grid.cable(1, 2)), EPS);
        assertTrue(s.cableLoadKw(grid.cable(1, 3)) > 0);
        assertTrue(s.cableLoadKw(grid.cable(3, 2)) > 0);
    }

    @Test
    void settingsReloadRebuildsAndAppliesNewLosses() {
        grid.addNode(1, NodeRole.PRODUCER);
        grid.addNode(2, NodeRole.CONSUMER);
        grid.connect(1, 2, 100, VoltageClass.LV);
        grid.setOffer(1, 100);
        grid.setDemand(2, 50);
        double before = grid.solutionOf(1).lossesKw();

        grid.setSettings(new GridSettings(Map.of(
                VoltageClass.LV, new CableSpec(0.4, 640, 0),
                VoltageClass.HV, new CableSpec(20, 100, 0),
                VoltageClass.EHV, new CableSpec(400, 40, 0))));
        assertTrue(before > 0);
        assertEquals(0, grid.solutionOf(1).lossesKw(), EPS);
    }

    @Test
    void largeNetworkStaysConsistent() {
        // A 200-node chain with producers and consumers spread along it.
        for (long id = 0; id < 200; id++) {
            NodeRole role = id % 10 == 0 ? NodeRole.PRODUCER : id % 10 == 5 ? NodeRole.CONSUMER : NodeRole.CONNECTOR;
            grid.addNode(id, role);
            if (id > 0) {
                grid.connect(id - 1, id, 20, VoltageClass.HV);
            }
            if (role == NodeRole.PRODUCER) {
                grid.setOffer(id, 64);
            } else if (role == NodeRole.CONSUMER) {
                grid.setDemand(id, 40);
            }
        }
        GridSolution s = grid.solutionOf(0);
        assertEquals(1, s.satisfaction(5), EPS);
        assertEquals(800, s.deliveredKw(), EPS);
        assertConservesEnergy(s);
        assertEquals(1, grid.networks().size());
    }

    @Test
    void largeNetworkSolvesQuickly() {
        // 2000 nodes, 100 producers and 100 consumers: a solve must stay well under a server tick (50 ms).
        for (long id = 0; id < 2000; id++) {
            NodeRole role = id % 20 == 0 ? NodeRole.PRODUCER : id % 20 == 10 ? NodeRole.CONSUMER : NodeRole.CONNECTOR;
            grid.addNode(id, role);
            if (id > 0) {
                grid.connect(id - 1, id, 10, VoltageClass.HV);
            }
            if (role == NodeRole.PRODUCER) {
                grid.setOffer(id, 64);
            } else if (role == NodeRole.CONSUMER) {
                grid.setDemand(id, 40);
            }
        }
        grid.solutionOf(0);
        long start = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            grid.setOffer(0, 64 - i);
            grid.solutionOf(0);
        }
        double millisPerSolve = (System.nanoTime() - start) / 1e6 / 20;
        assertTrue(millisPerSolve < 25, "solve took " + millisPerSolve + " ms");
    }
}
