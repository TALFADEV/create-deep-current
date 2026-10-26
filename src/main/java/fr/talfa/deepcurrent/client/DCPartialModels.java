package fr.talfa.deepcurrent.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import fr.talfa.deepcurrent.DeepCurrent;

/** Moving parts rendered apart from their block, as Create does with its own partial models. */
public final class DCPartialModels {
    /** The alternator's rotor: copper drum and through shaft, modelled along Z. */
    public static final PartialModel ALTERNATOR_ROTOR = PartialModel.of(DeepCurrent.asResource("block/alternator/rotor"));

    private DCPartialModels() {
    }

    /** Partial models must be declared before models load: call during mod construction. */
    public static void init() {
    }
}
