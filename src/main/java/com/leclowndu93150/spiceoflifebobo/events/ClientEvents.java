package com.leclowndu93150.spiceoflifebobo.events;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.data.ActiveFood;
import com.leclowndu93150.spiceoflifebobo.data.FoodAttributeModifier;
import com.leclowndu93150.spiceoflifebobo.data.FoodEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    private static final ResourceLocation FOOD_HUD_TEXTURE = new ResourceLocation(SpiceOfLifeBobo.MOD_ID, "textures/gui/food_hud.png");

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        if (!SpiceOfLifeConfig.CLIENT.showFoodHud.get()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        player.getCapability(SpiceOfLifeBobo.FOOD_STORAGE_CAPABILITY).ifPresent(foodStorage -> {
            List<ActiveFood> activeFoods = foodStorage.getActiveFoods();
            if (activeFoods.isEmpty()) return;

            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();

            int x, y;
            SpiceOfLifeConfig.HudPosition position = SpiceOfLifeConfig.CLIENT.hudPosition.get();

            switch (position) {
                case TOP_LEFT:
                    x = 5 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = 5 + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
                case TOP_RIGHT:
                    x = width - 65 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = 5 + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
                case BOTTOM_LEFT:
                    x = 5 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = height - 5 - (activeFoods.size() * 24) + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
                case BOTTOM_RIGHT:
                    x = width - 65 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = height - 5 - (activeFoods.size() * 24) + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
                case CENTER_LEFT:
                    x = 5 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = height / 2 - (activeFoods.size() * 12) + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
                case CENTER_RIGHT:
                default:
                    x = width - 65 + SpiceOfLifeConfig.CLIENT.hudOffsetX.get();
                    y = height / 2 - (activeFoods.size() * 12) + SpiceOfLifeConfig.CLIENT.hudOffsetY.get();
                    break;
            }

            GuiGraphics graphics = event.getGuiGraphics();
            renderFoodHud(graphics, foodStorage, x, y);
        });
    }

    private void renderFoodHud(GuiGraphics graphics, IFoodStorage foodStorage, int x, int y) {
        List<ActiveFood> activeFoods = foodStorage.getActiveFoods();

        // Render background panel
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.8F);
        graphics.blit(FOOD_HUD_TEXTURE, x, y, 0, 0, 60, activeFoods.size() * 24);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render food info
        int yOffset = 0;
        for (ActiveFood food : activeFoods) {
            // Render item icon
            ItemStack foodStack = new ItemStack(food.getItem());
            graphics.renderItem(foodStack, x + 5, y + yOffset + 4);

            // Render food name
            Component name = foodStack.getHoverName();
            graphics.drawString(Minecraft.getInstance().font, name, x + 25, y + yOffset + 4, 0xFFFFFF, true);

            // Render duration bar
            float durationPercent = food.getRemainingDurationPercent();
            int barWidth = (int)(50 * durationPercent);

            // Bar background
            graphics.fill(x + 25, y + yOffset + 16, x + 25 + 50, y + yOffset + 20, 0x80000000);

            // Bar filled part
            int barColor = getBarColor(durationPercent);
            graphics.fill(x + 25, y + yOffset + 16, x + 25 + barWidth, y + yOffset + 20, barColor);

            // Move to next food
            yOffset += 24;
        }
    }

    private int getBarColor(float percent) {
        if (percent > 0.6f) {
            return 0xFF00CC00; // Green
        } else if (percent > 0.3f) {
            return 0xFFCCCC00; // Yellow
        } else {
            return 0xFFCC0000; // Red
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!SpiceOfLifeConfig.COMMON.showTooltips.get()) return;

        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<Component> tooltip = event.getToolTip();

        List<FoodEffect> effects = SpiceOfLifeBobo.getFoodEffectManager().getEffectsForFood(item);
        if (effects.isEmpty()) return;

        // Add separator
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.spiceoflifebobo.effects").withStyle(ChatFormatting.GOLD));

        // Add duration
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
                Attribute attribute = modifier.getAttribute();
                double amount = modifier.getAmount();
                AttributeModifier.Operation operation = modifier.getOperation();

                Component modifierText = formatAttributeModifier(attribute, amount, operation);
                tooltip.add(modifierText);
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
            });
        }
    }

    private Component formatAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        Component attributeName = Component.translatable(attribute.getDescriptionId());

        String format;
        ChatFormatting color;

        // Format based on operation and whether it's positive or negative
        if (operation == AttributeModifier.Operation.ADDITION) {
            // Addition: +X or -X
            if (amount > 0) {
                format = "+%.1f %s";
                color = ChatFormatting.BLUE;
            } else {
                format = "%.1f %s";
                color = ChatFormatting.RED;
            }
        } else if (operation == AttributeModifier.Operation.MULTIPLY_BASE) {
            // Multiply base: +X% or -X%
            // Convert to percentage
            amount = amount * 100;
            if (amount > 0) {
                format = "+%.0f%% %s";
                color = ChatFormatting.BLUE;
            } else {
                format = "%.0f%% %s";
                color = ChatFormatting.RED;
            }
        } else {
            // Multiply total: +X% or -X%
            // Convert to percentage
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
}
