package com.leclowndu93150.spiceoflifebobo;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SpiceOfLifeConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();

        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue disableHunger;
        public final ForgeConfigSpec.BooleanValue keepFoodOnDeath;
        public final ForgeConfigSpec.BooleanValue showTooltips;
        public final ForgeConfigSpec.IntValue defaultFoodMemory;
        public final ForgeConfigSpec.IntValue defaultHealingCooldown;
        public final ForgeConfigSpec.BooleanValue enableHealingPenalty;
        public final ForgeConfigSpec.IntValue healingPenaltyDuration;
        public final ForgeConfigSpec.DoubleValue healingPenaltyPerStack;
        public final ForgeConfigSpec.IntValue maxHealingPenaltyStacks;
        public final ForgeConfigSpec.DoubleValue lowTimePercentage;
        public final ForgeConfigSpec.DoubleValue baseFoodAttributeBoost;
        public final ForgeConfigSpec.DoubleValue foodConsumptionSpeedIncrease;
        public final ForgeConfigSpec.DoubleValue foodDurationDecrease;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Common configuration settings").push("common");

            disableHunger = builder
                    .comment("Disable vanilla hunger mechanics")
                    .define("disableHunger", true);

            keepFoodOnDeath = builder
                    .comment("Keep food effects after death")
                    .define("keepFoodOnDeath", false);

            showTooltips = builder
                    .comment("Show tooltips for food effects")
                    .define("showTooltips", true);

            defaultFoodMemory = builder
                    .comment("Default food memory value (how many foods a player can benefit from at once)")
                    .defineInRange("defaultFoodMemory", 3, 1, 10);

            defaultHealingCooldown = builder
                    .comment("Default healing item cooldown in ticks (20 ticks = 1 second)")
                    .defineInRange("defaultHealingCooldown", 600, 0, 72000);

            enableHealingPenalty = builder
                    .comment("Enable diminishing returns for healing items to prevent spam healing")
                    .define("enableHealingPenalty", true);

            healingPenaltyDuration = builder
                    .comment("Duration in ticks that healing penalty lasts (20 ticks = 1 second)")
                    .defineInRange("healingPenaltyDuration", 1200, 0, 72000);

            healingPenaltyPerStack = builder
                    .comment("Healing reduction per penalty stack (0.2 = 20% less healing per stack)")
                    .defineInRange("healingPenaltyPerStack", 0.25, 0.0, 1.0);

            maxHealingPenaltyStacks = builder
                    .comment("Maximum penalty stacks (at max stacks, healing is nearly zero)")
                    .defineInRange("maxHealingPenaltyStacks", 4, 1, 10);

            lowTimePercentage = builder
                    .comment("Percentage of time remaining when food timer turns red")
                    .defineInRange("lowTimePercentage", 0.2D, 0.0D, 1.0D);

            builder.comment("Foodie Enchantment Settings").push("foodie_enchantment");

            baseFoodAttributeBoost = builder
                    .comment("Base food attribute bonus boost percentage per enchantment level")
                    .defineInRange("baseFoodAttributeBoost", 0.25D, 0.0D, 2.0D);

            foodConsumptionSpeedIncrease = builder
                    .comment("Food consumption speed increase percentage per enchantment level")
                    .defineInRange("foodConsumptionSpeedIncrease", 0.15D, 0.0D, 1.0D);

            foodDurationDecrease = builder
                    .comment("Food duration decrease percentage (fixed for all levels)")
                    .defineInRange("foodDurationDecrease", 0.20D, 0.0D, 1.99D);

            builder.pop();

            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showFoodHud;
        public final ForgeConfigSpec.BooleanValue useLargeIcons;
        public final ForgeConfigSpec.IntValue hudOffsetX;
        public final ForgeConfigSpec.IntValue hudOffsetY;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client configuration settings").push("client");

            showFoodHud = builder
                    .comment("Show food HUD")
                    .define("showFoodHud", true);

            useLargeIcons = builder
                    .comment("Use large icons in food HUD")
                    .define("useLargeIcons", true);

            hudOffsetX = builder
                    .comment("HUD X offset from position")
                    .defineInRange("hudOffsetX", 0, -100, 100);

            hudOffsetY = builder
                    .comment("HUD Y offset from position")
                    .defineInRange("hudOffsetY", 0, -100, 100);

            builder.pop();
        }
    }
}