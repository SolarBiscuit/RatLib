package com.solarbiscuit.ratlib.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * Touches Curios API types. Only invoked after {@link CuriosCompat#isLoaded()} is true.
 */
final class CuriosAccess {
    private CuriosAccess() {}

    static boolean isEquipped(LivingEntity entity, Item item) {
        if (entity == null || item == null) {
            return false;
        }
        LazyOptional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(entity);
        return curios.map(inv -> inv.isEquipped(item)).orElse(false);
    }

    static ItemStack getStack(LivingEntity entity, String slotId) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getStacksHandler(slotId)
                        .map(stacks -> stacks.getStacks().getStackInSlot(0))
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    static ItemStack getStack(Player player, String identifier, int slot) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getStacksHandler(identifier)
                        .map(stacks -> {
                            IDynamicStackHandler dynamic = stacks.getStacks();
                            if (slot < 0 || slot >= dynamic.getSlots()) {
                                return ItemStack.EMPTY;
                            }
                            return dynamic.getStackInSlot(slot);
                        })
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    static ItemStack getStackAt(LivingEntity entity, String slotId, int index) {
        return handler(entity, slotId)
                .filter(stacks -> index >= 0 && index < stacks.getStacks().getSlots())
                .map(stacks -> stacks.getStacks().getStackInSlot(index))
                .orElse(ItemStack.EMPTY);
    }

    static void setStackAt(LivingEntity entity, String slotId, int index, ItemStack stack) {
        handler(entity, slotId)
                .filter(stacks -> index >= 0 && index < stacks.getStacks().getSlots())
                .ifPresent(stacks -> stacks.getStacks().setStackInSlot(index, stack));
    }

    static int slotCount(LivingEntity entity, String slotId) {
        return handler(entity, slotId).map(stacks -> stacks.getStacks().getSlots()).orElse(0);
    }

    static void ensureSlotCount(LivingEntity entity, String slotId, int size, UUID modifierId, String name) {
        handler(entity, slotId).ifPresent(stacks -> {
            // The resize only lands on the next Curios tick, so never stack a second modifier.
            if (stacks.getModifiers().containsKey(modifierId)) {
                return;
            }
            int extra = size - stacks.getStacks().getSlots();
            if (extra > 0) {
                stacks.addPermanentModifier(new AttributeModifier(modifierId, name, extra,
                        AttributeModifier.Operation.ADDITION));
            }
        });
    }

    private static Optional<ICurioStacksHandler> handler(LivingEntity entity, String slotId) {
        if (entity == null) {
            return Optional.empty();
        }
        return CuriosApi.getCuriosInventory(entity).resolve().flatMap(inv -> inv.getStacksHandler(slotId));
    }

    static void setStack(LivingEntity entity, String slotId, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
                handler.getStacksHandler(slotId).ifPresent(stacks ->
                        stacks.getStacks().setStackInSlot(0, stack)));
    }

    static boolean canEquip(LivingEntity entity, String slotId, ItemStack stack) {
        if (entity == null || stack.isEmpty()) {
            return false;
        }
        return CuriosApi.getItemStackSlots(stack, entity).containsKey(slotId);
    }

    @Nullable
    static String identifierMatching(Player player, Slot slot) {
        if (!(slot instanceof SlotItemHandler handlerSlot)) {
            return null;
        }
        IItemHandler handler = handlerSlot.getItemHandler();
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            for (var entry : inv.getCurios().entrySet()) {
                if (entry.getValue().getStacks() == handler) {
                    return entry.getKey();
                }
            }
            return null;
        }).orElse(null);
    }

    static int countInAllSlots(Player player, ToIntFunction<ItemStack> counter) {
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            int total = 0;
            for (var entry : inv.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    total += counter.applyAsInt(stacks.getStackInSlot(i));
                }
            }
            return total;
        }).orElse(0);
    }

    static ItemStack insertIntoSlot(Player player, String slotId, ItemStack remaining,
                                    CuriosCompat.InsertFunction insert) {
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) {
            return remaining;
        }
        var handler = curios.getStacksHandler(slotId).orElse(null);
        if (handler == null) {
            return remaining;
        }
        IDynamicStackHandler stacks = handler.getStacks();
        for (int i = 0; i < stacks.getSlots() && !remaining.isEmpty(); i++) {
            remaining = insert.insert(stacks, i, stacks.getStackInSlot(i), remaining);
        }
        return remaining;
    }
}
