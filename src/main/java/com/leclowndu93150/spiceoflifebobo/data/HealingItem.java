package com.leclowndu93150.spiceoflifebobo.data;

import com.google.gson.JsonObject;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import net.minecraft.resources.ResourceLocation;

public class HealingItem {
    private final ResourceLocation id;
    private final boolean isHealingItem;
    private final int cooldownTicks;
    private final InstantHeal instantHeal;
    private final HealOverTime healOverTime;

    public HealingItem(ResourceLocation id, boolean isHealingItem, int cooldownTicks, InstantHeal instantHeal, HealOverTime healOverTime) {
        this.id = id;
        this.isHealingItem = isHealingItem;
        this.cooldownTicks = cooldownTicks;
        this.instantHeal = instantHeal;
        this.healOverTime = healOverTime;
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean isHealingItem() {
        return isHealingItem;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public InstantHeal getInstantHeal() {
        return instantHeal;
    }

    public HealOverTime getHealOverTime() {
        return healOverTime;
    }

    public static HealingItem fromJson(ResourceLocation id, JsonObject json) {
        boolean isHealingItem = json.has("is_healing_item") && json.get("is_healing_item").getAsBoolean();
        int cooldownTicks = json.has("cooldown_ticks") ? json.get("cooldown_ticks").getAsInt() : SpiceOfLifeConfig.COMMON.defaultHealingCooldown.get();
        
        InstantHeal instantHeal = null;
        if (json.has("instant_heal")) {
            JsonObject instantHealJson = json.getAsJsonObject("instant_heal");
            double percentMaxHp = instantHealJson.has("percent_max_hp") ? instantHealJson.get("percent_max_hp").getAsDouble() : 0.0;
            double percentMissingHp = instantHealJson.has("percent_missing_hp") ? instantHealJson.get("percent_missing_hp").getAsDouble() : 0.0;
            double flatHp = instantHealJson.has("flat_hp") ? instantHealJson.get("flat_hp").getAsDouble() : 0.0;
            instantHeal = new InstantHeal(percentMaxHp, percentMissingHp, flatHp);
        }

        HealOverTime healOverTime = null;
        if (json.has("heal_over_time")) {
            JsonObject healOverTimeJson = json.getAsJsonObject("heal_over_time");
            int duration = healOverTimeJson.has("duration") ? healOverTimeJson.get("duration").getAsInt() : 0;
            int interval = healOverTimeJson.has("interval") ? healOverTimeJson.get("interval").getAsInt() : 20;
            double amountFlat = healOverTimeJson.has("amount_flat") ? healOverTimeJson.get("amount_flat").getAsDouble() : 0.0;
            double amountMax = healOverTimeJson.has("amount_max") ? healOverTimeJson.get("amount_max").getAsDouble() : 0.0;
            healOverTime = new HealOverTime(duration, interval, amountFlat, amountMax);
        }

        return new HealingItem(id, isHealingItem, cooldownTicks, instantHeal, healOverTime);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("is_healing_item", isHealingItem);
        json.addProperty("cooldown_ticks", cooldownTicks);
        
        if (instantHeal != null) {
            JsonObject instantHealJson = new JsonObject();
            instantHealJson.addProperty("percent_max_hp", instantHeal.getPercentMaxHp());
            instantHealJson.addProperty("percent_missing_hp", instantHeal.getPercentMissingHp());
            instantHealJson.addProperty("flat_hp", instantHeal.getFlatHp());
            json.add("instant_heal", instantHealJson);
        }
        
        if (healOverTime != null) {
            JsonObject healOverTimeJson = new JsonObject();
            healOverTimeJson.addProperty("duration", healOverTime.getDuration());
            healOverTimeJson.addProperty("interval", healOverTime.getInterval());
            healOverTimeJson.addProperty("amount_flat", healOverTime.getAmountFlat());
            healOverTimeJson.addProperty("amount_max", healOverTime.getAmountMax());
            json.add("heal_over_time", healOverTimeJson);
        }
        
        return json;
    }

    public static class InstantHeal {
        private final double percentMaxHp;
        private final double percentMissingHp;
        private final double flatHp;

        public InstantHeal(double percentMaxHp, double percentMissingHp, double flatHp) {
            this.percentMaxHp = percentMaxHp;
            this.percentMissingHp = percentMissingHp;
            this.flatHp = flatHp;
        }

        public double getPercentMaxHp() {
            return percentMaxHp;
        }

        public double getPercentMissingHp() {
            return percentMissingHp;
        }

        public double getFlatHp() {
            return flatHp;
        }

        public boolean hasHealing() {
            return percentMaxHp > 0 || percentMissingHp > 0 || flatHp > 0;
        }
    }

    public static class HealOverTime {
        private final int duration;
        private final int interval;
        private final double amountFlat;
        private final double amountMax;

        public HealOverTime(int duration, int interval, double amountFlat, double amountMax) {
            this.duration = duration;
            this.interval = interval;
            this.amountFlat = amountFlat;
            this.amountMax = amountMax;
        }

        public int getDuration() {
            return duration;
        }

        public int getInterval() {
            return interval;
        }

        public double getAmountFlat() {
            return amountFlat;
        }

        public double getAmountMax() {
            return amountMax;
        }

        public boolean hasHealing() {
            return duration > 0 && (amountFlat > 0 || amountMax > 0);
        }
    }
}