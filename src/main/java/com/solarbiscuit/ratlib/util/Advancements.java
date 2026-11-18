package com.solarbiscuit.ratlib.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class Advancements {
    private Advancements() {}

    /** Grants one criterion of a datapack advancement; does nothing if the advancement isn't loaded. */
    public static void award(ServerPlayer player, ResourceLocation advancementId, String criterion) {
        if (player == null || player.server == null) {
            return;
        }
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
