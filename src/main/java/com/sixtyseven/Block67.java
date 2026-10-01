package com.sixtyseven;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Block67 extends Block {
    public Block67(Settings settings) {
        super(settings);
    }

    // Click derecho = salto épico 67
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world instanceof ServerWorld sw) {
            player.setVelocity(player.getVelocity().x, 1.34, player.getVelocity().z);
            player.velocityModified = true;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 134, 0));
            sw.spawnParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    67, 0.4, 0.2, 0.4, 0.2);
            world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        return ActionResult.success(world.isClient);
    }
}
