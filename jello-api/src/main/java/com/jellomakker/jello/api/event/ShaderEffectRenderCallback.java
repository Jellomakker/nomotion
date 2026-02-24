package com.jellomakker.jello.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired at the same point vanilla renders its post-process shader effects
 * (spectator shaders, entity outline framebuffer blit, etc.).
 *
 * <p>This is the correct place to call {@link com.jellomakker.jello.api.managed.ManagedShaderEffect#render(float)}.
 * Rendering post effects anywhere else can break Fabulous graphics and other vanilla effects.
 */
@FunctionalInterface
public interface ShaderEffectRenderCallback {
    /**
     * Fired once per frame, at the moment vanilla applies post-process shaders.
     */
    Event<ShaderEffectRenderCallback> EVENT = EventFactory.createArrayBacked(
        ShaderEffectRenderCallback.class,
        listeners -> tickDelta -> {
            for (ShaderEffectRenderCallback listener : listeners) {
                listener.renderShaderEffects(tickDelta);
            }
        }
    );

    /**
     * @param tickDelta fraction of a tick elapsed since the last full tick (0.0 – 1.0)
     */
    void renderShaderEffects(float tickDelta);
}
