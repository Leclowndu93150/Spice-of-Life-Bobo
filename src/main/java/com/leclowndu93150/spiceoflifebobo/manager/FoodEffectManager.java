package com.leclowndu93150.spiceoflifebobo.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.lang.reflect.Type;

public class FoodEffectManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Map<ResourceLocation, FoodEffect> effects = new HashMap<>();

    private Map<Item, List<ResourceLocation>> foodEffects = new HashMap<>();

    public FoodEffectManager() {
        super(GSON, "effects");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        effects.clear();
        foodEffects.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                ResourceLocation effectId = new ResourceLocation(
                        location.getNamespace(),
                        location.getPath().substring(0, location.getPath().length() - 5));

                JsonObject obj = json.getAsJsonObject();
                FoodEffect effect = FoodEffect.fromJson(effectId, obj);
                effects.put(effectId, effect);

                if (obj.has("items")) {
                    for (JsonElement itemElement : obj.getAsJsonArray("items")) {
                        String itemId = itemElement.getAsString();
                        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);

                        if (itemLocation != null) {
                            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

                            if (item != null) {
                                foodEffects.computeIfAbsent(item, k -> new ArrayList<>()).add(effectId);
                            } else {
                                SpiceOfLifeBobo.LOGGER.warn("Unknown item: {} for effect: {}", itemLocation, effectId);
                            }
                        } else {
                            SpiceOfLifeBobo.LOGGER.warn("Invalid item ID format: {} for effect: {}", itemId, effectId);
                        }
                    }
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error loading food effect {}: {}", location, e.getMessage());
                e.printStackTrace();
            }
        }

        SpiceOfLifeBobo.LOGGER.info("Loaded {} food effects for {} foods", effects.size(), foodEffects.size());
    }

    public FoodEffect getEffect(ResourceLocation id) {
        return effects.get(id);
    }

    public List<FoodEffect> getEffectsForFood(Item item) {
        List<ResourceLocation> effectIds = foodEffects.get(item);
        if (effectIds == null) {
            return Collections.emptyList();
        }

        List<FoodEffect> result = new ArrayList<>();
        for (ResourceLocation id : effectIds) {
            FoodEffect effect = effects.get(id);
            if (effect != null) {
                result.add(effect);
            }
        }

        return result;
    }

    public boolean hasEffects(Item item) {
        List<ResourceLocation> effectIds = foodEffects.get(item);
        return effectIds != null && !effectIds.isEmpty();
    }

    public Map<ResourceLocation, FoodEffect> getAllEffects() {
        return new HashMap<>(effects);
    }

    public Map<Item, List<ResourceLocation>> getAllFoodEffects() {
        return new HashMap<>(foodEffects);
    }

    public void syncFromServer(Map<ResourceLocation, String> effectData, Map<String, String> mappingData) {
        effects.clear();
        foodEffects.clear();
        
        // Deserialize effects
        for (Map.Entry<ResourceLocation, String> entry : effectData.entrySet()) {
            try {
                ResourceLocation effectId = entry.getKey();
                if (effectId != null && effectId.getNamespace() != null && effectId.getPath() != null) {
                    JsonObject json = GSON.fromJson(entry.getValue(), JsonObject.class);
                    FoodEffect effect = FoodEffect.fromJson(effectId, json);
                    effects.put(effectId, effect);
                } else {
                    SpiceOfLifeBobo.LOGGER.warn("Skipping invalid effect ID: {}", effectId);
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error syncing food effect {}: {}", entry.getKey(), e.getMessage());
            }
        }
        
        // Deserialize mappings
        Type listType = new TypeToken<List<String>>(){}.getType();
        for (Map.Entry<String, String> entry : mappingData.entrySet()) {
            try {
                ResourceLocation itemLocation = ResourceLocation.tryParse(entry.getKey());
                if (itemLocation != null && itemLocation.getNamespace() != null && itemLocation.getPath() != null) {
                    Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                    if (item != null) {
                        List<String> effectIdStrings = GSON.fromJson(entry.getValue(), listType);
                        // Convert string IDs back to ResourceLocations and validate
                        List<ResourceLocation> validEffectIds = new ArrayList<>();
                        for (String effectIdString : effectIdStrings) {
                            ResourceLocation effectId = ResourceLocation.tryParse(effectIdString);
                            if (effectId != null && effectId.getNamespace() != null && effectId.getPath() != null) {
                                validEffectIds.add(effectId);
                            } else {
                                SpiceOfLifeBobo.LOGGER.warn("Skipping invalid effect ID for item {}: {}", entry.getKey(), effectIdString);
                            }
                        }
                        if (!validEffectIds.isEmpty()) {
                            foodEffects.put(item, validEffectIds);
                        }
                    }
                }
            } catch (Exception e) {
                SpiceOfLifeBobo.LOGGER.error("Error syncing food effect mapping {}: {}", entry.getKey(), e.getMessage());
            }
        }
        
        SpiceOfLifeBobo.LOGGER.info("Synced {} food effects for {} foods from server", effects.size(), foodEffects.size());
    }
}
