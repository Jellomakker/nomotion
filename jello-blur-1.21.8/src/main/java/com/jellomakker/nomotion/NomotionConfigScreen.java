package com.jellomakker.nomotion;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * Config screen for NoMotion.  Accessible via Mod Menu.
 * Features: enable/disable, blur type, strength slider, only-when-moving, disable-in-fluids.
 */
public class NomotionConfigScreen extends Screen {

    private final Screen parent;

    public NomotionConfigScreen(Screen parent) {
        super(Text.literal("NoMotion Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        NomotionConfig config = NomotionClient.getConfig();
        int cx = this.width / 2;
        int top = this.height / 2 - 62;  // start a bit higher for 5 rows + done
        int rowH = 26;

        // ── Row 0: Enabled toggle ─────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
            enabledText(config.isEnabled()),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(enabledText(config.isEnabled()));
            }
        ).dimensions(cx - 100, top, 200, 20).build());

        // ── Row 1: Blur Type cycle ────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
            blurTypeText(config.getBlurType()),
            button -> {
                config.setBlurType(config.getBlurType().next());
                button.setMessage(blurTypeText(config.getBlurType()));
            }
        ).dimensions(cx - 100, top + rowH, 200, 20).build());

        // ── Row 2: Strength slider ────────────────────────────────────────
        double initial = (double)(config.getStrength() - NomotionConfig.MIN_STRENGTH)
                / (NomotionConfig.MAX_STRENGTH - NomotionConfig.MIN_STRENGTH);
        addDrawableChild(new SliderWidget(cx - 100, top + rowH * 2, 200, 20,
                strengthText(config.getStrength()), initial) {
            @Override
            protected void updateMessage() {
                setMessage(strengthText(pctFromValue(this.value)));
            }

            @Override
            protected void applyValue() {
                config.setStrength(pctFromValue(this.value));
            }
        });

        // ── Row 3: Only When Moving toggle ────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
            movingText(config.isOnlyWhenMoving()),
            button -> {
                config.setOnlyWhenMoving(!config.isOnlyWhenMoving());
                button.setMessage(movingText(config.isOnlyWhenMoving()));
            }
        ).dimensions(cx - 100, top + rowH * 3, 200, 20).build());

        // ── Row 4: Disable in Fluids toggle ───────────────────────────────
        addDrawableChild(ButtonWidget.builder(
            fluidsText(config.isDisableInFluids()),
            button -> {
                config.setDisableInFluids(!config.isDisableInFluids());
                button.setMessage(fluidsText(config.isDisableInFluids()));
            }
        ).dimensions(cx - 100, top + rowH * 4, 200, 20).build());

        // ── Done ──────────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> close()
        ).dimensions(cx - 100, top + rowH * 5 + 4, 200, 20).build());
    }

    @Override
    public void close() {
        NomotionClient.getConfig().save();
        if (client != null) client.setScreen(parent);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Convert slider 0.0-1.0 value to a snapped percentage (5-95). */
    private static int pctFromValue(double sliderValue) {
        int raw = (int) Math.round(sliderValue
                * (NomotionConfig.MAX_STRENGTH - NomotionConfig.MIN_STRENGTH)
                + NomotionConfig.MIN_STRENGTH);
        int snapped = (int)(Math.round((double) raw / NomotionConfig.STEP) * NomotionConfig.STEP);
        return Math.max(NomotionConfig.MIN_STRENGTH, Math.min(NomotionConfig.MAX_STRENGTH, snapped));
    }

    private static Text enabledText(boolean on) {
        return Text.literal("Motion Blur: " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private static Text blurTypeText(NomotionConfig.BlurType type) {
        return Text.literal("Blur Type: \u00a7b" + type.getLabel());
    }

    private static Text strengthText(int pct) {
        return Text.literal("Blur Strength: " + pct + "%");
    }

    private static Text movingText(boolean on) {
        return Text.literal("Only When Moving: " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private static Text fluidsText(boolean on) {
        return Text.literal("Disable in Fluids: " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }
}
