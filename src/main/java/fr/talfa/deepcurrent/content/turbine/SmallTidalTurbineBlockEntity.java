package fr.talfa.deepcurrent.content.turbine;

import java.util.List;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import fr.talfa.deepcurrent.config.DCClientConfig.ParticleLevel;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.content.grid.NetworkSummary;
import fr.talfa.deepcurrent.core.Depth;
import fr.talfa.deepcurrent.core.turbine.TidalTurbine;
import fr.talfa.deepcurrent.core.turbine.TurbineSpec;
import fr.talfa.deepcurrent.world.Tides;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

/**
 * Small tidal turbine: a rotation source whose speed follows the local current and the tide, with a fixed
 * stress capacity per RPM. It checks its surroundings and the tide every few seconds, not every tick, and only
 * updates its kinetic network when its whole-RPM speed changes.
 */
public class SmallTidalTurbineBlockEntity extends GeneratingKineticBlockEntity {
    /** Ticks between two checks of the water, current and tide. */
    public static final int UPDATE_TICKS = 100;

    private int timer;
    private int rpm;
    /** What the last check found, synced for the goggles and the bubbles. */
    private boolean immersed;
    private int wetFaces;
    private int waterAbove;
    private double currentSpeed;
    private double tide;

    public SmallTidalTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static TurbineSpec spec() {
        return DCConfigs.server().smallTurbineSpec();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed()) {
            updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            spawnBubbles();
        } else if (--timer <= 0) {
            timer = UPDATE_TICKS;
            refresh();
        }
    }

    /** Checks the water, the current and the tide now, and sets the speed they give. */
    public void refresh() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        TurbineSpec spec = spec();
        wetFaces = 0;
        for (Direction side : Direction.values()) {
            if (isWater(worldPosition.relative(side))) {
                wetFaces++;
            }
        }
        waterAbove = 0;
        while (waterAbove < spec.minWaterAbove() && isWater(worldPosition.above(waterAbove + 1))) {
            waterAbove++;
        }
        int depth = Depth.belowSeaLevel(server.getSeaLevel(), worldPosition.getY());
        immersed = TidalTurbine.isImmersed(spec, depth, wetFaces, waterAbove);
        currentSpeed = Tides.currentSpeed(server, worldPosition);
        tide = Tides.coefficient(server);
        // Efficiency stays 1 until pressure damage (milestone 6).
        int speed = immersed ? TidalTurbine.speed(spec, Tides.currentFactor(server, worldPosition), tide, 1) : 0;
        if (speed != rpm) {
            rpm = speed;
            updateGeneratedRotation();
        }
        sendData();
    }

    private boolean isWater(BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    public int rpm() {
        return rpm;
    }

    public boolean isImmersed() {
        return immersed;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!(getBlockState().getBlock() instanceof SmallTidalTurbineBlock)) {
            return 0;
        }
        return convertToDirection(rpm, getBlockState().getValue(SmallTidalTurbineBlock.HORIZONTAL_FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = (float) spec().capacityPerRpm();
        lastCapacityProvided = capacity;
        return capacity;
    }

    /** Bubbles shed by the blades, more as the rotor turns faster; none when particles are off. */
    private void spawnBubbles() {
        ParticleLevel setting = DCConfigs.client().particles.get();
        if (rpm == 0 || !immersed || setting == ParticleLevel.OFF) {
            return;
        }
        RandomSource random = level.random;
        double chance = Math.min(1, rpm / 48.0) * (setting == ParticleLevel.REDUCED ? 0.3 : 1);
        if (random.nextDouble() > chance) {
            return;
        }
        Direction facing = getBlockState().getValue(SmallTidalTurbineBlock.HORIZONTAL_FACING);
        // A point on the blades' circle, in the plane of the rotor (2.5 px behind the front face).
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 0.2 + random.nextDouble() * 0.25;
        double across = Math.cos(angle) * radius;
        double up = Math.sin(angle) * radius;
        double front = 0.5 - 2.5 / 16;
        boolean alongZ = facing.getAxis() == Direction.Axis.Z;
        double x = worldPosition.getX() + 0.5 + facing.getStepX() * front + (alongZ ? across : 0);
        double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * front + (alongZ ? 0 : across);
        double y = worldPosition.getY() + 0.5 + up;
        level.addParticle(ParticleTypes.BUBBLE, x, y, z, facing.getStepX() * 0.05, 0.02, facing.getStepZ() * 0.05);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        TurbineSpec spec = spec();
        tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.title"), ChatFormatting.WHITE));
        if (!immersed) {
            tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.dry", spec.minWetFaces(),
                    wetFaces, spec.minWaterAbove(), waterAbove), ChatFormatting.GOLD));
            return true;
        }
        if (currentSpeed > 0) {
            tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.current",
                    NetworkSummary.number(currentSpeed)), ChatFormatting.GRAY));
        } else {
            tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.no_current"),
                    ChatFormatting.GOLD));
        }
        tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.tide",
                NetworkSummary.number(tide)), ChatFormatting.GRAY));
        tooltip.add(line(Component.translatable("deepcurrent.small_tidal_turbine.speed", rpm),
                rpm > 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD));
        return true;
    }

    private static Component line(Component text, ChatFormatting style) {
        return Component.literal("    ").append(text.copy().withStyle(style));
    }

    //? if >=1.21 {
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        writeTurbine(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        readTurbine(tag, clientPacket);
    }
    //?} else {
    /*@Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        writeTurbine(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        readTurbine(tag, clientPacket);
    }
    *///?}

    private void writeTurbine(CompoundTag tag, boolean clientPacket) {
        tag.putInt("Rpm", rpm);
        if (clientPacket) {
            tag.putBoolean("Immersed", immersed);
            tag.putInt("WetFaces", wetFaces);
            tag.putInt("WaterAbove", waterAbove);
            tag.putDouble("CurrentSpeed", currentSpeed);
            tag.putDouble("Tide", tide);
        }
    }

    private void readTurbine(CompoundTag tag, boolean clientPacket) {
        rpm = tag.getInt("Rpm");
        if (clientPacket) {
            immersed = tag.getBoolean("Immersed");
            wetFaces = tag.getInt("WetFaces");
            waterAbove = tag.getInt("WaterAbove");
            currentSpeed = tag.getDouble("CurrentSpeed");
            tide = tag.getDouble("Tide");
        } else {
            // A saved turbine was running: keep it so until its first check.
            immersed = rpm > 0;
        }
    }
}
