package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RuffianReadBehavior extends Behavior<RuffianEntity> {
    private int readingTicks = 0;
    private BlockPos targetBookshelf = null;
    private boolean upgraded = false;

    public RuffianReadBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, RuffianEntity ruffian) {
        ItemStack mainHandItem = ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.is(Items.ENCHANTED_BOOK)) return true;

        if (!ruffian.canRead()) return false;
        float analytical = ruffian.getPersonalityEngine().getTrait("analytical");
        if (analytical < 0.8F) return false;

        this.targetBookshelf = findNearbyBookshelf(ruffian);
        return this.targetBookshelf != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        return (ruffian.isReading() || !ruffian.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) && this.readingTicks < 200;
    }

    @Override
    protected void start(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.readingTicks = 0;
        this.upgraded = false;

        ItemStack mainHand = ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.is(Items.ENCHANTED_BOOK) && this.targetBookshelf != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetBookshelf, 1.1F, 2));
        }
    }

    @Override
    protected void tick(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ItemStack mainHand = ruffian.getItemInHand(InteractionHand.MAIN_HAND);

        // Player directly gave them an Enchanted Book
        if (mainHand.is(Items.ENCHANTED_BOOK)) {
            if (!ruffian.isReading()) ruffian.setReading(true);
            ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

            this.readingTicks++;
            if (this.readingTicks == 100 && !this.upgraded) {
                upgradeBookPower(mainHand);
                this.upgraded = true;
            }
            return;
        }

        // Environment Reading (Bookshelf block found)
        if (this.targetBookshelf != null) {
            double distanceSq = ruffian.distanceToSqr(this.targetBookshelf.getX() + 0.5D, this.targetBookshelf.getY(), this.targetBookshelf.getZ() + 0.5D);

            if (distanceSq <= 4.0D) {
                ruffian.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.targetBookshelf));

                if (!ruffian.isReading()) {
                    ruffian.setReading(true);
                    ruffian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENCHANTED_BOOK));
                }

                this.readingTicks++;
                if (this.readingTicks == 100 && !this.upgraded) {
                    upgradeBookPower(ruffian.getItemInHand(InteractionHand.MAIN_HAND));
                    this.upgraded = true;
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setReading(false);

        ItemStack heldBook = ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (!heldBook.isEmpty()) {
            Player owner = ruffian.getOwner();
            // Directly hand the upgraded book back to the owner if close, otherwise give to entity or drop neatly
            if (owner != null && ruffian.distanceToSqr(owner) <= 16.0D) {
                if (!owner.getInventory().add(heldBook)) {
                    ruffian.spawnAtLocation(heldBook);
                }
            } else {
                ruffian.spawnAtLocation(heldBook);
            }
        }

        ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ruffian.applyReadCooldown(600);

        // Sparkle particles for finishing reading & upgrade
        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 0.8D;
            double offsetY = level.random.nextDouble() * 1.5D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 0.8D;
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    ruffian.getX() + offsetX, ruffian.getY() + offsetY, ruffian.getZ() + offsetZ,
                    1, 0.0D, 0.0D, 0.0D, 0.05D
            );
        }

        this.readingTicks = 0;
        this.targetBookshelf = null;
        this.upgraded = false;
    }

    private BlockPos findNearbyBookshelf(RuffianEntity ruffian) {
        BlockPos currentPos = ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(currentPos.getX() + x, currentPos.getY() + y, currentPos.getZ() + z);
                    BlockState state = ruffian.level().getBlockState(mutable);
                    if (state.is(Blocks.BOOKSHELF) || state.is(Blocks.CHISELED_BOOKSHELF)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private void upgradeBookPower(ItemStack book) {
        if (!book.is(Items.ENCHANTED_BOOK)) return;
        EnchantmentHelper.updateEnchantments(book, mutableEnchantments -> {
            var holders = new java.util.ArrayList<>(mutableEnchantments.keySet());
            for (var holder : holders) {
                int currentLevel = mutableEnchantments.getLevel(holder);
                mutableEnchantments.set(holder, currentLevel + 1);
            }
        });
    }
}