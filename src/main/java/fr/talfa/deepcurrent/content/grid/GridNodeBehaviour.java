package fr.talfa.deepcurrent.content.grid;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

/**
 * Makes a Create block entity a node of the electrical grid: registers the node and its bonds when the block
 * loads on the server, refreshes what the block shows at the lazy-tick rate, and clears its values when the chunk
 * unloads. Any {@link SmartBlockEntity} can carry it, kinetic ones included.
 */
public class GridNodeBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<GridNodeBehaviour> TYPE = new BehaviourType<>("deepcurrent_grid_node");

    private final GridParticipant participant;

    public <T extends SmartBlockEntity & GridParticipant> GridNodeBehaviour(T blockEntity) {
        super(blockEntity);
        this.participant = blockEntity;
        setLazyTickRate(10);
    }

    public GridParticipant participant() {
        return participant;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    /** The level's grid on the server, null on the client. */
    public LevelGrid grid() {
        return getWorld() instanceof ServerLevel level ? LevelGrid.get(level) : null;
    }

    /** Pushes the participant's values now, after one of them changed. */
    public void push() {
        LevelGrid grid = grid();
        if (grid != null && grid.hasNode(getPos())) {
            participant.pushValues(grid);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        LevelGrid grid = grid();
        if (grid != null) {
            grid.ensureNode(getPos(), participant.gridRole());
            participant.bondNeighbours(getWorld(), getPos(), grid);
            participant.pushValues(grid);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        LevelGrid grid = grid();
        if (grid != null && grid.hasNode(getPos()) && participant.pullValues(grid)) {
            blockEntity.sendData();
        }
    }

    @Override
    public void unload() {
        super.unload();
        LevelGrid grid = grid();
        if (grid != null && grid.hasNode(getPos())) {
            participant.clearValues(grid);
        }
    }

    //? if >=1.21 {
    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        participant.writeGridData(tag, clientPacket);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        participant.readGridData(tag, clientPacket);
    }
    //?} else {
    /*@Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        participant.writeGridData(tag, clientPacket);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        participant.readGridData(tag, clientPacket);
    }
    *///?}
}
