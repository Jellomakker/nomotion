package com.jellomakker.jello.mixin;

import com.jellomakker.jello.impl.ReloadableShaderEffectManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notifies the {@link ReloadableShaderEffectManager} when the world renderer reloads
 * its shaders (resource pack change, F3+T, world join).
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(method = "reload()V", at = @At("RETURN"))
    private void jello$onWorldRendererReload(CallbackInfo ci) {
        ReloadableShaderEffectManager.INSTANCE.onWorldRendererReload();
    }
}
