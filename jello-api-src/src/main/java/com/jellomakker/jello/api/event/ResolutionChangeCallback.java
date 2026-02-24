package com.jellomakker.jello.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fired whenever the Minecraft window is resized, including at initial startup
 * after the GL context is created.
 *
 * <p>Managed shader effects automatically listen to this event to resize their
 * internal framebuffers. Client mods that allocate resolution-dependent resources
 * should also register here.
 */
@FunctionalInterface
public interface ResolutionChangeCallback {
    /**
     * Fired each time the framebuffer resolution changes.
     */
    Event<ResolutionChangeCallback> EVENT = EventFactory.createArrayBacked(
        ResolutionChangeCallback.class,
        listeners -> (width, height) -> {
            for (ResolutionChangeCallback listener : listeners) {
                listener.onResolutionChanged(width, height);
            }
        }
    );

    /**
     * @param width  new framebuffer width in pixels
     * @param height new framebuffer height in pixels
     */
    void onResolutionChanged(int width, int height);
}
