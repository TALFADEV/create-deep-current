package fr.talfa.deepcurrent.client;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
//?} else {
/*import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
*///?}

/** Client-only setup; the one place where client events of the loader are named. */
public final class DCClient {
    private DCClient() {
    }

    public static void init(IEventBus modBus) {
        //? if neoforge {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> CablePreview.tick());
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
        //?} else {
        /*MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                CablePreview.tick();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
        *///?}
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                CablePreview.render(event.getPoseStack(), event.getCamera().getPosition());
            }
        });
    }
}
