package com.solarbiscuit.ratlib.inventory;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Right-click an item in any container screen to open it. Content mods register a predicate + handler.
 */
public final class OpenFromSlot {
    private static BiPredicate<ItemStack, AbstractContainerMenu> predicate = (stack, menu) -> false;
    private static Handler handler = (player, menu, slot) -> false;

    private OpenFromSlot() {}

    public static void register(BiPredicate<ItemStack, AbstractContainerMenu> match, Handler open) {
        predicate = match;
        handler = open;
    }

    public static boolean matches(ItemStack stack, AbstractContainerMenu menu) {
        return !stack.isEmpty() && predicate.test(stack, menu);
    }

    @FunctionalInterface
    public interface Handler {
        boolean tryOpen(ServerPlayer player, AbstractContainerMenu menu, Slot slot);
    }

    public static final class Packet {
        private final int slotIndex;

        public Packet(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        public static void encode(Packet packet, FriendlyByteBuf buf) {
            buf.writeVarInt(packet.slotIndex);
        }

        public static Packet decode(FriendlyByteBuf buf) {
            return new Packet(buf.readVarInt());
        }

        public static void handle(Packet packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (packet.slotIndex < 0 || packet.slotIndex >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.getSlot(packet.slotIndex);
            if (!matches(slot.getItem(), menu)) {
                return;
            }
            handler.tryOpen(player, menu, slot);
        }
    }

    @Mod.EventBusSubscriber(modid = RatLib.MOD_ID, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent
        public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
            if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                return;
            }
            if (!(event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) {
                return;
            }
            if (!screen.getMenu().getCarried().isEmpty()) {
                return;
            }
            Slot slot = screen.getSlotUnderMouse();
            if (slot == null || !matches(slot.getItem(), screen.getMenu())) {
                return;
            }
            RatLib.NETWORK.sendToServer(new Packet(slot.index));
            event.setCanceled(true);
        }
    }
}
