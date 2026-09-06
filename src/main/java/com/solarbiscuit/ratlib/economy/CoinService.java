package com.solarbiscuit.ratlib.economy;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
