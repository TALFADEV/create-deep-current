package fr.talfa.deepcurrent.content.grid;

import fr.talfa.deepcurrent.core.grid.NodeRole;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/** A block entity that is a node of the electrical grid, driven by its {@link GridNodeBehaviour}. */
public interface GridParticipant {
    NodeRole gridRole();

    /** Links this node to its neighbours; by default, bonds the connectors attached to its faces. */
    default void bondNeighbours(Level level, BlockPos pos, LevelGrid grid) {
        GridBlocks.bondConnectorsAround(level, pos, grid);
    }

    /** Pushes this node's offer, demand or storage limits into the grid. */
    default void pushValues(LevelGrid grid) {
    }

    /** Reads the grid's balance into the fields synced to clients; returns true if anything changed. */
    default boolean pullValues(LevelGrid grid) {
        return false;
    }

    /** Clears this node's offer or demand while its chunk is unloaded. */
    default void clearValues(LevelGrid grid) {
    }

    default void writeGridData(CompoundTag tag, boolean clientPacket) {
    }

    default void readGridData(CompoundTag tag, boolean clientPacket) {
    }
}
