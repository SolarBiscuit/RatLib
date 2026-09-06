package com.solarbiscuit.ratlib.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Cached registry scans by item id path. Rebuilt when tags reload.
 */
@Mod.EventBusSubscriber(modid = "ratlib", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ItemPathIndex {
    private static volatile List<Item> seeds = List.of();
    private static volatile List<Item> saplings = List.of();

    private ItemPathIndex() {}

    public static void register(IEventBus ignored) {
        rebuild();
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        rebuild();
    }

    public static synchronized void rebuild() {
        Set<Item> seedSet = new LinkedHashSet<>(vanillaSeeds());
        List<Item> saplingList = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || item == Items.AIR) {
                continue;
            }
            String path = id.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("sapling")) {
                saplingList.add(item);
            }
            if ("minecraft".equals(id.getNamespace())) {
                continue;
            }
            if (path.contains("spawn_egg")) {
                continue;
            }
            if (path.contains("seed") && !path.contains("sapling")) {
                seedSet.add(item);
            }
        }
        seeds = List.copyOf(seedSet);
        if (saplingList.isEmpty()) {
            saplingList.addAll(vanillaSaplings());
        }
        saplings = List.copyOf(saplingList);
    }

    public static List<Item> seeds() {
        ensure();
        return seeds;
    }

    public static List<Item> saplings() {
        ensure();
        return saplings;
    }

    public static ItemStack randomSapling(int count) {
        List<Item> list = saplings();
        Item item = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        return new ItemStack(item, Math.max(1, count));
    }

    public static List<Item> containing(String needle, Predicate<ResourceLocation> include) {
        String key = needle.toLowerCase(Locale.ROOT);
        List<Item> result = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || item == Items.AIR) {
                continue;
            }
            if (id.getPath().toLowerCase(Locale.ROOT).contains(key) && include.test(id)) {
                result.add(item);
            }
        }
        return result;
    }

    private static void ensure() {
        if (seeds.isEmpty() && saplings.isEmpty()) {
            rebuild();
        }
    }

    private static List<Item> vanillaSeeds() {
        List<Item> list = new ArrayList<>();
        list.add(Items.WHEAT_SEEDS);
        list.add(Items.BEETROOT_SEEDS);
        list.add(Items.MELON_SEEDS);
        list.add(Items.PUMPKIN_SEEDS);
        list.add(Items.TORCHFLOWER_SEEDS);
        list.add(Items.PITCHER_POD);
        list.add(Items.COCOA_BEANS);
        return list;
    }

    private static List<Item> vanillaSaplings() {
        return List.of(
                Items.OAK_SAPLING,
                Items.BIRCH_SAPLING,
                Items.SPRUCE_SAPLING,
                Items.JUNGLE_SAPLING,
                Items.ACACIA_SAPLING,
                Items.DARK_OAK_SAPLING,
                Items.CHERRY_SAPLING,
                Items.MANGROVE_PROPAGULE
        );
    }
}
