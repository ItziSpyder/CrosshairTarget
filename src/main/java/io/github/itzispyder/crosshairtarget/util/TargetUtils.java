package io.github.itzispyder.crosshairtarget.util;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static com.mojang.blaze3d.systems.RenderSystem.*;

public class TargetUtils {

    private static LivingEntity target = null;
    private static long expiration = 0L;

    public static boolean isTarget(Entity entity) {
        return entity != null && isValid() && entity.getId() == target.getId();
    }

    public static boolean isValid() {
        return target != null && target.isAlive() && !target.isSpectator();
    }

    public static void checkExpiration() {
        if (System.currentTimeMillis() > expiration && isValid()) {
            removeTarget();
        }
    }

    public static void setTarget(LivingEntity target) {
        expiration = System.currentTimeMillis() + (60 * 1000);
        TargetUtils.target = target;
    }

    public static void removeTarget() {
        target = null;
        expiration = 0L;
    }

    public static void highlight(MatrixStack matrices) {
        if (!isValid()) {
            return;
        }

        Vec3d pos = target.getPos();
        double radius = target.getWidth();

        for (int deg = 0; deg < 360; deg++) {
            double theta1 = Math.toRadians(deg);
            double theta2 = Math.toRadians(deg + 1);
            double x1 = pos.x + Math.cos(theta1) * radius;
            double y1 = pos.y;
            double z1 = pos.z + Math.sin(theta1) * radius;
            double x2 = pos.x + Math.cos(theta2) * radius;
            double y2 = pos.y + target.getHeight();
            double z2 = pos.z + Math.sin(theta2) * radius;
            fillQuad(matrices, pos, new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2), 0x80FF0000, 0x00FF0000);
        }
    }

    private static void fillQuad(MatrixStack matrices, Vec3d offset, Vec3d corner1, Vec3d corner2, int colorBottom, int colorTop) {
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        MatrixStack.Entry mat = matrices.peek();
        Box box = new Box(corner1, corner2);

        float minX = -(float)(offset.x - box.minX);
        float minY = -(float)(offset.y - box.minY);
        float minZ = -(float)(offset.z - box.minZ);
        float maxX = -(float)(offset.x - box.maxX);
        float maxY = -(float)(offset.y - box.maxY);
        float maxZ = -(float)(offset.z - box.maxZ);

        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(mat.getPositionMatrix(), minX, minY, minZ).color(colorBottom).next();
        buf.vertex(mat.getPositionMatrix(), minX, maxY, minZ).color(colorTop).next();
        buf.vertex(mat.getPositionMatrix(), maxX, maxY, maxZ).color(colorTop).next();
        buf.vertex(mat.getPositionMatrix(), maxX, minY, maxZ).color(colorBottom).next();

        disableCull();
        enableBlend();
        defaultBlendFunc();
        setShader(GameRenderer::getPositionColorProgram);
        setShaderColor(1, 1, 1, 1 );

        BufferRenderer.drawWithGlobalProgram(buf.end());

        disableBlend();
        enableCull();
        setShader(GameRenderer::getPositionTexProgram);
    }
}
