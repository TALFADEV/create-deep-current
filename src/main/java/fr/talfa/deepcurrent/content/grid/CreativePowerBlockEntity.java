package fr.talfa.deepcurrent.content.grid;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Creative source or load with a power set in kW through Create's value panel. */
public class CreativePowerBlockEntity extends SmartBlockEntity implements GridParticipant, IHaveGoggleInformation {
    /** Create's value panel draws one cell per value: the setting moves in steps to keep it short. */
    public static final int KW_PER_STEP = 8;
    public static final int MAX_KW = 1024;
    private static final int DEFAULT_KW = 64;

    private ScrollValueBehaviour power;
    private GridNodeBehaviour node;
    /** Power actually injected (source) or received (load), synced for the goggles. */
    private double actualKw;
    /** Fraction of the demand delivered, for a load. */
    private double satisfaction = 1;

    public CreativePowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private boolean isSource() {
        return getBlockState().getBlock() instanceof CreativePowerBlock block && block.isSource();
    }

    @Override
    public NodeRole gridRole() {
        return isSource() ? NodeRole.PRODUCER : NodeRole.CONSUMER;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        power = new ScrollValueBehaviour(Component.translatable("deepcurrent.creative_power.setting"), this,
                new CenteredSideValueBoxTransform((state, side) -> side.getAxis() != Direction.Axis.Y))
                .between(0, MAX_KW / KW_PER_STEP)
                .withFormatter(value -> value * KW_PER_STEP + " kW")
                .withCallback(value -> node.push());
        power.value = DEFAULT_KW / KW_PER_STEP;
        behaviours.add(power);
        node = new GridNodeBehaviour(this);
        behaviours.add(node);
    }

    public int settingKw() {
        return power.getValue() * KW_PER_STEP;
    }

    /** Sets the power, rounded down to a whole step. */
    public void setSettingKw(int kw) {
        power.setValue(kw / KW_PER_STEP);
    }

    @Override
    public void pushValues(LevelGrid grid) {
        if (isSource()) {
            grid.graph().setOffer(worldPosition.asLong(), settingKw());
        } else {
            grid.graph().setDemand(worldPosition.asLong(), settingKw());
        }
    }

    @Override
    public void clearValues(LevelGrid grid) {
        if (isSource()) {
            grid.graph().setOffer(worldPosition.asLong(), 0);
        } else {
            grid.graph().setDemand(worldPosition.asLong(), 0);
        }
    }

    @Override
    public boolean pullValues(LevelGrid grid) {
        GridSolution solution = grid.solution(worldPosition);
        double flow = Math.abs(solution.flowKw(worldPosition.asLong()));
        double served = solution.satisfaction(worldPosition.asLong());
        if (Math.abs(flow - actualKw) < 0.05 && Math.abs(served - satisfaction) < 0.001) {
            return false;
        }
        actualKw = flow;
        satisfaction = served;
        return true;
    }

    public double actualKw() {
        return actualKw;
    }

    public double satisfaction() {
        return satisfaction;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        String key = isSource() ? "deepcurrent.creative_power.source" : "deepcurrent.creative_power.load";
        tooltip.add(Component.literal("    ").append(Component.translatable(key).withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("    ").append(Component.translatable(key + ".value",
                NetworkSummary.number(actualKw), settingKw()).withStyle(ChatFormatting.GRAY)));
        if (!isSource() && satisfaction < 0.999) {
            tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.creative_power.short",
                    NetworkSummary.number(satisfaction * 100)).withStyle(ChatFormatting.GOLD)));
        }
        return true;
    }

    @Override
    public void writeGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            tag.putDouble("ActualKw", actualKw);
            tag.putDouble("Satisfaction", satisfaction);
        }
    }

    @Override
    public void readGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            actualKw = tag.getDouble("ActualKw");
            satisfaction = tag.getDouble("Satisfaction");
        }
    }
}
