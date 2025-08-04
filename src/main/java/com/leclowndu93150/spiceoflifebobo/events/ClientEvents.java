package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.data.FoodAttributeModifier;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import com.leclowndu93150.spiceoflifebobo.data.HealingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = SpiceOfLifeBobo.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    private static final Minecraft client = Minecraft.getInstance();
    private static int mouseX = 0;
    private static int mouseY = 0;
    private static Item hoveredFoodItem = null;
    private static List<ActiveFood> hoveredFoods = null;

    private static final int slotSpacing = 2; // Horizontal spacing between food slots
    private static final int yLevelOffset = 39; // Y offset from bottom of screen

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        int screenWidth = client.getWindow().getScreenWidth();
        int screenHeight = client.getWindow().getScreenHeight();

        if (screenWidth > 0 && screenHeight > 0) {
            mouseX = (int) (client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / screenWidth);
            mouseY = (int) (client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / screenHeight);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()) return;

        if (!SpiceOfLifeConfig.CLIENT.showFoodHud.get() || !Minecraft.getInstance().gameMode.getPlayerMode().equals(GameType.DEFAULT_MODE)) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        hoveredFoodItem = null;
        hoveredFoods = null;

        player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
            Map<Item, List<ActiveFood>> foodsByType = foodStorage.getFoodsByType();
            if (foodsByType.isEmpty()) return;

            int width = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;
            boolean useLargeIcons = SpiceOfLifeConfig.CLIENT.useLargeIcons.get();
            int height = minecraft.getWindow().getGuiScaledHeight() - yLevelOffset - (useLargeIcons ? 6 : 0);

            int offset = 1;
            int size = useLargeIcons ? 14 : 9;

            for (Map.Entry<Item, List<ActiveFood>> entry : foodsByType.entrySet()) {
                Item foodItem = entry.getKey();
                List<ActiveFood> foodsOfType = entry.getValue();
                if (foodsOfType.isEmpty()) continue;

                foodsOfType.sort(Comparator.comparing(ActiveFood::getDuration));

                ActiveFood activeTicking = foodsOfType.get(0);

                renderFoodTypeSlot(event.getGuiGraphics(), activeTicking, foodsOfType.size(), width, size, offset, height, useLargeIcons, foodStorage);

                // Using slotSpacing variable for calculating the slot position
                int startWidth = width - (size * offset) - (slotSpacing * (offset - 1)) + 1;
                if (mouseX >= startWidth && mouseX <= startWidth + size &&
                        mouseY >= height && mouseY <= height + size) {
                    hoveredFoodItem = foodItem;
                    hoveredFoods = new ArrayList<>(foodsOfType);
                }

                offset++;
            }

            if (hoveredFoods != null && !hoveredFoods.isEmpty()) {
                renderFoodTooltip(event.getGuiGraphics(), hoveredFoods, mouseX, mouseY, foodStorage);
            }
        });
    }

    private static void renderFoodTypeSlot(GuiGraphics graphics, ActiveFood activeTicking, int count,
                                           int width, int size, int offset, int height,
                                           boolean useLargeIcons, IFoodStorage foodStorage) {
        ItemStack foodStack = new ItemStack(activeTicking.getItem());
        boolean isDrink = foodStack.getUseAnimation() == UseAnim.DRINK;

        int bgColor = isDrink ? FastColor.ARGB32.color(96, 52, 104, 163) : FastColor.ARGB32.color(96, 0, 0, 0);
        int yellow = FastColor.ARGB32.color(255, 255, 191, 0);

        int startWidth = width - (size * offset) - (slotSpacing * (offset - 1)) + 1;
        float ticksLeftPercent = (float) activeTicking.getDuration() / activeTicking.getMaxDuration();
        int barHeight = Math.max(1, (int) ((size + 2f) * ticksLeftPercent));
        int barColor = ticksLeftPercent < SpiceOfLifeConfig.COMMON.lowTimePercentage.get() ?
                FastColor.ARGB32.color(180, 255, 10, 10) : FastColor.ARGB32.color(96, 0, 0, 0);

        float time = (float) activeTicking.getDuration() / (20 * 60);
        boolean isSeconds = false;
        String minutes = String.format("%.0f", time);

        if (time < 1f) {
            isSeconds = true;
            time = (float) activeTicking.getDuration() / 20;
            minutes = String.format("%.0f", time);
        }

        graphics.fill(startWidth, height, startWidth + size, height + size, bgColor);
        graphics.fill(startWidth, Math.max(height, height - barHeight + size), startWidth + size, height + size, barColor);

        var pose = graphics.pose();
        pose.pushPose();

        float scale = useLargeIcons ? 0.75f : 0.5f;
        pose.scale(scale, scale, scale);

        float scaleFactor = 1f / scale;
        float centerX = (startWidth + size / 2f) * scaleFactor - 8f;
        float centerY = (height + size / 2f) * scaleFactor - 8f;

        graphics.renderItem(foodStack, (int) centerX, (int) centerY);

        pose.pushPose();
        pose.translate(0.0f, 0.0f, 200.0f);

        pose.scale(1/scale, 1/scale, 1/scale);

        int textX = startWidth + (minutes.length() > 1 ? 3 : 5);
        int textColor = isSeconds ? FastColor.ARGB32.color(255, 237, 57, 57) : FastColor.ARGB32.color(255, 255, 255, 255);

        graphics.drawString(client.font, minutes, textX, height + 4, textColor);

        if (activeTicking.getEffects().size() > 1) {
            graphics.drawString(client.font, "+" + (activeTicking.getEffects().size() - 1), startWidth + size - 6, height, yellow);
        }

        pose.popPose();

        pose.popPose();
    }

    private static void renderFoodTooltip(GuiGraphics graphics, List<ActiveFood> foodsOfType,
                                          int mouseX, int mouseY, IFoodStorage foodStorage) {
        if (foodsOfType.isEmpty()) return;

        List<Component> tooltip = new ArrayList<>();
        ActiveFood firstFood = foodsOfType.get(0);

        ItemStack foodStack = new ItemStack(firstFood.getItem());
        tooltip.add(foodStack.getHoverName());

        if (foodsOfType.size() > 1) {
            tooltip.add(Component.literal("x" + foodsOfType.size() + " " + foodStack.getHoverName().getString())
                    .withStyle(ChatFormatting.YELLOW));
        }

        tooltip.add(Component.literal(""));

        for (int i = 0; i < foodsOfType.size(); i++) {
            ActiveFood food = foodsOfType.get(i);
            int seconds = food.getDuration() / 20;
            int minutes = seconds / 60;
            seconds %= 60;

            boolean isActive = foodStorage.isActiveTicking(food);
            String prefix = foodsOfType.size() > 1 ? "#" + (i + 1) + ": " : "";
            ChatFormatting color = isActive ? ChatFormatting.GREEN : ChatFormatting.GRAY;

            if (minutes > 0) {
                tooltip.add(Component.literal(prefix + (isActive ? "➤ " : ""))
                        .append(Component.translatable("tooltip.spiceoflifebobo.duration.minutes", minutes, seconds))
                        .withStyle(color));
            } else {
                tooltip.add(Component.literal(prefix + (isActive ? "➤ " : ""))
                        .append(Component.translatable("tooltip.spiceoflifebobo.duration.seconds", seconds))
                        .withStyle(color));
            }
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.spiceoflifebobo.effects").withStyle(ChatFormatting.GOLD));

        for (FoodEffect effect : firstFood.getEffects()) {
            for (FoodAttributeModifier modifier : effect.getAttributeModifiers()) {
                double baseAmount = modifier.getAmount();
                double stackedAmount = baseAmount * foodsOfType.size();

                tooltip.add(formatAttributeModifier(modifier, stackedAmount));
            }
        }

        graphics.renderComponentTooltip(client.font, tooltip, mouseX, mouseY);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SpiceOfLifeConfig.COMMON.showTooltips.get()) return;

        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        List<FoodEffect> effects = SpiceOfLifeBobo.getFoodEffectManager().getEffectsForFood(stack.getItem());
        HealingItem healingItem = SpiceOfLifeBobo.getHealingItemManager().getHealingItemForFood(stack.getItem());
        
        if (effects.isEmpty() && healingItem == null) return;
        
        // Add healing item tooltip
        if (healingItem != null && healingItem.isHealingItem()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.spiceoflifebobo.healing_item").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            
            if (healingItem.getInstantHeal() != null && healingItem.getInstantHeal().hasHealing()) {
                HealingItem.InstantHeal instantHeal = healingItem.getInstantHeal();
                addHealAmountTooltips(tooltip, "Instant:", instantHeal.getPercentMaxHp(), instantHeal.getPercentMissingHp(), instantHeal.getFlatHp());
            }
            
            if (healingItem.getHealOverTime() != null && healingItem.getHealOverTime().hasHealing()) {
                HealingItem.HealOverTime healOverTime = healingItem.getHealOverTime();
                int duration = healOverTime.getDuration() / 20;
                int interval = healOverTime.getInterval() / 20;
                tooltip.add(Component.literal("Over Time: Every " + interval + "s for " + duration + "s")
                    .withStyle(ChatFormatting.DARK_GREEN));
                addHealAmountTooltips(tooltip, "  ", 0, 0, healOverTime.getAmountFlat());
                if (healOverTime.getAmountMax() > 0) {
                    tooltip.add(Component.literal("  +" + String.format("%.1f%% Max HP", healOverTime.getAmountMax() * 100))
                        .withStyle(ChatFormatting.GREEN));
                }
            }
        }
        
        if (effects.isEmpty()) return;

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.spiceoflifebobo.effects").withStyle(ChatFormatting.GOLD));

        int maxDuration = 0;
        for (FoodEffect effect : effects) {
            maxDuration = Math.max(maxDuration, effect.getDuration());
        }

        int seconds = maxDuration / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        if (minutes > 0) {
            tooltip.add(Component.translatable("tooltip.spiceoflifebobo.duration.minutes", minutes, seconds)
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.spiceoflifebobo.duration.seconds", seconds)
                    .withStyle(ChatFormatting.GRAY));
        }

        for (FoodEffect effect : effects) {
            for (FoodAttributeModifier modifier : effect.getAttributeModifiers()) {
                tooltip.add(formatAttributeModifier(modifier, modifier.getAmount()));
            }
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
                if (!foodStorage.canEatFood()) {
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.translatable("tooltip.spiceoflifebobo.cannot_eat", foodStorage.getMaxFoods())
                            .withStyle(ChatFormatting.RED));
                }

                Item item = stack.getItem();
                int count = foodStorage.getFoodTypeCount(item);
                if (count > 0) {
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("Currently active: x" + count)
                            .withStyle(ChatFormatting.GREEN));
                }
            });
        }
    }

    private static Component formatAttributeModifier(FoodAttributeModifier modifier, double stackedAmount) {
        Component attributeName = Component.translatable(modifier.getAttribute().getDescriptionId());

        String format;
        ChatFormatting color;
        double amount = stackedAmount;

        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            if (amount > 0) {
                format = "+%.1f %s";
                color = ChatFormatting.BLUE;
            } else {
                format = "%.1f %s";
                color = ChatFormatting.RED;
            }
        } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
            amount = amount * 100;
            if (amount > 0) {
                format = "+%.0f%% %s";
                color = ChatFormatting.BLUE;
            } else {
                format = "%.0f%% %s";
                color = ChatFormatting.RED;
            }
        } else {
            amount = amount * 100;
            if (amount > 0) {
                format = "+%.0f%% %s (Total)";
                color = ChatFormatting.BLUE;
            } else {
                format = "%.0f%% %s (Total)";
                color = ChatFormatting.RED;
            }
        }

        return Component.literal(String.format(format, amount, attributeName.getString())).withStyle(color);
    }
    
    private static void addHealAmountTooltips(List<Component> tooltip, String prefix, double percentMax, double percentMissing, double flat) {
        if (flat > 0) {
            tooltip.add(Component.literal(prefix + " +" + String.format("%.1f HP", flat))
                .withStyle(ChatFormatting.GREEN));
        }
        
        if (percentMax > 0) {
            tooltip.add(Component.literal(prefix + " +" + String.format("%.1f%% Max HP", percentMax * 100))
                .withStyle(ChatFormatting.GREEN));
        }
        
        if (percentMissing > 0) {
            tooltip.add(Component.literal(prefix + " +" + String.format("%.1f%% Missing HP", percentMissing * 100))
                .withStyle(ChatFormatting.GREEN));
        }
    }
}