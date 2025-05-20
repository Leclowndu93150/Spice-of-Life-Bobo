package com.leclowndu93150.spiceoflifebobo.mixin;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeConfig;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.spellcraftgaming.rpghud.gui.hud.element.HudElementType;
import net.spellcraftgaming.rpghud.main.RenderOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderOverlay.class, remap = false)
public class RenderOverlayMixin {

    @Inject(method = "preventEventType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onPreventEventType(HudElementType type, CallbackInfoReturnable<Boolean> cir) {
        if (type == HudElementType.FOOD && SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            // Return false to ensure RPGHud doesn't cancel food events
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onGameOverlayRenderPre", at = @At("HEAD"), cancellable = true)
    private void onRenderGuiOverlayEventPre(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        if (event.getOverlay().id() == VanillaGuiOverlay.FOOD_LEVEL.id() &&
                SpiceOfLifeConfig.COMMON.disableHunger.get()) {
            ci.cancel();
        }
    }
}