package com.solarbiscuit.ratlib.spawn;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.Locale;

public record ForestNameSpawnBiomeModifier(
        List<String> contains,
        boolean overworldOnly,
        MobCategory category,
        List<MobSpawnSettings.SpawnerData> spawners
) implements BiomeModifier {

    private static final Codec<List<String>> CONTAINS_CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf())
            .xmap(
                    either -> either.map(List::of, list -> list),
                    list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)
            );

    @SuppressWarnings("removal")
    private static final TagKey<Biome> FORGE_IS_FOREST =
            TagKey.create(Registries.BIOME, new ResourceLocation("forge", "is_forest"));

    public static final Codec<ForestNameSpawnBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CONTAINS_CODEC.optionalFieldOf("contains", List.of("forest")).forGetter(ForestNameSpawnBiomeModifier::contains),
            Codec.BOOL.optionalFieldOf("overworld_only", true).forGetter(ForestNameSpawnBiomeModifier::overworldOnly),
            MobCategory.CODEC.fieldOf("category").forGetter(ForestNameSpawnBiomeModifier::category),
            MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(ForestNameSpawnBiomeModifier::spawners)
    ).apply(instance, ForestNameSpawnBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || this.spawners.isEmpty()) {
            return;
        }
        if (this.overworldOnly && (isNetherOrEnd(biome) || isAether(biome))) {
            return;
        }
        if (!isForestLike(biome)) {
            return;
        }
        for (MobSpawnSettings.SpawnerData spawner : this.spawners) {
            builder.getMobSpawnSettings().addSpawn(this.category, spawner);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }

    private boolean isForestLike(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(FORGE_IS_FOREST)) {
            return true;
        }
        ResourceLocation id = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) {
            return false;
        }
        String path = id.getPath().toLowerCase(Locale.ROOT);
        String strippedPath = path.replace("_", "").replace("-", "");
        for (String rawNeedle : this.contains) {
            String needle = rawNeedle.toLowerCase(Locale.ROOT);
            if (path.contains(needle)) {
                return true;
            }
            String strippedNeedle = needle.replace("_", "").replace("-", "");
            if (strippedPath.contains(strippedNeedle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNetherOrEnd(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END);
    }

    private static boolean isAether(Holder<Biome> biome) {
        ResourceLocation id = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        return id != null && "aether".equals(id.getNamespace());
    }
}
