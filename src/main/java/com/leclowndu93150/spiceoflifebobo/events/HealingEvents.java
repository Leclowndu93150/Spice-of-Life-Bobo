package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.data.ActiveHealOverTime;
import com.leclowndu93150.spiceoflifebobo.data.HealingItem;
import com.leclowndu93150.spiceoflifebobo.data.HealingPenalty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HealingEvents {
    
    private static final Map<UUID, Long> healingItemCooldowns = new HashMap<>();
    private static final Map<UUID, ActiveHealOverTime> activeHealOverTime = new HashMap<>();
    private static final Map<UUID, HealingPenalty> healingPenalties = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFoodStartUse(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        HealingItem healingItem = SpiceOfLifeBobo.getHealingItemManager().getHealingItemForFood(item);
        if (healingItem == null || !healingItem.isHealingItem()) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        Long cooldownEnd = healingItemCooldowns.get(playerId);
        if (cooldownEnd != null && currentTime < cooldownEnd) {
            long remainingTicks = cooldownEnd - currentTime;
            int remainingSeconds = (int) (remainingTicks / 20);
            player.displayClientMessage(
                    Component.translatable("message.spiceoflifebobo.healing_cooldown", remainingSeconds), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFoodFinishUse(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        HealingItem healingItem = SpiceOfLifeBobo.getHealingItemManager().getHealingItemForFood(item);
        if (healingItem == null) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        if (healingItem.isHealingItem()) {
            healingItemCooldowns.put(playerId, currentTime + healingItem.getCooldownTicks());
            
            // Add healing penalty stack if enabled
            if (SpiceOfLifeConfig.COMMON.enableHealingPenalty.get()) {
                HealingPenalty penalty = healingPenalties.computeIfAbsent(playerId, k -> new HealingPenalty());
                penalty.addStack(
                    SpiceOfLifeConfig.COMMON.healingPenaltyDuration.get(),
                    SpiceOfLifeConfig.COMMON.maxHealingPenaltyStacks.get()
                );
                healingPenalties.put(playerId, penalty);
            }
        }

        if (healingItem.getInstantHeal() != null && healingItem.getInstantHeal().hasHealing()) {
            applyInstantHealing(player, healingItem.getInstantHeal());
        }

        if (healingItem.getHealOverTime() != null && healingItem.getHealOverTime().hasHealing()) {
            HealingItem.HealOverTime healOverTime = healingItem.getHealOverTime();
            activeHealOverTime.put(playerId, new ActiveHealOverTime(
                    healOverTime.getDuration(),
                    healOverTime.getInterval(),
                    healOverTime.getAmountFlat(),
                    healOverTime.getAmountMax()
            ));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        UUID playerId = event.player.getUUID();
        
        // Tick heal over time
        ActiveHealOverTime healOverTime = activeHealOverTime.get(playerId);
        if (healOverTime != null) {
            if (!healOverTime.tick()) {
                activeHealOverTime.remove(playerId);
            } else if (healOverTime.shouldHeal()) {
                applyHealOverTime(event.player, healOverTime);
                healOverTime.resetHealTimer();
            }
        }
        
        // Tick healing penalties
        HealingPenalty penalty = healingPenalties.get(playerId);
        if (penalty != null) {
            if (!penalty.tick()) {
                healingPenalties.remove(playerId);
            }
        }
    }

    private void applyInstantHealing(Player player, HealingItem.InstantHeal instantHeal) {
        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        float missingHealth = maxHealth - currentHealth;

        double healAmount = 0;
        healAmount += instantHeal.getPercentMaxHp() * maxHealth;
        healAmount += instantHeal.getPercentMissingHp() * missingHealth;
        healAmount += instantHeal.getFlatHp();

        // Apply healing penalty if enabled
        if (SpiceOfLifeConfig.COMMON.enableHealingPenalty.get()) {
            HealingPenalty penalty = healingPenalties.get(player.getUUID());
            if (penalty != null && penalty.hasActive()) {
                double multiplier = penalty.getHealingMultiplier(SpiceOfLifeConfig.COMMON.healingPenaltyPerStack.get());
                healAmount *= multiplier;
                
                // Show penalty message
                if (penalty.getPenaltyStacks() > 0) {
                    int penaltyPercent = (int) ((1.0 - multiplier) * 100);
                    player.displayClientMessage(
                            Component.translatable("message.spiceoflifebobo.healing_penalty", 
                                    penalty.getPenaltyStacks(), penaltyPercent), true);
                }
            }
        }

        if (healAmount > 0) {
            float newHealth = Math.min(maxHealth, currentHealth + (float) healAmount);
            player.setHealth(newHealth);
        }
    }

    private void applyHealOverTime(Player player, ActiveHealOverTime healOverTime) {
        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();

        double healAmount = 0;
        healAmount += healOverTime.getAmountFlat();
        healAmount += healOverTime.getAmountMax() * maxHealth;

        // Apply healing penalty if enabled
        if (SpiceOfLifeConfig.COMMON.enableHealingPenalty.get()) {
            HealingPenalty penalty = healingPenalties.get(player.getUUID());
            if (penalty != null && penalty.hasActive()) {
                double multiplier = penalty.getHealingMultiplier(SpiceOfLifeConfig.COMMON.healingPenaltyPerStack.get());
                healAmount *= multiplier;
            }
        }

        if (healAmount > 0) {
            float newHealth = Math.min(maxHealth, currentHealth + (float) healAmount);
            player.setHealth(newHealth);
        }
    }

    public static boolean isOnHealingCooldown(Player player) {
        UUID playerId = player.getUUID();
        Long cooldownEnd = healingItemCooldowns.get(playerId);
        if (cooldownEnd == null) {
            return false;
        }
        return player.level().getGameTime() < cooldownEnd;
    }

    public static int getHealingCooldownRemaining(Player player) {
        UUID playerId = player.getUUID();
        Long cooldownEnd = healingItemCooldowns.get(playerId);
        if (cooldownEnd == null) {
            return 0;
        }
        long remainingTicks = cooldownEnd - player.level().getGameTime();
        return Math.max(0, (int) (remainingTicks / 20));
    }

    public static HealingPenalty getHealingPenalty(Player player) {
        return healingPenalties.get(player.getUUID());
    }

    public static boolean hasHealingPenalty(Player player) {
        HealingPenalty penalty = healingPenalties.get(player.getUUID());
        return penalty != null && penalty.hasActive();
    }
}