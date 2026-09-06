package com.solarbiscuit.ratlib.core;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.resources.ResourceLocation;

public final class RL {
    private RL() {}

    @SuppressWarnings("removal")
    public static ResourceLocation id(String path) {
        return new ResourceLocation(RatLib.MOD_ID, path);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
