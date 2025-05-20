package com.leclowndu93150.spiceoflifebobo.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemUtils.class)
public class ItemUtilsMixin {
    @Inject(method = "startUsingInstantly", at = @At("HEAD"), cancellable = true)
    private static void onStartUsingInstantly(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemstack = player.getItemInHand(hand);
        // Always allow the player to start using the item
        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.consume(itemstack));
    }
}