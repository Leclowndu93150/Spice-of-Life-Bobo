package com.leclowndu93150.spiceoflifebobo.capability;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
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

    // This class handles the registration of the capability with Forge
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            // This is the only place where we should register the capability
            SpiceOfLifeBobo.LOGGER.info("Registering IFoodStorage capability");
            event.register(IFoodStorage.class);
        }
    }

    // This class handles the events related to the capability
    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                FoodStorageProvider provider = new FoodStorageProvider();
                event.addCapability(CAPABILITY_ID, provider);
                // Ensure capability persists with the entity
                event.addListener(provider::invalidate);
            }
        }

        @SubscribeEvent
        public static void playerClone(PlayerEvent.Clone event) {
            if (!event.isWasDeath() || SpiceOfLifeConfig.COMMON.keepFoodOnDeath.get()) {
                event.getOriginal().getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(oldStorage -> {
                    event.getEntity().getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(newStorage -> {
                        CompoundTag nbt = ((INBTSerializable<CompoundTag>) oldStorage).serializeNBT();
                        ((INBTSerializable<CompoundTag>) newStorage).deserializeNBT(nbt);
                        ((FoodStorage) newStorage).setPlayer(event.getEntity());
                    });
                });
            }
        }

        @SubscribeEvent
        public static void onPlayerSave(PlayerEvent.SaveToFile event) {
            Player player = event.getEntity();
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                // Data is automatically saved through capability system, but we ensure player reference is updated
                ((FoodStorage) storage).setPlayer(player);
            });
        }

        @SubscribeEvent
        public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
            Player player = event.getEntity();
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(storage -> {
                // Data is automatically loaded through capability system, but we ensure player reference is updated
                ((FoodStorage) storage).setPlayer(player);
            });
        }
    }

    public static class FoodStorageProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final LazyOptional<IFoodStorage> instance = LazyOptional.of(FoodStorage::new);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY.orEmpty(cap, instance);
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.map(f -> ((INBTSerializable<CompoundTag>) f).serializeNBT())
                    .orElse(new CompoundTag());
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.ifPresent(f -> ((INBTSerializable<CompoundTag>) f).deserializeNBT(nbt));
        }

        // Add this method to properly handle capability invalidation
        void invalidate() {
            instance.invalidate();
        }
    }
}