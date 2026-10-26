package fr.talfa.deepcurrent.content.grid;

import java.util.EnumMap;
import java.util.Map;

import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cable connector: an insulator placed on a face. Cables run between connectors; a connector placed on a grid
 * machine is bonded to it. Waterloggable, since most of them end up under the sea.
 */
public class ConnectorBlock extends Block implements IBE<ConnectorBlockEntity>, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /** Height of the cable anchor above the supporting face, in pixels. */
    private static final double ANCHOR_HEIGHT = 11;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            // Wall plate and collar, insulator discs, clamp and lug (see tools/textures/network_art.py).
            SHAPES.put(facing, Shapes.or(
                    box(facing, 4, 0, 4, 12, 3, 12),
                    box(facing, 4.5, 3, 4.5, 11.5, 10, 11.5),
                    box(facing, 6, 10, 6, 10, 12, 10)));
        }
    }

    private final VoltageClass voltage;

    public ConnectorBlock(Properties properties, VoltageClass voltage) {
        super(properties);
        this.voltage = voltage;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false));
    }

    public VoltageClass voltage() {
        return voltage;
    }

    /** Point where cables attach, in world coordinates. */
    public static Vec3 anchor(BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        double offset = (ANCHOR_HEIGHT - 8) / 16;
        return Vec3.atCenterOf(pos).add(facing.getStepX() * offset, facing.getStepY() * offset,
                facing.getStepZ() * offset);
    }

    /** The block this connector sits on. */
    public static BlockPos support(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, water);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            GridBlocks.removed(level, pos);
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public Class<ConnectorBlockEntity> getBlockEntityClass() {
        return ConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConnectorBlockEntity> getBlockEntityType() {
        return DCBlockEntities.CONNECTOR.get();
    }

    /** A box given for a connector facing up, turned to {@code facing}. Coordinates in pixels. */
    private static VoxelShape box(Direction facing, double x1, double y1, double z1, double x2, double y2,
                                  double z2) {
        double[] a = turn(facing, x1, y1, z1);
        double[] b = turn(facing, x2, y2, z2);
        return Block.box(Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]), Math.max(a[1], b[1]), Math.max(a[2], b[2]));
    }

    private static double[] turn(Direction facing, double x, double y, double z) {
        return switch (facing) {
            case UP -> new double[] {x, y, z};
            case DOWN -> new double[] {x, 16 - y, z};
            case NORTH -> new double[] {x, z, 16 - y};
            case SOUTH -> new double[] {x, z, y};
            case WEST -> new double[] {16 - y, x, z};
            case EAST -> new double[] {y, x, z};
        };
    }
}
