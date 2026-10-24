package fr.talfa.deepcurrent.content.grid;

import java.util.ArrayList;
import java.util.List;

import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Connector state. The grid itself lives in {@link LevelGrid}; this block entity only mirrors, for clients, the
 * cables to draw and a summary of the network for the goggles.
 */
public class ConnectorBlockEntity extends GridBlockEntity {
    /** Far ends of the cables attached here (bonds excluded). */
    private List<BlockPos> links = List.of();
    private NetworkSummary summary = NetworkSummary.EMPTY;

    public ConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Re-reads the cables of the connector at {@code pos} and syncs them, if it is loaded. */
    public static void refresh(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof ConnectorBlockEntity connector) {
            LevelGrid grid = LevelGrid.get(level);
            connector.readLinks(grid);
            connector.summary = NetworkSummary.of(grid, pos);
            connector.sendData();
        }
    }

    public List<BlockPos> links() {
        return links;
    }

    public NetworkSummary summary() {
        return summary;
    }

    public VoltageClass voltage() {
        return getBlockState().getBlock() instanceof ConnectorBlock block ? block.voltage() : VoltageClass.LV;
    }

    @Override
    public NodeRole gridRole() {
        return NodeRole.CONNECTOR;
    }

    @Override
    protected void bondNeighbours(LevelGrid grid) {
        BlockPos support = ConnectorBlock.support(worldPosition, getBlockState());
        BlockEntity machine = level.getBlockEntity(support);
        if (machine instanceof GridBlockEntity gridMachine && gridMachine.gridRole() != NodeRole.CONNECTOR) {
            grid.ensureNode(support, gridMachine.gridRole());
            grid.bond(worldPosition, support, voltage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level instanceof ServerLevel serverLevel) {
            LevelGrid grid = LevelGrid.get(serverLevel);
            readLinks(grid);
            summary = NetworkSummary.of(grid, worldPosition);
            sendData();
        }
    }

    @Override
    protected boolean pullValues(LevelGrid grid) {
        NetworkSummary fresh = NetworkSummary.of(grid, worldPosition);
        if (fresh.equals(summary)) {
            return false;
        }
        summary = fresh;
        return true;
    }

    private void readLinks(LevelGrid grid) {
        List<BlockPos> fresh = new ArrayList<>();
        for (LevelGrid.StoredCable cable : grid.cablesOf(worldPosition)) {
            if (!cable.bond()) {
                fresh.add(BlockPos.of(cable.other(worldPosition.asLong())));
            }
        }
        if (!fresh.equals(links)) {
            links = List.copyOf(fresh);
            invalidateRenderBoundingBox();
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        AABB box = new AABB(worldPosition);
        for (BlockPos other : links) {
            box = box.minmax(new AABB(other));
        }
        return box.inflate(1);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.grid.title",
                Component.translatable("deepcurrent.voltage." + voltage().name().toLowerCase()))
                .withStyle(ChatFormatting.WHITE)));
        summary.describe(tooltip, true);
        return true;
    }

    @Override
    protected void writeData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            tag.putLongArray("Links", links.stream().mapToLong(BlockPos::asLong).toArray());
            tag.put("Summary", summary.write());
        }
    }

    @Override
    protected void readData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            List<BlockPos> read = new ArrayList<>();
            for (long packed : tag.getLongArray("Links")) {
                read.add(BlockPos.of(packed));
            }
            links = List.copyOf(read);
            summary = NetworkSummary.read(tag.getCompound("Summary"));
            invalidateRenderBoundingBox();
        }
    }
}
