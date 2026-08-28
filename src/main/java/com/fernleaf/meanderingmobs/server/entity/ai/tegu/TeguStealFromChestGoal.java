package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.server.entity.tameable.TeguEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class TeguStealFromChestGoal extends AbstractBlockInteractionGoal<TeguEntity> {

    public static final TagKey<Block> TEGU_STEALS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "tegu_steals"));
    private int eatTimer = 0;

    public TeguStealFromChestGoal(TeguEntity tegu) {
        super(tegu, 1.2D, 6.25D);
    }

    @Override
    protected boolean canInteract() {
        return !this.entity.isTamed() && this.entity.getMouthItem().isEmpty() && this.entity.getAiState() == 0;
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos teguPos = this.entity.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(teguPos.getX() + x, teguPos.getY() + y, teguPos.getZ() + z);
                    if (this.entity.level().getBlockState(mutable).is(TEGU_STEALS)) {
                        BlockEntity be = this.entity.level().getBlockEntity(mutable);
                        if (be instanceof Container container && !container.isEmpty()) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        // Reset timer when first reaching the chest
        this.eatTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        // If we have reached the target position, continuously eat items every 20 ticks (1 second)
        if (this.targetPos != null && this.reachedTarget) {
            this.entity.getNavigation().stop();

            if (++this.eatTimer % 20 == 0) {
                BlockEntity blockEntity = this.entity.level().getBlockEntity(this.targetPos);

                if (blockEntity instanceof Container container && !container.isEmpty()) {
                    List<Integer> validSlots = new ArrayList<>();
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        if (!container.getItem(i).isEmpty()) {
                            validSlots.add(i);
                        }
                    }

                    if (!validSlots.isEmpty()) {
                        int randomSlot = validSlots.get(this.entity.getRandom().nextInt(validSlots.size()));
                        ItemStack stolenStack = container.removeItem(randomSlot, 1);

                        if (!stolenStack.isEmpty()) {
                            container.setChanged();

                            this.entity.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);

                            if (this.entity.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(
                                        ParticleTypes.ITEM_SNOWBALL,
                                        this.entity.getX(), this.entity.getY() + 0.3D, this.entity.getZ(),
                                        10, 0.2D, 0.2D, 0.2D, 0.05D
                                );
                            }

                            if (stolenStack.is(TeguEntity.TEGU_TAMEABLE)) {
                                if (this.entity.getRandom().nextInt(3) == 0) {
                                    Player nearestPlayer = this.entity.level().getNearestPlayer(this.entity, 16.0D);
                                    if (nearestPlayer != null) {
                                        this.entity.tame(nearestPlayer);
                                    }
                                    this.entity.level().broadcastEntityEvent(this.entity, (byte) 7);
                                    setCooldown(200);
                                    stop();
                                } else {
                                    this.entity.level().broadcastEntityEvent(this.entity, (byte) 6);
                                }
                            } else {
                                this.entity.setMouthItem(stolenStack);
                                setCooldown(200);
                                stop();
                            }
                        }
                    }
                } else {
                    setCooldown(200);
                    stop();
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.eatTimer = 0;
    }
}