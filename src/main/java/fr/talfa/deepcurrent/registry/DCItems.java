package fr.talfa.deepcurrent.registry;

import static fr.talfa.deepcurrent.DeepCurrent.REGISTRATE;

import com.tterrag.registrate.util.entry.ItemEntry;
import fr.talfa.deepcurrent.content.grid.CableCoilItem;
import fr.talfa.deepcurrent.core.grid.VoltageClass;

public final class DCItems {
    public static final ItemEntry<CableCoilItem> LV_CABLE_COIL = REGISTRATE
            .item("lv_cable_coil", properties -> new CableCoilItem(properties, VoltageClass.LV))
            .register();

    private DCItems() {
    }

    public static void register() {
    }
}
