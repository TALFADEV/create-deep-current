package fr.talfa.deepcurrent.content.machines;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import fr.talfa.deepcurrent.content.grid.GridBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A machine turning between rotation and electricity: one shaft on its front face (like Create's creative motor),
 * cables through a connector placed on any other face.
 */
public abstract class ElectricMachineBlock extends DirectionalKineticBlock {
    protected ElectricMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null) {
            return super.getStateForPlacement(context);
        }
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            GridBlocks.removed(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
