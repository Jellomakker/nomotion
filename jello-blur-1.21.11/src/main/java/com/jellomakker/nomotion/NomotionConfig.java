package com.jellomakker.nomotion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for NoMotion motion blur effect.
 * Persisted to {@code config/nomotion.json}.
 */
public class NomotionConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("nomotion.json");

    /** Strength bounds (percentage, step 5). */
    public static final int MIN_STRENGTH = 5;
    public static final int MAX_STRENGTH = 95;
    public static final int DEFAULT_STRENGTH = 50;
    public static final int STEP = 5;

    /** Available blur types. */
    public enum BlurType {
        ACCUMULATION("Accumulation"),
        RADIAL("Radial");

        private final String label;
        BlurType(String label) { this.label = label; }
        public String getLabel() { return label; }

        public BlurType next() {
            BlurType[] vals = values();
            return vals[(ordinal() + 1) % vals.length];
        }
    }

    // ── Persisted fields ──────────────────────────────────────────────────────
    private boolean enabled = true;
    private int strength = DEFAULT_STRENGTH;
    private BlurType blurType = BlurType.ACCUMULATION;
    private boolean onlyWhenMoving = false;
    private boolean disableInFluids = false;

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { this.enabled = value; }

    public int getStrength() {
        int clamped = Math.max(MIN_STRENGTH, Math.min(MAX_STRENGTH, strength));
        return (int)(Math.round((double) clamped / STEP) * STEP);
    }

    public void setStrength(int value) {
        int snapped = (int)(Math.round((double) value / STEP) * STEP);
        this.strength = Math.max(MIN_STRENGTH, Math.min(MAX_STRENGTH, snapped));
    }

    public BlurType getBlurType() { return blurType != null ? blurType : BlurType.ACCUMULATION; }
    public void setBlurType(BlurType type) { this.blurType = type; }

    public boolean isOnlyWhenMoving() { return onlyWhenMoving; }
    public void setOnlyWhenMoving(boolean value) { this.onlyWhenMoving = value; }

    public boolean isDisableInFluids() { return disableInFluids; }
    public void setDisableInFluids(boolean value) { this.disableInFluids = value; }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                NomotionConfig loaded = GSON.fromJson(r, NomotionConfig.class);
                if (loaded != null) {
                    this.enabled         = loaded.enabled;
                    this.strength        = loaded.getStrength();
                    this.blurType        = loaded.getBlurType();
                    this.onlyWhenMoving  = loaded.onlyWhenMoving;
                    this.disableInFluids = loaded.disableInFluids;
                }
            } catch (IOException e) {
                System.err.println("[NoMotion] Failed to read config: " + e.getMessage());
            }
        } else {
            save();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            System.err.println("[NoMotion] Failed to save config: " + e.getMessage());
        }
    }
}
