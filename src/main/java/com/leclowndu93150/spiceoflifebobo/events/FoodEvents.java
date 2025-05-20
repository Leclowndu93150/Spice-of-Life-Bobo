package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class FoodEvents {

    private static int hungerRefreshTimer = 0;
    private static final int HUNGER_REFRESH_INTERVAL = 20;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            hungerRefreshTimer++;

            if (hungerRefreshTimer >= HUNGER_REFRESH_INTERVAL) {
                hungerRefreshTimer = 0;

                Player player = event.player;
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        if (!isFood(stack)) {
            return;
        }

        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
        }

        List<FoodEffect> effects = SpiceOfLifeBobo.getFoodEffectManager().getEffectsForFood(item);
        if (effects.isEmpty()) {
            return;
        }

        player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
            if (foodStorage.hasFood(item)) {
                player.displayClientMessage(
                        Component.translatable("message.spiceoflifebobo.already_eaten", item.getDescription()), true);
                return;
            }

            SpiceOfLifeBobo.LOGGER.info("Max foods for player: {}", foodStorage.getMaxFoods());

            if (foodStorage.canEatFood()) {
                foodStorage.addFood(new ActiveFood(item, effects));
            } else {
                player.displayClientMessage(
                        Component.translatable("message.spiceoflifebobo.stomach_full", foodStorage.getMaxFoods()), true);
            }
        });
    }

    private boolean isFood(ItemStack stack) {
        FoodProperties foodProperties = stack.getItem().getFoodProperties();
        if (foodProperties != null) {
            return true;
        }

        return SpiceOfLifeBobo.getFoodEffectManager().hasEffects(stack.getItem());
    }
}