package com.solarbiscuit.ratlib.compat;

import net.minecraftforge.fml.ModList;

public final class Mods {
    private Mods() {}

    public static boolean loaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
