package com.jellomakker.nomotion;

import com.jellomakker.jello.api.event.ShaderEffectRenderCallback;
import com.jellomakker.jello.api.managed.ManagedShaderEffect;
import com.jellomakker.jello.api.managed.ShaderEffectManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Jello Blur – Frame-accumulation & radial motion blur for Fabric 1.21.5+.
 *
 * <p>Uses Jello API's {@link ShaderEffectManager} to manage post-process shader
 * lifecycle. The blur shader is registered via {@link ShaderEffectRenderCallback}.
 */
public class NomotionClient implements ClientModInitializer {

    public static final String MOD_ID = "nomotion";

    private static final NomotionConfig CONFIG = new NomotionConfig();
    private static ManagedShaderEffect currentEffect = null;
    private static String activeKey = "";

    @Override
    public void onInitializeClient() {
        System.out.println("[NoMotion] Initializing motion blur with Jello API");
        CONFIG.load();
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            boolean shouldRender = CONFIG.isEnabled();
            if (shouldRender && player != null) {
                if (CONFIG.isDisableInFluids() && (player.isSubmergedInWater() || player.isInLava())) {
                    shouldRender = false;
                }
                if (shouldRender && CONFIG.isOnlyWhenMoving()) {
                    double speed = player.getVelocity().horizontalLengthSquared();
                    boolean moving = speed > 1.0E-4;
                    boolean looking = player.lastYaw != player.getYaw()
                            || player.lastPitch != player.getPitch();
                    if (!moving && !looking) {
                        shouldRender = false;
                    }
                }
            }
            if (!shouldRender) {
                if (currentEffect != null) {
                    ShaderEffectManager.getInstance().dispose(currentEffect);
                    currentEffect = null;
                    activeKey = "";
                }
                return;
            }
            int pct = CONFIG.getStrength();
            NomotionConfig.BlurType type = CONFIG.getBlurType();
            String prefix = type == NomotionConfig.BlurType.RADIAL ? "radial" : "blur";
            String wantedKey = prefix + "_" + pct;
            if (!wantedKey.equals(activeKey)) {
                if (currentEffect != null) {
                    ShaderEffectManager.getInstance().dispose(currentEffect);
                }
                currentEffect = ShaderEffectManager.getInstance().manage(
                        Identifier.of(MOD_ID, wantedKey),
                        effect -> System.out.println("[NoMotion] Shader loaded: " + wantedKey));
                activeKey = wantedKey;
            }
            if (currentEffect != null) {
                currentEffect.render(tickDelta);
            }
        });
    }

    public static NomotionConfig getConfig() {
        return CONFIG;
    }
}
