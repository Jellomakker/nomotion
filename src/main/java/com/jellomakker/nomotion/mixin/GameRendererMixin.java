package com.jellomakker.nomotion.mixin;

import com.jellomakker.nomotion.NomotionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

/**
 * Injects motion-blur rendering right after entity outlines are drawn,
 * matching the exact timing vanilla uses for its own post-process effects.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Pool pool;

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V",
            shift = AFTER
        )
    )
    private void nomotion$afterOutlines(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        NomotionClient.renderMotionBlur(this.client, this.pool);
    }
}
