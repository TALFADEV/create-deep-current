package fr.talfa.deepcurrent.core.tide;

/**
 * Tide values at one moment.
 *
 * @param coefficient  multiplier applied to turbine output
 * @param lunarFactor  spring/neap part of the coefficient
 * @param streamFactor time-of-day part of the coefficient
 * @param level        water level from -1 (low) to 1 (high)
 * @param rising       whether the tide is coming in
 */
public record TideState(double coefficient, double lunarFactor, double streamFactor, double level, boolean rising) {
}
