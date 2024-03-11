package io.github.itzispyder.crosshairtarget.mixin;

import io.github.itzispyder.crosshairtarget.util.TargetUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @Inject(method = "attack", at = @At("HEAD"))
    public void attack(Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity living) {
            TargetUtils.setTarget(living);
        }
    }
}
