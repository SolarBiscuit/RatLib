package com.solarbiscuit.ratlib.entity;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive regen for content-mod mobs: 1 heart (2 HP) every 30s once undamaged for 30s.
 * A mod opts its mobs in with {@link #register(String)}.
 */
@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PassiveRegen {
    public static final int INTERVAL_TICKS = 600;
    public static final float HEAL_AMOUNT = 2.0F;
    private static final String LAST_HURT = "ratlib.lastHurt";
    private static final String LAST_HEAL = "ratlib.lastHeal";

    private static volatile Set<String> namespaces = Set.of();
    /** Per-type result; checked for every living entity each tick. */
    private static final Map<EntityType<?>, Boolean> BY_TYPE = new ConcurrentHashMap<>();

    private PassiveRegen() {}

    /** Gives every mob registered under {@code namespace} (the mod id) passive regen. */
    public static synchronized void register(String namespace) {
        Set<String> next = new LinkedHashSet<>(namespaces);
        next.add(namespace);
        namespaces = Collections.unmodifiableSet(next);
        BY_TYPE.clear();
    }

    public static boolean applies(LivingEntity entity) {
        if (!(entity instanceof Mob) || namespaces.isEmpty()) {
            return false;
        }
        EntityType<?> type = entity.getType();
        Boolean cached = BY_TYPE.get(type);
        if (cached != null) {
            return cached;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (key == null) {
            // Not registered yet; don't cache a miss that may become a hit.
            return false;
        }
        boolean applies = namespaces.contains(key.getNamespace());
        BY_TYPE.put(type, applies);
        return applies;
    }

    public static void markHurt(LivingEntity entity) {
        if (!applies(entity)) {
            return;
        }
        long now = entity.level().getGameTime();
        CompoundTag data = entity.getPersistentData();
        data.putLong(LAST_HURT, now);
        data.putLong(LAST_HEAL, now);
    }

    public static void tick(LivingEntity entity) {
        // Runs for every living entity every tick: cheapest checks first.
        if (entity.level().isClientSide || !entity.isAlive() || entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }
        if (!applies(entity)) {
            return;
        }
        long now = entity.level().getGameTime();
        CompoundTag data = entity.getPersistentData();
        long lastHurt = data.getLong(LAST_HURT);
        if (lastHurt <= 0L) {
            data.putLong(LAST_HURT, now);
            data.putLong(LAST_HEAL, now);
            return;
        }
        if (now - lastHurt < INTERVAL_TICKS) {
            return;
        }
        long lastHeal = data.getLong(LAST_HEAL);
        if (now - lastHeal < INTERVAL_TICKS) {
            return;
        }
        entity.heal(HEAL_AMOUNT);
        data.putLong(LAST_HEAL, now);
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        tick(event.getEntity());
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        markHurt(event.getEntity());
    }
}
