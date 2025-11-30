package com.nixord.unbreakingplus.mixin;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class UnbreakingEnchantmentMixin {

    /**
     * Changes the max level of Unbreaking from 3 to 4
     */
    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void modifyMaxLevel(CallbackInfoReturnable<Integer> cir) {
        Enchantment self = (Enchantment)(Object)this;

        // Check if this is the Unbreaking enchantment
        if (self == Enchantments.UNBREAKING) {
            cir.setReturnValue(4);
            cir.cancel();
        }
    }
}