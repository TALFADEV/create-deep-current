package fr.talfa.deepcurrent;

import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import fr.talfa.deepcurrent.client.DCClient;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.platform.DCGameEvents;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import fr.talfa.deepcurrent.registry.DCBlocks;
import fr.talfa.deepcurrent.registry.DCCreativeTabs;
import fr.talfa.deepcurrent.registry.DCItems;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
//?} else {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
*///?}

@Mod(DeepCurrent.ID)
public final class DeepCurrent {
    public static final String ID = "deepcurrent";
    public static final String NAME = "Create: Deep Current";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

    //? if neoforge {
    public DeepCurrent(IEventBus modBus, ModContainer container) {
        DCConfigs.register(container, modBus);
        init(modBus);
    }
    //?} else {
    /*public DeepCurrent() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DCConfigs.register(ModLoadingContext.get(), modBus);
        init(modBus);
    }
    *///?}

    private static void init(IEventBus modBus) {
        LOGGER.info("{} loading on top of {}", NAME, Create.NAME);
        REGISTRATE.registerEventListeners(modBus);
        // The tab first: entries pick Registrate's default tab when they are declared.
        DCCreativeTabs.register(modBus);
        DCBlocks.register();
        DCBlockEntities.register();
        DCItems.register();
        DCGameEvents.register();
        if (FMLEnvironment.dist.isClient()) {
            DCClient.init(modBus);
        }
    }

    public static ResourceLocation asResource(String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
        //?} else
        //return new ResourceLocation(ID, path);
    }
}
