package io.github.itzispyder.crosshairtarget.mixin;

import io.github.itzispyder.crosshairtarget.util.CrosshairRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(DrawContext context, CallbackInfo ci) {
        ci.cancel();
        CrosshairRenderer.render(context);
    }
}
