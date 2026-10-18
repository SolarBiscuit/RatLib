package com.solarbiscuit.ratlib.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a copper value into a small, varied pile of coin stacks for drops.
 * Values are {@code long} so high tiers (100^9 copper for netherite) never overflow.
 * See {@link CoinService#mix(long, int)}.
 */
public final class CoinMixer {
    private CoinMixer() {}

    public static List<ItemStack> mix(CoinService coins, long copper, int maxStacks) {
        if (copper <= 0 || maxStacks <= 0 || !coins.isPresent()) {
            return List.of();
        }
        List<String> paths = new ArrayList<>();
        List<Long> unitList = new ArrayList<>();
        for (String path : coins.tierPaths()) {
            long unit = coins.unitValue(path);
            if (unit > 0 && coins.isTierEnabled(path) && coins.coin(path) != Items.AIR) {
                paths.add(path);
                unitList.add(unit);
            }
        }
        if (paths.isEmpty()) {
            return List.of();
        }
        int tiers = paths.size();
        long[] units = new long[tiers];
        int[] maxStack = new int[tiers];
        for (int i = 0; i < tiers; i++) {
            units[i] = unitList.get(i);
            maxStack[i] = Math.max(1, new ItemStack(coins.coin(paths.get(i))).getMaxStackSize());
        }

        // Greedy split, highest tier first.
        long[] counts = new long[tiers];
        long remaining = copper;
        for (int i = tiers - 1; i >= 0; i--) {
            counts[i] = remaining / units[i];
            remaining -= counts[i] * units[i];
        }

        // A single pile looks dull: break one coin down a tier at a time while the pile still fits.
        int top = highestNonZero(counts);
        if (top > 0 && top == lowestNonZero(counts)) {
            for (int i = top; i > 0 && counts[i] > 0; i--) {
                long split = units[i] / units[i - 1];
                counts[i]--;
                counts[i - 1] += split;
                if (stackCount(counts, maxStack) > maxStacks) {
                    counts[i]++;
                    counts[i - 1] -= split;
                    break;
                }
            }
        }

        // Fold the lowest tiers upward until the pile fits in maxStacks.
        while (stackCount(counts, maxStack) > maxStacks) {
            int low = lowestNonZero(counts);
            int up = low + 1;
            if (low < 0 || up >= tiers) {
                break;
            }
            long value = counts[low] * units[low];
            counts[low] = 0;
            counts[up] += value / units[up];
            long rem = (value % units[up]) / units[low];
            if (rem > 0) {
                counts[low] = rem;
                if (stackCount(counts, maxStack) > maxStacks) {
                    counts[low] = 0; // drop sub-unit change rather than exceed the cap
                }
            }
        }

        List<ItemStack> out = new ArrayList<>();
        for (int i = tiers - 1; i >= 0; i--) {
            Item item = coins.coin(paths.get(i));
            long count = counts[i];
            while (count > 0) {
                int size = (int) Math.min(count, maxStack[i]);
                out.add(new ItemStack(item, size));
                count -= size;
            }
        }
        return out;
    }

    private static long stackCount(long[] counts, int[] maxStack) {
        long stacks = 0;
        for (int i = 0; i < counts.length; i++) {
            stacks += (counts[i] + maxStack[i] - 1) / maxStack[i];
        }
        return stacks;
    }

    private static int highestNonZero(long[] counts) {
        for (int i = counts.length - 1; i >= 0; i--) {
            if (counts[i] > 0) {
                return i;
            }
        }
        return -1;
    }

    private static int lowestNonZero(long[] counts) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                return i;
            }
        }
        return -1;
    }
}
