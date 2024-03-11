package io.github.itzispyder.crosshairtarget.mixin;

import io.github.itzispyder.crosshairtarget.util.TargetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.github.itzispyder.crosshairtarget.util.RenderUtils.*;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Unique private static final MinecraftClient mc = MinecraftClient.getInstance();
    @Unique private static final AtomicInteger scale = new AtomicInteger(0);
    @Unique private static final AtomicBoolean targeting = new AtomicBoolean(false);
    @Unique private static final AtomicReference<String> lastTarget = new AtomicReference<>("miss");

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(DrawContext context, CallbackInfo ci) {
        ci.cancel();

        if (mc == null || mc.player == null || mc.world == null || mc.interactionManager == null) {
            return;
        }

        Function<Item, Boolean> isUsing = item -> mc.player.isUsingItem() && mc.player.getActiveItem().getItem() == item;
        var hit = mc.crosshairTarget;
        int x = width() / 2;
        int y = height() / 2;
        float s = (float)(scale.get() / 100.0);
        float angle = s * 45;

        if (!isUsing.apply(Items.BOW)) {
            context.getMatrices().push();
            if (lastTarget.get().equals("entity")) {
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle), x, y, 0);
            }

            drawHorLine(context, x - 5, y, 11, 0xD0FFFFFF);
            drawVerLine(context, x, y - 5, 11, 0xD0FFFFFF);
            context.getMatrices().pop();
        }

        float rs = 1 / s;
        x = (int)(x * rs);
        y = (int)(y * rs);

        context.getMatrices().push();
        context.getMatrices().scale(s, s, s);

        if (isUsing.apply(Items.BOW)) {
            targeting.set(true);
            lastTarget.set("bow");
        }
        else if (mc.player.isUsingItem()) {
            targeting.set(true);
            lastTarget.set("use");
        }
        else if (hit instanceof EntityHitResult) {
            targeting.set(true);
            lastTarget.set("entity");
        }
        else if (mc.interactionManager.isBreakingBlock()) {
            targeting.set(true);
            lastTarget.set("block");
        }
        else {
            targeting.set(false);
        }

        switch (lastTarget.get()) {
            case "entity" -> {
                double attack = mc.player.getAttackCooldownProgress(1.0F);
                if (attack < 1) {
                    fillCircle(context, x, y, (int)(21 * attack), 0x80FF5E5E);
                }

                double dist = hit.getPos().distanceTo(mc.player.getEyePos());
                double rat = dist / mc.interactionManager.getReachDistance();
                drawCircle(context, x, y, (int)(10 + (10 * rat)), 0xFF8080FF);
                drawCircle(context, x, y, (int)(11 + (10 * rat)), 0xFF8080FF);
                drawArc(context, x, y, 21, 0, (int)(attack * 360), attack < 1 ? 0xFFFF5E5E : 0xD0FFFFFF);
            }
            case "block" -> {
                int attack = mc.interactionManager.getBlockBreakingProgress() + 1;
                double rat = attack / 10.0;
                fillCircle(context, x, y, (int)(21 * rat), 0x50FFFFFF);
                drawCircle(context, x, y, 21, 0xD0FFFFFF);
            }
            case "use" -> {
                int max = mc.player.getInventory().getMainHandStack().getMaxUseTime();
                int use = Math.min(mc.player.getItemUseTime(), max);
                double rat = (double)use / (double)max;
                fillArc(context, x, y, 21, 0, (int)(rat * 360), 0x806BF029);
                drawCircle(context, x, y, 21, 0xFF6BF029);
            }
            case "bow" -> {
                float rat = BowItem.getPullProgress(mc.player.getItemUseTime());
                drawLine(context, x - 30, y, x + 30, y, 0xD0FFFFFF);
                drawLine(context, x, y - 30, x, y + 30, 0xD0FFFFFF);

                int lineY = y;
                for (int i = 0; i < 6; i++) {
                    lineY = (int)(lineY + Math.exp(rat / 2 * i));
                    drawLine(context, x - 4, lineY, x + 4, lineY, 0xFFFFFFFF);
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

                float pitch = -Math.min(mc.player.getPitch(), 0);
                rat = Math.abs(45 - pitch) / 45;
                radius = 35 + rat * 20;
                for (int i = 0; i <= 270; i += 90) {
                    drawArc(context, x, y, (int)radius, i - 22, i + 22, 0xFFFF0000);
                    drawArc(context, x, y, (int)radius + 1, i - 10, i + 10, 0xFFFF0000);
                }
            }
        }

        context.getMatrices().pop();
    }

    @Inject(method = "tick(Z)V", at = @At("TAIL"))
    public void tick(boolean paused, CallbackInfo ci) {
        if (targeting.get() && scale.get() < 100) {
            scale.getAndAdd(20);
        }
        else if (!targeting.get()) {
            if (scale.get() > 0) {
                scale.getAndAdd(-20);
            }
            else {
                lastTarget.set("miss");
            }
        }

        TargetUtils.checkExpiration();
    }
}
