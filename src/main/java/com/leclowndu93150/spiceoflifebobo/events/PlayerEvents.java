package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.capability.FoodStorage;
import net.minecraft.server.level.ServerPlayer;
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
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        boolean keepFoodOnDeath = SpiceOfLifeConfig.COMMON.keepFoodOnDeath.get();

        if (keepFoodOnDeath || !event.isWasDeath()) {
            event.getEntity().getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                ((FoodStorage) foodStorage).setPlayer(event.getEntity());
            });
        }
    }
}
