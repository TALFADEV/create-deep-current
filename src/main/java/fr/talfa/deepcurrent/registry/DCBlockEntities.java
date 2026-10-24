package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import fr.talfa.deepcurrent.client.ConnectorRenderer;
import fr.talfa.deepcurrent.content.grid.ConnectorBlockEntity;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;

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

    private DCBlockEntities() {
    }

    public static void register() {
    }
}
