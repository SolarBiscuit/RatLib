package com.solarbiscuit.ratlib.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;

/**
 * A natural-spawn rule that applies to every RatLib-configured mob, whichever mod it is from
 * (e.g. "nothing but my scavenger in my Wasteland biomes"). Forced spawns (eggs, commands,
 * spawners) are never excluded. Register with {@link SpawnConfigEvents#registerExclusion}.
 */
@FunctionalInterface
public interface SpawnExclusion {
    boolean excludes(Mob mob, LevelAccessor level, BlockPos pos);
}
