package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlock;
import fr.talfa.deepcurrent.content.machines.AlternatorBlock;
import fr.talfa.deepcurrent.content.machines.AlternatorBlockEntity;
import fr.talfa.deepcurrent.content.machines.ElectricMotorBlock;
import fr.talfa.deepcurrent.content.machines.ElectricMotorBlockEntity;
import fr.talfa.deepcurrent.content.storage.SmallBatteryBlock;
import fr.talfa.deepcurrent.content.storage.SmallBatteryItem;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public final class DCBlocks {
    public static final BlockEntry<ConnectorBlock> LV_CONNECTOR = REGISTRATE
            .block("lv_connector", properties -> new ConnectorBlock(properties, VoltageClass.LV))
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE).strength(1.5f).sound(SoundType.COPPER).noOcclusion())
            .item().build()
            .register();

    public static final BlockEntry<CreativePowerBlock> CREATIVE_POWER_SOURCE = REGISTRATE
            .block("creative_power_source", properties -> new CreativePowerBlock(properties, true))
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE).strength(2f).sound(SoundType.NETHERITE_BLOCK))
            .item().build()
            .register();

    public static final BlockEntry<CreativePowerBlock> CREATIVE_POWER_CONSUMER = REGISTRATE
            .block("creative_power_consumer", properties -> new CreativePowerBlock(properties, false))
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE).strength(2f).sound(SoundType.NETHERITE_BLOCK))
            .item().build()
            .register();

    // Stress values are also registered with Create so that item tooltips show them.
    public static final BlockEntry<AlternatorBlock> ALTERNATOR = REGISTRATE
            .block("alternator", AlternatorBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).strength(2f).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
                    .lightLevel(state -> state.getValue(AlternatorBlock.ACTIVE) ? AlternatorBlock.ACTIVE_LIGHT : 0))
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, AlternatorBlockEntity::impactPerRpm))
            .item().build()
            .register();

    public static final BlockEntry<ElectricMotorBlock> ELECTRIC_MOTOR = REGISTRATE
            .block("electric_motor", ElectricMotorBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE).strength(2f).sound(SoundType.COPPER).noOcclusion())
            .onRegister(block -> BlockStressValues.CAPACITIES.register(block,
                    ElectricMotorBlockEntity::maxCapacityPerRpm))
            .onRegister(BlockStressValues.setGeneratorSpeed(ElectricMotorBlockEntity.MAX_SPEED, true))
            .item().build()
            .register();

    public static final BlockEntry<SmallBatteryBlock> SMALL_BATTERY = REGISTRATE
            .block("small_battery", SmallBatteryBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).strength(2f).sound(SoundType.NETHERITE_BLOCK).noOcclusion())
            .item(SmallBatteryItem::new).build()
            .register();

    private DCBlocks() {
    }

    /** Loads the class so its entries reach Registrate before registration starts. */
    public static void register() {
    }
}
