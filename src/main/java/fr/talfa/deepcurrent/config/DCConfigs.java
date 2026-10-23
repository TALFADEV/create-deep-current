package fr.talfa.deepcurrent.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.createmod.catnip.config.ConfigBase;
import org.apache.commons.lang3.tuple.Pair;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
//?} else {
/*import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
*///?}

/**
 * Owns the mod's config files, following Create's {@code AllConfigs} pattern:
 * balancing values in the server file (per world, synced to clients), visual effects in the client file.
 */
public final class DCConfigs {
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static DCServerConfig server;
    private static DCClientConfig client;

    private DCConfigs() {
    }

    public static DCServerConfig server() {
        return server;
    }

    public static DCClientConfig client() {
        return client;
    }

    private static <T extends ConfigBase> T create(Supplier<T> factory, ModConfig.Type side) {
        //? if neoforge {
        Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
        //?} else
        //Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });
        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    //? if neoforge {
    public static void register(ModContainer container, IEventBus modBus) {
    //?} else
    //public static void register(ModLoadingContext container, IEventBus modBus) {
        server = create(DCServerConfig::new, ModConfig.Type.SERVER);
        client = create(DCClientConfig::new, ModConfig.Type.CLIENT);

        for (Map.Entry<ModConfig.Type, ConfigBase> entry : CONFIGS.entrySet()) {
            container.registerConfig(entry.getKey(), entry.getValue().specification);
        }

        modBus.addListener(DCConfigs::onLoad);
        modBus.addListener(DCConfigs::onReload);
    }

    private static void onLoad(ModConfigEvent.Loading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec()) {
                config.onLoad();
            }
        }
    }

    private static void onReload(ModConfigEvent.Reloading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec()) {
                config.onReload();
            }
        }
    }
}
