package com.jellomakker.jello.impl;

import com.jellomakker.jello.api.managed.ManagedShaderEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Concrete implementation of {@link ManagedShaderEffect}.
 *
 * <p>Created by {@link ReloadableShaderEffectManager} – do not instantiate directly.
 */
public class ManagedShaderEffectImpl implements ManagedShaderEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger("JelloApi");

    private final Identifier location;
    private final Consumer<ManagedShaderEffect> initCallback;

    @Nullable private PostEffectProcessor processor;
    private boolean errored;

    public ManagedShaderEffectImpl(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        this.location = location;
        this.initCallback = initCallback;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public @Nullable PostEffectProcessor getShaderEffect() {
        if (processor == null && !errored) {
            try {
                initialize();
            } catch (IOException ignored) {
                // already logged in initialize()
            }
        }
        return processor;
    }

    @Override
    public void initialize() throws IOException {
        release();
        errored = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getShaderLoader() == null) return;
        try {
            PostEffectProcessor loaded = mc.getShaderLoader().loadPostEffect(location, DefaultFramebufferSet.MAIN_ONLY);
            if (loaded == null) {
                LOGGER.error("[JelloApi] Failed to load shader effect '{}' (null result)", location);
                this.errored = true;
                throw new IOException("Shader load returned null: " + location);
            }
            this.processor = loaded;
            this.initCallback.accept(this);
            LOGGER.debug("[JelloApi] Loaded shader effect: {}", location);
        } catch (IOException e) {
            LOGGER.error("[JelloApi] Failed to load shader effect '{}': {}", location, e.getMessage());
            this.errored = true;
            throw e;
        } catch (Exception e) {
            LOGGER.error("[JelloApi] Failed to load shader effect '{}': {}", location, e.getMessage());
            this.errored = true;
            throw new IOException("Shader load failed: " + location, e);
        }
    }

    @Override
    public boolean isInitialized() {
        return processor != null;
    }

    @Override
    public boolean isErrored() {
        return errored;
    }

    @Override
    public void release() {
        closeProcessor();
        errored = false;
    }

    /** Close the GPU-side resources without resetting the error flag. */
    private void closeProcessor() {
        if (processor != null) {
            try {
                processor.close();
            } catch (Exception ignored) {
                // Best effort – may already be closed.
            }
            processor = null;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void render(float tickDelta) {
        if (errored) return;

        PostEffectProcessor p = getShaderEffect();
        if (p == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getFramebuffer() == null) return;

        // MC 1.21.8: render() takes (Framebuffer, ObjectAllocator) — uniforms are
        // baked into GPU buffers from the post_effect JSON at load time.
        try {
            p.render(mc.getFramebuffer(), ObjectAllocator.TRIVIAL);
        } catch (IllegalStateException e) {
            // GPU buffers were freed during a resource reload (e.g. texture pack change).
            // Mark for lazy re-initialization on the next frame.
            LOGGER.warn("[JelloApi] Shader buffer invalidated during reload, will re-init: {}", location);
            release();
        } catch (Exception e) {
            // Catch pipeline creation failures from alternative renderers (e.g. VulkanMod)
            // so we don't crash the game.  Disable this effect permanently.
            LOGGER.error("[JelloApi] Shader render failed for '{}', disabling: {}", location, e.getMessage());
            closeProcessor();
            errored = true;   // Set AFTER closeProcessor so it sticks — stops retry loop.
        }
    }

    // ── Uniforms ──────────────────────────────────────────────────────────────

    // ── Uniforms (no-ops in MC 1.21.8+) ──────────────────────────────────────
    // In 1.21.8 uniforms are baked into GPU UBOs from the post_effect JSON.
    // Dynamic per-frame uniform setting is no longer supported by the engine.
    // These methods are retained for API compatibility but have no effect.

    @Override
    public void setUniformValue(String name, float value) { /* no-op */ }

    @Override
    public void setUniformValue(String name, float x, float y) { /* no-op */ }

    @Override
    public void setUniformValue(String name, float x, float y, float z) { /* no-op */ }

    @Override
    public void setUniformValue(String name, float x, float y, float z, float w) { /* no-op */ }

    @Override
    public void setUniformValue(String name, int value) { /* no-op */ }

    // ── Internal ──────────────────────────────────────────────────────────────

    /** Package-visible: called by {@link ReloadableShaderEffectManager} on reload. */
    void onReload() {
        // Release existing processor; it will be re-created lazily on next render()
        release();
    }
}
