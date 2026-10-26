package fr.talfa.deepcurrent.content.storage;

import java.util.List;

import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.content.grid.NetworkSummary;
import fr.talfa.deepcurrent.core.grid.StorageSpec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

//? if <1.21 {
/*import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
*///?}

/** The battery as an item: shows the energy it carries. */
public class SmallBatteryItem extends BlockItem {
    public SmallBatteryItem(Block block, Properties properties) {
        super(block, properties);
    }

    //? if >=1.21 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        addCharge(stack, tooltip);
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        addCharge(stack, tooltip);
    }
    *///?}

    private static void addCharge(ItemStack stack, List<Component> tooltip) {
        StorageSpec spec = DCConfigs.server().smallBatterySpec();
        String stored = NetworkSummary.number(BatteryCharge.read(stack) / 3600);
        tooltip.add(Component.translatable("deepcurrent.small_battery.item", stored,
                NetworkSummary.number(spec.capacityKwh())).withStyle(ChatFormatting.GRAY));
    }
}
