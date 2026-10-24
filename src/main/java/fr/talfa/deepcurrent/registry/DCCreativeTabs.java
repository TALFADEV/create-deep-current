package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import fr.talfa.deepcurrent.DeepCurrent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
//?} else {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
*///?}

public final class DCCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DeepCurrent.ID);

    //? if neoforge {
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
    //?} else
    /*public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->*/
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.deepcurrent.main"))
                    .icon(() -> DCBlocks.LV_CONNECTOR.asStack())
                    .build());

    private DCCreativeTabs() {
    }

    /** Registers the tab and makes it Registrate's default, which fills it with every item. */
    public static void register(IEventBus modBus) {
        TABS.register(modBus);
        REGISTRATE.defaultCreativeTab(MAIN.getKey());
    }
}
