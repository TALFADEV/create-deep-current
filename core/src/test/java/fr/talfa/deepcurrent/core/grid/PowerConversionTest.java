package fr.talfa.deepcurrent.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PowerConversionTest {
    private static final double SU_PER_KW = 256;
    private static final double EPS = 1e-9;

    @Test
    void alternatorConvertsAbsorbedStressAfterEfficiency() {
        // 64 SU/RPM at 256 RPM absorbs 16384 SU: 64 kW before losses, 57.6 kW at 90 %.
        assertEquals(57.6, PowerConversion.alternatorKw(64, 256, SU_PER_KW, 0.9), EPS);
        assertEquals(57.6, PowerConversion.alternatorKw(64, -256, SU_PER_KW, 0.9), EPS);
        assertEquals(0, PowerConversion.alternatorKw(64, 0, SU_PER_KW, 0.9), EPS);
    }

    @Test
    void lonelyMotorAsksForTheStressUsed() {
        assertEquals(2048.0 / SU_PER_KW, PowerConversion.motorDemandKw(2048, 0, 16384, SU_PER_KW), EPS);
    }

    @Test
    void motorNeverAsksForMoreThanItsCapacity() {
        assertEquals(16384 / SU_PER_KW, PowerConversion.motorDemandKw(50000, 0, 16384, SU_PER_KW), EPS);
    }

    @Test
    void motorSharesTheLoadWithOtherSources() {
        // Another source of equal capacity takes half the stress.
        assertEquals(1024 / SU_PER_KW, PowerConversion.motorDemandKw(2048, 16384, 16384, SU_PER_KW), EPS);
    }

    @Test
    void idleOrStoppedMotorAsksForNothing() {
        assertEquals(0, PowerConversion.motorDemandKw(0, 0, 16384, SU_PER_KW), EPS);
        assertEquals(0, PowerConversion.motorDemandKw(2048, 0, 0, SU_PER_KW), EPS);
    }

    @Test
    void fullySuppliedMotorOffersItsFullCapacity() {
        assertEquals(64, PowerConversion.motorCapacityPerRpm(64, 128, 8, 1, SU_PER_KW), EPS);
        assertEquals(64, PowerConversion.motorCapacityPerRpm(64, 128, 0, 0, SU_PER_KW), EPS);
    }

    @Test
    void starvedMotorOffersWhatTheElectricityPaysFor() {
        // Half of 8 kW delivered = 1024 SU, spread over 128 RPM = 8 SU/RPM.
        assertEquals(8, PowerConversion.motorCapacityPerRpm(64, 128, 8, 0.5, SU_PER_KW), EPS);
        assertEquals(0, PowerConversion.motorCapacityPerRpm(64, 128, 8, 0, SU_PER_KW), EPS);
    }
}
