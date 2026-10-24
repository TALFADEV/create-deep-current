package fr.talfa.deepcurrent.gametest;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.content.grid.CableCoilItem;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.registry.DCBlocks;
import fr.talfa.deepcurrent.registry.DCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

//? if neoforge {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}

//? if >=1.21 {
import net.minecraft.world.level.GameType;
//?}

/**
 * Grid in a real level: a creative source and load, each with a connector on top, four blocks apart.
 */
@GameTestHolder(DeepCurrent.ID)
@PrefixGameTestTemplate(false)
public final class GridGameTests {
    private static final BlockPos SOURCE = new BlockPos(1, 1, 1);
    private static final BlockPos LOAD = new BlockPos(5, 1, 1);
    private static final BlockPos SOURCE_CONNECTOR = SOURCE.above();
    private static final BlockPos LOAD_CONNECTOR = LOAD.above();

    private GridGameTests() {
    }

    /** Places both machines with their connectors; block entities register their nodes on their first tick. */
    private static void build(GameTestHelper helper, int sourceKw, int loadKw) {
        helper.setBlock(SOURCE, DCBlocks.CREATIVE_POWER_SOURCE.getDefaultState());
        helper.setBlock(LOAD, DCBlocks.CREATIVE_POWER_CONSUMER.getDefaultState());
        helper.setBlock(SOURCE_CONNECTOR, connector());
        helper.setBlock(LOAD_CONNECTOR, connector());
        helper.runAfterDelay(1, () -> {
            setPower(helper, SOURCE, sourceKw);
            setPower(helper, LOAD, loadKw);
        });
    }

    private static BlockState connector() {
        return DCBlocks.LV_CONNECTOR.getDefaultState().setValue(ConnectorBlock.FACING, Direction.UP);
    }

    private static void setPower(GameTestHelper helper, BlockPos pos, int kw) {
        if (helper.getBlockEntity(pos) instanceof CreativePowerBlockEntity machine) {
            machine.setSettingKw(kw);
        } else {
            helper.fail("no creative power block entity", pos);
        }
    }

    private static LevelGrid grid(GameTestHelper helper) {
        return LevelGrid.get(helper.getLevel());
    }

    /** Fails the current attempt (succeedWhen retries) until every block has registered its node and the cable. */
    private static LevelGrid linkedGrid(GameTestHelper helper) {
        LevelGrid grid = grid(helper);
        for (BlockPos pos : new BlockPos[] {SOURCE, LOAD, SOURCE_CONNECTOR, LOAD_CONNECTOR}) {
            helper.assertTrue(grid.hasNode(helper.absolutePos(pos)), "node not registered yet at " + pos);
        }
        helper.assertTrue(grid.linked(helper.absolutePos(SOURCE_CONNECTOR), helper.absolutePos(LOAD_CONNECTOR)),
                "cable not laid yet");
        return grid;
    }

    private static void link(GameTestHelper helper, int cost) {
        BlockPos a = helper.absolutePos(SOURCE_CONNECTOR);
        BlockPos b = helper.absolutePos(LOAD_CONNECTOR);
        grid(helper).link(a, b, Math.sqrt(a.distSqr(b)), VoltageClass.LV, cost);
    }

    @GameTest(template = "platform")
    public static void connectorsBondToMachinesAndCarryPower(GameTestHelper helper) {
        build(helper, 104, 48);
        helper.runAfterDelay(2, () -> link(helper, 0));
        helper.succeedWhen(() -> {
            LevelGrid grid = linkedGrid(helper);
            BlockPos load = helper.absolutePos(LOAD);
            helper.assertTrue(grid.role(helper.absolutePos(SOURCE)) == NodeRole.PRODUCER, "source not registered");
            helper.assertTrue(grid.network(load) == grid.network(helper.absolutePos(SOURCE)),
                    "source and load should share a network");
            GridSolution solution = grid.solution(load);
            helper.assertTrue(Math.abs(solution.satisfaction(load.asLong()) - 1) < 1e-6,
                    "load should be fully supplied, got " + solution.satisfaction(load.asLong()));
            helper.assertTrue(Math.abs(solution.deliveredKw() - 48) < 1e-6, "48 kW should be delivered");
            helper.assertTrue(solution.lossesKw() > 0, "a 4-block cable should lose a little power");
        });
    }

    @GameTest(template = "platform")
    public static void overloadedCableIsCapped(GameTestHelper helper) {
        build(helper, 1000, 1000);
        helper.runAfterDelay(2, () -> link(helper, 0));
        helper.succeedWhen(() -> {
            GridSolution solution = linkedGrid(helper).solution(helper.absolutePos(LOAD));
            double capacity = DCConfigs.server().gridSettings().spec(VoltageClass.LV).maxPowerKw();
            helper.assertTrue(solution.isOverloaded(), "the LV cable should be overloaded");
            helper.assertTrue(solution.deliveredKw() < capacity + 1e-6, "delivered power should be capped");
        });
    }

    @GameTest(template = "platform")
    public static void breakingAConnectorGivesBackItsCable(GameTestHelper helper) {
        build(helper, 10, 10);
        helper.runAfterDelay(2, () -> {
            link(helper, 3);
            helper.destroyBlock(LOAD_CONNECTOR);
        });
        helper.runAfterDelay(4, () -> {
            LevelGrid grid = grid(helper);
            helper.assertFalse(grid.hasNode(helper.absolutePos(LOAD_CONNECTOR)), "connector node should be gone");
            helper.assertTrue(grid.cablesOf(helper.absolutePos(SOURCE_CONNECTOR)).stream()
                    .noneMatch(cable -> !cable.bond()), "the cable should be gone");
            helper.assertItemEntityCountIs(DCItems.LV_CABLE_COIL.get(), LOAD_CONNECTOR, 2, 3);
            helper.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void gridSurvivesSaveAndLoad(GameTestHelper helper) {
        build(helper, 104, 48);
        helper.runAfterDelay(2, () -> link(helper, 2));
        helper.runAfterDelay(3, () -> {
            LevelGrid copy = grid(helper).reloadedCopy();
            BlockPos a = helper.absolutePos(SOURCE_CONNECTOR);
            BlockPos b = helper.absolutePos(LOAD_CONNECTOR);
            helper.assertTrue(copy.linked(a, b), "cable should survive a save");
            helper.assertTrue(copy.role(helper.absolutePos(LOAD)) == NodeRole.CONSUMER, "role should survive");
            helper.assertTrue(copy.network(a) == copy.network(helper.absolutePos(LOAD)),
                    "bonds should survive a save");
            helper.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void coilLaysACableAndConsumesCoils(GameTestHelper helper) {
        build(helper, 10, 10);
        helper.runAfterDelay(2, () -> {
            //? if >=1.21 {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            //?} else
            /*Player player = helper.makeMockPlayer();*/
            CableCoilItem coil = DCItems.LV_CABLE_COIL.get();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(coil, 10));
            click(helper, player, SOURCE_CONNECTOR);
            click(helper, player, LOAD_CONNECTOR);

            BlockPos a = helper.absolutePos(SOURCE_CONNECTOR);
            BlockPos b = helper.absolutePos(LOAD_CONNECTOR);
            helper.assertTrue(grid(helper).linked(a, b), "the coil should have laid a cable");
            int cost = CableCoilItem.cost(Math.sqrt(a.distSqr(b)), DCConfigs.server());
            int left = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
            helper.assertTrue(left == 10 - cost, "expected " + (10 - cost) + " coils left, got " + left);
            helper.succeed();
        });
    }

    private static void click(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        player.getItemInHand(InteractionHand.MAIN_HAND).useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
