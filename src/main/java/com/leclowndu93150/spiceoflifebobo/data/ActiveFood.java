package com.leclowndu93150.spiceoflifebobo.data;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActiveFood {
    private final Item item;
    private final List<FoodEffect> effects;
    private final UUID uuid;
    private int duration;
    private int maxDuration;

    public ActiveFood(Item item, List<FoodEffect> effects) {
        this.item = item;
        this.effects = new ArrayList<>(effects);
        this.uuid = UUID.randomUUID();

        this.maxDuration = 0;
        for (FoodEffect effect : effects) {
            this.maxDuration = Math.max(this.maxDuration, effect.getDuration());
        }
        this.duration = this.maxDuration;
    }

    public ActiveFood(CompoundTag nbt) {
        ResourceLocation itemId = ResourceLocation.tryParse(nbt.getString("Item"));
        this.item = ForgeRegistries.ITEMS.getValue(itemId);

        this.uuid = nbt.getUUID("UUID");

        this.duration = nbt.getInt("Duration");
        this.maxDuration = nbt.getInt("MaxDuration");

        this.effects = new ArrayList<>();
        CompoundTag effectsTag = nbt.getCompound("Effects");
        for (String key : effectsTag.getAllKeys()) {
            ResourceLocation effectId = ResourceLocation.tryParse(key);
            if (effectId != null) {
                FoodEffect effect = SpiceOfLifeBobo.getFoodEffectManager().getEffect(effectId);
                if (effect != null) {
                    this.effects.add(effect);
                }
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId != null) {
            nbt.putString("Item", itemId.toString());
        }

        nbt.putUUID("UUID", uuid);

        nbt.putInt("Duration", duration);
        nbt.putInt("MaxDuration", maxDuration);

        CompoundTag effectsTag = new CompoundTag();
        for (FoodEffect effect : effects) {
            effectsTag.putBoolean(effect.getId().toString(), true);
        }
        nbt.put("Effects", effectsTag);

        return nbt;
    }

    public Item getItem() {
        return item;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<FoodEffect> getEffects() {
        return effects;
    }

    public int getDuration() {
        return duration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public int addDuration(int additionalDuration) {
        this.duration += additionalDuration;


        int maxStackedDuration = this.maxDuration * 3;
        this.duration = Math.min(this.duration, maxStackedDuration);


        return this.duration;
    }

    public float getRemainingDurationPercent() {
        return (float) duration / maxDuration;
    }

    public void tick() {
        if (duration > 0) {
            duration--;
        }
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public void applyModifiers(Player player) {
        for (FoodEffect effect : effects) {
            for (FoodAttributeModifier modifier : effect.getAttributeModifiers()) {
                AttributeInstance attribute = player.getAttribute(modifier.getAttribute());
                if (attribute != null) {
                    AttributeModifier attributeModifier = modifier.createModifier(uuid);

                    if (attribute.getModifier(attributeModifier.getId()) != null) {
                        attribute.removeModifier(attributeModifier.getId());
                    }

                    attribute.addTransientModifier(attributeModifier);
                }
            }
        }
    }

    public void removeModifiers(Player player) {
        for (FoodEffect effect : effects) {
            for (FoodAttributeModifier modifier : effect.getAttributeModifiers()) {
                AttributeInstance attribute = player.getAttribute(modifier.getAttribute());
                if (attribute != null) {
                    AttributeModifier tempModifier = modifier.createModifier(uuid);
                    attribute.removeModifier(tempModifier.getId());
                }
            }
        }
    }
}