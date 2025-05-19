package com.leclowndu93150.spiceoflifebobo.api;

import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

public interface IFoodStorage {
    boolean canEatFood();

    void addFood(ActiveFood food);

    void removeFood(ActiveFood food);

    void clearAllFood();

    void tick();

    List<ActiveFood> getActiveFoods();

    default Map<Item, List<ActiveFood>> getFoodsByType() {
        return Map.of();
    }

    default int getFoodTypeCount(Item item) {
        return 0;
    }

    default boolean isActiveTicking(ActiveFood food) {
        return false;
    }

    int getMaxFoods();

    boolean hasFood(Item item);
}