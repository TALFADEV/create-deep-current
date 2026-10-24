package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntry;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlock;
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

    private DCBlocks() {
    }

    /** Loads the class so its entries reach Registrate before registration starts. */
    public static void register() {
    }
}
