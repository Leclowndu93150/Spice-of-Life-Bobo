package com.leclowndu93150.spiceoflifebobo.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

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
                        location.getPath().substring(0, location.getPath().length() - 5)); // Remove ".json"

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
}
