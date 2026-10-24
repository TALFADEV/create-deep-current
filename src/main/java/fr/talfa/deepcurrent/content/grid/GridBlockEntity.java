package fr.talfa.deepcurrent.content.grid;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

/**
 * A block that is a node of the electrical grid. Registers its node when it loads on the server, bonds to the
 * connectors around it, and refreshes what it shows at Create's lazy-tick rate.
 */
public abstract class GridBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    protected GridBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
    }

    public abstract NodeRole gridRole();

    /** Links this node to its neighbours (a connector to its machine, a machine to its connectors). */
    protected abstract void bondNeighbours(LevelGrid grid);

    /** Pushes this node's offer, demand or storage limits into the grid. */
    protected void pushValues(LevelGrid grid) {
    }

    /** Reads the grid's balance into the fields synced to clients; returns true if anything changed. */
    protected boolean pullValues(LevelGrid grid) {
        return false;
    }

    /** Clears this node's offer or demand while its chunk is unloaded. */
    protected void clearValues(LevelGrid grid) {
    }

    protected void writeData(CompoundTag tag, boolean clientPacket) {
    }

    protected void readData(CompoundTag tag, boolean clientPacket) {
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        // Before super: Create's initialize() runs a first lazyTick, which reads the node.
        if (level instanceof ServerLevel serverLevel) {
            LevelGrid grid = LevelGrid.get(serverLevel);
            grid.ensureNode(worldPosition, gridRole());
            bondNeighbours(grid);
            pushValues(grid);
        }
        super.initialize();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level instanceof ServerLevel serverLevel) {
            LevelGrid grid = LevelGrid.get(serverLevel);
            if (grid.hasNode(worldPosition) && pullValues(grid)) {
                sendData();
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel serverLevel) {
            LevelGrid grid = LevelGrid.get(serverLevel);
            if (grid.hasNode(worldPosition)) {
                clearValues(grid);
            }
        }
    }

    protected LevelGrid grid() {
        return level instanceof ServerLevel serverLevel ? LevelGrid.get(serverLevel) : null;
    }

    //? if >=1.21 {
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        writeData(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        readData(tag, clientPacket);
    }
    //?} else {
    /*@Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        writeData(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        readData(tag, clientPacket);
    }
    *///?}
}
