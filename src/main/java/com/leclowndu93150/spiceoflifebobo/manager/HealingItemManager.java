package com.leclowndu93150.spiceoflifebobo.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.data.HealingItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class HealingItemManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Map<ResourceLocation, HealingItem> healingItems = new HashMap<>();
    private Map<Item, ResourceLocation> itemToHealingItem = new HashMap<>();
    private Set<Item> healingItemSet = new HashSet<>();

    public HealingItemManager() {
        super(GSON, "healing_items");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        healingItems.clear();
        itemToHealingItem.clear();
        healingItemSet.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                ResourceLocation healingItemId = new ResourceLocation(
                        location.getNamespace(),
                        location.getPath().substring(0, location.getPath().length() - 5));

                JsonObject obj = json.getAsJsonObject();
                HealingItem healingItem = HealingItem.fromJson(healingItemId, obj);
                healingItems.put(healingItemId, healingItem);

                if (obj.has("items")) {
                    for (JsonElement itemElement : obj.getAsJsonArray("items")) {
                        String itemId = itemElement.getAsString();
                        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);

                        if (itemLocation != null) {
                            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

                            if (item != null) {
                                itemToHealingItem.put(item, healingItemId);
                                if (healingItem.isHealingItem()) {
                                    healingItemSet.add(item);
                                }
                            } else {
                                SpiceOfLifeBobo.LOGGER.warn("Unknown item: {} for healing item: {}", itemLocation, healingItemId);
                            }
                        } else {
                            SpiceOfLifeBobo.LOGGER.warn("Invalid item ID format: {} for healing item: {}", itemId, healingItemId);
                        }
                    }
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error loading healing item {}: {}", location, e.getMessage());
                e.printStackTrace();
            }
        }

        SpiceOfLifeBobo.LOGGER.info("Loaded {} healing items for {} foods", healingItems.size(), itemToHealingItem.size());
    }

    public HealingItem getHealingItem(ResourceLocation id) {
        return healingItems.get(id);
    }

    public HealingItem getHealingItemForFood(Item item) {
        ResourceLocation healingItemId = itemToHealingItem.get(item);
        if (healingItemId == null) {
            return null;
        }
        return healingItems.get(healingItemId);
    }

    public boolean isHealingItem(Item item) {
        return healingItemSet.contains(item);
    }

    public boolean hasHealingProperties(Item item) {
        HealingItem healingItem = getHealingItemForFood(item);
        if (healingItem == null) {
            return false;
        }
        
        boolean hasInstant = healingItem.getInstantHeal() != null && healingItem.getInstantHeal().hasHealing();
        boolean hasOverTime = healingItem.getHealOverTime() != null && healingItem.getHealOverTime().hasHealing();
        
        return hasInstant || hasOverTime;
    }

    public Map<ResourceLocation, HealingItem> getAllHealingItems() {
        return new HashMap<>(healingItems);
    }

    public Map<Item, ResourceLocation> getAllItemToHealingItem() {
        return new HashMap<>(itemToHealingItem);
    }

    public void syncFromServer(Map<ResourceLocation, String> healingItemData, Map<String, String> mappingData) {
        healingItems.clear();
        itemToHealingItem.clear();
        healingItemSet.clear();
        
        // Deserialize healing items
        for (Map.Entry<ResourceLocation, String> entry : healingItemData.entrySet()) {
            try {
                ResourceLocation healingItemId = entry.getKey();
                if (healingItemId != null && healingItemId.getNamespace() != null && healingItemId.getPath() != null) {
                    JsonObject json = GSON.fromJson(entry.getValue(), JsonObject.class);
                    HealingItem healingItem = HealingItem.fromJson(healingItemId, json);
                    healingItems.put(healingItemId, healingItem);
                } else {
                    SpiceOfLifeBobo.LOGGER.warn("Skipping invalid healing item ID: {}", healingItemId);
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error syncing healing item {}: {}", entry.getKey(), e.getMessage());
            }
        }
        
        // Deserialize mappings
        for (Map.Entry<String, String> entry : mappingData.entrySet()) {
            try {
                ResourceLocation itemLocation = ResourceLocation.tryParse(entry.getKey());
                ResourceLocation healingItemId = ResourceLocation.tryParse(entry.getValue());
                
                if (itemLocation != null && itemLocation.getNamespace() != null && itemLocation.getPath() != null
                        && healingItemId != null && healingItemId.getNamespace() != null && healingItemId.getPath() != null) {
                    Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                    HealingItem healingItem = healingItems.get(healingItemId);
                    
                    if (item != null && healingItem != null) {
                        itemToHealingItem.put(item, healingItemId);
                        if (healingItem.isHealingItem()) {
                            healingItemSet.add(item);
                        }
                    }
                } else {
                    SpiceOfLifeBobo.LOGGER.warn("Skipping invalid healing item mapping - item: {}, healingItem: {}", entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error syncing healing item mapping {}: {}", entry.getKey(), e.getMessage());
            }
        }
        
        SpiceOfLifeBobo.LOGGER.info("Synced {} healing items for {} foods from server", healingItems.size(), itemToHealingItem.size());
    }
}