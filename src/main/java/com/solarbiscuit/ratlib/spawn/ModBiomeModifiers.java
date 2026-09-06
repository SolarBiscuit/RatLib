package com.solarbiscuit.ratlib.spawn;

import com.mojang.serialization.Codec;
import com.solarbiscuit.ratlib.RatLib;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, RatLib.MOD_ID);

    public static final RegistryObject<Codec<ForestNameSpawnBiomeModifier>> FOREST_NAME_SPAWNS =
            SERIALIZERS.register("forest_name_spawns", () -> ForestNameSpawnBiomeModifier.CODEC);

    private ModBiomeModifiers() {}
}
