package com.solarbiscuit.ratlib.loot;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, RatLib.MOD_ID);

    public static final RegistryObject<LootItemConditionType> DROP_RATE = CONDITIONS.register("drop_rate",
            () -> new LootItemConditionType(new ConfigDropChanceCondition.ConditionSerializer()));

    private ModLootConditions() {}
}
