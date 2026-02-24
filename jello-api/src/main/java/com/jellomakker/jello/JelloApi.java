package com.jellomakker.jello;

import com.jellomakker.jello.api.event.ResolutionChangeCallback;
import com.jellomakker.jello.impl.ReloadableShaderEffectManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jello API – a Fabric 1.21.5+ client library for post-processing shaders.
 *
 * <h2>Quick start</h2>
 * <pre>{@code
 * // 1. Declare a managed effect (static field – obtained once, reloads automatically)
 * private static final ManagedShaderEffect MY_EFFECT =
 *     ShaderEffectManager.getInstance()
 *         .manage(Identifier.of("mymod", "shaders/post/myeffect.json"));
 *
 * // 2. Render it at the right time
 * ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
 *     MY_EFFECT.setUniformValue("Radius", 2.0f);
 *     MY_EFFECT.render(tickDelta);
 * });
 * }</pre>
 *
 * <p>The shader JSON lives at {@code assets/mymod/shaders/post/myeffect.json}
 * and follows the standard Minecraft post-effect pipeline format.
 */
public class JelloApi implements ClientModInitializer {

    public static final String MOD_ID = "jello-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[JelloApi] Initializing Jello API");

        // Wire resolution changes to the managed shader manager
        ResolutionChangeCallback.EVENT.register(
            ReloadableShaderEffectManager.INSTANCE::onResolutionChanged
        );
    }
}
