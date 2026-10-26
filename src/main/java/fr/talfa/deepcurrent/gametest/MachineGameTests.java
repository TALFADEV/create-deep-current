package fr.talfa.deepcurrent.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.CreativePowerBlockEntity;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.content.machines.AlternatorBlock;
import fr.talfa.deepcurrent.content.machines.AlternatorBlockEntity;
import fr.talfa.deepcurrent.content.machines.ElectricMotorBlockEntity;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.registry.DCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

//? if neoforge {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}

/**
 * Alternator and electric motor on real Create kinetic networks. Machines sit in a row along x with their
 * connector on top; a cable joins the two connectors.
 */
@GameTestHolder(DeepCurrent.ID)
@PrefixGameTestTemplate(false)
public final class MachineGameTests {
    private MachineGameTests() {
    }

    private static BlockState facing(BlockState state, Direction direction) {
        return state.setValue(DirectionalKineticBlock.FACING, direction);
    }

    /** The alternator lies on a horizontal axis with a through shaft. */
    private static BlockState alongX(BlockState state) {
        return state.setValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, Direction.Axis.X);
    }

    private static BlockState connector() {
        return DCBlocks.LV_CONNECTOR.getDefaultState().setValue(ConnectorBlock.FACING, Direction.UP);
    }

    private static void link(GameTestHelper helper, BlockPos a, BlockPos b) {
        BlockPos absA = helper.absolutePos(a);
        BlockPos absB = helper.absolutePos(b);
        LevelGrid grid = LevelGrid.get(helper.getLevel());
        if (grid.hasNode(absA) && grid.hasNode(absB) && !grid.linked(absA, absB)) {
            grid.link(absA, absB, Math.sqrt(absA.distSqr(absB)), VoltageClass.LV, 0);
        }
    }

    /** Create's creative motor turns the alternator, which feeds a creative load through a cable. */
    @GameTest(template = "platform")
    public static void alternatorFeedsTheGrid(GameTestHelper helper) {
        BlockPos motor = new BlockPos(1, 1, 1);
        BlockPos alternator = new BlockPos(2, 1, 1);
        BlockPos load = new BlockPos(6, 1, 1);
        helper.setBlock(motor, facing(AllBlocks.CREATIVE_MOTOR.getDefaultState(), Direction.EAST));
        helper.setBlock(alternator, alongX(DCBlocks.ALTERNATOR.getDefaultState()));
        helper.setBlock(alternator.above(), connector());
        helper.setBlock(load, DCBlocks.CREATIVE_POWER_CONSUMER.getDefaultState());
        helper.setBlock(load.above(), connector());
        helper.runAfterDelay(1, () -> {
            if (helper.getBlockEntity(motor) instanceof CreativeMotorBlockEntity creative) {
                creative.generatedSpeed.setValue(256);
            }
            if (helper.getBlockEntity(load) instanceof CreativePowerBlockEntity consumer) {
                consumer.setSettingKw(48);
            }
        });
        helper.succeedWhen(() -> {
            link(helper, alternator.above(), load.above());
            AlternatorBlockEntity machine = (AlternatorBlockEntity) helper.getBlockEntity(alternator);
            double expected = 64 * 256 / 256.0 * 0.9;
            helper.assertTrue(Math.abs(Math.abs(machine.getSpeed()) - 256) < 1e-3,
                    "alternator should turn at 256 RPM, got " + machine.getSpeed());
            helper.assertTrue(Math.abs(machine.currentOfferKw() - expected) < 1e-3,
                    "alternator should offer " + expected + " kW, got " + machine.currentOfferKw());
            LevelGrid grid = LevelGrid.get(helper.getLevel());
            BlockPos absLoad = helper.absolutePos(load);
            helper.assertTrue(grid.hasNode(absLoad) && grid.linked(helper.absolutePos(alternator.above()),
                    helper.absolutePos(load.above())), "cable not laid yet");
            GridSolution solution = grid.solution(absLoad);
            helper.assertTrue(Math.abs(solution.satisfaction(absLoad.asLong()) - 1) < 1e-6,
                    "48 kW load should be fully supplied by a " + expected + " kW alternator");
            helper.assertTrue(helper.getBlockState(alternator).getValue(AlternatorBlock.ACTIVE),
                    "a producing alternator should be lit");
        });
    }

    /**
     * The electric motor turns an alternator used as a kinetic load: with power it runs, without it the line
     * overstresses.
     */
    @GameTest(template = "platform", timeoutTicks = 200)
    public static void motorRunsOnGridPowerAndOverstressesWithout(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 1);
        BlockPos motor = new BlockPos(5, 1, 1);
        BlockPos kineticLoad = new BlockPos(6, 1, 1);
        helper.setBlock(source, DCBlocks.CREATIVE_POWER_SOURCE.getDefaultState());
        helper.setBlock(source.above(), connector());
        helper.setBlock(motor, facing(DCBlocks.ELECTRIC_MOTOR.getDefaultState(), Direction.EAST));
        helper.setBlock(motor.above(), connector());
        helper.setBlock(kineticLoad, alongX(DCBlocks.ALTERNATOR.getDefaultState()));
        helper.runAfterDelay(1, () -> {
            ((CreativePowerBlockEntity) helper.getBlockEntity(source)).setSettingKw(1024);
            link(helper, source.above(), motor.above());
        });

        helper.startSequence()
                .thenWaitUntil(() -> {
                    link(helper, source.above(), motor.above());
                    ElectricMotorBlockEntity electric = (ElectricMotorBlockEntity) helper.getBlockEntity(motor);
                    AlternatorBlockEntity load = (AlternatorBlockEntity) helper.getBlockEntity(kineticLoad);
                    // 64 SU/RPM at 32 RPM = 2048 SU = 8 kW.
                    helper.assertTrue(Math.abs(electric.demandKw() - 8) < 0.05,
                            "motor should ask for 8 kW, asks " + electric.demandKw());
                    helper.assertTrue(electric.satisfaction() > 0.999, "motor should be fully supplied");
                    helper.assertFalse(load.isOverStressed(), "a supplied motor should carry its load");
                })
                .thenExecute(() -> ((CreativePowerBlockEntity) helper.getBlockEntity(source)).setSettingKw(0))
                .thenWaitUntil(() -> {
                    AlternatorBlockEntity load = (AlternatorBlockEntity) helper.getBlockEntity(kineticLoad);
                    helper.assertTrue(load.isOverStressed(), "without power the line should overstress");
                })
                .thenSucceed();
    }
}
