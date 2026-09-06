package com.solarbiscuit.ratlib.entity.crowd;

import com.solarbiscuit.ratlib.core.RL;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.WeakHashMap;

/**
 * Cheap crowding cache so path recasts can slow down when tagged mobs share a small area.
 */
public final class CrowdPathing {
    public static final TagKey<EntityType<?>> CROWD_COUNTED =
            TagKey.create(Registries.ENTITY_TYPE, RL.id("crowd_counted"));

    private static final int SAMPLE_INTERVAL = 20;
    private static final int CROWD_RADIUS = 16;
    private static final int CROWD_THRESHOLD = 24;
    private static final WeakHashMap<Mob, Cache> CACHE = new WeakHashMap<>();

    private CrowdPathing() {}

    public static boolean isCrowded(Mob mob) {
        if (mob.level().isClientSide) {
            return false;
        }
        Cache cache = CACHE.computeIfAbsent(mob, ignored -> new Cache());
        if (mob.tickCount - cache.lastSample >= SAMPLE_INTERVAL || cache.lastSample < 0) {
            cache.lastSample = mob.tickCount;
            AABB box = mob.getBoundingBox().inflate(CROWD_RADIUS);
            int count = 0;
            for (Entity other : mob.level().getEntities(mob, box, CrowdPathing::isCrowdCounted)) {
                count++;
                if (count >= CROWD_THRESHOLD) {
                    break;
                }
            }
            cache.crowded = count >= CROWD_THRESHOLD;
        }
        return cache.crowded;
    }

    public static int recastInterval(Mob mob) {
        return isCrowded(mob) ? 20 : 10;
    }

    public static boolean allowIdleStroll(Mob mob) {
        return !isCrowded(mob) || mob.getRandom().nextInt(3) == 0;
    }

    public static boolean shouldDeferHeavyScan(Mob mob) {
        return isCrowded(mob) && (mob.tickCount + mob.getId()) % 40 != 0;
    }

    public static boolean isCrowdCounted(Entity entity) {
        return entity.getType().is(CROWD_COUNTED);
    }

    private static final class Cache {
        private int lastSample = -100;
        private boolean crowded;
    }
}
