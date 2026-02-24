package com.jellomakker.jello.api.managed;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A lazily-initialized, lifecycle-managed wrapper around Minecraft's {@link PostEffectProcessor}.
 *
 * <p>Instances are obtained via {@link ShaderEffectManager#manage(Identifier)}.
 * The underlying shader is automatically re-initialized whenever the screen resolution
 * changes or assets are reloaded (F3+T). Uniforms can be pushed each frame before calling
 * {@link #render(float)}.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * private static final ManagedShaderEffect BLUR =
 *     ShaderEffectManager.getInstance().manage(Identifier.of("mymod", "shaders/post/blur.json"));
 *
 * // In your ShaderEffectRenderCallback:
 * ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
 *     if (enabled) BLUR.render(tickDelta);
 * });
 * }</pre>
 */
public interface ManagedShaderEffect {

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    /**
     * Returns the underlying {@link PostEffectProcessor}, creating it if necessary.
     * Returns {@code null} if initialization failed.
     */
    @Nullable PostEffectProcessor getShaderEffect();

    /**
     * Forcibly (re-)initializes the shader. Useful if you want to pre-warm it
     * before the first render call.
     *
     * @throws IOException if the shader JSON or GLSL sources cannot be loaded
     */
    void initialize() throws IOException;

    /** @return {@code true} if the shader has been successfully initialized */
    boolean isInitialized();

    /** @return {@code true} if initialization previously failed with an error */
    boolean isErrored();

    /**
     * Releases the shader's GL resources and marks it as uninitialized.
     * The next call to {@link #getShaderEffect()} or {@link #render(float)} will re-create it.
     *
     * <p>To permanently stop managing this shader, call
     * {@link ShaderEffectManager#dispose(ManagedShaderEffect)} instead.
     */
    void release();

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Renders the post-processing chain for this frame.
     *
     * <p>Reads from {@link net.minecraft.client.MinecraftClient#getFramebuffer() the main framebuffer},
     * runs each pass defined in the JSON, and writes the result back.
     * The shader is lazily initialized on the first call.
     *
     * @param tickDelta fraction of a tick since the last full tick
     */
    void render(float tickDelta);

    // ── Uniforms ──────────────────────────────────────────────────────────────

    /**
     * Sets a {@code float} uniform across all passes that declare it.
     *
     * @param name  uniform name as declared in the shader JSON / GLSL source
     * @param value the value to upload
     */
    void setUniformValue(String name, float value);

    /**
     * Sets a {@code vec2} uniform.
     */
    void setUniformValue(String name, float x, float y);

    /**
     * Sets a {@code vec3} uniform.
     */
    void setUniformValue(String name, float x, float y, float z);

    /**
     * Sets a {@code vec4} uniform.
     */
    void setUniformValue(String name, float x, float y, float z, float w);

    /**
     * Sets an {@code int} uniform.
     */
    void setUniformValue(String name, int value);
}
