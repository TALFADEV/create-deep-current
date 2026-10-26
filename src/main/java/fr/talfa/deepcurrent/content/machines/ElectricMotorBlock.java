package fr.talfa.deepcurrent.content.machines;

import com.simibubi.create.foundation.block.IBE;
import fr.talfa.deepcurrent.registry.DCBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Turns electricity back into rotation (GDD §4.1). */
public class ElectricMotorBlock extends ElectricMachineBlock implements IBE<ElectricMotorBlockEntity> {
    public ElectricMotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ElectricMotorBlockEntity> getBlockEntityClass() {
        return ElectricMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectricMotorBlockEntity> getBlockEntityType() {
        return DCBlockEntities.ELECTRIC_MOTOR.get();
    }
}
