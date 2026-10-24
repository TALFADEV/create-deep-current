package fr.talfa.deepcurrent.client;

import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.config.DCServerConfig;
import fr.talfa.deepcurrent.content.grid.CableCoilItem;
import fr.talfa.deepcurrent.content.grid.CablePlacement;
import fr.talfa.deepcurrent.content.grid.ConnectorBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ghost preview of a cable being laid (GDD §10 ter): a line from the start connector to the targeted one, green
 * when it can be laid and red when it is too long, with its length and cost above the hotbar.
 */
public final class CablePreview {
    private static final int VALID = 0xFF70FF70;
    private static final int INVALID = 0xFFFF6060;
    private static final int MESSAGE_INTERVAL = 4;

    private static int ticks;

    private CablePreview() {
    }

    private record Preview(CableCoilItem coil, Vec3 from, Vec3 to, boolean onConnector) {
        double length() {
            return from.distanceTo(to);
        }
    }

    public static void render(PoseStack poseStack, Vec3 camera) {
        Preview preview = current();
        if (preview == null) {
            return;
        }
        DCServerConfig config = DCConfigs.server();
        boolean valid = preview.onConnector() && preview.length() <= config.manualRange.get();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        CableRenderer.render(poseStack, buffers, camera, preview.from(), preview.to(), LightTexture.FULL_BRIGHT,
                LightTexture.FULL_BRIGHT, preview.coil().voltage(), valid ? VALID : INVALID);
        buffers.endBatch();
    }

    public static void tick() {
        if (++ticks % MESSAGE_INTERVAL != 0) {
            return;
        }
        Preview preview = current();
        LocalPlayer player = Minecraft.getInstance().player;
        if (preview == null || player == null) {
            return;
        }
        DCServerConfig config = DCConfigs.server();
        double length = preview.length();
        int range = config.manualRange.get();
        Component message = Component.translatable("deepcurrent.cable.preview",
                String.format(Locale.ROOT, "%.1f", length), range,
                CableCoilItem.cost(length, config));
        player.displayClientMessage(message.copy().withStyle(length <= range ? ChatFormatting.GREEN
                : ChatFormatting.RED), true);
    }

    private static Preview current() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || DCConfigs.server() == null) {
            return null;
        }
        CableCoilItem coil = heldCoil(player);
        CablePlacement.Start start = coil == null ? null : CablePlacement.get(player);
        if (start == null || !start.dimension().equals(minecraft.level.dimension())) {
            return null;
        }
        BlockState startState = minecraft.level.getBlockState(start.pos());
        if (!(startState.getBlock() instanceof ConnectorBlock)) {
            return null;
        }
        Vec3 from = ConnectorBlock.anchor(start.pos(), startState);

        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos target = blockHit.getBlockPos();
            BlockState state = minecraft.level.getBlockState(target);
            if (state.getBlock() instanceof ConnectorBlock && !target.equals(start.pos())) {
                return new Preview(coil, from, ConnectorBlock.anchor(target, state), true);
            }
        }
        Vec3 inFront = player.getEyePosition().add(player.getLookAngle().scale(1.5)).add(0, -0.3, 0);
        return new Preview(coil, from, inFront, false);
    }

    private static CableCoilItem heldCoil(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof CableCoilItem coil) {
                return coil;
            }
        }
        return null;
    }
}
