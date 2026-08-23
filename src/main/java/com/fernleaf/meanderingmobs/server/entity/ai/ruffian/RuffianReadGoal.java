package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class RuffianReadGoal extends Goal {
    private final RuffianEntity ruffian;
    private int readingTicks = 0;
    private BlockPos targetBookshelf = null;
    private boolean upgraded = false;

    public RuffianReadGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.ruffian.isCrouchingAnxious() || this.ruffian.isPlaying()) {
            return false;
        }

        ItemStack mainHandItem = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.is(Items.ENCHANTED_BOOK)) {
            return true;
        }

        if (!this.ruffian.canRead()) return false;
        float analytical = this.ruffian.getPersonalityEngine().getTrait("analytical");
        if (analytical < 0.8F) return false;

        this.targetBookshelf = findNearbyBookshelf();
        return this.targetBookshelf != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ruffian.isReading() && this.readingTicks < 200;
    }

    @Override
    public void start() {
        this.readingTicks = 0;
        this.upgraded = false;

        ItemStack mainHand = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.is(Items.ENCHANTED_BOOK) && this.targetBookshelf != null) {
            this.ruffian.getNavigation().moveTo(
                    this.targetBookshelf.getX() + 0.5D,
                    this.targetBookshelf.getY(),
                    this.targetBookshelf.getZ() + 0.5D,
                    1.1D
            );
        }
    }

    @Override
    public void tick() {
        ItemStack mainHand = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHand.is(Items.ENCHANTED_BOOK)) {
            if (!this.ruffian.isReading()) {
                this.ruffian.setReading(true);
            }
            this.ruffian.getNavigation().stop();
            this.ruffian.getLookControl().setLookAt(
                    this.ruffian.getX() + this.ruffian.getLookAngle().x * 2,
                    this.ruffian.getY(),
                    this.ruffian.getZ() + this.ruffian.getLookAngle().z * 2,
                    30.0F, 30.0F
            );

            this.readingTicks++;
            // Upgrade book power halfway through their session
            if (this.readingTicks == 100 && !this.upgraded) {
                upgradeBookPower(mainHand);
                this.upgraded = true;
            }
            return;
        }

        if (this.targetBookshelf != null) {
            double distanceSq = this.ruffian.distanceToSqr(
                    this.targetBookshelf.getX() + 0.5D,
                    this.targetBookshelf.getY(),
                    this.targetBookshelf.getZ() + 0.5D
            );

            if (distanceSq <= 4.0D) {
                this.ruffian.getNavigation().stop();
                this.ruffian.getLookControl().setLookAt(
                        this.targetBookshelf.getX() + 0.5D,
                        this.targetBookshelf.getY(),
                        this.targetBookshelf.getZ() + 0.5D,
                        30.0F, 30.0F
                );

                if (!this.ruffian.isReading()) {
                    this.ruffian.setReading(true);
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENCHANTED_BOOK));
                }

                this.readingTicks++;
                if (this.readingTicks == 100 && !this.upgraded) {
                    upgradeBookPower(this.ruffian.getItemInHand(InteractionHand.MAIN_HAND));
                    this.upgraded = true;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.ruffian.setReading(false);

        // When they slam the book shut in anger, drop the book item on the ground for the player!
        ItemStack heldBook = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (!heldBook.isEmpty() && this.ruffian.level() instanceof ServerLevel serverLevel) {
            ItemEntity droppedBook = new ItemEntity(
                    serverLevel,
                    this.ruffian.getX(),
                    this.ruffian.getY() + 0.5D,
                    this.ruffian.getZ(),
                    heldBook.copy()
            );
            droppedBook.setDeltaMovement(
                    (serverLevel.random.nextDouble() - 0.5D) * 0.3D,
                    0.2D,
                    (serverLevel.random.nextDouble() - 0.5D) * 0.3D
            );
            serverLevel.addFreshEntity(droppedBook);
        }

        this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.ruffian.applyReadCooldown(600);

        // "NO I READ ENOUGH!" angry particle burst
        if (this.ruffian.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5D) * 0.8D;
                double offsetY = serverLevel.random.nextDouble() * 1.5D;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5D) * 0.8D;
                serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        this.ruffian.getX() + offsetX,
                        this.ruffian.getY() + offsetY,
                        this.ruffian.getZ() + offsetZ,
                        1, 0.0D, 0.0D, 0.0D, 0.02D
                );
            }
        }
        this.readingTicks = 0;
        this.targetBookshelf = null;
        this.upgraded = false;
    }

    private BlockPos findNearbyBookshelf() {
        BlockPos currentPos = this.ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(currentPos.getX() + x, currentPos.getY() + y, currentPos.getZ() + z);
                    BlockState state = this.ruffian.level().getBlockState(mutable);
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
                int maxLevel = holder.value().getMaxLevel();
                mutableEnchantments.set(holder, currentLevel + 1);
            }
        });
    }
}