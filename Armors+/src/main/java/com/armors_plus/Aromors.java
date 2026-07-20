package com.armors_plus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Aromors implements ModInitializer {
    
    public static final String MOD_ID = "aromors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Identifier NETHERITE_KB_ID = Identifier.of(MOD_ID, "netherite_knockback_boost");
    private static final Random RANDOM = new Random();

    @Override
    public void onInitialize() {
        // Pętla serwera obsługująca efekty stałe zbroi
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkArmorEffects(player);
            }
        });

        // Obsługa szansy na piorun przy uderzeniu moba w miedzianej zbroi (5% na 1, 1% na 3)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && entity instanceof LivingEntity target && player instanceof ServerPlayerEntity serverPlayer) {
                if (hasFullSet(serverPlayer, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS)) {
                    int roll = RANDOM.nextInt(100); // Losowanie 0 - 99

                    if (roll < 1) { // 1% szansy na 3 pioruny
                        spawnLightning(world, target.getBlockPos());
                        spawnLightning(world, target.getBlockPos());
                        spawnLightning(world, target.getBlockPos());
                    } else if (roll < 6) { // 5% szansy na 1 piorun
                        spawnLightning(world, target.getBlockPos());
                    }
                }
            }
            return ActionResult.PASS;
        });

        // Odporność na obrażenia od piorunów w miedzianej zbroi
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (hasFullSet(player, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS)) {
                    if (source.isOf(DamageTypes.LIGHTNING_BOLT)) {
                        return false; // Anuluje obrażenia od pioruna w 100%
                    }
                }
            }
            return true;
        });

        LOGGER.info("Aromors+ odpala się z miedzianymi piorunami!");
    }

    private void spawnLightning(net.minecraft.world.World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            EntityType.LIGHTNING_BOLT.spawn(serverWorld, pos, SpawnReason.TRIGGERED);
        }
    }

    private void checkArmorEffects(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // a) Skórzana zbroja - Usuwa truciznę
        if (hasFullSet(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS)) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                player.removeStatusEffect(StatusEffects.POISON);
            }
        }

        // c) Kolczuga - Obrażenia z bliska (1 serce na sekundę)
        if (hasFullSet(player, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS)) {
            if (player.age % 20 == 0) { 
                Box box = player.getBoundingBox().expand(0.5); 
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, box, e -> e != player)) {
                    entity.damage(world, player.getDamageSources().thorns(player), 2.0f); 
                }
            }
        }

        // d) Żelazna zbroja - Siła i lekkie spowolnienie
        if (hasFullSet(player, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, false, true));
        }

        // e) Diamentowa zbroja - Szybkość i radar rud (promień 3 kratki)
        if (hasFullSet(player, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true)); 
            
            if (player.age % 25 == 0) { 
                BlockPos playerPos = player.getBlockPos();
                int radius = 3;
                BlockPos closestOre = null;
                double minDistanceSq = Double.MAX_VALUE;
                
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos checkPos = playerPos.add(x, y, z);
                            if (world.getBlockState(checkPos).isIn(BlockTags.DIAMOND_ORES) || 
                                world.getBlockState(checkPos).isIn(BlockTags.GOLD_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.IRON_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.COAL_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.COPPER_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.REDSTONE_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.LAPIS_ORES) ||
                                world.getBlockState(checkPos).isIn(BlockTags.EMERALD_ORES) ||
                                world.getBlockState(checkPos).isOf(Blocks.ANCIENT_DEBRIS)) {
                                
                                double distSq = playerPos.getSquaredDistance(checkPos);
                                if (distSq < minDistanceSq) {
                                    minDistanceSq = distSq;
                                    closestOre = checkPos;
                                }
                            }
                        }
                    }
                }
                
                if (closestOre != null) {
                    Text message = Text.translatable(
                        "text.aromors.ore_radar", 
                        closestOre.getX(), closestOre.getY(), closestOre.getZ()
                    );
                    
                    player.sendMessage(message, true);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 
                        net.minecraft.sound.SoundCategory.PLAYERS, 0.6f, 1.2f);
                }
            }
        }

        // f) Netherytowa zbroja - Odporność na ogień i 100% odporności na odrzut
        boolean hasNetherite = hasFullSet(player, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        if (hasNetherite) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
            
            EntityAttributeInstance kbAttr = player.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE);
            if (kbAttr != null && !kbAttr.hasModifier(NETHERITE_KB_ID)) {
                kbAttr.addTemporaryModifier(new EntityAttributeModifier(NETHERITE_KB_ID, 0.6, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        } else {
            EntityAttributeInstance kbAttr = player.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE);
            if (kbAttr != null && kbAttr.hasModifier(NETHERITE_KB_ID)) {
                kbAttr.removeModifier(NETHERITE_KB_ID);
            }
        }

        // g) Czapka żółwia - Oddychanie pod wodą
        if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.TURTLE_HELMET)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false, true));
        }
    }

    private boolean hasFullSet(ServerPlayerEntity player, Item helmet, Item chestplate, Item leggings, Item boots) {
        return player.getEquippedStack(EquipmentSlot.HEAD).isOf(helmet) &&
               player.getEquippedStack(EquipmentSlot.CHEST).isOf(chestplate) &&
               player.getEquippedStack(EquipmentSlot.LEGS).isOf(leggings) &&
               player.getEquippedStack(EquipmentSlot.FEET).isOf(boots);
    }
}