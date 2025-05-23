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
        public final ForgeConfigSpec.DoubleValue lowTimePercentage;

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

            lowTimePercentage = builder
                    .comment("Percentage of time remaining when food timer turns red")
                    .defineInRange("lowTimePercentage", 0.2D, 0.0D, 1.0D);

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