package com.leclowndu93150.spiceoflifebobo.mixin;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ForgeGui.class)
public class ForgeGuiMixin {
    /**
     * Prevent rendering the hunger bar if hunger is disabled
     */
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderFood(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            ci.cancel();
        }
    }
}