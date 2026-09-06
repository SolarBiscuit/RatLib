package com.solarbiscuit.ratlib.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public final class SkinIndex {
    public static final String TAG = "SkinIndex";

    private SkinIndex() {}

    public static EntityDataAccessor<Integer> define(Class<? extends Entity> type) {
        return SynchedEntityData.defineId(type, EntityDataSerializers.INT);
    }

    public static void register(SynchedEntityData data, EntityDataAccessor<Integer> accessor) {
        data.define(accessor, 0);
    }

    public static int get(SynchedEntityData data, EntityDataAccessor<Integer> accessor) {
        return data.get(accessor);
    }

    public static void set(SynchedEntityData data, EntityDataAccessor<Integer> accessor, int index, int max) {
        data.set(accessor, Math.floorMod(index, Math.max(1, max)));
    }

    public static void save(CompoundTag tag, int index) {
        tag.putInt(TAG, index);
    }

    public static int load(CompoundTag tag, int max) {
        return Math.floorMod(tag.getInt(TAG), Math.max(1, max));
    }
}
