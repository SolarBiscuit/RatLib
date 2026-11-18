package com.solarbiscuit.ratlib.economy;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

/**
 * Coin drops for content-mod mobs. A mod registers a roll for its namespace; on death the rolled
 * copper is added to any coins already dropped (e.g. Rat Currency's own handler) and the total is
 * re-mixed into at most {@link #MAX_COIN_STACKS} stacks. Does nothing without a coin service.
 */
@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KillCoinDrops {
    public static final int MAX_COIN_STACKS = 5;

    private static volatile Map<String, ToLongFunction<LivingEntity>> rolls = Map.of();

    private KillCoinDrops() {}

    /**
     * Rolls coins (in copper) for every mob registered under {@code namespace}. Return 0 for no
     * coins, e.g. when the mod's own coin toggle is off.
     */
    public static synchronized void register(String namespace, ToLongFunction<LivingEntity> copperRoll) {
        Map<String, ToLongFunction<LivingEntity>> next = new LinkedHashMap<>(rolls);
        next.put(namespace, copperRoll);
        rolls = Collections.unmodifiableMap(next);
    }

    /** A roll spread evenly across each order of magnitude between {@code min} and {@code max}. */
    public static long rollLogUniform(RandomSource random, long min, long max) {
        min = Math.max(1L, min);
        max = Math.max(min, max);
        double logMin = Math.log(min);
        double rolled = Math.exp(logMin + random.nextDouble() * (Math.log(max) - logMin));
        return Math.max(min, Math.min(max, Math.round(rolled)));
    }

    // LOW so coins added by Rat Currency's own drop handler (default priority) are mixed in too.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void mixCoinDrops(LivingDropsEvent event) {
        if (rolls.isEmpty() || !CoinServices.isLoaded()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        ToLongFunction<LivingEntity> roll = key == null ? null : rolls.get(key.getNamespace());
        if (roll == null) {
            return;
        }
        CoinService coins = CoinServices.get();
        long copper = Math.max(0L, roll.applyAsLong(entity));
        List<ItemEntity> coinEntities = new ArrayList<>();
        for (ItemEntity item : event.getDrops()) {
            long value = coins.copperValue(item.getItem());
            // Only coins with a known value are re-mixed; anything else stays as dropped.
            if (value > 0) {
                copper = value > Long.MAX_VALUE - copper ? Long.MAX_VALUE : copper + value;
                coinEntities.add(item);
            }
        }
        if (copper <= 0) {
            return;
        }
        event.getDrops().removeAll(coinEntities);
        for (ItemStack stack : coins.mix(copper, MAX_COIN_STACKS)) {
            ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }
}
