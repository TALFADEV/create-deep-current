package fr.talfa.deepcurrent.content.grid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The connector each player started a cable from. Kept separately on each side: the server validates and lays
 * the cable, the client draws the preview. Not saved: a pending cable is dropped on logout or server stop.
 */
public final class CablePlacement {
    private static final Map<UUID, Start> SERVER = new HashMap<>();
    private static final Map<UUID, Start> CLIENT = new HashMap<>();

    private CablePlacement() {
    }

    public record Start(ResourceKey<Level> dimension, BlockPos pos) {
    }

    public static Start get(Player player) {
        return side(player).get(player.getUUID());
    }

    public static void set(Player player, Start start) {
        side(player).put(player.getUUID(), start);
    }

    public static void clear(Player player) {
        side(player).remove(player.getUUID());
    }

    public static void clearServer() {
        SERVER.clear();
    }

    private static Map<UUID, Start> side(Player player) {
        return player.level().isClientSide ? CLIENT : SERVER;
    }
}
