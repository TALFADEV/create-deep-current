package fr.talfa.deepcurrent.core.pressure;

import java.util.Arrays;

/**
 * Hull tiers and their maximum depth (GDD §3.3). Tiers are numbered from 1; the last tier has no depth limit.
 */
public final class PressureTiers {
    public static final int UNLIMITED = Integer.MAX_VALUE;
    /** Defaults: brass 16, riveted steel 32, abyssal alloy 64, hadal hull unlimited. */
    public static final PressureTiers DEFAULT = new PressureTiers(16, 32, 64);

    private final int[] maxDepths;

    /**
     * @param limitedMaxDepths maximum depth of every tier but the last one, strictly increasing
     */
    public PressureTiers(int... limitedMaxDepths) {
        for (int i = 0; i < limitedMaxDepths.length; i++) {
            if (limitedMaxDepths[i] < 0 || (i > 0 && limitedMaxDepths[i] <= limitedMaxDepths[i - 1])) {
                throw new IllegalArgumentException("tier depths must be positive and strictly increasing: "
                        + Arrays.toString(limitedMaxDepths));
            }
        }
        this.maxDepths = Arrays.copyOf(limitedMaxDepths, limitedMaxDepths.length + 1);
        this.maxDepths[limitedMaxDepths.length] = UNLIMITED;
    }

    public int count() {
        return maxDepths.length;
    }

    /** Maximum depth a hull of this tier withstands, {@link #UNLIMITED} for the last tier. */
    public int maxDepth(int tier) {
        checkTier(tier);
        return maxDepths[tier - 1];
    }

    /** Lowest tier that withstands this depth. */
    public int requiredTier(int depth) {
        for (int i = 0; i < maxDepths.length; i++) {
            if (depth <= maxDepths[i]) {
                return i + 1;
            }
        }
        return maxDepths.length;
    }

    /** Blocks beyond the tier's maximum depth; zero when the hull holds. */
    public int overshoot(int tier, int depth) {
        int max = maxDepth(tier);
        return max == UNLIMITED ? 0 : Math.max(0, depth - max);
    }

    private void checkTier(int tier) {
        if (tier < 1 || tier > maxDepths.length) {
            throw new IllegalArgumentException("tier " + tier + " outside 1.." + maxDepths.length);
        }
    }
}
