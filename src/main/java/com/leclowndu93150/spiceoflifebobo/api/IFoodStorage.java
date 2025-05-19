package com.leclowndu93150.spiceoflifebobo.api;

import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import net.minecraft.world.item.Item;

import java.util.List;

public interface IFoodStorage {
    /**
     * Check if the player can eat more food (based on food memory)
     */
    boolean canEatFood();

    /**
     * Add a food effect to the player
     */
    void addFood(ActiveFood food);

    /**
     * Remove a food effect from the player
     */
    void removeFood(ActiveFood food);

    /**
     * Remove all food effects from the player
     */
    void clearAllFood();

    /**
     * Called every tick to update food effects
     */
    void tick();

    /**
     * Get the list of active food effects
     */
    List<ActiveFood> getActiveFoods();

    /**
     * Get the maximum number of foods the player can eat
     */
    int getMaxFoods();

    /**
     * Check if the player has a specific food active
     */
    boolean hasFood(Item item);
}
