package com.solarbiscuit.ratlib.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public final class Dimensions {
    private Dimensions() {}

    /** Any dimension from The Aether mod. */
    public static boolean isAether(LevelAccessor level) {
        return level instanceof Level world && isAether(world.dimension().location());
    }

    public static boolean isAether(ResourceLocation dimension) {
        return dimension != null && "aether".equals(dimension.getNamespace());
    }
}
