package com.solarbiscuit.ratlib.config;

import net.minecraft.resources.ResourceLocation;

/**
 * Default per-mob stats used when building a {@link MobSettingsSpec}.
 */
public record MobDefaults(
        ResourceLocation typeId,
        String path,
        double health,
        double damage,
        double speed,
        int spawnWeight,
        int minGroup,
        int maxGroup
) {}
