package fr.talfa.deepcurrent.content.turbine;

import java.util.EnumMap;
import java.util.Map;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Small tidal turbine (GDD §4.2): a rotation source placed under water. Its rotor faces {@code facing}, into the
 * current; its output shaft comes out of the opposite face. Waterloggable, since it only runs immersed.
 */
public class SmallTidalTurbineBlock extends HorizontalKineticBlock
        implements IBE<SmallTidalTurbineBlockEntity>, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        // Rotor disc, nacelle with its collar, foot and pylon, facing north (see tools/textures/turbine_art.py).
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            SHAPES.put(facing, Shapes.or(
                    box(facing, 0.5, 0.5, 0, 15.5, 15.5, 4),
                    box(facing, 4, 4, 4, 12, 12, 16),
                    box(facing, 3, 0, 5, 13, 5, 14)));
        }
    }

    public SmallTidalTurbineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED));
    }

    /** Rotor toward the player, unless a shaft next to it calls for the output (sneak to ignore it). */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction shaft = getPreferredHorizontalFacing(context);
        if (shaft != null && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            facing = shaft.getOpposite();
        }
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(HORIZONTAL_FACING, facing).setValue(WATERLOGGED, water);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public Class<SmallTidalTurbineBlockEntity> getBlockEntityClass() {
        return SmallTidalTurbineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmallTidalTurbineBlockEntity> getBlockEntityType() {
        return DCBlockEntities.SMALL_TIDAL_TURBINE.get();
    }

    /** A box given for a turbine facing north, turned to {@code facing}. Coordinates in pixels. */
    private static VoxelShape box(Direction facing, double x1, double y1, double z1, double x2, double y2,
                                  double z2) {
        double[] a = turn(facing, x1, z1);
        double[] b = turn(facing, x2, z2);
        return Block.box(Math.min(a[0], b[0]), y1, Math.min(a[1], b[1]), Math.max(a[0], b[0]), y2,
                Math.max(a[1], b[1]));
    }

    private static double[] turn(Direction facing, double x, double z) {
        return switch (facing) {
            case SOUTH -> new double[] {16 - x, 16 - z};
            case EAST -> new double[] {16 - z, x};
            case WEST -> new double[] {z, 16 - x};
            default -> new double[] {x, z};
        };
    }
}
