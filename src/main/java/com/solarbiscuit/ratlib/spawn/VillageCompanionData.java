package com.solarbiscuit.ratlib.spawn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which villages each {@link VillageCompanions} entry has already rolled for, per dimension. Loaded on the server
 * thread when the level loads; worldgen threads only touch the concurrent set.
 */
final class VillageCompanionData extends SavedData {
    private static final String NAME = "ratlib_village_companions";
    private static final Map<ResourceKey<Level>, VillageCompanionData> LOADED = new ConcurrentHashMap<>();

    private final Set<String> claimed = ConcurrentHashMap.newKeySet();

    static void load(ServerLevel level) {
        LOADED.put(level.dimension(), level.getDataStorage()
                .computeIfAbsent(VillageCompanionData::read, VillageCompanionData::new, NAME));
    }

    static void unload(ServerLevel level) {
        LOADED.remove(level.dimension());
    }

    @Nullable
    static VillageCompanionData get(ServerLevel level) {
        return LOADED.get(level.dimension());
    }

    /** True for the first caller per companion and village start; later calls for the same village get false. */
    boolean claim(String companion, ResourceLocation structure, long startChunk) {
        ChunkPos start = new ChunkPos(startChunk);
        boolean added = this.claimed.add(companion + "|" + structure + "|" + start.x + "," + start.z);
        if (added) {
            this.setDirty();
        }
        return added;
    }

    private static VillageCompanionData read(CompoundTag tag) {
        VillageCompanionData data = new VillageCompanionData();
        ListTag list = tag.getList("Claimed", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.claimed.add(list.getString(i));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String key : this.claimed) {
            list.add(StringTag.valueOf(key));
        }
        tag.put("Claimed", list);
        return tag;
    }
}
