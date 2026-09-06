package com.solarbiscuit.ratlib.registry;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

import java.util.List;

public final class MobCatalog {
    private MobCatalog() {}

    public static void registerAttributes(Iterable<MobEntry<?>> entries, EntityAttributeCreationEvent event) {
        for (MobEntry<?> mob : entries) {
            mob.registerAttributes(event);
        }
    }

    public static void registerSpawns(Iterable<MobEntry<?>> entries, SpawnPlacementRegisterEvent event) {
        for (MobEntry<?> mob : entries) {
            mob.registerSpawn(event);
        }
    }

    public static List<MobEntry<?>> of(MobEntry<?>... entries) {
        return List.of(entries);
    }
}
