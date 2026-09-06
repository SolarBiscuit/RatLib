package com.solarbiscuit.ratlib.spawn;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/**
 * Content-mod extras for spawn rewriting. Defaults do nothing.
 */
public interface SpawnPolicy {
    SpawnPolicy NONE = new SpawnPolicy() {};

    default boolean skipWhitelist(ResourceLocation typeId) {
        return false;
    }

    default boolean forceSoloGroup(ResourceLocation typeId) {
        return false;
    }

    default boolean extraDenyPosition(Mob mob, MobSpawnEvent.PositionCheck event) {
        return false;
    }

    default boolean extraCancelFinalize(MobSpawnEvent.FinalizeSpawn event) {
        return false;
    }
}
