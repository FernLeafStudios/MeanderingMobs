package com.fernleaf.meanderingmobs.server.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ActiveSoulOrbItem extends Item {

    public ActiveSoulOrbItem(Properties properties) {
        super(properties.stacksTo(1)); // Ensures max stack size is strictly 1
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null || customData.isEmpty()) {
            return InteractionResult.FAIL;
        }

        CompoundTag tag = customData.copyTag();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        ServerLevel serverLevel = (ServerLevel) level;

        Optional<EntityType<?>> entityType = EntityType.by(tag);
        if (entityType.isPresent()) {
            Entity entity = entityType.get().create(serverLevel);
            if (entity != null) {
                entity.load(tag);
                entity.moveTo(
                        spawnPos.getX() + 0.5D,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5D,
                        player != null ? player.getYRot() : 0.0F,
                        0.0F
                );

                serverLevel.addFreshEntity(entity);


                serverLevel.sendParticles(
                        ParticleTypes.GUST,
                        spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D,
                        1, 0.0, 0.0, 0.0, 0.0
                );

                level.playSound(
                        null, spawnPos,
                        SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                        1.0F, 0.8F
                );

                if (player != null && !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.FAIL;
    }
}