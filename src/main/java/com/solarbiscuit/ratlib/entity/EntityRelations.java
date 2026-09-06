package com.solarbiscuit.ratlib.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.Item;

import java.util.UUID;

/**
 * Shared living-entity helpers used by content mods.
 */
public final class EntityRelations {
    private EntityRelations() {}

    public static boolean sameOwner(LivingEntity a, LivingEntity b) {
        UUID ownerA = ownerId(a);
        UUID ownerB = ownerId(b);
        return ownerA != null && ownerA.equals(ownerB);
    }

    public static boolean isHolding(LivingEntity entity, Item item) {
        return entity.getMainHandItem().is(item) || entity.getOffhandItem().is(item);
    }

    private static UUID ownerId(LivingEntity entity) {
        if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        return null;
    }
}
