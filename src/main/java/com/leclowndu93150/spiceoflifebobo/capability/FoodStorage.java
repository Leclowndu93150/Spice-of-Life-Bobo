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

import java.util.*;

public class FoodStorage implements IFoodStorage, INBTSerializable<CompoundTag> {
    private final List<ActiveFood> activeFoods = new ArrayList<>();

    private final Map<Item, List<ActiveFood>> foodsByType = new HashMap<>();

    private final Map<Item, ActiveFood> activeTicking = new HashMap<>();

    private Player player = null;

    private String playerUUID = "";

    private boolean needsReapply = false;

    public void setPlayer(Player player) {
        boolean wasNull = this.player == null;
        this.player = player;

        if (player != null) {
            this.playerUUID = player.getStringUUID();

            if (wasNull || needsReapply) {
                for (ActiveFood food : activeFoods) {
                    food.applyModifiers(player);
                }
                needsReapply = false;
            }

            updateActiveTicking();
        }
    }

    public void forceReapplyModifiers() {
        needsReapply = true;

        if (player != null) {
            for (ActiveFood food : activeFoods) {
                food.removeModifiers(player);
            }

            for (ActiveFood food : activeFoods) {
                food.applyModifiers(player);
            }

            needsReapply = false;
        }
    }

    @Override
    public boolean canEatFood() {
        int currentFoods = activeFoods.size();
        int maxFoods = getMaxFoods();
        if (player != null && !player.level().isClientSide()) {
            SpiceOfLifeBobo.LOGGER.info("Can eat food check: current={}, max={}, result={}",
                    currentFoods, maxFoods, currentFoods < maxFoods);
        }
        return currentFoods < maxFoods;
    }

    @Override
    public void addFood(ActiveFood food) {
        if (player == null) return;

        if (!canEatFood()) return;

        activeFoods.add(food);

        food.applyModifiers(player);

        updateFoodsByType();

        updateActiveTicking();

        syncToClient();
    }

    @Override
    public void removeFood(ActiveFood food) {
        if (player == null) return;

        if (activeFoods.remove(food)) {
            food.removeModifiers(player);

            updateFoodsByType();

            updateActiveTicking();

            syncToClient();
        }
    }

    @Override
    public void clearAllFood() {
        if (player == null) return;

        List<ActiveFood> foodsCopy = new ArrayList<>(activeFoods);

        for (ActiveFood food : foodsCopy) {
            removeFood(food);
        }
    }

    @Override
    public void tick() {
        if (player == null) return;

        boolean changed = false;
        List<ActiveFood> expiredFoods = new ArrayList<>();

        for (Map.Entry<Item, ActiveFood> entry : activeTicking.entrySet()) {
            ActiveFood food = entry.getValue();

            food.tick();

            if (food.isExpired()) {
                expiredFoods.add(food);
                changed = true;
            }
        }

        for (ActiveFood food : expiredFoods) {
            food.removeModifiers(player);
            activeFoods.remove(food);
        }

        if (changed) {
            updateFoodsByType();
            updateActiveTicking();

            syncToClient();
        }
    }

    private void updateActiveTicking() {
        activeTicking.clear();

        for (Map.Entry<Item, List<ActiveFood>> entry : foodsByType.entrySet()) {
            Item foodItem = entry.getKey();
            List<ActiveFood> foods = entry.getValue();

            if (!foods.isEmpty()) {
                foods.sort(Comparator.comparing(ActiveFood::getDuration));

                activeTicking.put(foodItem, foods.get(0));
            }
        }
    }

    @Override
    public List<ActiveFood> getActiveFoods() {
        return Collections.unmodifiableList(activeFoods);
    }

    @Override
    public Map<Item, List<ActiveFood>> getFoodsByType() {
        return Collections.unmodifiableMap(foodsByType);
    }

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
            SpiceOfLifeBobo.LOGGER.debug("Player is null, using config value: {}", SpiceOfLifeConfig.COMMON.defaultFoodMemory.get());
            return SpiceOfLifeConfig.COMMON.defaultFoodMemory.get();
        }

        AttributeInstance attribute = player.getAttribute(SpiceOfLifeBobo.FOOD_MEMORY.get());
        if (attribute != null) {
            double value = attribute.getValue();
            SpiceOfLifeBobo.LOGGER.debug("Food memory attribute value: {}", value);
            return (int) Math.floor(value);
        }

        SpiceOfLifeBobo.LOGGER.warn("Food memory attribute is null for player {}", player.getName().getString());
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

        ListTag foodList = nbt.getList("Foods", 10);
        for (int i = 0; i < foodList.size(); i++) {
            CompoundTag foodTag = foodList.getCompound(i);
            ActiveFood food = new ActiveFood(foodTag);
            activeFoods.add(food);
        }

        updateFoodsByType();

        updateActiveTicking();

        needsReapply = true;

        if (nbt.contains("PlayerUUID")) {
            this.playerUUID = nbt.getString("PlayerUUID");
        }
    }
}