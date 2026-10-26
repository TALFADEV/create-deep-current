package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import fr.talfa.deepcurrent.client.AlternatorRenderer;
import fr.talfa.deepcurrent.client.ConnectorRenderer;
import fr.talfa.deepcurrent.client.DCPartialModels;
import fr.talfa.deepcurrent.client.HalfShaftRenderer;
import fr.talfa.deepcurrent.content.grid.ConnectorBlockEntity;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;
import fr.talfa.deepcurrent.content.machines.AlternatorBlockEntity;
import fr.talfa.deepcurrent.content.machines.ElectricMotorBlockEntity;

public final class DCBlockEntities {
    public static final BlockEntityEntry<ConnectorBlockEntity> CONNECTOR = REGISTRATE
            .blockEntity("connector", ConnectorBlockEntity::new)
            .validBlocks(DCBlocks.LV_CONNECTOR)
            .renderer(() -> ConnectorRenderer::new)
            .register();

    public static final BlockEntityEntry<CreativePowerBlockEntity> CREATIVE_POWER_SOURCE = REGISTRATE
            .blockEntity("creative_power_source", CreativePowerBlockEntity::new)
            .validBlocks(DCBlocks.CREATIVE_POWER_SOURCE)
            .register();

    public static final BlockEntityEntry<CreativePowerBlockEntity> CREATIVE_POWER_CONSUMER = REGISTRATE
            .blockEntity("creative_power_consumer", CreativePowerBlockEntity::new)
            .validBlocks(DCBlocks.CREATIVE_POWER_CONSUMER)
            .register();

    // The spinning half shaft: Flywheel visual when available, block entity renderer otherwise (as Create does).
    // The rotor spins with the through shaft (modelled along Z, turned to the block's axis).
    public static final BlockEntityEntry<AlternatorBlockEntity> ALTERNATOR = REGISTRATE
            .blockEntity("alternator", AlternatorBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.ofZ(DCPartialModels.ALTERNATOR_ROTOR), false)
            .validBlocks(DCBlocks.ALTERNATOR)
            .renderer(() -> AlternatorRenderer::new)
            .register();

    public static final BlockEntityEntry<ElectricMotorBlockEntity> ELECTRIC_MOTOR = REGISTRATE
            .blockEntity("electric_motor", ElectricMotorBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(DCBlocks.ELECTRIC_MOTOR)
            .renderer(() -> HalfShaftRenderer::new)
            .register();

    private DCBlockEntities() {
    }

    public static void register() {
    }
}
