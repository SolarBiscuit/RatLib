package com.solarbiscuit.ratlib.faction;

import net.minecraft.resources.ResourceLocation;

/**
 * Optional faction membership for living entities. Companion mods such as RatRep
 * read this when present; content mods can implement it without requiring RatRep.
 */
public interface Factioned {
    ResourceLocation getFactionId();
}
