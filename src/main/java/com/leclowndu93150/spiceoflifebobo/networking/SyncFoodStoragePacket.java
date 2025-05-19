package com.leclowndu93150.spiceoflifebobo.networking;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.capability.FoodStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncFoodStoragePacket {
    private final CompoundTag data;

    public SyncFoodStoragePacket(IFoodStorage storage) {
        this.data = ((INBTSerializable<CompoundTag>) storage).serializeNBT();
    }

    public SyncFoodStoragePacket(CompoundTag data) {
        this.data = data;
    }

    public static void encode(SyncFoodStoragePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    public static SyncFoodStoragePacket decode(FriendlyByteBuf buffer) {
        return new SyncFoodStoragePacket(buffer.readNbt());
    }

    public static void handle(SyncFoodStoragePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClient(packet);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncFoodStoragePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                ((INBTSerializable<CompoundTag>) storage).deserializeNBT(packet.data);
                ((FoodStorage) storage).setPlayer(player);
            });
        }
    }
}
