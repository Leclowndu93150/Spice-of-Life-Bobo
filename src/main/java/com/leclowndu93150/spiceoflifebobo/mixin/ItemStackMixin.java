package com.leclowndu93150.spiceoflifebobo.mixin;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "isEdible", at = @At("RETURN"), cancellable = true)
    private void onIsEdible(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            cir.setReturnValue(true);
        }
    }
}