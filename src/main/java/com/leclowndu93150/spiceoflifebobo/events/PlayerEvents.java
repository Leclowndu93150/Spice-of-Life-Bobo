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

                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(player);

                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(player);

                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

                NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(foodStorage), player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                reapplyAllFoodEffects((FoodStorage) foodStorage, player);

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
                    ((FoodStorage) newStorage).deserializeNBT(((FoodStorage) oldStorage).serializeNBT());
                    ((FoodStorage) newStorage).setPlayer(event.getEntity());
                });
            });
        }
    }

    private void reapplyAllFoodEffects(FoodStorage foodStorage, Player player) {
        if (player == null || player.level().isClientSide()) return;

        for (ActiveFood food : foodStorage.getActiveFoods()) {
            food.removeModifiers(player);
        }

        player.level().getServer().execute(() -> {
            for (ActiveFood food : foodStorage.getActiveFoods()) {
                food.applyModifiers(player);
            }

            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                float healthPercent = player.getHealth() / player.getMaxHealth();
                float newMaxHealth = (float) maxHealthAttr.getValue();

                player.setHealth(healthPercent * newMaxHealth);

                if (player.getHealth() < 1.0F) {
                    player.setHealth(1.0F);
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.resetPosition();
            }
        });
    }
}