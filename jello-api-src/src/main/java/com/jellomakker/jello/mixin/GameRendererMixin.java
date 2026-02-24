package com.jellomakker.jello.mixin;

import com.jellomakker.jello.api.event.ShaderEffectRenderCallback;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

/**
 * Fires {@link ShaderEffectRenderCallback#EVENT} at the point where vanilla applies
 * its own post-process shader effects (after entity outlines have been resolved).
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Inject after the entity-outline framebuffer is drawn to the screen.
     * This is the same timing used by Vanilla's own post-process shader rendering,
     * so it is safe to call {@link net.minecraft.client.gl.PostEffectProcessor#render} here.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V",
            shift = AFTER
        )
    )
    private void jello$hookShaderRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        ShaderEffectRenderCallback.EVENT.invoker().renderShaderEffects(tickCounter.getTickProgress(tick));
    }
}
