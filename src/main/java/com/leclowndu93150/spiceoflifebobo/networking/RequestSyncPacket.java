package com.leclowndu93150.spiceoflifebobo.networking;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncPacket {

    public RequestSyncPacket() {
    }

    public static void encode(RequestSyncPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestSyncPacket decode(FriendlyByteBuf buffer) {
        return new RequestSyncPacket();
    }

    public static void handle(RequestSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.reviveCaps();
                player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                    NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(storage), player);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
