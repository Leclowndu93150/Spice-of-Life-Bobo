package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.enchantments.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EnchantmentEvents {

    public static double getFoodieAttributeMultiplier(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int foodieLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOODIE.get(), chestplate);
        
        if (foodieLevel > 0) {
            double baseBoost = SpiceOfLifeConfig.COMMON.baseFoodAttributeBoost.get();
            return 1.0 + (baseBoost * foodieLevel);
        }
        
        return 1.0;
    }

    public static double getFoodieDurationMultiplier(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int foodieLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOODIE.get(), chestplate);
        
        if (foodieLevel > 0) {
            double durationDecrease = SpiceOfLifeConfig.COMMON.foodDurationDecrease.get();
            return 1.0 - durationDecrease;
        }
        
        return 1.0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFoodConsumptionStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItem();
        if (stack.getItem().getFoodProperties() == null) {
            return;
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int foodieLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOODIE.get(), chestplate);
        
        if (foodieLevel > 0) {
            double speedIncrease = SpiceOfLifeConfig.COMMON.foodConsumptionSpeedIncrease.get();
            double speedMultiplier = 1.0 + (speedIncrease * foodieLevel);
            
            int defaultDuration = stack.getUseDuration();
            int newDuration = (int) Math.max(1, defaultDuration / speedMultiplier);
            
            event.setDuration(newDuration);
        }
    }
}