package com.solarbiscuit.ratlib.entity.merchant;

import com.google.common.collect.ImmutableSet;
import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Villager professions that belong to one custom merchant mob. They have no job site, so no villager can take
 * one up, and a villager or zombie villager that ends up with one anyway (zombie villagers roll a random
 * profession when they spawn) is reset to unemployed as it joins the world.
 */
@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExclusiveProfessions {
    private static final Set<VillagerProfession> EXCLUSIVE = new CopyOnWriteArraySet<>();

    private ExclusiveProfessions() {}

    /** Call from the profession's DeferredRegister supplier. */
    public static VillagerProfession create(String name, @Nullable SoundEvent workSound) {
        VillagerProfession profession = new VillagerProfession(name, poi -> false, poi -> false,
                ImmutableSet.of(), ImmutableSet.of(), workSound);
        EXCLUSIVE.add(profession);
        return profession;
    }

    public static boolean isExclusive(VillagerProfession profession) {
        return EXCLUSIVE.contains(profession);
    }

    /** The profession's name under the key villagers use, e.g. entity.minecraft.villager.mymod.vape_dealer. */
    public static Component displayName(VillagerProfession profession) {
        ResourceLocation id = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
        if (id == null) {
            return Component.literal(profession.name());
        }
        String prefix = "minecraft".equals(id.getNamespace()) ? "" : id.getNamespace() + ".";
        return Component.translatable(EntityType.VILLAGER.getDescriptionId() + "." + prefix + id.getPath());
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (EXCLUSIVE.isEmpty() || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof VillagerDataHolder holder)) {
            return;
        }
        VillagerData data = holder.getVillagerData();
        if (EXCLUSIVE.contains(data.getProfession())) {
            holder.setVillagerData(data.setProfession(VillagerProfession.NONE).setLevel(1));
        }
    }
}
