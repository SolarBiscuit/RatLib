package com.solarbiscuit.ratlib.config;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * Registry of per-mob config specs. Empty unless a content mod registers entries.
 * Content mods register from their constructors, which Forge runs in parallel, so writes are
 * copy-on-write and reads never lock.
 */
public final class MobConfigs {
    private static volatile Map<ResourceLocation, MobSettingsSpec> byType = Map.of();
    private static volatile Map<String, DoubleSupplier> rooftopByNamespace = Map.of();
    private static volatile DoubleSupplier rooftopRejectChance = () -> 0.0D;

    private MobConfigs() {}

    public static synchronized void register(MobSettingsSpec spec) {
        Map<ResourceLocation, MobSettingsSpec> next = new LinkedHashMap<>(byType);
        next.put(spec.typeId, spec);
        byType = Collections.unmodifiableMap(next);
    }

    /** Fallback for mobs whose mod has not set its own chance. Prefer the namespaced overload. */
    public static void setRooftopRejectChance(DoubleSupplier chance) {
        rooftopRejectChance = chance;
    }

    /** Rooftop reject chance for every mob registered under {@code namespace} (the mod id). */
    public static synchronized void setRooftopRejectChance(String namespace, DoubleSupplier chance) {
        Map<String, DoubleSupplier> next = new LinkedHashMap<>(rooftopByNamespace);
        next.put(namespace, chance);
        rooftopByNamespace = Collections.unmodifiableMap(next);
    }

    public static double rooftopRejectChance() {
        return rooftopRejectChance.getAsDouble();
    }

    public static double rooftopRejectChance(@Nullable ResourceLocation typeId) {
        DoubleSupplier chance = typeId == null ? null : rooftopByNamespace.get(typeId.getNamespace());
        return (chance != null ? chance : rooftopRejectChance).getAsDouble();
    }

    public static boolean hasAny() {
        return !byType.isEmpty();
    }

    public static Collection<MobSettingsSpec> all() {
        return byType.values();
    }

    @Nullable
    public static MobSettingsSpec get(ResourceLocation typeId) {
        return byType.get(typeId);
    }

    @Nullable
    public static MobSettingsSpec get(EntityType<?> type) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key == null ? null : byType.get(key);
    }

    public static boolean isEnabled(EntityType<?> type) {
        MobSettingsSpec settings = get(type);
        return settings == null || settings.enabled.get();
    }

    public static boolean isDimensionBanned(EntityType<?> type, LevelAccessor level) {
        MobSettingsSpec settings = get(type);
        if (settings == null) {
            return false;
        }
        String dim = level instanceof Level world ? world.dimension().location().toString() : "";
        if (dim.isEmpty()) {
            return false;
        }
        return containsId(settings.dimensionBlacklist.get(), dim);
    }

    public static boolean isBiomeBanned(EntityType<?> type, LevelAccessor level, BlockPos pos) {
        MobSettingsSpec settings = get(type);
        if (settings == null) {
            return false;
        }
        Holder<Biome> biome = level.getBiome(pos);
        ResourceLocation biomeId = biome.unwrapKey().map(key -> key.location()).orElse(null);
        String id = biomeId == null ? "" : biomeId.toString();
        if (containsId(settings.biomeBlacklist.get(), id)) {
            return true;
        }
        List<? extends String> whitelist = settings.biomeWhitelist.get();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return !containsId(whitelist, id);
    }

    public static float dropMultiplier(@Nullable Entity entity) {
        if (entity == null) {
            return 1.0F;
        }
        MobSettingsSpec settings = get(entity.getType());
        if (settings == null) {
            return 1.0F;
        }
        return settings.dropChanceMultiplier.get().floatValue();
    }

    private static boolean containsId(List<? extends String> list, String value) {
        if (list == null || value == null || value.isEmpty()) {
            return false;
        }
        String needle = value.toLowerCase(Locale.ROOT);
        for (String entry : list) {
            if (entry != null && needle.equals(entry.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
