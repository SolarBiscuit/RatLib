package com.solarbiscuit.ratlib.spawn;

import net.minecraft.world.entity.AgeableMob;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/**
 * Tracks one natural spawn group across its FinalizeSpawn events: Forge hands the event's spawn
 * data to the next member of the same group. Extends {@link AgeableMob.AgeableMobGroupData}
 * because AgeableMob.finalizeSpawn casts the group data (and this never spawns babies).
 */
public final class PackSpawnData extends AgeableMob.AgeableMobGroupData {
    public boolean leaderSpawned;
    /** The group failed its spawn-chance roll, so every member is cancelled. */
    public boolean rejected;

    public PackSpawnData() {
        super(false);
    }

    /** This group's data, installing a fresh one on the event for the group's first member. */
    public static PackSpawnData of(MobSpawnEvent.FinalizeSpawn event) {
        PackSpawnData pack = event.getSpawnData() instanceof PackSpawnData existing ? existing : new PackSpawnData();
        event.setSpawnData(pack);
        return pack;
    }
}
