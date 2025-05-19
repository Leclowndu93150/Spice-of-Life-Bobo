package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.capability.FoodStorage;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.networking.NetworkHandler;
import com.leclowndu93150.spiceoflifebobo.networking.SyncFoodStoragePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerEvents {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(player);
                foodStorage.tick();
            });
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(player);

                // Fully reapply all food effects to ensure attributes are correctly set
                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                // Sync capability data to client when player joins
                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                // Ensure the player is set when logging in
                ((FoodStorage) foodStorage).setPlayer(player);

                // Fully reapply all food effects to ensure attributes are correctly set
                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                // Sync to client
                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(player);

                // Reapply all food effects to ensure attributes are correctly set after respawn
                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                // Sync to client after respawn
                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                // Ensure that attributes are correctly set after dimension change
                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                // Sync to client after dimension change
                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        boolean keepFoodOnDeath = SpiceOfLifeConfig.COMMON.keepFoodOnDeath.get();

        if (keepFoodOnDeath || !event.isWasDeath()) {
            event.getEntity().getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(newStorage -> {
                event.getOriginal().getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(oldStorage -> {
                    // Copy the capability data from the old player to the new one
                    ((FoodStorage) newStorage).deserializeNBT(((FoodStorage) oldStorage).serializeNBT());
                    ((FoodStorage) newStorage).setPlayer(event.getEntity());
                });
            });
        }
    }

    /**
     * Helper method to fully reapply all food effects to ensure attributes are correctly set
     * This is necessary because Minecraft sometimes doesn't properly restore attribute modifiers on login
     */
    private void reapplyAllFoodEffects(FoodStorage foodStorage, Player player) {
        if (player == null || player.level().isClientSide()) return;

        // First, remove all existing food attribute modifiers to avoid duplicates
        for (ActiveFood food : foodStorage.getActiveFoods()) {
            food.removeModifiers(player);
        }

        // Wait a tick to ensure all attributes are properly reset
        player.level().getServer().execute(() -> {
            // Then reapply all food effects
            for (ActiveFood food : foodStorage.getActiveFoods()) {
                food.applyModifiers(player);
            }

            // For health specifically, ensure current health is scaled properly with max health
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                float healthPercent = player.getHealth() / player.getMaxHealth();
                float newMaxHealth = (float) maxHealthAttr.getValue();

                // Set health to the same percentage of the new max health
                player.setHealth(healthPercent * newMaxHealth);

                // Ensure health is at least 1.0F to prevent death
                if (player.getHealth() < 1.0F) {
                    player.setHealth(1.0F);
                }
            }

            // Force sync health to client
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.resetPosition();
            }
        });
    }
}