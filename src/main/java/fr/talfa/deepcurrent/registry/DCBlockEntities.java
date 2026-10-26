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
import fr.talfa.deepcurrent.client.TidalTurbineRenderer;
import fr.talfa.deepcurrent.content.grid.ConnectorBlockEntity;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;
import fr.talfa.deepcurrent.content.machines.AlternatorBlockEntity;
import fr.talfa.deepcurrent.content.machines.ElectricMotorBlockEntity;
import fr.talfa.deepcurrent.content.storage.SmallBatteryBlockEntity;
import fr.talfa.deepcurrent.content.turbine.SmallTidalTurbineBlockEntity;

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

    public static final BlockEntityEntry<SmallBatteryBlockEntity> SMALL_BATTERY = REGISTRATE
            .blockEntity("small_battery", SmallBatteryBlockEntity::new)
            .validBlocks(DCBlocks.SMALL_BATTERY)
            .register();

    // Rotor and output shaft spin together, modelled facing north with the shaft at the back.
    public static final BlockEntityEntry<SmallTidalTurbineBlockEntity> SMALL_TIDAL_TURBINE = REGISTRATE
            .blockEntity("small_tidal_turbine", SmallTidalTurbineBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.backHorizontal(DCPartialModels.TIDAL_TURBINE_ROTOR), false)
            .validBlocks(DCBlocks.SMALL_TIDAL_TURBINE)
            .renderer(() -> TidalTurbineRenderer::new)
            .register();

    private DCBlockEntities() {
    }

    public static void register() {
    }
}
