package com.leclowndu93150.spiceoflifebobo.networking;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(SpiceOfLifeBobo.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;

        CHANNEL.registerMessage(id++, SyncFoodStoragePacket.class,
                SyncFoodStoragePacket::encode,
                SyncFoodStoragePacket::decode,
                SyncFoodStoragePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, SyncDatapacksPacket.class,
                SyncDatapacksPacket::encode,
                SyncDatapacksPacket::decode,
                SyncDatapacksPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, RequestSyncPacket.class,
                RequestSyncPacket::encode,
                RequestSyncPacket::decode,
                RequestSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
