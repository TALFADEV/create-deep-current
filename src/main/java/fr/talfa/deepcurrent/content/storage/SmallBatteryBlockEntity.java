package fr.talfa.deepcurrent.content.storage;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.content.grid.GridNodeBehaviour;
import fr.talfa.deepcurrent.content.grid.GridParticipant;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.content.grid.NetworkSummary;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.StorageCell;
import fr.talfa.deepcurrent.core.grid.StorageSpec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Battery as a grid storage node. Every server tick it books the power the grid solution gives or takes from it;
 * it only pushes new limits (so the grid solves again) when it becomes full or empty, or stops being so. The
 * gauge (block state) and the comparator follow the charge.
 */
public class SmallBatteryBlockEntity extends SmartBlockEntity implements GridParticipant, IHaveGoggleInformation {
    private static final double SECONDS_PER_TICK = 0.05;
    /** Below this flow, in kW, the battery shows as idle. */
    private static final double IDLE_KW = 0.01;

    private final StorageCell cell = new StorageCell();
    private GridNodeBehaviour node;
    /** Power the battery supplies (positive) or draws (negative), last seen; synced for the goggles. */
    private double flowKw;
    private double syncedKj = -1;
    private double syncedFlowKw;
    private int comparator;

    public SmallBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static StorageSpec spec() {
        return DCConfigs.server().smallBatterySpec();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        node = new GridNodeBehaviour(this);
        behaviours.add(node);
    }

    @Override
    public NodeRole gridRole() {
        return NodeRole.STORAGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        LevelGrid grid = node.grid();
        if (grid == null || !grid.hasNode(worldPosition)) {
            return;
        }
        flowKw = grid.solution(worldPosition).flowKw(worldPosition.asLong());
        if (flowKw != 0) {
            if (cell.step(flowKw, SECONDS_PER_TICK, spec())) {
                pushValues(grid);
            }
            chargeChanged();
        }
    }

    public double storedKj() {
        return cell.storedKj();
    }

    public double flowKw() {
        return flowKw;
    }

    /** Sets the stored energy (placing a charged item, tests), within the capacity. */
    public void setStoredKj(double kj) {
        cell.setStoredKj(kj, spec());
        LevelGrid grid = node.grid();
        if (grid != null && grid.hasNode(worldPosition)) {
            pushValues(grid);
        }
        chargeChanged();
    }

    public int comparatorSignal() {
        return cell.comparatorSignal(spec());
    }

    /** Saves the charge and keeps the gauge and the comparator in step with it. */
    private void chargeChanged() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        int lit = cell.level(spec(), SmallBatteryBlock.GAUGE_CELLS);
        if (state.hasProperty(SmallBatteryBlock.CHARGE) && state.getValue(SmallBatteryBlock.CHARGE) != lit) {
            level.setBlock(worldPosition, state.setValue(SmallBatteryBlock.CHARGE, lit), Block.UPDATE_ALL);
        }
        int signal = comparatorSignal();
        if (signal != comparator) {
            comparator = signal;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public void pushValues(LevelGrid grid) {
        StorageSpec spec = spec();
        grid.graph().setStorageLimits(worldPosition.asLong(), cell.chargeLimitKw(spec), cell.dischargeLimitKw(spec));
    }

    @Override
    public void clearValues(LevelGrid grid) {
        grid.graph().setStorageLimits(worldPosition.asLong(), 0, 0);
    }

    /** Syncs the goggles when the charge moved by 0.1 % of the capacity or the flow changed. */
    @Override
    public boolean pullValues(LevelGrid grid) {
        double step = spec().capacityKj() * 0.001;
        if (Math.abs(cell.storedKj() - syncedKj) < step && Math.abs(flowKw - syncedFlowKw) < 0.05) {
            return false;
        }
        syncedKj = cell.storedKj();
        syncedFlowKw = flowKw;
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        StorageSpec spec = spec();
        tooltip.add(line(Component.translatable("deepcurrent.small_battery.title"), ChatFormatting.WHITE));
        tooltip.add(line(Component.translatable("deepcurrent.small_battery.stored",
                NetworkSummary.number(cell.storedKj() / 3600), NetworkSummary.number(spec.capacityKwh()),
                NetworkSummary.number(cell.fraction(spec) * 100)), ChatFormatting.GRAY));
        Component state;
        if (flowKw < -IDLE_KW) {
            state = Component.translatable("deepcurrent.small_battery.charging", NetworkSummary.number(-flowKw));
        } else if (flowKw > IDLE_KW) {
            state = Component.translatable("deepcurrent.small_battery.discharging", NetworkSummary.number(flowKw));
        } else if (cell.isFull(spec)) {
            state = Component.translatable("deepcurrent.small_battery.full");
        } else if (cell.isEmpty()) {
            state = Component.translatable("deepcurrent.small_battery.empty");
        } else {
            state = Component.translatable("deepcurrent.small_battery.idle");
        }
        tooltip.add(line(state, flowKw < -IDLE_KW ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        return true;
    }

    private static Component line(Component text, ChatFormatting style) {
        return Component.literal("    ").append(text.copy().withStyle(style));
    }

    @Override
    public void writeGridData(CompoundTag tag, boolean clientPacket) {
        BatteryCharge.write(tag, cell.storedKj());
        if (clientPacket) {
            tag.putDouble("FlowKw", flowKw);
        }
    }

    @Override
    public void readGridData(CompoundTag tag, boolean clientPacket) {
        cell.setStoredKj(tag.getDouble(BatteryCharge.KEY), spec());
        if (clientPacket) {
            flowKw = tag.getDouble("FlowKw");
        }
    }
}
