package com.leclowndu93150.spiceoflifebobo.capability;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.networking.NetworkHandler;
import com.leclowndu93150.spiceoflifebobo.networking.SyncFoodStoragePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FoodStorage implements IFoodStorage, INBTSerializable<CompoundTag> {
    private final List<ActiveFood> activeFoods = new ArrayList<>();
    private Player player = null;

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public boolean canEatFood() {
        return activeFoods.size() < getMaxFoods();
    }

    @Override
    public void addFood(ActiveFood food) {
        if (!canEatFood() || player == null) return;

        // Add food to active list
        activeFoods.add(food);

        // Apply attribute modifiers
        food.applyModifiers(player);

        // Sync to client if on server
        syncToClient();
    }

    @Override
    public void removeFood(ActiveFood food) {
        if (player == null) return;

        if (activeFoods.remove(food)) {
            // Remove attribute modifiers
            food.removeModifiers(player);

            // Sync to client if on server
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

        // Check for expired foods
        Iterator<ActiveFood> iterator = activeFoods.iterator();
        boolean changed = false;

        while (iterator.hasNext()) {
            ActiveFood food = iterator.next();
            food.tick();

            if (food.isExpired()) {
                // Remove attribute modifiers
                food.removeModifiers(player);
                iterator.remove();
                changed = true;
            }
        }

        // Sync to client if anything changed
        if (changed) {
            syncToClient();
        }
    }

    @Override
    public List<ActiveFood> getActiveFoods() {
        return Collections.unmodifiableList(activeFoods);
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
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        activeFoods.clear();

        ListTag foodList = nbt.getList("Foods", 10); // 10 = CompoundTag
        for (int i = 0; i < foodList.size(); i++) {
            CompoundTag foodTag = foodList.getCompound(i);
            ActiveFood food = new ActiveFood(foodTag);
            activeFoods.add(food);
        }
    }
}
