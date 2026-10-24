package fr.talfa.deepcurrent.content.grid;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.config.DCServerConfig;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cable coil (GDD §4.6, §10 ter). Click a connector to start a cable, click a second one to lay it. Sneak-click a
 * connector to rewind its cables (100 % of the coils back); sneak-click the air to cancel a pending cable.
 */
public class CableCoilItem extends Item {
    private static final Map<VoltageClass, CableCoilItem> BY_VOLTAGE = new EnumMap<>(VoltageClass.class);
    private static final String PREFIX = "deepcurrent.cable.";

    private final VoltageClass voltage;

    public CableCoilItem(Properties properties, VoltageClass voltage) {
        super(properties);
        this.voltage = voltage;
        BY_VOLTAGE.put(voltage, this);
    }

    public static CableCoilItem forVoltage(VoltageClass voltage) {
        return BY_VOLTAGE.get(voltage);
    }

    public VoltageClass voltage() {
        return voltage;
    }

    /** Coils needed for a cable of this length. */
    public static int cost(double length, DCServerConfig config) {
        return Math.max(1, (int) Math.ceil(length / config.blocksPerCoil.get()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (player == null || !(state.getBlock() instanceof ConnectorBlock connector)) {
            return InteractionResult.PASS;
        }
        if (connector.voltage() != voltage) {
            tell(player, Component.translatable(PREFIX + "wrong_voltage").withStyle(ChatFormatting.RED));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown()) {
            CablePlacement.clear(player);
            if (level instanceof ServerLevel serverLevel) {
                rewind(serverLevel, player, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        CablePlacement.Start start = CablePlacement.get(player);
        if (start == null || !start.dimension().equals(level.dimension())) {
            CablePlacement.set(player, new CablePlacement.Start(level.dimension(), pos));
            tell(player, Component.translatable(PREFIX + "start", DCConfigs.server().manualRange.get()));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        CablePlacement.clear(player);
        if (start.pos().equals(pos)) {
            tell(player, Component.translatable(PREFIX + "cancelled"));
        } else if (level instanceof ServerLevel serverLevel) {
            lay(serverLevel, player, start.pos(), pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && CablePlacement.get(player) != null) {
            CablePlacement.clear(player);
            tell(player, Component.translatable(PREFIX + "cancelled"));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    private void lay(ServerLevel level, Player player, BlockPos from, BlockPos to) {
        DCServerConfig config = DCConfigs.server();
        BlockState fromState = level.getBlockState(from);
        if (!(fromState.getBlock() instanceof ConnectorBlock)) {
            tell(player, Component.translatable(PREFIX + "start_gone").withStyle(ChatFormatting.RED));
            return;
        }
        double length = ConnectorBlock.anchor(from, fromState).distanceTo(
                ConnectorBlock.anchor(to, level.getBlockState(to)));
        int range = config.manualRange.get();
        if (length > range) {
            tell(player, Component.translatable(PREFIX + "too_far", NetworkSummary.number(length), range)
                    .withStyle(ChatFormatting.RED));
            return;
        }
        LevelGrid grid = LevelGrid.get(level);
        if (grid.linked(from, to)) {
            tell(player, Component.translatable(PREFIX + "already").withStyle(ChatFormatting.RED));
            return;
        }
        int cost = cost(length, config);
        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            int carried = player.getInventory().countItem(this);
            if (carried < cost) {
                tell(player, Component.translatable(PREFIX + "not_enough", cost, carried)
                        .withStyle(ChatFormatting.RED));
                return;
            }
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(this), cost,
                    player.inventoryMenu.getCraftSlots());
        }

        grid.ensureNode(from, NodeRole.CONNECTOR);
        grid.ensureNode(to, NodeRole.CONNECTOR);
        // Cables laid in creative cost nothing, so breaking them gives nothing back.
        grid.link(from, to, length, voltage, creative ? 0 : cost);
        ConnectorBlockEntity.refresh(level, from);
        ConnectorBlockEntity.refresh(level, to);
        tell(player, Component.translatable(PREFIX + "laid", NetworkSummary.number(length), cost));

        NetworkSummary summary = NetworkSummary.of(grid, to);
        if (summary.lossRatio() * 100 > config.lossWarningPercent.get()) {
            player.sendSystemMessage(Component.translatable(PREFIX + "loss_warning",
                    NetworkSummary.number(summary.lossRatio() * 100)).withStyle(ChatFormatting.GOLD));
        }
    }

    private void rewind(ServerLevel level, Player player, BlockPos pos) {
        List<LevelGrid.StoredCable> removed = LevelGrid.get(level).unlinkAll(pos);
        int coils = 0;
        for (LevelGrid.StoredCable cable : removed) {
            coils += cable.cost();
            ConnectorBlockEntity.refresh(level, BlockPos.of(cable.other(pos.asLong())));
        }
        ConnectorBlockEntity.refresh(level, pos);
        while (coils > 0) {
            int stack = Math.min(coils, getDefaultInstance().getMaxStackSize());
            player.getInventory().placeItemBackInInventory(new ItemStack(this, stack));
            coils -= stack;
        }
        tell(player, Component.translatable(PREFIX + "rewound", removed.size()));
    }

    private static void tell(Player player, Component message) {
        if (!player.level().isClientSide) {
            player.displayClientMessage(message, true);
        }
    }
}
