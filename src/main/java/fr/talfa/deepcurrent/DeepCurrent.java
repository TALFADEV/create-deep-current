package fr.talfa.deepcurrent;

import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import fr.talfa.deepcurrent.config.DCConfigs;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
//?} else {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}

@Mod(DeepCurrent.ID)
public final class DeepCurrent {
    public static final String ID = "deepcurrent";
    public static final String NAME = "Create: Deep Current";
    public static final Logger LOGGER = LogUtils.getLogger();

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
    }

    public static ResourceLocation asResource(String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
        //?} else
        //return new ResourceLocation(ID, path);
    }
}
