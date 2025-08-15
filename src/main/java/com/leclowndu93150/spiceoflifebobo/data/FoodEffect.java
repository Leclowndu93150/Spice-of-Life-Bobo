package com.leclowndu93150.spiceoflifebobo.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodEffect {
    private final ResourceLocation id;
    private final int duration;
    private final List<FoodAttributeModifier> attributeModifiers;

    public FoodEffect(ResourceLocation id, int duration) {
        this.id = id;
        this.duration = duration;
        this.attributeModifiers = new ArrayList<>();
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public List<FoodAttributeModifier> getAttributeModifiers() {
        return attributeModifiers;
    }

    public void addAttributeModifier(FoodAttributeModifier modifier) {
        attributeModifiers.add(modifier);
    }

    public static FoodEffect fromJson(ResourceLocation id, JsonObject json) {
        int duration = json.get("duration").getAsInt();
        FoodEffect effect = new FoodEffect(id, duration);

        JsonArray attributes = json.getAsJsonArray("attributes");
        for (JsonElement element : attributes) {
            JsonObject attribute = element.getAsJsonObject();

            String attributeName = attribute.get("name").getAsString();
            double amount = attribute.get("amount").getAsDouble();
            String operationStr = attribute.get("operation").getAsString();

            AttributeModifier.Operation operation;
            switch (operationStr.toLowerCase()) {
                case "add":
                    operation = AttributeModifier.Operation.ADDITION;
                    break;
                case "multiply_base":
                    operation = AttributeModifier.Operation.MULTIPLY_BASE;
                    break;
                case "multiply_total":
                    operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                    break;
                default:
                    SpiceOfLifeBobo.LOGGER.error("Invalid operation: {} for attribute: {}", operationStr, attributeName);
                    continue;
            }

            ResourceLocation attributeId = ResourceLocation.tryParse(attributeName);
            if (attributeId == null) {
                SpiceOfLifeBobo.LOGGER.error("Invalid attribute name: {}", attributeName);
                continue;
            }

            Attribute attribute1 = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
            if (attribute1 == null) {
                SpiceOfLifeBobo.LOGGER.error("Unknown attribute: {}", attributeId);
                continue;
            }

            effect.addAttributeModifier(new FoodAttributeModifier(attribute1, amount, operation));
        }

        return effect;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("duration", duration);
        
        JsonArray attributes = new JsonArray();
        for (FoodAttributeModifier modifier : attributeModifiers) {
            JsonObject attributeJson = new JsonObject();
            attributeJson.addProperty("name", ForgeRegistries.ATTRIBUTES.getKey(modifier.getAttribute()).toString());
            attributeJson.addProperty("amount", modifier.getAmount());
            
            String operationStr;
            switch (modifier.getOperation()) {
                case ADDITION:
                    operationStr = "add";
                    break;
                case MULTIPLY_BASE:
                    operationStr = "multiply_base";
                    break;
                case MULTIPLY_TOTAL:
                    operationStr = "multiply_total";
                    break;
                default:
                    operationStr = "add";
                    break;
            }
            attributeJson.addProperty("operation", operationStr);
            attributes.add(attributeJson);
        }
        json.add("attributes", attributes);
        
        return json;
    }
}