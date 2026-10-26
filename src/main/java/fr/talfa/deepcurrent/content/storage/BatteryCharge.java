package fr.talfa.deepcurrent.content.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

/**
 * Energy carried by a battery item, so that a broken battery keeps its charge. The loot table copies the block
 * entity's {@link #KEY} into the item (custom data on 1.21, the stack tag on 1.20); placing the item gives it back.
 */
public final class BatteryCharge {
    /** Stored energy in kJ, the same key in the block entity and on the item. */
    public static final String KEY = "StoredKj";

    private BatteryCharge() {
    }

    public static double read(ItemStack stack) {
        //? if >=1.21 {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : data.copyTag().getDouble(KEY);
        //?} else
        /*return stack.hasTag() ? stack.getTag().getDouble(KEY) : 0;*/
    }

    public static void write(ItemStack stack, double kj) {
        //? if >=1.21 {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(KEY, kj));
        //?} else
        /*stack.getOrCreateTag().putDouble(KEY, kj);*/
    }

    static void write(CompoundTag tag, double kj) {
        tag.putDouble(KEY, kj);
    }
}
