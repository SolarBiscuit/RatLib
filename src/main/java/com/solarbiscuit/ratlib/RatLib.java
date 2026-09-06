package com.solarbiscuit.ratlib;

import com.solarbiscuit.ratlib.inventory.OpenFromSlot;
import com.solarbiscuit.ratlib.loot.ModLootConditions;
import com.solarbiscuit.ratlib.loot.ModLootModifiers;
import com.solarbiscuit.ratlib.network.NetworkChannel;
import com.solarbiscuit.ratlib.spawn.ModBiomeModifiers;
import com.solarbiscuit.ratlib.util.ItemPathIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;

@Mod(RatLib.MOD_ID)
public class RatLib {
    public static final String MOD_ID = "ratlib";
    public static final NetworkChannel NETWORK = new NetworkChannel(MOD_ID, "1");

    @SuppressWarnings("removal")
    public RatLib() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLootModifiers.SERIALIZERS.register(modBus);
        ModLootConditions.CONDITIONS.register(modBus);
        ModBiomeModifiers.SERIALIZERS.register(modBus);
        ItemPathIndex.register(modBus);
        registerPackets();
    }

    private static void registerPackets() {
        NETWORK.register(OpenFromSlot.Packet.class, NetworkDirection.PLAY_TO_SERVER,
                OpenFromSlot.Packet::encode, OpenFromSlot.Packet::decode, OpenFromSlot.Packet::handle);
    }
}
