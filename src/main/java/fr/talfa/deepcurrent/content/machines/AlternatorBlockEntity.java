package fr.talfa.deepcurrent.content.machines;

import java.util.List;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.config.DCServerConfig;
import fr.talfa.deepcurrent.content.grid.GridNodeBehaviour;
import fr.talfa.deepcurrent.content.grid.GridParticipant;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.content.grid.NetworkSummary;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.PowerConversion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Alternator: a kinetic consumer with a fixed stress per RPM, like Create's machines. It offers the grid the
 * electricity that stress is worth, after its efficiency; what the grid does not take is lost (until batteries).
 */
public class AlternatorBlockEntity extends KineticBlockEntity implements GridParticipant {
    public static final double DEFAULT_IMPACT = 64;

    private GridNodeBehaviour node;
    /** Power offered and power actually injected, synced for the goggles. */
    private double offerKw;
    private double producedKw;

    public AlternatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static double impactPerRpm() {
        return DCServerConfig.orDefault(DCConfigs.server().alternatorStressImpact, DEFAULT_IMPACT);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        node = new GridNodeBehaviour(this);
        behaviours.add(node);
    }

    @Override
    public float calculateStressApplied() {
        float impact = (float) impactPerRpm();
        lastStressApplied = impact;
        return impact;
    }

    /** Electricity the alternator can inject right now: none while its kinetic network is overstressed. */
    public double currentOfferKw() {
        if (isOverStressed()) {
            return 0;
        }
        DCServerConfig config = DCConfigs.server();
        return PowerConversion.alternatorKw(impactPerRpm(), getSpeed(), config.stressPerKilowatt.get(),
                config.alternatorEfficiency.get());
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        if (node != null) {
            node.push();
        }
    }

    @Override
    public NodeRole gridRole() {
        return NodeRole.PRODUCER;
    }

    @Override
    public void pushValues(LevelGrid grid) {
        grid.graph().setOffer(worldPosition.asLong(), currentOfferKw());
    }

    @Override
    public void clearValues(LevelGrid grid) {
        grid.graph().setOffer(worldPosition.asLong(), 0);
    }

    @Override
    public boolean pullValues(LevelGrid grid) {
        // Overstress and config changes do not call onSpeedChanged: refresh the offer here too.
        pushValues(grid);
        double offer = currentOfferKw();
        double produced = grid.solution(worldPosition).flowKw(worldPosition.asLong());
        boolean active = produced > 0.05;
        BlockState state = getBlockState();
        if (state.hasProperty(AlternatorBlock.ACTIVE) && state.getValue(AlternatorBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(AlternatorBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
        if (Math.abs(offer - offerKw) < 0.05 && Math.abs(produced - producedKw) < 0.05) {
            return false;
        }
        offerKw = offer;
        producedKw = produced;
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.alternator.title")
                .withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.alternator.output",
                NetworkSummary.number(producedKw), NetworkSummary.number(offerKw)).withStyle(ChatFormatting.GRAY)));
        return true;
    }

    @Override
    public void writeGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            tag.putDouble("OfferKw", offerKw);
            tag.putDouble("ProducedKw", producedKw);
        }
    }

    @Override
    public void readGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            offerKw = tag.getDouble("OfferKw");
            producedKw = tag.getDouble("ProducedKw");
        }
    }
}
