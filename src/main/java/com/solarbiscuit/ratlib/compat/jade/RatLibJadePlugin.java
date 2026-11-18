package com.solarbiscuit.ratlib.compat.jade;

import com.solarbiscuit.ratlib.core.RL;
import com.solarbiscuit.ratlib.entity.merchant.ProfessionTrader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Shows a {@link ProfessionTrader}'s profession the way Jade shows a villager's. */
@WailaPlugin
public class RatLibJadePlugin implements IWailaPlugin {
    public static final ResourceLocation MERCHANT_PROFESSION = RL.id("merchant_profession");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ProfessionProvider.INSTANCE, ProfessionTrader.class);
    }

    enum ProfessionProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof ProfessionTrader trader) {
                tooltip.add(Component.translatable("tooltip.jade.villager_profession", trader.getProfessionName()));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return MERCHANT_PROFESSION;
        }
    }
}
