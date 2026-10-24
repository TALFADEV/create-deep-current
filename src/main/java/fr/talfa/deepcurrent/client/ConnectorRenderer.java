package fr.talfa.deepcurrent.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import fr.talfa.deepcurrent.content.grid.ConnectorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Draws the cables attached to a connector. Each cable is drawn once, by the end with the lower position. */
public class ConnectorRenderer extends SafeBlockEntityRenderer<ConnectorBlockEntity> {
    private static final int VIEW_DISTANCE = 128;

    public ConnectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(ConnectorBlockEntity connector, float partialTicks, PoseStack poseStack,
                              MultiBufferSource buffers, int light, int overlay) {
        Level level = connector.getLevel();
        BlockPos pos = connector.getBlockPos();
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 from = ConnectorBlock.anchor(pos, connector.getBlockState());
        for (BlockPos other : connector.links()) {
            boolean otherDraws = level.getBlockEntity(other) instanceof ConnectorBlockEntity;
            if (otherDraws && other.asLong() < pos.asLong()) {
                continue;
            }
            BlockState otherState = level.getBlockState(other);
            Vec3 to = otherState.getBlock() instanceof ConnectorBlock
                    ? ConnectorBlock.anchor(other, otherState)
                    : Vec3.atCenterOf(other);
            CableRenderer.render(poseStack, buffers, origin, from, to, light,
                    LevelRenderer.getLightColor(level, other), connector.voltage(), CableRenderer.WHITE);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ConnectorBlockEntity connector) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }
}
