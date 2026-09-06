package com.solarbiscuit.ratlib.spawn;

import com.solarbiscuit.ratlib.RatLib;
import com.solarbiscuit.ratlib.config.MobConfigs;
import com.solarbiscuit.ratlib.config.MobSettingsSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpawnConfigEvents {
    private static SpawnPolicy policy = SpawnPolicy.NONE;

    private SpawnConfigEvents() {}

    public static void setPolicy(SpawnPolicy next) {
        policy = next == null ? SpawnPolicy.NONE : next;
    }

    @SubscribeEvent
    public static void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (!MobConfigs.hasAny()) {
            return;
        }
        List<MobSpawnSettings.SpawnerData> list = event.getSpawnerDataList();
        if (list == null || list.isEmpty()) {
            if (event.getMobCategory() == MobCategory.CREATURE || event.getMobCategory() == MobCategory.MONSTER) {
                addWhitelistedSpawns(event);
            }
            return;
        }
        BlockPos pos = event.getPos();
        List<MobSpawnSettings.SpawnerData> snapshot = new ArrayList<>(list);
        for (MobSpawnSettings.SpawnerData data : snapshot) {
            MobSettingsSpec settings = MobConfigs.get(data.type);
            if (settings == null) {
                continue;
            }
            if (!settings.enabled.get()
                    || MobConfigs.isDimensionBanned(data.type, event.getLevel())
                    || MobConfigs.isBiomeBanned(data.type, event.getLevel(), pos)) {
                event.removeSpawnerData(data);
                continue;
            }
            int existingWeight = data.getWeight().asInt();
            int defaultWeight = Math.max(1, settings.defaultWeight);
            int configured = settings.spawnWeight.get();
            int scaled = Math.max(1, Math.round(existingWeight * (configured / (float) defaultWeight)));
            boolean customGroups = settings.minGroup.get() != settings.defaultMinGroup
                    || settings.maxGroup.get() != settings.defaultMaxGroup;
            int min = data.minCount;
            int max = data.maxCount;
            ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(data.type);
            if (typeId != null && policy.forceSoloGroup(typeId)) {
                min = 1;
                max = 1;
            } else if (customGroups) {
                min = Math.min(settings.minGroup.get(), settings.maxGroup.get());
                max = Math.max(settings.minGroup.get(), settings.maxGroup.get());
            }
            if (scaled != existingWeight || min != data.minCount || max != data.maxCount) {
                event.removeSpawnerData(data);
                event.addSpawnerData(new MobSpawnSettings.SpawnerData(data.type, scaled, min, max));
            }
        }

        if (event.getMobCategory() == MobCategory.CREATURE || event.getMobCategory() == MobCategory.MONSTER) {
            addWhitelistedSpawns(event);
        }
    }

    private static void addWhitelistedSpawns(LevelEvent.PotentialSpawns event) {
        BlockPos pos = event.getPos();
        for (MobSettingsSpec settings : MobConfigs.all()) {
            if (policy.skipWhitelist(settings.typeId)) {
                continue;
            }
            List<String> whitelist = settings.whitelist();
            if (whitelist.isEmpty() || !settings.enabled.get()) {
                continue;
            }
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(settings.typeId);
            if (type == null || type.getCategory() != event.getMobCategory()) {
                continue;
            }
            if (MobConfigs.isDimensionBanned(type, event.getLevel())
                    || MobConfigs.isBiomeBanned(type, event.getLevel(), pos)) {
                continue;
            }
            boolean present = false;
            for (MobSpawnSettings.SpawnerData data : event.getSpawnerDataList()) {
                if (data.type == type) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                int min = Math.min(settings.minGroup.get(), settings.maxGroup.get());
                int max = Math.max(settings.minGroup.get(), settings.maxGroup.get());
                event.addSpawnerData(new MobSpawnSettings.SpawnerData(type, Math.max(1, settings.spawnWeight.get()), min, max));
            }
        }
    }

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (!MobConfigs.hasAny()) {
            return;
        }
        Mob mob = event.getEntity();
        if (MobConfigs.get(mob.getType()) == null) {
            return;
        }
        if (!ConfiguredAttributes.allowNatural(mob, event.getSpawnType(), event.getLevel(),
                BlockPos.containing(event.getX(), event.getY(), event.getZ()), event.getLevel().getRandom())) {
            event.setResult(Event.Result.DENY);
            return;
        }
        if (policy.extraDenyPosition(mob, event)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!MobConfigs.hasAny()) {
            return;
        }
        Mob mob = event.getEntity();
        if (MobConfigs.get(mob.getType()) == null) {
            return;
        }
        ConfiguredAttributes.apply(mob);
        if (ConfiguredAttributes.isForcedSpawn(event.getSpawnType())) {
            return;
        }
        if (policy.extraCancelFinalize(event)) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!MobConfigs.hasAny() || event.getLevel().isClientSide) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob) || MobConfigs.get(mob.getType()) == null) {
            return;
        }
        ConfiguredAttributes.apply(mob);
    }
}
