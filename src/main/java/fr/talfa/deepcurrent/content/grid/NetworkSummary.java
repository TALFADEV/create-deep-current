package fr.talfa.deepcurrent.content.grid;

import java.util.List;
import java.util.Locale;

import fr.talfa.deepcurrent.core.grid.GridNetwork;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** What players see of a network: in the goggles, and in {@code /deepcurrent grid info}. Powers in kW. */
public record NetworkSummary(int nodes, double cableLength, double offered, double demanded, double produced,
                             double delivered, double losses, boolean overloaded) {
    public static final NetworkSummary EMPTY = new NetworkSummary(0, 0, 0, 0, 0, 0, 0, false);
    private static final String PREFIX = "deepcurrent.grid.";

    public static NetworkSummary of(LevelGrid grid, BlockPos pos) {
        if (!grid.hasNode(pos)) {
            return EMPTY;
        }
        GridNetwork network = grid.network(pos);
        GridSolution solution = network.solution();
        return new NetworkSummary(network.size(), network.cableLength(), solution.offeredKw(),
                solution.demandedKw(), solution.producedKw() + solution.dischargedKw(), solution.deliveredKw(),
                solution.lossesKw(), solution.isOverloaded());
    }

    /** Fraction of the injected power lost in cables. */
    public double lossRatio() {
        return produced > 0 ? losses / produced : 0;
    }

    /** Lines describing the network, indented for Create's goggle overlay when {@code indent} is set. */
    public void describe(List<Component> lines, boolean indent) {
        String pad = indent ? "    " : "";
        lines.add(line(pad, "nodes", nodes, number(cableLength)));
        lines.add(line(pad, "production", number(produced), number(offered)));
        lines.add(line(pad, "consumption", number(delivered), number(demanded)));
        lines.add(line(pad, "losses", number(losses), number(lossRatio() * 100)));
        if (overloaded) {
            lines.add(Component.literal(pad).append(
                    Component.translatable(PREFIX + "overloaded").withStyle(ChatFormatting.RED)));
        }
    }

    private static MutableComponent line(String pad, String key, Object... args) {
        return Component.literal(pad).append(Component.translatable(PREFIX + key, args)
                .withStyle(ChatFormatting.GRAY));
    }

    /** Power or percentage as shown to players: one decimal below 100, none above. */
    public static String number(double value) {
        return String.format(Locale.ROOT, value >= 100 ? "%.0f" : "%.1f", value);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Nodes", nodes);
        tag.putDouble("Length", cableLength);
        tag.putDouble("Offered", offered);
        tag.putDouble("Demanded", demanded);
        tag.putDouble("Produced", produced);
        tag.putDouble("Delivered", delivered);
        tag.putDouble("Losses", losses);
        tag.putBoolean("Overloaded", overloaded);
        return tag;
    }

    public static NetworkSummary read(CompoundTag tag) {
        return new NetworkSummary(tag.getInt("Nodes"), tag.getDouble("Length"), tag.getDouble("Offered"),
                tag.getDouble("Demanded"), tag.getDouble("Produced"), tag.getDouble("Delivered"),
                tag.getDouble("Losses"), tag.getBoolean("Overloaded"));
    }
}
