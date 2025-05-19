package com.leclowndu93150.spiceoflifebobo.mixin;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;

    /**
     * Prevent hunger from decreasing if hunger is disabled
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(Player player, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Keep food and saturation at max
            this.foodLevel = 20;
            this.saturationLevel = 20.0F;

            // Cancel vanilla hunger mechanics
            ci.cancel();
        }
    }

    /**
     * Always return that the player can eat if hunger is disabled
     */
    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    private void onNeedsFood(CallbackInfoReturnable<Boolean> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Always allow eating
            cir.setReturnValue(true);
        }
    }

    /**
     * Keep the food level full if hunger is disabled
     */
    @Inject(method = "getFoodLevel", at = @At("HEAD"), cancellable = true)
    private void onGetFoodLevel(CallbackInfoReturnable<Integer> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Always return full food level
            cir.setReturnValue(20);
        }
    }

    /**
     * Keep the saturation full if hunger is disabled
     */
    @Inject(method = "getSaturationLevel", at = @At("HEAD"), cancellable = true)
    private void onGetSaturationLevel(CallbackInfoReturnable<Float> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Always return full saturation
            cir.setReturnValue(20.0F);
        }
    }

    /**
     * Prevent the food level from changing if hunger is disabled
     */
    @Inject(method = "setFoodLevel", at = @At("HEAD"), cancellable = true)
    private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Cancel vanilla food level changes
            ci.cancel();
        }
    }

    /**
     * Prevent saturation from changing if hunger is disabled
     */
    @Inject(method = "setSaturation", at = @At("HEAD"), cancellable = true)
    private void onSetSaturation(float saturation, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Cancel vanilla saturation changes
            ci.cancel();
        }
    }

    /**
     * Prevent exhaustion from accumulating if hunger is disabled
     */
    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void onAddExhaustion(float exhaustion, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Cancel vanilla exhaustion changes
            ci.cancel();
        }
    }
}