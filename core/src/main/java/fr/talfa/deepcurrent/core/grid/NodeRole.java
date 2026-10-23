package fr.talfa.deepcurrent.core.grid;

/** What a grid node does with power. */
public enum NodeRole {
    /** Passive junction (cable connector). */
    CONNECTOR,
    /** Injects power, up to its offer (alternator). */
    PRODUCER,
    /** Draws power, up to its demand (electric motor). */
    CONSUMER,
    /** Stores surplus and covers deficits, within its charge and discharge limits (battery). */
    STORAGE
}
