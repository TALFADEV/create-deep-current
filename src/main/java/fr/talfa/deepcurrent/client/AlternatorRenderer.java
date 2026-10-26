package fr.talfa.deepcurrent.client;

import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import fr.talfa.deepcurrent.content.machines.AlternatorBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Spins the alternator's rotor with the shaft when Flywheel's instanced visual is off. */
public class AlternatorRenderer extends KineticBlockEntityRenderer<AlternatorBlockEntity> {
    public AlternatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(AlternatorBlockEntity blockEntity, BlockState state) {
        // The rotor is modelled along Z: turn it toward the block's axis.
        Direction.Axis axis = state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS);
        Direction facing = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        return CachedBuffers.partialFacing(DCPartialModels.ALTERNATOR_ROTOR, state, facing);
    }
}
