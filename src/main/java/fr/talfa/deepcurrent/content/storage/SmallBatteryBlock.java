package fr.talfa.deepcurrent.content.storage;

import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.content.grid.GridBlocks;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Small battery (GDD §4.7): stores the grid's surplus and covers its deficits. Its gauge shows the charge in four
 * cells; cables reach it through a connector placed on any face. It keeps its charge when broken.
 */
public class SmallBatteryBlock extends Block implements IBE<SmallBatteryBlockEntity> {
    public static final int GAUGE_CELLS = 4;
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, GAUGE_CELLS);
    // Base, cell and lid, terminals (see tools/textures/battery_art.py).
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 2, 15),
            Block.box(2, 2, 2, 14, 14, 14),
            Block.box(4, 14, 7, 12, 16, 9));

    public SmallBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            withBlockEntityDo(level, pos, battery -> battery.setStoredKj(BatteryCharge.read(stack)));
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getBlockEntityOptional(level, pos).map(SmallBatteryBlockEntity::comparatorSignal).orElse(0);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            GridBlocks.removed(level, pos);
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<SmallBatteryBlockEntity> getBlockEntityClass() {
        return SmallBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmallBatteryBlockEntity> getBlockEntityType() {
        return DCBlockEntities.SMALL_BATTERY.get();
    }
}
