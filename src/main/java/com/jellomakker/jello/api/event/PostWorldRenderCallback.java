package com.jellomakker.jello.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Fired after Minecraft has finished rendering the world (entities, blocks, particles, sky),
 * but before it renders the player hand, HUD, and GUIs.
 *
 * <p><strong>Do not</strong> render {@link com.jellomakker.jello.api.managed.ManagedShaderEffect post effect
 * processors} here – use {@link ShaderEffectRenderCallback} instead.
 * Doing so can corrupt Fabulous graphics and vanilla post effects.
 */
@FunctionalInterface
public interface PostWorldRenderCallback {
    /**
     * Fired after world rendering is complete for the current frame.
     */
    Event<PostWorldRenderCallback> EVENT = EventFactory.createArrayBacked(
        PostWorldRenderCallback.class,
        listeners -> (matrices, camera, tickDelta) -> {
            for (PostWorldRenderCallback listener : listeners) {
                listener.onWorldRendered(matrices, camera, tickDelta);
            }
        }
    );

    /**
     * @param matrices  a blank {@link MatrixStack} that may be used for custom rendering
     * @param camera    the camera used for world rendering this frame
     * @param tickDelta fraction of a tick elapsed since the last full tick (0.0 – 1.0)
     */
    void onWorldRendered(MatrixStack matrices, Camera camera, float tickDelta);
}
