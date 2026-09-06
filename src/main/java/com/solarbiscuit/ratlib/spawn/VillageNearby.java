package com.solarbiscuit.ratlib.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Locale;
import java.util.Map;

public final class VillageNearby {
    private VillageNearby() {}

    public static boolean checkVillageSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                           BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.MOB_SUMMONED || spawnType == MobSpawnType.DISPENSER) {
            return true;
        }
        return nearVillageStructure(level, pos);
    }

    public static boolean nearVillageStructure(ServerLevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return true;
        }
        var chunkPos = new net.minecraft.world.level.ChunkPos(pos);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (!server.hasChunk(chunkPos.x + dx, chunkPos.z + dz)) {
                    continue;
                }
                ChunkAccess chunk = server.getChunk(chunkPos.x + dx, chunkPos.z + dz, ChunkStatus.STRUCTURE_STARTS, false);
                if (chunk != null && hasVillageStart(server, chunk)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasVillageStart(ServerLevel server, ChunkAccess chunk) {
        Map<Structure, StructureStart> starts = chunk.getAllStarts();
        var registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            StructureStart start = entry.getValue();
            if (start == null || !start.isValid()) {
                continue;
            }
            ResourceLocation id = registry.getKey(entry.getKey());
            if (id == null) {
                continue;
            }
            String path = id.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("village")) {
                return true;
            }
        }
        return false;
    }
}
