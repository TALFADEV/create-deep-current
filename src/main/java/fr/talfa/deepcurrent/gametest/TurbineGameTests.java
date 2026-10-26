package fr.talfa.deepcurrent.gametest;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.content.turbine.SmallTidalTurbineBlock;
import fr.talfa.deepcurrent.content.turbine.SmallTidalTurbineBlockEntity;
import fr.talfa.deepcurrent.registry.DCBlocks;
import fr.talfa.deepcurrent.world.Tides;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

//? if neoforge {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}

/**
 * Small tidal turbine in a real level. Test worlds are superflat (sea level -63) with no ocean: the immersed test
 * builds its pool on the bottom layer, the only one below sea level, and forces the current and the tide.
 */
@GameTestHolder(DeepCurrent.ID)
@PrefixGameTestTemplate(false)
public final class TurbineGameTests {
    private static final BlockPos TURBINE = new BlockPos(4, 1, 2);

    private TurbineGameTests() {
    }

    private static BlockState turbine() {
        return DCBlocks.SMALL_TIDAL_TURBINE.getDefaultState()
                .setValue(SmallTidalTurbineBlock.HORIZONTAL_FACING, Direction.NORTH);
    }

    @GameTest(template = "platform")
    public static void dryTurbineStaysStill(GameTestHelper helper) {
        helper.setBlock(TURBINE, turbine());
        helper.runAfterDelay(1, () -> {
            if (!(helper.getBlockEntity(TURBINE) instanceof SmallTidalTurbineBlockEntity turbine)) {
                helper.fail("no turbine block entity", TURBINE);
                return;
            }
            turbine.refresh();
            helper.assertFalse(turbine.isImmersed(), "a turbine in the air is not immersed");
            helper.assertTrue(turbine.rpm() == 0, "a dry turbine should not turn");
            helper.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void immersedTurbineFollowsCurrentAndTide(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos column = helper.absolutePos(TURBINE);
        BlockPos pos = new BlockPos(column.getX(), level.getMinBuildHeight(), column.getZ());
        helper.assertTrue(pos.getY() < level.getSeaLevel(), "the bottom layer should be below sea level");
        level.setBlockAndUpdate(pos, turbine().setValue(SmallTidalTurbineBlock.WATERLOGGED, true));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(pos.relative(side), Blocks.WATER.defaultBlockState());
        }
        for (int dy = 1; dy <= 3; dy++) {
            level.setBlockAndUpdate(pos.above(dy), Blocks.WATER.defaultBlockState());
        }
        helper.runAfterDelay(1, () -> {
            if (!(level.getBlockEntity(pos) instanceof SmallTidalTurbineBlockEntity turbine)) {
                helper.fail("no turbine block entity at " + pos);
                return;
            }
            try {
                // Reference current (2 m/s): 32 RPM at a coefficient of 1, half at 0.5.
                Tides.forceCurrent(2);
                Tides.force(1);
                turbine.refresh();
                helper.assertTrue(turbine.isImmersed(), "the turbine should be immersed");
                helper.assertTrue(turbine.rpm() == 32, "expected 32 RPM, got " + turbine.rpm());
                helper.assertTrue(Math.abs(turbine.getGeneratedSpeed()) == 32, "the generated speed should be 32");
                Tides.force(0.5);
                turbine.refresh();
                helper.assertTrue(turbine.rpm() == 16, "expected 16 RPM at half tide, got " + turbine.rpm());
                Tides.forceCurrent(0);
                turbine.refresh();
                helper.assertTrue(turbine.rpm() == 0, "no current, no rotation");
            } finally {
                Tides.release();
                Tides.forceCurrent(Double.NaN);
            }
            helper.succeed();
        });
    }
}
