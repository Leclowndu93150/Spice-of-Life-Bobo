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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class FoodEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        if (!isFood(stack)) {
            return;
        }

        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel());
            player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel());
        }

        List<FoodEffect> effects = SpiceOfLifeBobo.getFoodEffectManager().getEffectsForFood(item);
        if (effects.isEmpty()) {
            return;
        }

        player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
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