package com.leclowndu93150.spiceoflifebobo.capability;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FoodStorageCapability {
    public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(SpiceOfLifeBobo.MOD_ID, "food_storage");

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IFoodStorage.class);
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                FoodStorageProvider provider = new FoodStorageProvider();
                event.addCapability(CAPABILITY_ID, provider);
                event.addListener(provider::invalidate);
            }
        }

        @SubscribeEvent
        public static void onPlayerSave(PlayerEvent.SaveToFile event) {
            Player player = event.getEntity();
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                ((FoodStorage) storage).setPlayer(player);
            });
        }

        @SubscribeEvent
        public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
            Player player = event.getEntity();
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                ((FoodStorage) storage).setPlayer(player);
            });
        }
    }

    public static class FoodStorageProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final FoodStorage storage = new FoodStorage();
        private LazyOptional<IFoodStorage> instance = LazyOptional.of(() -> storage);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (!instance.isPresent()) {
                instance = LazyOptional.of(() -> storage);
            }
            return SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY.orEmpty(cap, instance);
        }

        @Override
        public CompoundTag serializeNBT() {
            return ((INBTSerializable<CompoundTag>) storage).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            ((INBTSerializable<CompoundTag>) storage).deserializeNBT(nbt);
        }

        void invalidate() {
            instance.invalidate();
        }
    }
}
