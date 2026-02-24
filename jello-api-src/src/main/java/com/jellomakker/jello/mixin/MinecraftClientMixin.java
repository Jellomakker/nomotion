package com.jellomakker.jello.mixin;

import com.jellomakker.jello.api.event.ResolutionChangeCallback;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@link ResolutionChangeCallback#EVENT} whenever the game window is resized.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void jello$onResolutionChanged(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();
        ResolutionChangeCallback.EVENT.invoker().onResolutionChanged(w, h);
    }
}
