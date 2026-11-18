package com.solarbiscuit.ratlib.spawn;

import com.solarbiscuit.ratlib.config.MobConfigs;
import com.solarbiscuit.ratlib.config.MobSettingsSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.ForgeRegistries;

public final class ConfiguredAttributes {
    private ConfiguredAttributes() {}

    public static void apply(LivingEntity entity) {
        MobSettingsSpec settings = MobConfigs.get(entity.getType());
        if (settings == null) {
            return;
        }
        float oldMax = entity.getMaxHealth();
        float health = entity.getHealth();
        setBase(entity, Attributes.MAX_HEALTH, settings.health.get());
        AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(settings.attackDamage.get());
        }
        setBase(entity, Attributes.MOVEMENT_SPEED, settings.movementSpeed.get());
        float newMax = entity.getMaxHealth();
        if (oldMax <= 0.01F) {
            entity.setHealth(newMax);
            return;
        }
        entity.setHealth(Math.max(1.0F, Math.min(newMax, health * (newMax / oldMax))));
    }

    public static boolean isForcedSpawn(MobSpawnType type) {
        return type == MobSpawnType.SPAWN_EGG
                || type == MobSpawnType.COMMAND
                || type == MobSpawnType.MOB_SUMMONED
                || type == MobSpawnType.DISPENSER
                || type == MobSpawnType.BUCKET
                || type == MobSpawnType.SPAWNER;
    }

    public static boolean allowNatural(Mob mob, MobSpawnType type, ServerLevelAccessor level,
                                      BlockPos pos, RandomSource random) {
        if (isForcedSpawn(type)) {
            return true;
        }
        if (!MobConfigs.isEnabled(mob.getType())) {
            return false;
        }
        if (MobConfigs.isDimensionBanned(mob.getType(), level)) {
            return false;
        }
        if (MobConfigs.isBiomeBanned(mob.getType(), level, pos)) {
            return false;
        }
        return !StructureRooftop.shouldReject(level, pos, random, ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()));
    }

    private static void setBase(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
