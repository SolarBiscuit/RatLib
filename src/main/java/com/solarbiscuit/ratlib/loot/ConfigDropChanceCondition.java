package com.solarbiscuit.ratlib.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.solarbiscuit.ratlib.config.MobConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ConfigDropChanceCondition implements LootItemCondition {
    public static final ConfigDropChanceCondition INSTANCE = new ConfigDropChanceCondition();

    private ConfigDropChanceCondition() {}

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.DROP_RATE.get();
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        float multiplier = MobConfigs.dropMultiplier(entity);
        if (multiplier >= 1.0F) {
            return true;
        }
        if (multiplier <= 0.0F) {
            return false;
        }
        return context.getRandom().nextFloat() < multiplier;
    }

    public static LootItemCondition.Builder builder() {
        return () -> INSTANCE;
    }

    public static final class ConditionSerializer implements Serializer<ConfigDropChanceCondition> {
        @Override
        public void serialize(JsonObject json, ConfigDropChanceCondition value, JsonSerializationContext context) {
        }

        @Override
        public ConfigDropChanceCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return INSTANCE;
        }
    }
}
