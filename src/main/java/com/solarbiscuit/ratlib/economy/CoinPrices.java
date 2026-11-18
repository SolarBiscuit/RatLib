package com.solarbiscuit.ratlib.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Merchant prices in coins with an item fallback, for traders that should still work without a coin economy.
 */
public final class CoinPrices {
    private CoinPrices() {}

    /**
     * {@code count} coins of tier {@code coinPath} when {@code coinsAllowed} and the economy has that tier,
     * otherwise {@code fallbackCount} of {@code fallback}. Both are capped at one stack, the most a trade slot holds.
     */
    public static DualCost coinsOr(boolean coinsAllowed, String coinPath, int count, Item fallback, int fallbackCount) {
        if (coinsAllowed && CoinServices.isLoaded()) {
            ItemStack coins = CoinServices.get().stack(coinPath, 1);
            if (!coins.isEmpty()) {
                coins.setCount(clamp(count, coins.getMaxStackSize()));
                return new DualCost(coins, ItemStack.EMPTY);
            }
        }
        ItemStack stack = new ItemStack(fallback);
        stack.setCount(clamp(fallbackCount, stack.getMaxStackSize()));
        return new DualCost(stack, ItemStack.EMPTY);
    }

    /** Same count either way, e.g. gold coins or gold ingots one for one. */
    public static DualCost coinsOr(boolean coinsAllowed, String coinPath, int count, Item fallback) {
        return coinsOr(coinsAllowed, coinPath, count, fallback, count);
    }

    private static int clamp(int count, int max) {
        return Math.max(1, Math.min(count, Math.max(1, max)));
    }
}
