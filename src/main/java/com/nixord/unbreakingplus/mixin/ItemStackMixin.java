package com.nixord.unbreakingplus.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract boolean isDamageableItem();
    @Shadow public abstract int getDamageValue();
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract void setDamageValue(int damage);

    /**
     * Overrides the vanilla Unbreaking durability consumption logic
     * Each level gives a fixed 25% chance to not consume durability per damage point
     */
    @Inject(method = "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z",
            at = @At("HEAD"), cancellable = true)
    private void modifyUnbreakingBehavior(int amount, RandomSource random, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;

        if (!this.isDamageableItem()) {
            cir.setReturnValue(false);
            return;
        }

        // Get the Unbreaking level
        int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, self);

        if (unbreakingLevel > 0) {
            // Cancel vanilla behavior
            cir.cancel();

            // Custom Unbreaking logic: each level gives 25% chance to not consume durability
            int durabilityToConsume = 0;

            for (int i = 0; i < amount; i++) {
                // Roll for each point of damage
                // Each level of Unbreaking gives 25% chance to prevent this damage
                float chanceToPrevent = unbreakingLevel * 0.25f;

                if (random.nextFloat() >= chanceToPrevent) {
                    // Damage goes through
                    durabilityToConsume++;
                }
            }

            // Apply the calculated durability loss
            if (durabilityToConsume > 0) {
                int newDamage = this.getDamageValue() + durabilityToConsume;
                this.setDamageValue(newDamage);
                cir.setReturnValue(newDamage >= this.getMaxDamage());
            } else {
                cir.setReturnValue(false);
            }
        }
        // If no Unbreaking, let vanilla behavior proceed (don't cancel)
    }
}