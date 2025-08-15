package com.leclowndu93150.spiceoflifebobo.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import com.leclowndu93150.spiceoflifebobo.data.HealingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncDatapacksPacket {
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Map<ResourceLocation, String> foodEffects;
    private final Map<ResourceLocation, String> healingItems;
    private final Map<String, String> foodEffectMappings; // item -> effect IDs
    private final Map<String, String> healingItemMappings; // item -> healing item ID

    public SyncDatapacksPacket(Map<ResourceLocation, FoodEffect> effects, 
                              Map<Item, java.util.List<ResourceLocation>> foodEffectMap,
                              Map<ResourceLocation, HealingItem> healingItemsData,
                              Map<Item, ResourceLocation> healingItemMap) {
        this.foodEffects = new HashMap<>();
        this.healingItems = new HashMap<>();
        this.foodEffectMappings = new HashMap<>();
        this.healingItemMappings = new HashMap<>();
        
        // Serialize food effects
        for (Map.Entry<ResourceLocation, FoodEffect> entry : effects.entrySet()) {
            ResourceLocation key = entry.getKey();
            if (key != null && key.getNamespace() != null && key.getPath() != null) {
                this.foodEffects.put(key, GSON.toJson(entry.getValue().toJson()));
            }
        }
        
        // Serialize food effect mappings
        for (Map.Entry<Item, java.util.List<ResourceLocation>> entry : foodEffectMap.entrySet()) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemId != null && itemId.getNamespace() != null && itemId.getPath() != null) {
                // Convert ResourceLocations to strings for proper serialization
                java.util.List<String> validEffectIdStrings = new java.util.ArrayList<>();
                for (ResourceLocation effectId : entry.getValue()) {
                    if (effectId != null && effectId.getNamespace() != null && effectId.getPath() != null) {
                        validEffectIdStrings.add(effectId.toString());
                    }
                }
                if (!validEffectIdStrings.isEmpty()) {
                    this.foodEffectMappings.put(itemId.toString(), GSON.toJson(validEffectIdStrings));
                }
            }
        }
        
        // Serialize healing items
        for (Map.Entry<ResourceLocation, HealingItem> entry : healingItemsData.entrySet()) {
            ResourceLocation key = entry.getKey();
            if (key != null && key.getNamespace() != null && key.getPath() != null) {
                this.healingItems.put(key, GSON.toJson(entry.getValue().toJson()));
            }
        }
        
        // Serialize healing item mappings
        for (Map.Entry<Item, ResourceLocation> entry : healingItemMap.entrySet()) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getKey());
            ResourceLocation healingItemId = entry.getValue();
            if (itemId != null && itemId.getNamespace() != null && itemId.getPath() != null
                    && healingItemId != null && healingItemId.getNamespace() != null && healingItemId.getPath() != null) {
                this.healingItemMappings.put(itemId.toString(), healingItemId.toString());
            }
        }
    }

    public static void encode(SyncDatapacksPacket packet, FriendlyByteBuf buffer) {
        // Encode food effects
        buffer.writeInt(packet.foodEffects.size());
        for (Map.Entry<ResourceLocation, String> entry : packet.foodEffects.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
        
        // Encode food effect mappings
        buffer.writeInt(packet.foodEffectMappings.size());
        for (Map.Entry<String, String> entry : packet.foodEffectMappings.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
        
        // Encode healing items
        buffer.writeInt(packet.healingItems.size());
        for (Map.Entry<ResourceLocation, String> entry : packet.healingItems.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
        
        // Encode healing item mappings
        buffer.writeInt(packet.healingItemMappings.size());
        for (Map.Entry<String, String> entry : packet.healingItemMappings.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
    }

    public static SyncDatapacksPacket decode(FriendlyByteBuf buffer) {
        Map<ResourceLocation, String> foodEffects = new HashMap<>();
        Map<String, String> foodEffectMappings = new HashMap<>();
        Map<ResourceLocation, String> healingItems = new HashMap<>();
        Map<String, String> healingItemMappings = new HashMap<>();
        
        // Decode food effects
        int foodEffectCount = buffer.readInt();
        for (int i = 0; i < foodEffectCount; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            String data = buffer.readUtf();
            foodEffects.put(id, data);
        }
        
        // Decode food effect mappings
        int mappingCount = buffer.readInt();
        for (int i = 0; i < mappingCount; i++) {
            String itemId = buffer.readUtf();
            String effectIds = buffer.readUtf();
            foodEffectMappings.put(itemId, effectIds);
        }
        
        // Decode healing items
        int healingItemCount = buffer.readInt();
        for (int i = 0; i < healingItemCount; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            String data = buffer.readUtf();
            healingItems.put(id, data);
        }
        
        // Decode healing item mappings
        int healingMappingCount = buffer.readInt();
        for (int i = 0; i < healingMappingCount; i++) {
            String itemId = buffer.readUtf();
            String healingItemId = buffer.readUtf();
            healingItemMappings.put(itemId, healingItemId);
        }
        
        return new SyncDatapacksPacket(foodEffects, foodEffectMappings, healingItems, healingItemMappings, true);
    }
    
    private SyncDatapacksPacket(Map<ResourceLocation, String> foodEffects,
                               Map<String, String> foodEffectMappings,
                               Map<ResourceLocation, String> healingItems,
                               Map<String, String> healingItemMappings,
                               boolean isDecoded) {
        this.foodEffects = foodEffects;
        this.foodEffectMappings = foodEffectMappings;
        this.healingItems = healingItems;
        this.healingItemMappings = healingItemMappings;
    }

    public static void handle(SyncDatapacksPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        context.setPacketHandled(true);
    }

    private static void handleClient(SyncDatapacksPacket packet) {
        SpiceOfLifeBobo.getFoodEffectManager().syncFromServer(packet.foodEffects, packet.foodEffectMappings);
        SpiceOfLifeBobo.getHealingItemManager().syncFromServer(packet.healingItems, packet.healingItemMappings);
    }
}