package com.armors_plus.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @Inject(method = "onAttacked", at = @At("HEAD"), cancellable = true)
    private static void aromors_cancelPiglinAggro(ServerWorld world, PiglinEntity piglin, LivingEntity attacker, CallbackInfo ci) {
        if (attacker instanceof ServerPlayerEntity player) {
            if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.GOLDEN_HELMET) &&
                player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.GOLDEN_CHESTPLATE) &&
                player.getEquippedStack(EquipmentSlot.LEGS).isOf(Items.GOLDEN_LEGGINGS) &&
                player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.GOLDEN_BOOTS)) {
                ci.cancel();
            }
        }
    }
}