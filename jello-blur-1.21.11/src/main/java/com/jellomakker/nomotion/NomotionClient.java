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
 * Jello Blur – motion blur for Fabric 1.21.11+.
 *
 * <p>In 1.21.11, post-processing shaders are applied via
 * {@link MinecraftClient#getShaderLoader()} and rendered with the new
 * {@link ObjectAllocator} GPU pool API. Rendering is triggered each frame
 * through {@link com.jellomakker.nomotion.mixin.GameRendererMixin}.
 */
public class NomotionClient implements ClientModInitializer {

    public static final String MOD_ID = "nomotion";
    private static final Logger LOGGER = LoggerFactory.getLogger("JelloBlur");
    private static final NomotionConfig CONFIG = new NomotionConfig();
    private static boolean loggedSuccess = false;
    private static Identifier lastFailedId = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[JelloBlur] Initializing motion blur");
        CONFIG.load();
    }

    /**
     * Renders motion blur post-processing. Called by
     * {@link com.jellomakker.nomotion.mixin.GameRendererMixin} after entity
     * outline compositing, each frame.
     *
     * @param mc   the Minecraft client instance
     * @param pool the per-frame GPU object allocator from {@code GameRenderer}
     */
    public static void renderMotionBlur(MinecraftClient mc, ObjectAllocator pool) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.getFramebuffer() == null) {
            return;
        }
        if (!CONFIG.isEnabled()) {
            return;
        }
        if (CONFIG.isDisableInFluids() && (player.isSubmergedInWater() || player.isInLava())) {
            return;
        }
        if (CONFIG.isOnlyWhenMoving()) {
            double speed = player.getVelocity().horizontalLengthSquared();
            boolean moving = speed > 1.0E-4;
            boolean looking = player.lastYaw != player.getYaw()
                    || player.lastPitch != player.getPitch();
            if (!moving && !looking) {
                return;
            }
        }

        String prefix = CONFIG.getBlurType() == NomotionConfig.BlurType.RADIAL ? "radial" : "blur";
        Identifier effectId = Identifier.of(MOD_ID, prefix + "_" + CONFIG.getStrength());

        try {
            if (mc.getShaderLoader() == null) {
                return;
            }
            PostEffectProcessor processor = mc.getShaderLoader().loadPostEffect(
                    effectId, DefaultFramebufferSet.MAIN_ONLY);
            if (processor == null) {
                if (!effectId.equals(lastFailedId)) {
                    LOGGER.warn("[JelloBlur] Failed to load post-effect: {}", effectId);
                    lastFailedId = effectId;
                }
                return;
            }
            lastFailedId = null;
            processor.render(mc.getFramebuffer(), pool != null ? pool : ObjectAllocator.TRIVIAL);
            if (!loggedSuccess) {
                LOGGER.info("[JelloBlur] Motion blur active: {}", effectId);
                loggedSuccess = true;
            }
        } catch (Exception e) {
            if (effectId.equals(lastFailedId)) return;
            LOGGER.error("[JelloBlur] Error rendering post-effect {}: {}", effectId, e.getMessage());
            lastFailedId = effectId;
        }
    }

    public static NomotionConfig getConfig() {
        return CONFIG;
    }
}
