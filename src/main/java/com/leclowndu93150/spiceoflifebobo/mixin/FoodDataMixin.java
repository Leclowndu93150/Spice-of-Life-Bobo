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

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(Player player, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            this.foodLevel = 20;
            this.saturationLevel = 20.0F;

            ci.cancel();
        }
    }

    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    private void onNeedsFood(CallbackInfoReturnable<Boolean> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getFoodLevel", at = @At("HEAD"), cancellable = true)
    private void onGetFoodLevel(CallbackInfoReturnable<Integer> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            cir.setReturnValue(20);
        }
    }

    @Inject(method = "getSaturationLevel", at = @At("HEAD"), cancellable = true)
    private void onGetSaturationLevel(CallbackInfoReturnable<Float> cir) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            cir.setReturnValue(20.0F);
        }
    }

    @Inject(method = "setFoodLevel", at = @At("HEAD"), cancellable = true)
    private void onSetFoodLevel(int foodLevel, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "setSaturation", at = @At("HEAD"), cancellable = true)
    private void onSetSaturation(float saturation, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void onAddExhaustion(float exhaustion, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            ci.cancel();
        }
    }
}