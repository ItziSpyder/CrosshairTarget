package io.github.itzispyder.crosshairtarget.util;

import io.github.itzispyder.crosshairtarget.gui.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.itzispyder.crosshairtarget.util.RenderUtils.*;

public class CrosshairRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final AtomicBoolean active = new AtomicBoolean(false);
    private static final AtomicReference<String> mode = new AtomicReference<>("miss");

    private static final Animator crossHairCrossAnimator = new PollingAnimator(250, () -> mc.crosshairTarget instanceof EntityHitResult);
    private static final Animator bowAnimator = new PollingAnimator(250, () -> active.get() && mode.get().equals("bow"));
    private static final Animator attackAnimator = new PollingAnimator(250, () -> active.get() && mode.get().equals("attack"));
    private static final Animator useAnimator = new PollingAnimator(250, () -> active.get() && mode.get().equals("use"));
    private static final Animator entityAnimator = new PollingAnimator(250, () -> active.get() && mode.get().equals("entity"));
    private static final Animator blockAnimator = new PollingAnimator(250, () -> active.get() && mode.get().equals("block"));

    public static void render(DrawContext context) {
        if (mc == null || mc.player == null || mc.world == null || mc.interactionManager == null) {
            return;
        }

        var hit = mc.crosshairTarget;
        int x = width() / 2;
        int y = height() / 2;
        float angle = (float)(crossHairCrossAnimator.getProgressClamped() * 45);
        float attackProgress = mc.player.getAttackCooldownProgress(1.0F);

        boolean attacking = attackProgress < 1;
        boolean using = mc.player.isUsingItem();
        boolean targeting = hit instanceof EntityHitResult;
        boolean breaking = mc.interactionManager.isBreakingBlock();
        boolean aiming = using(Items.BOW) || CrossbowItem.isCharged(mc.player.getMainHandStack()) || using(Items.TRIDENT);

        if (!aiming) {
            context.getMatrices().push();
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle), x, y, 0);
            drawHorLine(context, x - 5, y, 11, 0xD0FFFFFF);
            drawVerLine(context, x, y - 5, 11, 0xD0FFFFFF);
            context.getMatrices().pop();
        }

        float scale = (float)Config.readDouble("hud-scale");
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, scale);
        x = (int)(x / scale);
        y = (int)(y / scale);

        if (Config.readBool("aiming") && aiming) {
            active.set(true);
            mode.set("bow");
        }
        else if (Config.readBool("using") && using) {
            active.set(true);
            mode.set("use");
        }
        else if (Config.readBool("attacking") && attacking) {
            active.set(true);
            mode.set("attack");
        }
        else if (Config.readBool("targeting") && targeting) {
            active.set(true);
            mode.set("entity");
        }
        else if (Config.readBool("breaking") && breaking) {
            active.set(true);
            mode.set("block");
        }
        else {
            active.set(false);
        }

        renderAttack(context, x, y, attackProgress);
        renderEntity(context, x, y, hit, mc.player, mc.player.getEntityInteractionRange());
        renderBlock(context, x, y, mc.interactionManager.getBlockBreakingProgress());
        renderUse(context, x, y, mc.player);
        renderBow(context, x, y, mc.player);

        context.getMatrices().pop();
    }
    
    private static boolean using(Item item) {
        if (mc.player == null) {
            return false;
        }
        return mc.player.isUsingItem() && mc.player.getActiveItem().getItem() == item;
    }
    
    
    

    private static void renderAttack(DrawContext context, int x, int y, float attackProgress) {
        float s = (float)(attackAnimator.getProgressClamped());
        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);
        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);
        
        fillCircle(context, x, y, (int)(21 * attackProgress), 0x80FF5E5E);
        drawArc(context, x, y, 21, 0, (int)(attackProgress * 360), 0xFFFF5E5E);
        
        context.getMatrices().pop();
    }
    
    private static void renderEntity(DrawContext context, int x, int y, HitResult hit, ClientPlayerEntity player, double reach) {
        float s = (float)(entityAnimator.getProgressClamped());
        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);
        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);
        
        double dist = hit.getPos().distanceTo(player.getEyePos());
        double rat = dist / reach;
        drawCircle(context, x, y, (int)(10 + (10 * rat)), 0xFF8080FF);
        drawCircle(context, x, y, (int)(11 + (10 * rat)), 0xFF8080FF);
        drawCircle(context, x, y, 21, 0xD0FFFFFF);
        
        context.getMatrices().pop();
    }
    
    private static void renderBlock(DrawContext context, int x, int y, int blockBreakProgress) {
        float s = (float)(blockAnimator.getProgressClamped());
        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);
        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);
        
        int attack = blockBreakProgress + 1;
        double rat = attack / 10.0;
        fillCircle(context, x, y, (int)(21 * rat), 0x50FFFFFF);
        drawCircle(context, x, y, 21, 0xD0FFFFFF);
        
        context.getMatrices().pop();
    }
    
    private static void renderUse(DrawContext context, int x, int y, ClientPlayerEntity player) {
        float s = (float)(useAnimator.getProgressClamped());
        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);
        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);
        
        int max = player.getMainHandStack().getMaxUseTime();
        int use = Math.min(player.getItemUseTime(), max);
        double rat = (double)use / (double)max;
        fillArc(context, x, y, 21, 0, (int)(rat * 360), 0x806BF029);
        drawCircle(context, x, y, 21, 0xFF6BF029);
        
        context.getMatrices().pop();
    }
    
    private static void renderBow(DrawContext context, int x, int y, ClientPlayerEntity player) {
        float s = (float)(bowAnimator.getProgressClamped());
        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);
        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);
        
        float rat = BowItem.getPullProgress(player.getItemUseTime());
        drawLine(context, x - 30, y, x + 30, y, 0xD0FFFFFF);
        drawLine(context, x, y - 30, x, y + 30, 0xD0FFFFFF);

        for (int i = 0; i < 7; i++) {
            drawLine(context, x - 4, y + i * 5, x + 4, y + i * 5, 0xFFFFFFFF);
        }

        double radius = 40 - (40 * rat);
        for (int i = 0; i < 360; i += 45) {
            double deg = Math.toRadians(i);
            int sx = (int)(Math.cos(deg) * radius) + x;
            int sy = (int)(Math.sin(deg) * radius) + y;
            int lx = (int)(Math.cos(deg) * (radius + 5)) + x;
            int ly = (int)(Math.sin(deg) * (radius + 5)) + y;
            drawLine(context, sx, sy, lx, ly, 0xFFFF0000);
        }

        float pitch = -Math.min(player.getPitch(), 0);
        rat = Math.abs(45 - pitch) / 45;
        radius = 35 + rat * 20;
        for (int i = 0; i <= 270; i += 90) {
            drawArc(context, x, y, (int)radius, i - 22, i + 22, 0xFFFF0000);
            drawArc(context, x, y, (int)radius + 1, i - 10, i + 10, 0xFFFF0000);
        }
        
        context.getMatrices().pop();
    }
}
