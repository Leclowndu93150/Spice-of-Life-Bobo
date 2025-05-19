package com.leclowndu93150.spiceoflifebobo.capability;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import com.leclowndu93150.spiceoflifebobo.networking.NetworkHandler;
import com.leclowndu93150.spiceoflifebobo.networking.SyncFoodStoragePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class FoodStorage implements IFoodStorage, INBTSerializable<CompoundTag> {
    // Main list of active foods - including potentially multiple of the same type
    private final List<ActiveFood> activeFoods = new ArrayList<>();

    // Map to group foods by item type for rendering purposes only
    private final Map<Item, List<ActiveFood>> foodsByType = new HashMap<>();

    // Map to track which food of each type is currently ticking
    private final Map<Item, ActiveFood> activeTicking = new HashMap<>();

    private Player player = null;

    // Store UUID to maintain reference across player logout/login
    private String playerUUID = "";

    // Flag to track if we need to force reapply modifiers
    private boolean needsReapply = false;

    public void setPlayer(Player player) {
        boolean wasNull = this.player == null;
        this.player = player;

        if (player != null) {
            // Store player UUID for persistence
            this.playerUUID = player.getStringUUID();

            // Only reapply if the player was null before (new assignment) or if we need to force reapply
            if (wasNull || needsReapply) {
                // Reapply all food effects when player reference is set
                for (ActiveFood food : activeFoods) {
                    food.applyModifiers(player);
                }
                needsReapply = false;
            }

            // Reset active ticking foods
            updateActiveTicking();
        }
    }

    /**
     * Force a reapplication of all attribute modifiers on next player set
     */
    public void forceReapplyModifiers() {
        needsReapply = true;

        if (player != null) {
            // Remove all existing modifiers
            for (ActiveFood food : activeFoods) {
                food.removeModifiers(player);
            }

            // Reapply all modifiers
            for (ActiveFood food : activeFoods) {
                food.applyModifiers(player);
            }

            needsReapply = false;
        }
    }

    @Override
    public boolean canEatFood() {
        return activeFoods.size() < getMaxFoods();
    }

    @Override
    public void addFood(ActiveFood food) {
        if (player == null) return;

        // Check if we can add more food
        if (!canEatFood()) return;

        // Add the food to our main list
        activeFoods.add(food);

        // Apply food modifiers
        food.applyModifiers(player);

        // Update our type grouping for rendering
        updateFoodsByType();

        // Update which foods are actively ticking
        updateActiveTicking();

        // Sync to client
        syncToClient();
    }

    @Override
    public void removeFood(ActiveFood food) {
        if (player == null) return;

        if (activeFoods.remove(food)) {
            // Remove attribute modifiers
            food.removeModifiers(player);

            // Update our type grouping for rendering
            updateFoodsByType();

            // Update which foods are actively ticking
            updateActiveTicking();

            // Sync to client
            syncToClient();
        }
    }

    @Override
    public void clearAllFood() {
        if (player == null) return;

        // Make a copy to avoid concurrent modification
        List<ActiveFood> foodsCopy = new ArrayList<>(activeFoods);

        // Remove each food
        for (ActiveFood food : foodsCopy) {
            removeFood(food);
        }
    }

    @Override
    public void tick() {
        if (player == null) return;

        boolean changed = false;
        List<ActiveFood> expiredFoods = new ArrayList<>();

        // Only tick one food of each type at a time
        for (Map.Entry<Item, ActiveFood> entry : activeTicking.entrySet()) {
            ActiveFood food = entry.getValue();

            // Tick the food
            food.tick();

            // Check if it's expired
            if (food.isExpired()) {
                expiredFoods.add(food);
                changed = true;
            }
        }

        // Remove expired foods
        for (ActiveFood food : expiredFoods) {
            food.removeModifiers(player);
            activeFoods.remove(food);
        }

        // If any foods expired, update our groupings and active ticking foods
        if (changed) {
            updateFoodsByType();
            updateActiveTicking();

            // Sync to client
            syncToClient();
        }
    }

    /**
     * Update which food of each type is actively ticking
     * For each food type, only the earliest eaten food (shortest remaining duration) ticks
     */
    private void updateActiveTicking() {
        activeTicking.clear();

        // For each food type, find the one with the shortest time remaining
        for (Map.Entry<Item, List<ActiveFood>> entry : foodsByType.entrySet()) {
            Item foodItem = entry.getKey();
            List<ActiveFood> foods = entry.getValue();

            if (!foods.isEmpty()) {
                // Sort by duration ascending (shortest first)
                foods.sort(Comparator.comparing(ActiveFood::getDuration));

                // The food with the shortest time remaining ticks first
                activeTicking.put(foodItem, foods.get(0));
            }
        }
    }

    @Override
    public List<ActiveFood> getActiveFoods() {
        return Collections.unmodifiableList(activeFoods);
    }

    /**
     * Get foods grouped by type for rendering purposes
     */
    @Override
    public Map<Item, List<ActiveFood>> getFoodsByType() {
        return Collections.unmodifiableMap(foodsByType);
    }

    /**
     * Update the foods by type mapping
     */
    private void updateFoodsByType() {
        foodsByType.clear();

        for (ActiveFood food : activeFoods) {
            Item item = food.getItem();
            if (!foodsByType.containsKey(item)) {
                foodsByType.put(item, new ArrayList<>());
            }
            foodsByType.get(item).add(food);
        }
    }

    @Override
    public int getMaxFoods() {
        if (player == null) {
            return SpiceOfLifeConfig.COMMON.defaultFoodMemory.get();
        }

        // Get Food Memory attribute value (rounded down)
        AttributeInstance attribute = player.getAttribute(SpiceOfLifeBobo.FOOD_MEMORY.get());
        if (attribute != null) {
            return (int) Math.floor(attribute.getValue());
        }

        return SpiceOfLifeConfig.COMMON.defaultFoodMemory.get();
    }

    @Override
    public boolean hasFood(Item item) {
        return activeFoods.stream().anyMatch(food -> food.getItem() == item);
    }

    @Override
    public int getFoodTypeCount(Item item) {
        if (!foodsByType.containsKey(item)) {
            return 0;
        }
        return foodsByType.get(item).size();
    }

    /**
     * Check if the given food is actively ticking down
     */
    @Override
    public boolean isActiveTicking(ActiveFood food) {
        Item item = food.getItem();
        return activeTicking.containsKey(item) && activeTicking.get(item) == food;
    }

    private void syncToClient() {
        if (player != null && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new SyncFoodStoragePacket(this), serverPlayer);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag foodList = new ListTag();

        for (ActiveFood food : activeFoods) {
            foodList.add(food.serializeNBT());
        }

        tag.put("Foods", foodList);

        // Store player UUID for retrieval after server restart
        if (player != null) {
            tag.putString("PlayerUUID", player.getStringUUID());
        } else if (!playerUUID.isEmpty()) {
            tag.putString("PlayerUUID", playerUUID);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        activeFoods.clear();
        foodsByType.clear();
        activeTicking.clear();

        ListTag foodList = nbt.getList("Foods", 10); // 10 = CompoundTag
        for (int i = 0; i < foodList.size(); i++) {
            CompoundTag foodTag = foodList.getCompound(i);
            ActiveFood food = new ActiveFood(foodTag);
            activeFoods.add(food);
        }

        // Update our type grouping for rendering
        updateFoodsByType();

        // Update which foods are actively ticking
        updateActiveTicking();

        // Flag that we need to reapply modifiers
        needsReapply = true;

        // Restore player UUID
        if (nbt.contains("PlayerUUID")) {
            this.playerUUID = nbt.getString("PlayerUUID");
        }
    }
}