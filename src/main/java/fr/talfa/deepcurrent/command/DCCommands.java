package fr.talfa.deepcurrent.command;

import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.core.Depth;
import fr.talfa.deepcurrent.core.pressure.PressureTiers;
import fr.talfa.deepcurrent.core.tide.TideState;
import fr.talfa.deepcurrent.world.Tides;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;

/** Operator debug commands (GDD §12, admin tools). */
public final class DCCommands {
    private static final String PREFIX = "deepcurrent.command.";

    private DCCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deepcurrent")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("tide")
                        .executes(DCCommands::tideInfo)
                        .then(Commands.literal("reset").executes(DCCommands::tideReset))
                        .then(Commands.argument("coefficient", DoubleArgumentType.doubleArg(0, 10))
                                .executes(DCCommands::tideForce)))
                .then(Commands.literal("pressure").executes(DCCommands::pressure)));
    }

    private static int tideInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        TideState tide = Tides.state(level);
        int moonPhase = level.getMoonPhase();

        if (Tides.isForced()) {
            send(source, Component.translatable(PREFIX + "tide.forced", percent(tide.coefficient())));
        } else {
            send(source, Component.translatable(PREFIX + "tide.natural",
                    percent(tide.coefficient()),
                    Component.translatable(PREFIX + (tide.rising() ? "tide.rising" : "tide.falling")),
                    Component.translatable(PREFIX + "moon." + moonPhase),
                    percent(tide.lunarFactor()),
                    percent(tide.streamFactor())));
        }

        BlockPos pos = BlockPos.containing(source.getPosition());
        Tides.CurrentKind kind = Tides.currentKind(level, pos);
        send(source, Component.translatable(PREFIX + "tide.current",
                decimal(Tides.currentSpeed(level, pos)),
                Component.translatable(PREFIX + "current." + kind.name().toLowerCase(Locale.ROOT)),
                decimal(Tides.currentFactor(level, pos)),
                percent(Tides.currentFactor(level, pos) * tide.coefficient())));
        return 1;
    }

    private static int tideForce(CommandContext<CommandSourceStack> context) {
        double coefficient = DoubleArgumentType.getDouble(context, "coefficient");
        Tides.force(coefficient);
        send(context.getSource(), Component.translatable(PREFIX + "tide.set", percent(coefficient)));
        return 1;
    }

    private static int tideReset(CommandContext<CommandSourceStack> context) {
        Tides.release();
        send(context.getSource(), Component.translatable(PREFIX + "tide.reset"));
        return 1;
    }

    private static int pressure(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        int seaLevel = level.getSeaLevel();
        int depth = Depth.belowSeaLevel(seaLevel, pos.getY());
        Entity entity = source.getEntity();
        boolean underwater = entity != null
                ? entity.isUnderWater()
                : level.getFluidState(pos).is(FluidTags.WATER);

        PressureTiers tiers = DCConfigs.server().pressureTiers();
        int tier = tiers.requiredTier(depth);

        send(source, Component.translatable(PREFIX + "pressure.depth",
                depth, pos.getY(), seaLevel,
                Component.translatable(PREFIX + (underwater ? "pressure.underwater" : "pressure.dry"))));
        send(source, Component.translatable(PREFIX + "pressure.tier", tier, limit(tiers.maxDepth(tier))));

        StringBuilder limits = new StringBuilder();
        for (int t = 1; t <= tiers.count(); t++) {
            if (t > 1) {
                limits.append(" / ");
            }
            limits.append(tiers.maxDepth(t) == PressureTiers.UNLIMITED ? "∞" : String.valueOf(tiers.maxDepth(t)));
        }
        send(source, Component.translatable(PREFIX + "pressure.limits", limits.toString()));
        return 1;
    }

    private static Component limit(int maxDepth) {
        return maxDepth == PressureTiers.UNLIMITED
                ? Component.translatable(PREFIX + "pressure.unlimited")
                : Component.literal(String.valueOf(maxDepth));
    }

    private static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f %%", value * 100);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
