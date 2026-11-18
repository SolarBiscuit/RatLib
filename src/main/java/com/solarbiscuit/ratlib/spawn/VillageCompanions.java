package com.solarbiscuit.ratlib.spawn;

import com.solarbiscuit.ratlib.RatLib;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Extra mobs that move into villages as they generate, next to the village's own villagers rather than instead
 * of them. When the first villager of a matching village is placed, each registered companion rolls its chance
 * once for that village and, on success, spawns beside that villager. Rolls are saved per dimension, so a village
 * generated across several sessions still gets one roll.
 */
@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VillageCompanions {
    /**
     * @param id         unique key for saved rolls, e.g. "mymod:vape_dealer"
     * @param structures which structure ids count as this companion's villages
     * @param enabled    checked before every roll (installed mods, config toggles)
     * @param chance     chance per village, 0-1
     */
    public record Companion(String id, Supplier<? extends EntityType<? extends Mob>> type,
                            Predicate<ResourceLocation> structures, BooleanSupplier enabled, DoubleSupplier chance) {}

    private static final List<Companion> COMPANIONS = new CopyOnWriteArrayList<>();

    private VillageCompanions() {}

    public static void register(Companion companion) {
        COMPANIONS.add(companion);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!COMPANIONS.isEmpty() && event.getLevel() instanceof ServerLevel level) {
            VillageCompanionData.load(level);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            VillageCompanionData.unload(level);
        }
    }

    /** Lowest priority so spawns other listeners cancelled are skipped. Runs on worldgen threads. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (COMPANIONS.isEmpty() || event.getSpawnType() != MobSpawnType.STRUCTURE
                || !(event.getEntity() instanceof Villager villager) || event.isSpawnCancelled()) {
            return;
        }
        ServerLevelAccessor level = event.getLevel();
        VillageCompanionData data = VillageCompanionData.get(level.getLevel());
        if (data == null) {
            return;
        }
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        ChunkAccess chunk = level.getChunk(pos);
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, LongSet> reference : chunk.getAllReferences().entrySet()) {
            ResourceLocation structureId = structures.getKey(reference.getKey());
            if (structureId == null) {
                continue;
            }
            for (Companion companion : COMPANIONS) {
                if (!companion.structures().test(structureId) || !companion.enabled().getAsBoolean()) {
                    continue;
                }
                for (long start : reference.getValue()) {
                    if (data.claim(companion.id(), structureId, start)
                            && villager.getRandom().nextDouble() < companion.chance().getAsDouble()) {
                        spawnBeside(companion, level, villager, pos);
                    }
                }
            }
        }
    }

    private static void spawnBeside(Companion companion, ServerLevelAccessor level, Villager villager, BlockPos pos) {
        Mob mob = companion.type().get().create(level.getLevel());
        if (mob == null) {
            return;
        }
        mob.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getRandom().nextFloat() * 360.0F, 0.0F);
        ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE,
                null, null);
        if (!mob.isSpawnCancelled()) {
            level.addFreshEntityWithPassengers(mob);
        }
    }
}
