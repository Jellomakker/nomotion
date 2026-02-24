package com.jellomakker.jello.api.util;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;

/**
 * Utility methods for post-processing mods targeting Minecraft 1.21.5+.
 *
 * <p>In 1.21.5, Minecraft moved to a GPU-abstraction rendering layer. Raw LWJGL
 * OpenGL calls still work but most {@code RenderSystem} helpers for blend state and
 * framebuffer binding have been removed. This class provides safe helpers that
 * work with the new API surface.
 *
 * <p>All methods must be called on the render thread.
 */
public final class GlHelper {

    private GlHelper() {}

    // ── Framebuffer / texture helpers ─────────────────────────────────────────

    /**
     * Returns the main Minecraft framebuffer's current color attachment as a
     * {@link GpuTexture}. Useful for sampling the scene in a post-process shader.
     *
     * @return the GPU texture for the main framebuffer's colour target
     */
    public static GpuTexture getMainColorGpuTexture() {
        return MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
    }

    // ── Resolution ────────────────────────────────────────────────────────────

    /**
     * Returns the current framebuffer width in pixels.
     */
    public static int getFramebufferWidth() {
        return MinecraftClient.getInstance().getWindow().getFramebufferWidth();
    }

    /**
     * Returns the current framebuffer height in pixels.
     */
    public static int getFramebufferHeight() {
        return MinecraftClient.getInstance().getWindow().getFramebufferHeight();
    }
}
