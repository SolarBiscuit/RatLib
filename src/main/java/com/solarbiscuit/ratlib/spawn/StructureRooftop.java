package com.solarbiscuit.ratlib.spawn;

import com.solarbiscuit.ratlib.config.MobConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public final class StructureRooftop {
    private StructureRooftop() {}

    public static boolean shouldReject(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return shouldReject(level, pos, random, null);
    }

    /** Uses the rooftop chance of {@code typeId}'s mod, falling back to the global one. */
    public static boolean shouldReject(ServerLevelAccessor level, BlockPos pos, RandomSource random,
                                       @Nullable ResourceLocation typeId) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        double chance = MobConfigs.rooftopRejectChance(typeId);
        if (chance <= 0.0D) {
            return false;
        }
        var refs = server.structureManager().getAllStructuresAt(pos);
        if (refs.isEmpty()) {
            return false;
        }
        for (Structure structure : refs.keySet()) {
            StructureStart start = server.structureManager().getStructureAt(pos, structure);
            if (start == null || !start.isValid()) {
                continue;
            }
            var box = start.getBoundingBox();
            if (!box.isInside(pos)) {
                continue;
            }
            if (pos.getY() >= box.maxY() - 4) {
                return random.nextFloat() < chance;
            }
        }
        return false;
    }
}
