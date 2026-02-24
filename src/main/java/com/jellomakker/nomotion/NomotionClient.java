package com.jellomakker.nomotion;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jello Blur – Frame-accumulation & radial motion blur for Fabric 1.21.8+.
 *
 * <p>Uses vanilla's {@link PostEffectProcessor} pipeline directly, following the
 * exact same pattern as {@code GameRenderer.renderBlur()} and spectator post-effects.
 * The shader is loaded from the {@link net.minecraft.client.gl.ShaderLoader} cache
 * every frame, which automatically handles resource reloads and resolution changes.
 */
public class NomotionClient implements ClientModInitializer {

    public static final String MOD_ID = "nomotion";
    private static final Logger LOGGER = LoggerFactory.getLogger("JelloBlur");

    private static final NomotionConfig CONFIG = new NomotionConfig();

    /** Set to true after the FIRST successful render, used for one-time log. */
    private static boolean loggedSuccess = false;
    /** Tracks the last effect ID that failed to load, prevents log spam. */
    private static Identifier lastFailedId = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[JelloBlur] Initializing motion blur");
        CONFIG.load();
    }

    /**
     * Called from {@code GameRendererMixin} after entity outlines are drawn.
     * This is the same hook point vanilla uses for its own post-process effects.
     *
     * @param mc   the MinecraftClient instance
     * @param pool the object allocator (GameRenderer's pool, or TRIVIAL as fallback)
     */
    public static void renderMotionBlur(MinecraftClient mc, ObjectAllocator pool) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.getFramebuffer() == null) return;

        // ── Should we render blur this frame? ─────────────────────────────
        if (!CONFIG.isEnabled()) return;

        if (CONFIG.isDisableInFluids() &&
            (player.isSubmergedInWater() || player.isInLava())) {
            return;
        }

        if (CONFIG.isOnlyWhenMoving()) {
            double speed = player.getVelocity().horizontalLengthSquared();
            boolean moving = speed > 0.0001;
            boolean looking = player.lastYaw != player.getYaw()
                           || player.lastPitch != player.getPitch();
            if (!moving && !looking) return;
        }

        // ── Determine which shader to use ─────────────────────────────────
        String prefix = CONFIG.getBlurType() == NomotionConfig.BlurType.RADIAL
                      ? "radial" : "blur";
        Identifier effectId = Identifier.of(MOD_ID, prefix + "_" + CONFIG.getStrength());

        // ── Load from ShaderLoader cache & render ─────────────────────────
        // This mirrors vanilla's approach in GameRenderer.render():
        //   PostEffectProcessor p = mc.getShaderLoader().loadPostEffect(id, MAIN_ONLY);
        //   if (p != null) p.render(framebuffer, pool);
        try {
            if (mc.getShaderLoader() == null) return;

            PostEffectProcessor processor = mc.getShaderLoader()
                .loadPostEffect(effectId, DefaultFramebufferSet.MAIN_ONLY);

            if (processor == null) {
                // Log once per unique failed ID
                if (!effectId.equals(lastFailedId)) {
                    LOGGER.warn("[JelloBlur] Failed to load post-effect: {}", effectId);
                    lastFailedId = effectId;
                }
                return;
            }

            // Reset failure tracking on success
            lastFailedId = null;

            processor.render(mc.getFramebuffer(), pool != null ? pool : ObjectAllocator.TRIVIAL);

            if (!loggedSuccess) {
                LOGGER.info("[JelloBlur] Motion blur active: {}", effectId);
                loggedSuccess = true;
            }
        } catch (Exception e) {
            // Never crash the game — log and continue
            if (!effectId.equals(lastFailedId)) {
                LOGGER.error("[JelloBlur] Error rendering post-effect {}: {}", effectId, e.getMessage());
                lastFailedId = effectId;
            }
        }
    }

    public static NomotionConfig getConfig() {
        return CONFIG;
    }
}
