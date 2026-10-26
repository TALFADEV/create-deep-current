package fr.talfa.deepcurrent.gametest;

import java.util.List;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.content.storage.BatteryCharge;
import fr.talfa.deepcurrent.content.storage.SmallBatteryBlock;
import fr.talfa.deepcurrent.content.storage.SmallBatteryBlockEntity;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.registry.DCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

//? if neoforge {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}

/**
 * Small battery on a real grid: a creative source or load and a battery, each with a connector on top, joined by
 * a cable four blocks long.
 */
@GameTestHolder(DeepCurrent.ID)
@PrefixGameTestTemplate(false)
public final class StorageGameTests {
    private static final BlockPos CREATIVE = new BlockPos(1, 1, 1);
    private static final BlockPos BATTERY = new BlockPos(5, 1, 1);

    private StorageGameTests() {
    }

    /** Places the creative block (source or load) and the battery with their connectors, then lays the cable. */
    private static void build(GameTestHelper helper, boolean source, int kw, double storedFraction) {
        helper.setBlock(CREATIVE, source ? DCBlocks.CREATIVE_POWER_SOURCE.getDefaultState()
                : DCBlocks.CREATIVE_POWER_CONSUMER.getDefaultState());
        helper.setBlock(BATTERY, DCBlocks.SMALL_BATTERY.getDefaultState());
        helper.setBlock(CREATIVE.above(), connector());
        helper.setBlock(BATTERY.above(), connector());
        helper.runAfterDelay(1, () -> {
            if (helper.getBlockEntity(CREATIVE) instanceof CreativePowerBlockEntity creative) {
                creative.setSettingKw(kw);
            }
            battery(helper).setStoredKj(SmallBatteryBlockEntity.spec().capacityKj() * storedFraction);
        });
        helper.runAfterDelay(2, () -> {
            BlockPos a = helper.absolutePos(CREATIVE.above());
            BlockPos b = helper.absolutePos(BATTERY.above());
            LevelGrid.get(helper.getLevel()).link(a, b, Math.sqrt(a.distSqr(b)), VoltageClass.LV, 0);
        });
    }

    private static BlockState connector() {
        return DCBlocks.LV_CONNECTOR.getDefaultState().setValue(ConnectorBlock.FACING, Direction.UP);
    }

    private static SmallBatteryBlockEntity battery(GameTestHelper helper) {
        if (helper.getBlockEntity(BATTERY) instanceof SmallBatteryBlockEntity battery) {
            return battery;
        }
        helper.fail("no battery block entity", BATTERY);
        throw new IllegalStateException();
    }

    /** Fails the current attempt (succeedWhen retries) until the cable joins both blocks. */
    private static GridSolution linkedSolution(GameTestHelper helper) {
        LevelGrid grid = LevelGrid.get(helper.getLevel());
        BlockPos battery = helper.absolutePos(BATTERY);
        helper.assertTrue(grid.hasNode(battery) && grid.hasNode(helper.absolutePos(CREATIVE)), "nodes not ready");
        helper.assertTrue(grid.linked(helper.absolutePos(CREATIVE.above()), helper.absolutePos(BATTERY.above())),
                "cable not laid yet");
        return grid.solution(battery);
    }

    @GameTest(template = "platform")
    public static void batteryChargesFromSurplus(GameTestHelper helper) {
        build(helper, true, 16, 0);
        helper.succeedWhen(() -> {
            linkedSolution(helper);
            SmallBatteryBlockEntity battery = battery(helper);
            helper.assertTrue(battery.flowKw() < -15, "the battery should draw about 16 kW, got " + battery.flowKw());
            helper.assertTrue(battery.storedKj() > 0, "the battery should store energy");
        });
    }

    @GameTest(template = "platform")
    public static void gaugeFollowsTheCharge(GameTestHelper helper) {
        // Just under a quarter: one lit cell, then the second one as it charges past 25 %.
        double quarter = SmallBatteryBlockEntity.spec().capacityKj() / 4;
        build(helper, true, 16, (quarter - 1) / SmallBatteryBlockEntity.spec().capacityKj());
        helper.succeedWhen(() -> {
            linkedSolution(helper);
            int lit = helper.getBlockState(BATTERY).getValue(SmallBatteryBlock.CHARGE);
            helper.assertTrue(lit == 2, "two gauge cells should be lit, got " + lit);
        });
    }

    @GameTest(template = "platform")
    public static void fullBatteryStopsCharging(GameTestHelper helper) {
        build(helper, true, 16, 1);
        helper.succeedWhen(() -> {
            GridSolution solution = linkedSolution(helper);
            helper.assertTrue(solution.producedKw() == 0, "nothing should be produced for a full battery");
            helper.assertTrue(battery(helper).flowKw() == 0, "a full battery should not charge");
            helper.assertTrue(helper.getBlockState(BATTERY).getValue(SmallBatteryBlock.CHARGE) == 4,
                    "all four gauge cells should be lit");
        });
    }

    @GameTest(template = "platform")
    public static void batteryCoversADeficit(GameTestHelper helper) {
        build(helper, false, 8, 1);
        helper.succeedWhen(() -> {
            GridSolution solution = linkedSolution(helper);
            long load = helper.absolutePos(CREATIVE).asLong();
            helper.assertTrue(Math.abs(solution.satisfaction(load) - 1) < 1e-6, "the load should be fully supplied");
            SmallBatteryBlockEntity battery = battery(helper);
            helper.assertTrue(battery.flowKw() > 8, "the battery should supply 8 kW plus line losses");
            helper.assertTrue(battery.storedKj() < SmallBatteryBlockEntity.spec().capacityKj(),
                    "the battery should lose charge");
        });
    }

    @GameTest(template = "platform")
    public static void comparatorReadsTheCharge(GameTestHelper helper) {
        helper.setBlock(BATTERY, DCBlocks.SMALL_BATTERY.getDefaultState());
        helper.runAfterDelay(1, () -> {
            battery(helper).setStoredKj(SmallBatteryBlockEntity.spec().capacityKj() / 2);
            BlockPos pos = helper.absolutePos(BATTERY);
            int signal = helper.getBlockState(BATTERY).getAnalogOutputSignal(helper.getLevel(), pos);
            helper.assertTrue(signal == 8, "half charge should give a signal of 8, got " + signal);
            helper.succeed();
        });
    }

    @GameTest(template = "platform")
    public static void brokenBatteryKeepsItsCharge(GameTestHelper helper) {
        helper.setBlock(BATTERY, DCBlocks.SMALL_BATTERY.getDefaultState());
        helper.runAfterDelay(1, () -> {
            battery(helper).setStoredKj(3000);
            BlockPos pos = helper.absolutePos(BATTERY);
            helper.getLevel().destroyBlock(pos, true);
        });
        helper.runAfterDelay(3, () -> {
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(helper.absolutePos(BATTERY)).inflate(2));
            helper.assertTrue(drops.size() == 1, "the battery should drop itself, got " + drops.size() + " items");
            ItemStack stack = drops.get(0).getItem();
            helper.assertTrue(stack.is(DCBlocks.SMALL_BATTERY.asItem()), "the drop should be the battery");
            double carried = BatteryCharge.read(stack);
            helper.assertTrue(Math.abs(carried - 3000) < 1e-6, "the item should carry 3000 kJ, got " + carried);

            // Placing it again gives the charge back.
            BlockPos again = new BlockPos(3, 1, 2);
            helper.setBlock(again, DCBlocks.SMALL_BATTERY.getDefaultState());
            BlockPos absolute = helper.absolutePos(again);
            DCBlocks.SMALL_BATTERY.get().setPlacedBy(helper.getLevel(), absolute, helper.getBlockState(again),
                    null, stack);
            if (helper.getBlockEntity(again) instanceof SmallBatteryBlockEntity placed) {
                helper.assertTrue(Math.abs(placed.storedKj() - 3000) < 1e-6, "the placed battery should hold 3000 kJ");
            } else {
                helper.fail("no battery block entity", again);
            }
            helper.succeed();
        });
    }
}
