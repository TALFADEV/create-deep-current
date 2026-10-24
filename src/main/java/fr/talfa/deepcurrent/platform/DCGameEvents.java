package fr.talfa.deepcurrent.platform;

import fr.talfa.deepcurrent.command.DCCommands;
import fr.talfa.deepcurrent.content.grid.CablePlacement;
import fr.talfa.deepcurrent.world.Tides;

//? if neoforge {
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
//?} else {
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
*///?}

/** Listeners on the game event bus, the one place where its loader-specific classes are named. */
public final class DCGameEvents {
    private DCGameEvents() {
    }

    public static void register() {
        //? if neoforge {
        var bus = NeoForge.EVENT_BUS;
        //?} else
        /*var bus = MinecraftForge.EVENT_BUS;*/
        bus.addListener((RegisterCommandsEvent event) -> DCCommands.register(event.getDispatcher()));
        bus.addListener((ServerStoppedEvent event) -> {
            Tides.reset();
            CablePlacement.clearServer();
        });
    }
}
