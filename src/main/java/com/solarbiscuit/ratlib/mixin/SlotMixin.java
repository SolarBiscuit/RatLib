package com.solarbiscuit.ratlib.mixin;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true)
    private void ratlib$honorCurrentStackMax(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = ((Slot) (Object) this).getItem();
        if (stack.isEmpty()) {
            return;
        }
        int itemMax = stack.getMaxStackSize();
        if (itemMax > cir.getReturnValue()) {
            cir.setReturnValue(itemMax);
        }
    }

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void ratlib$honorItemMaxStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isEmpty()) {
            return;
        }
        int itemMax = stack.getMaxStackSize();
        if (itemMax > cir.getReturnValue()) {
            cir.setReturnValue(itemMax);
        }
    }
}
