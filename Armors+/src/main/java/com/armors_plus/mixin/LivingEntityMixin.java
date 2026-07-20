package com.armors_plus.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyVariable(method = "handleFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float aromors_increaseFallDamage(float fallDistance) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.GOLDEN_HELMET) &&
                player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.GOLDEN_CHESTPLATE) &&
                player.getEquippedStack(EquipmentSlot.LEGS).isOf(Items.GOLDEN_LEGGINGS) &&
                player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.GOLDEN_BOOTS)) {
                return fallDistance * 2.0f; 
            }
        }
        return fallDistance;
    }
}