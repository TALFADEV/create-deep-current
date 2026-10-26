package fr.talfa.deepcurrent.client;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import fr.talfa.deepcurrent.content.turbine.SmallTidalTurbineBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Spins the turbine's rotor and output shaft when Flywheel's instanced visual is off. */
public class TidalTurbineRenderer extends KineticBlockEntityRenderer<SmallTidalTurbineBlockEntity> {
    public TidalTurbineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SmallTidalTurbineBlockEntity blockEntity, BlockState state) {
        // Modelled facing north, shaft to the south: the shaft side goes to the back, as in the instanced visual.
        return CachedBuffers.partialFacing(DCPartialModels.TIDAL_TURBINE_ROTOR, state,
                state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getOpposite());
    }
}
