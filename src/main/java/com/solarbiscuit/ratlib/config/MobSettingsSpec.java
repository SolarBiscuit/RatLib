package com.solarbiscuit.ratlib.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Per-mob server config block. Content mods supply defaults; RatLib owns the spec keys.
 */
public final class MobSettingsSpec {
    public final ResourceLocation typeId;
    public final ForgeConfigSpec.BooleanValue enabled;
    public final ForgeConfigSpec.DoubleValue health;
    public final ForgeConfigSpec.DoubleValue attackDamage;
    public final ForgeConfigSpec.DoubleValue movementSpeed;
    public final ForgeConfigSpec.IntValue spawnWeight;
    public final ForgeConfigSpec.IntValue minGroup;
    public final ForgeConfigSpec.IntValue maxGroup;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionBlacklist;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> biomeBlacklist;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> biomeWhitelist;
    public final ForgeConfigSpec.DoubleValue dropChanceMultiplier;
    public final int defaultWeight;
    public final int defaultMinGroup;
    public final int defaultMaxGroup;

    public MobSettingsSpec(ForgeConfigSpec.Builder builder, MobDefaults defaults) {
        this.typeId = defaults.typeId();
        this.defaultWeight = defaults.spawnWeight();
        this.defaultMinGroup = defaults.minGroup();
        this.defaultMaxGroup = defaults.maxGroup();
        builder.push(defaults.path());
        this.enabled = builder.comment("Allow this mob to spawn naturally.").define("enabled", true);
        this.health = builder.defineInRange("health", defaults.health(), 1.0D, 1024.0D);
        this.attackDamage = builder.defineInRange("attackDamage", defaults.damage(), 0.0D, 1024.0D);
        this.movementSpeed = builder.defineInRange("movementSpeed", defaults.speed(), 0.0D, 4.0D);
        this.spawnWeight = builder.comment("Natural spawn weight. Stacked datapack entries are scaled from this default.")
                .defineInRange("spawnWeight", defaults.spawnWeight(), 0, 1000);
        this.minGroup = builder.defineInRange("minGroup", defaults.minGroup(), 1, 16);
        this.maxGroup = builder.defineInRange("maxGroup", defaults.maxGroup(), 1, 16);
        this.dimensionBlacklist = builder.comment("Dimension ids that may never spawn this mob, e.g. minecraft:the_nether.")
                .defineListAllowEmpty(List.of("dimensionBlacklist"), ArrayList::new, o -> o instanceof String);
        this.biomeBlacklist = builder.comment("Biome ids that may never spawn this mob.")
                .defineListAllowEmpty(List.of("biomeBlacklist"), ArrayList::new, o -> o instanceof String);
        this.biomeWhitelist = builder.comment("If non-empty, this mob only spawns in these biomes (in addition to datapack defaults).")
                .defineListAllowEmpty(List.of("biomeWhitelist"), ArrayList::new, o -> o instanceof String);
        this.dropChanceMultiplier = builder.comment("Scales loot-table and injected drop chances.")
                .defineInRange("dropChanceMultiplier", 1.0D, 0.0D, 8.0D);
        builder.pop();
    }

    public List<String> whitelist() {
        List<? extends String> list = this.biomeWhitelist.get();
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> copy = new ArrayList<>();
        for (String entry : list) {
            if (entry != null && !entry.isBlank()) {
                copy.add(entry);
            }
        }
        return copy;
    }
}
