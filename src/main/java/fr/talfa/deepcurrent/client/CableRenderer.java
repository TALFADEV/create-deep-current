package fr.talfa.deepcurrent.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Draws a cable as a sagging curve between two anchors. Its cross-section is two perpendicular ribbons, so it
 * reads as a round cable from any angle at a fraction of the cost of a tube.
 */
public final class CableRenderer {
    public static final int WHITE = 0xFFFFFFFF;
    private static final float HALF_WIDTH = 0.045f;

    private CableRenderer() {
    }

    public static ResourceLocation texture(VoltageClass voltage) {
        return DeepCurrent.asResource("textures/entity/cable_" + voltage.name().toLowerCase() + ".png");
    }

    /**
     * @param origin    world position of the pose stack's origin
     * @param from      first anchor, world coordinates
     * @param to        second anchor, world coordinates
     * @param lightFrom packed light at the first anchor
     * @param lightTo   packed light at the second anchor
     * @param tint      ARGB colour multiplied with the texture
     */
    public static void render(PoseStack poseStack, MultiBufferSource buffers, Vec3 origin, Vec3 from, Vec3 to,
                              int lightFrom, int lightTo, VoltageClass voltage, int tint) {
        Vec3 span = to.subtract(from);
        double length = span.length();
        if (length < 1e-3) {
            return;
        }
        Vec3 direction = span.scale(1 / length);
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1e-6) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();

        int segments = Mth.clamp((int) (length * 2), 8, 48);
        double sag = Math.min(length * 0.06, 2.5);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture(voltage)));
        PoseStack.Pose pose = poseStack.last();
        Vec3 start = from.subtract(origin);

        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            Vec3 p0 = point(start, span, sag, t0);
            Vec3 p1 = point(start, span, sag, t1);
            int light0 = lerpLight(lightFrom, lightTo, t0);
            int light1 = lerpLight(lightFrom, lightTo, t1);
            ribbon(consumer, pose, p0, p1, side.scale(HALF_WIDTH), up, light0, light1, tint);
            ribbon(consumer, pose, p0, p1, up.scale(HALF_WIDTH), side, light0, light1, tint);
        }
    }

    /** Point along the cable: straight line plus a parabolic sag, deepest in the middle. */
    private static Vec3 point(Vec3 start, Vec3 span, double sag, float t) {
        return start.add(span.scale(t)).add(0, -sag * 4 * t * (1 - t), 0);
    }

    private static void ribbon(VertexConsumer consumer, PoseStack.Pose pose, Vec3 p0, Vec3 p1, Vec3 offset,
                               Vec3 normal, int light0, int light1, int tint) {
        vertex(consumer, pose, p0.subtract(offset), 0, 0, light0, normal, tint);
        vertex(consumer, pose, p0.add(offset), 1, 0, light0, normal, tint);
        vertex(consumer, pose, p1.add(offset), 1, 1, light1, normal, tint);
        vertex(consumer, pose, p1.subtract(offset), 0, 1, light1, normal, tint);
    }

    private static int lerpLight(int from, int to, float t) {
        int block = Math.round(Mth.lerp(t, LightTexture.block(from), LightTexture.block(to)));
        int sky = Math.round(Mth.lerp(t, LightTexture.sky(from), LightTexture.sky(to)));
        return LightTexture.pack(block, sky);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 p, float u, float v, int light,
                               Vec3 normal, int tint) {
        //? if >=1.21 {
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(tint)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        //?} else {
        /*consumer.vertex(pose.pose(), (float) p.x, (float) p.y, (float) p.z)
                .color((tint >> 16) & 0xFF, (tint >> 8) & 0xFF, tint & 0xFF, (tint >>> 24) & 0xFF)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
        *///?}
    }
}
