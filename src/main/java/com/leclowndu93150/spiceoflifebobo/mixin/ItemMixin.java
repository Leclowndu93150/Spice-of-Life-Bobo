package com.leclowndu93150.spiceoflifebobo.mixin;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow public abstract FoodProperties getFoodProperties();

    @Inject(method = "isEdible", at = @At("HEAD"), cancellable = true)
    private void onIsEdible(CallbackInfoReturnable<Boolean> cir) {
        if (getFoodProperties() != null) {
            cir.setReturnValue(true);
        }
    }
}