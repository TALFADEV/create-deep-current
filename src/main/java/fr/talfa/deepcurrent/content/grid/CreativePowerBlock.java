package fr.talfa.deepcurrent.content.grid;

import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Creative-only test blocks (GDD §12): an endless electricity source and an adjustable electrical load.
 * Not craftable. Connect them through a connector placed on any face.
 */
public class CreativePowerBlock extends Block implements IBE<CreativePowerBlockEntity> {
    private final boolean source;

    public CreativePowerBlock(Properties properties, boolean source) {
        super(properties);
        this.source = source;
    }

    public boolean isSource() {
        return source;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            GridBlocks.removed(level, pos);
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<CreativePowerBlockEntity> getBlockEntityClass() {
        return CreativePowerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativePowerBlockEntity> getBlockEntityType() {
        return source ? DCBlockEntities.CREATIVE_POWER_SOURCE.get() : DCBlockEntities.CREATIVE_POWER_CONSUMER.get();
    }
}
