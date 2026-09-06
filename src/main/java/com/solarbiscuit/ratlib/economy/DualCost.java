package com.solarbiscuit.ratlib.economy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

public record DualCost(ItemStack primary, ItemStack secondary) {
    public static DualCost emeralds(int count) {
        return new DualCost(new ItemStack(Items.EMERALD, Math.max(1, count)), ItemStack.EMPTY);
    }

    public MerchantOffer toOffer(ItemStack result, int maxUses, int xp) {
        if (this.secondary.isEmpty()) {
            return new MerchantOffer(this.primary.copy(), result, maxUses, xp, 0.05F);
        }
        return new MerchantOffer(this.primary.copy(), this.secondary.copy(), result, maxUses, xp, 0.05F);
    }
}
