package io.github.itzispyder.crosshairtarget.mixin;

import io.github.itzispyder.crosshairtarget.util.TargetUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.LivingEntityRenderer.class)
public abstract class LivingEntityRenderer {

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public <T extends LivingEntity> void render(T entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (TargetUtils.isTarget(entity)) {
            TargetUtils.highlight(matrices);
        }
    }
}
