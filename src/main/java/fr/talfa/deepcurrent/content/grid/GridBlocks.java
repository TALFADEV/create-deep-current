package fr.talfa.deepcurrent.content.grid;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Placement and removal rules shared by every block of the grid. */
public final class GridBlocks {
    private GridBlocks() {
    }

    /**
     * A grid block was removed: drops its node, gives back the cable it had (100 % of the material, GDD §10 ter)
     * and updates the connectors at the other ends.
     */
    public static void removed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LevelGrid grid = LevelGrid.get(serverLevel);
        List<LevelGrid.StoredCable> cables = grid.removeNode(pos);
        Map<VoltageClass, Integer> coils = new EnumMap<>(VoltageClass.class);
        for (LevelGrid.StoredCable cable : cables) {
            if (!cable.bond()) {
                coils.merge(cable.voltage(), cable.cost(), Integer::sum);
            }
            ConnectorBlockEntity.refresh(serverLevel, BlockPos.of(cable.other(pos.asLong())));
        }
        for (Map.Entry<VoltageClass, Integer> entry : coils.entrySet()) {
            dropCoils(serverLevel, pos, entry.getKey(), entry.getValue());
        }
    }

    /** Bonds a machine to every connector attached to one of its faces. */
    public static void bondConnectorsAround(Level level, BlockPos machine, LevelGrid grid) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = machine.relative(direction);
            BlockState state = level.getBlockState(neighbour);
            if (state.getBlock() instanceof ConnectorBlock connector
                    && state.getValue(ConnectorBlock.FACING) == direction) {
                grid.ensureNode(neighbour, NodeRole.CONNECTOR);
                grid.bond(neighbour, machine, connector.voltage());
            }
        }
    }

    /** Drops {@code count} coils of the given voltage, split into stacks. */
    public static void dropCoils(Level level, BlockPos pos, VoltageClass voltage, int count) {
        CableCoilItem coil = CableCoilItem.forVoltage(voltage);
        if (coil == null) {
            return;
        }
        while (count > 0) {
            int stack = Math.min(count, coil.getDefaultInstance().getMaxStackSize());
            Block.popResource(level, pos, new ItemStack(coil, stack));
            count -= stack;
        }
    }
}
