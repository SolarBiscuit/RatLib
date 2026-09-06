package com.solarbiscuit.ratlib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkChannel {
    private final SimpleChannel channel;
    private int nextId;

    @SuppressWarnings("removal")
    public NetworkChannel(String modId, String protocol) {
        this.channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(modId, "main"))
                .networkProtocolVersion(() -> protocol)
                .clientAcceptedVersions(protocol::equals)
                .serverAcceptedVersions(protocol::equals)
                .simpleChannel();
    }

    public <MSG> void register(
            Class<MSG> type,
            NetworkDirection direction,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler
    ) {
        this.channel.messageBuilder(type, this.nextId++, direction)
                .encoder(encoder)
                .decoder(decoder)
                .consumerMainThread(handler)
                .add();
    }

    public void sendTo(ServerPlayer player, Object packet) {
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void sendToServer(Object packet) {
        this.channel.sendToServer(packet);
    }

    public SimpleChannel raw() {
        return this.channel;
    }
}
