package fr.talfa.deepcurrent.content.machines;

import java.util.List;

import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.config.DCServerConfig;
import fr.talfa.deepcurrent.content.grid.GridNodeBehaviour;
import fr.talfa.deepcurrent.content.grid.GridParticipant;
import fr.talfa.deepcurrent.content.grid.LevelGrid;
import fr.talfa.deepcurrent.content.grid.NetworkSummary;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.PowerConversion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electric motor: a rotation source with a speed set like Create's creative motor. It asks the grid for the
 * electricity matching the stress its kinetic network actually uses; when the grid falls short, its capacity
 * drops to what the delivered power pays for, and the machines behind it overstress like with any weak source.
 */
public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity implements GridParticipant {
    public static final int MAX_SPEED = 256;
    public static final int DEFAULT_SPEED = 32;
    public static final double DEFAULT_CAPACITY = 64;
    private static final double DEMAND_STEP_KW = 0.01;

    private ScrollValueBehaviour speedSetting;
    private GridNodeBehaviour node;
    /** Capacity per RPM currently offered; negative until the grid has answered (full capacity). */
    private double capacityPerRpm = -1;
    private double demandKw;
    /** Power received and fraction of the demand met, synced for the goggles. */
    private double receivedKw;
    private double satisfaction = 1;

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static double maxCapacityPerRpm() {
        return DCServerConfig.orDefault(DCConfigs.server().motorStressCapacity, DEFAULT_CAPACITY);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        speedSetting = new KineticScrollValueBehaviour(Component.translatable("deepcurrent.electric_motor.speed"),
                this, new CenteredSideValueBoxTransform((state, side) ->
                        side.getAxis() != state.getValue(ElectricMachineBlock.FACING).getAxis()));
        speedSetting.between(-MAX_SPEED, MAX_SPEED);
        speedSetting.value = DEFAULT_SPEED;
        speedSetting.withCallback(value -> updateGeneratedRotation());
        behaviours.add(speedSetting);
        node = new GridNodeBehaviour(this);
        behaviours.add(node);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed()) {
            updateGeneratedRotation();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        if (!(getBlockState().getBlock() instanceof ElectricMotorBlock)) {
            return 0;
        }
        return convertToDirection(speedSetting.getValue(), getBlockState().getValue(ElectricMachineBlock.FACING));
    }

    /** Sets the speed, in RPM, from -256 to 256. */
    public void setSpeedSetting(int rpm) {
        speedSetting.setValue(rpm);
    }

    @Override
    public float calculateAddedStressCapacity() {
        double max = maxCapacityPerRpm();
        float capacity = (float) (capacityPerRpm < 0 ? max : Math.min(capacityPerRpm, max));
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public NodeRole gridRole() {
        return NodeRole.CONSUMER;
    }

    @Override
    public void pushValues(LevelGrid grid) {
        grid.graph().setDemand(worldPosition.asLong(), demandKw);
    }

    @Override
    public void clearValues(LevelGrid grid) {
        grid.graph().setDemand(worldPosition.asLong(), 0);
    }

    @Override
    public boolean pullValues(LevelGrid grid) {
        double stressPerKw = DCConfigs.server().stressPerKilowatt.get();
        double max = maxCapacityPerRpm();
        double rpm = Math.abs(getSpeed());

        double demand = 0;
        if (hasNetwork() && rpm > 0) {
            KineticNetwork network = getOrCreateNetwork();
            double others = network.calculateCapacity() - network.getActualCapacityOf(this);
            demand = PowerConversion.motorDemandKw(network.calculateStress(), others, max * rpm, stressPerKw);
        }
        boolean changed = false;
        if (Math.abs(demand - demandKw) > DEMAND_STEP_KW) {
            demandKw = demand;
            pushValues(grid);
            changed = true;
        }

        long id = worldPosition.asLong();
        GridSolution solution = grid.solution(worldPosition);
        double served = demandKw > 0 ? solution.satisfaction(id) : 1;
        double received = Math.max(0, -solution.flowKw(id));
        double capacity = PowerConversion.motorCapacityPerRpm(max, getSpeed(), demandKw, served, stressPerKw);
        double current = capacityPerRpm < 0 ? max : capacityPerRpm;
        if (Math.abs(capacity - current) > max * 0.01) {
            capacityPerRpm = capacity;
            notifyStressCapacityChange((float) capacity);
            changed = true;
        }
        if (Math.abs(received - receivedKw) > 0.05 || Math.abs(served - satisfaction) > 0.001) {
            receivedKw = received;
            satisfaction = served;
            changed = true;
        }
        return changed;
    }

    public double demandKw() {
        return demandKw;
    }

    public double satisfaction() {
        return satisfaction;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.electric_motor.title")
                .withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.electric_motor.input",
                NetworkSummary.number(receivedKw), NetworkSummary.number(demandKw)).withStyle(ChatFormatting.GRAY)));
        if (demandKw > 0 && satisfaction < 0.999) {
            tooltip.add(Component.literal("    ").append(Component.translatable("deepcurrent.electric_motor.short",
                    NetworkSummary.number(satisfaction * 100)).withStyle(ChatFormatting.GOLD)));
        }
        return true;
    }

    @Override
    public void writeGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            tag.putDouble("DemandKw", demandKw);
            tag.putDouble("ReceivedKw", receivedKw);
            tag.putDouble("Satisfaction", satisfaction);
        }
    }

    @Override
    public void readGridData(CompoundTag tag, boolean clientPacket) {
        if (clientPacket) {
            demandKw = tag.getDouble("DemandKw");
            receivedKw = tag.getDouble("ReceivedKw");
            satisfaction = tag.getDouble("Satisfaction");
        }
    }
}
