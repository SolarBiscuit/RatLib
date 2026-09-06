package com.solarbiscuit.ratlib.compat.curios;

import com.solarbiscuit.ratlib.compat.Mods;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public final class CuriosCompat {
    public static final String MOD_ID = "curios";

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return Mods.loaded(MOD_ID);
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        return isLoaded() && CuriosAccess.isEquipped(entity, item);
    }

    public static ItemStack getStack(LivingEntity entity, String slotId) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return CuriosAccess.getStack(entity, slotId);
    }

    public static ItemStack getStack(Player player, String identifier, int slot) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return CuriosAccess.getStack(player, identifier, slot);
    }

    public static void setStack(LivingEntity entity, String slotId, ItemStack stack) {
        if (isLoaded()) {
            CuriosAccess.setStack(entity, slotId, stack);
        }
    }

    public static boolean canEquip(LivingEntity entity, String slotId, ItemStack stack) {
        return isLoaded() && CuriosAccess.canEquip(entity, slotId, stack);
    }

    @Nullable
    public static String identifierMatching(Player player, Slot slot) {
        if (!isLoaded()) {
            return null;
        }
        return CuriosAccess.identifierMatching(player, slot);
    }

    public static int countInAllSlots(Player player, ToIntFunction<ItemStack> counter) {
        if (!isLoaded()) {
            return 0;
        }
        return CuriosAccess.countInAllSlots(player, counter);
    }

    public static ItemStack insertIntoSlot(Player player, String slotId, ItemStack remaining,
                                          InsertFunction insert) {
        if (!isLoaded() || remaining.isEmpty()) {
            return remaining;
        }
        return CuriosAccess.insertIntoSlot(player, slotId, remaining, insert);
    }

    @FunctionalInterface
    public interface InsertFunction {
        ItemStack insert(IItemHandler curiosSlotStacks, int slot, ItemStack stored, ItemStack remaining);
    }
}
