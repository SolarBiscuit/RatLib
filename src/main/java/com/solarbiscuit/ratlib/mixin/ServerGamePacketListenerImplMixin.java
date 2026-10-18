package com.solarbiscuit.ratlib.mixin;

import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    // Vanilla rejects creative-mode stacks over 64, so dropping or placing a 100-stack from the
    // creative inventory silently deleted it. Allow up to the item's own max stack size instead.
    @ModifyConstant(method = "handleSetCreativeModeSlot", constant = @Constant(intValue = 64))
    private int ratlib$allowLargeCreativeStacks(int original, ServerboundSetCreativeModeSlotPacket packet) {
        return Math.max(original, packet.getItem().getMaxStackSize());
    }
}
