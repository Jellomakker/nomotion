package com.jellomakker.jello.api.managed;

import com.jellomakker.jello.impl.ReloadableShaderEffectManager;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

/**
 * Central registry for {@link ManagedShaderEffect} instances.
 *
 * <p>All managed shaders are automatically re-initialized on:
 * <ul>
 *   <li>Screen resolution change</li>
 *   <li>Resource pack reload (F3+T)</li>
 * </ul>
 *
 * <h2>Obtaining an instance</h2>
 * <pre>{@code
 * ShaderEffectManager.getInstance().manage(Identifier.of("mymod", "shaders/post/myeffect.json"));
 * }</pre>
 */
public interface ShaderEffectManager {

    /**
     * Returns the singleton {@link ShaderEffectManager}.
     */
    static ShaderEffectManager getInstance() {
        return ReloadableShaderEffectManager.INSTANCE;
    }

    // ── Factory methods ────────────────────────────────────────────────────────

    /**
     * Registers a post-processing shader that will be reloaded automatically.
     *
     * <p>The shader JSON must be located at
     * {@code assets/<namespace>/shaders/post/<path>.json}.
     *
     * @param location resource location of the shader JSON
     * @return a managed handle; hold a static reference to it
     */
    ManagedShaderEffect manage(Identifier location);

    /**
     * Registers a post-processing shader with an initialization callback that
     * runs each time the shader is (re-)initialized.
     *
     * @param location     resource location of the shader JSON
     * @param initCallback called with the shader after (re-)initialization succeeds
     * @return a managed handle
     */
    ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> initCallback);

    // ── Disposal ──────────────────────────────────────────────────────────────

    /**
     * Permanently disposes of a managed shader, releasing its GL resources and
     * removing it from the reload list. The {@link ManagedShaderEffect} handle
     * must not be used afterwards.
     *
     * @param shader a shader previously obtained from {@link #manage(Identifier)}
     */
    void dispose(ManagedShaderEffect shader);
}
