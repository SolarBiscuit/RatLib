package com.solarbiscuit.ratlib.economy;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Optional coin economy. Rat Currency registers an implementation; other mods consume
 * {@link CoinServices#get()} and fall back to emeralds when nothing is registered.
 */
public interface CoinService {
    CoinService NONE = new None();

    default boolean isPresent() {
        return this != NONE;
    }

    boolean isCoin(ItemStack stack);

    boolean isGoldCoin(ItemStack stack);

    Item coin(String path);

    ItemStack stack(String path, int count);

    int coinsPerTier();

    int tierIndex(String path);

    List<ItemStack> denominate(int copperUnits);

    List<ItemStack> mixedFromCopper(int copperValue);

    List<ItemStack> randomPurse(RandomSource random, int maxGold);

    int count(Player player, String tierPath);

    boolean tryTake(Player player, String tierPath, int count);

    ItemStack insert(Player player, ItemStack coins);

    DualCost exact(String path, int count, int emeraldFallback);

    DualCost floorWithVariance(String floorPath, int floorCount, RandomSource random, int emeraldFallback);

    /** Coin tier item paths, lowest value first. Empty when the economy has no tiers. */
    default List<String> tierPaths() {
        return Collections.emptyList();
    }

    /** Whether this tier may be handed out (e.g. not disabled in config). */
    default boolean isTierEnabled(String path) {
        return true;
    }

    /** Copper value of one coin of this tier, or 0 when the path is not a tier. */
    default long unitValue(String path) {
        int index = tierIndex(path);
        if (index < 0) {
            return 0L;
        }
        long per = Math.max(1, coinsPerTier());
        long unit = 1L;
        for (int i = 0; i < index; i++) {
            if (unit > Long.MAX_VALUE / per) {
                return Long.MAX_VALUE;
            }
            unit *= per;
        }
        return unit;
    }

    /** Tier path of this stack's coin, or null when it is not a tiered coin. */
    @Nullable
    default String tierPath(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (String path : tierPaths()) {
            if (stack.is(coin(path))) {
                return path;
            }
        }
        return null;
    }

    /** Total copper value of a stack; 0 for non-coins and coins without a tier value. */
    default long copperValue(ItemStack stack) {
        String path = tierPath(stack);
        if (path == null) {
            return 0L;
        }
        long unit = unitValue(path);
        return unit > Long.MAX_VALUE / Math.max(1, stack.getCount()) ? Long.MAX_VALUE : unit * stack.getCount();
    }

    /** Mixed-denomination drop worth {@code copper}, at most {@code maxStacks} stacks. See {@link CoinMixer}. */
    default List<ItemStack> mix(long copper, int maxStacks) {
        return CoinMixer.mix(this, copper, maxStacks);
    }

    final class None implements CoinService {
        @Override
        public boolean isCoin(ItemStack stack) {
            return false;
        }

        @Override
        public boolean isGoldCoin(ItemStack stack) {
            return false;
        }

        @Override
        public Item coin(String path) {
            return Items.AIR;
        }

        @Override
        public ItemStack stack(String path, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public int coinsPerTier() {
            return 100;
        }

        @Override
        public int tierIndex(String path) {
            return -1;
        }

        @Override
        public List<ItemStack> denominate(int copperUnits) {
            return Collections.emptyList();
        }

        @Override
        public List<ItemStack> mixedFromCopper(int copperValue) {
            return Collections.emptyList();
        }

        @Override
        public List<ItemStack> randomPurse(RandomSource random, int maxGold) {
            return Collections.emptyList();
        }

        @Override
        public int count(Player player, String tierPath) {
            return 0;
        }

        @Override
        public boolean tryTake(Player player, String tierPath, int count) {
            return false;
        }

        @Override
        public ItemStack insert(Player player, ItemStack coins) {
            return coins;
        }

        @Override
        public DualCost exact(String path, int count, int emeraldFallback) {
            return DualCost.emeralds(emeraldFallback);
        }

        @Override
        public DualCost floorWithVariance(String floorPath, int floorCount, RandomSource random, int emeraldFallback) {
            return DualCost.emeralds(emeraldFallback);
        }
    }
}
