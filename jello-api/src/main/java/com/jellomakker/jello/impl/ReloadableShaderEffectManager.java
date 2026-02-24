package com.jellomakker.jello.impl;

import com.jellomakker.jello.api.managed.ManagedShaderEffect;
import com.jellomakker.jello.api.managed.ShaderEffectManager;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Singleton implementation of {@link ShaderEffectManager}.
 *
 * <p>All managed shader effects are released on:
 * <ul>
 *   <li>World renderer reload (resource pack change, F3+T)</li>
 *   <li>Screen resolution change</li>
 * </ul>
 * Released effects are re-initialized lazily on the next {@link ManagedShaderEffect#render(float)} call.
 */
public class ReloadableShaderEffectManager implements ShaderEffectManager {

    /** The global singleton. */
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();

    /**
     * Tracks all live managed effects. WeakHashMap so that callers who truly no
     * longer reference their effect don't prevent GC (they should call dispose() though).
     */
    private final Set<ManagedShaderEffectImpl> managed =
        Collections.newSetFromMap(new WeakHashMap<>());

    private ReloadableShaderEffectManager() {}

    // ── ShaderEffectManager ───────────────────────────────────────────────────

    @Override
    public ManagedShaderEffect manage(Identifier location) {
        return manage(location, e -> {});
    }

    @Override
    public ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        ManagedShaderEffectImpl effect = new ManagedShaderEffectImpl(location, initCallback);
        synchronized (managed) {
            managed.add(effect);
        }
        return effect;
    }

    @Override
    public void dispose(ManagedShaderEffect shader) {
        shader.release();
        synchronized (managed) {
            managed.remove(shader);
        }
    }

    // ── Lifecycle callbacks ───────────────────────────────────────────────────

    /**
     * Called when the world renderer (and therefore the ShaderLoader cache) has been reloaded.
     * All managed effects must be invalidated so they reload from the fresh cache.
     */
    public void onWorldRendererReload() {
        synchronized (managed) {
            for (ManagedShaderEffectImpl effect : managed) {
                effect.onReload();
            }
        }
    }

    /**
     * Called when the framebuffer resolution changes.
     * In MC 1.21.5+ the pipeline targets are screen-sized by default, so this
     * is equivalent to a full reload – existing processors are invalidated and
     * will be recreated lazily at the new size.
     *
     * @param width  new framebuffer width
     * @param height new framebuffer height
     */
    public void onResolutionChanged(int width, int height) {
        onWorldRendererReload();
    }
}
