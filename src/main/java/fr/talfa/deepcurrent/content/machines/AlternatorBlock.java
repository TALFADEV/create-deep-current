package fr.talfa.deepcurrent.content.machines;

import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.content.grid.GridBlocks;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Turns rotation into electricity (GDD §4.1). A lying machine with a through shaft on a horizontal axis, so
 * several alternators chain on one line; its rotor spins with the shaft and its slots glow while it produces.
 */
public class AlternatorBlock extends HorizontalAxisKineticBlock implements IBE<AlternatorBlockEntity> {
    /** True while the alternator injects power: lit model and a little light. */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final int ACTIVE_LIGHT = 5;

    public AlternatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            GridBlocks.removed(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<AlternatorBlockEntity> getBlockEntityClass() {
        return AlternatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AlternatorBlockEntity> getBlockEntityType() {
        return DCBlockEntities.ALTERNATOR.get();
    }
}
