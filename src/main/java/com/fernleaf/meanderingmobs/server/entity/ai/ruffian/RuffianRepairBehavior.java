package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.WorkstationRecipeUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianRepairBehavior extends Behavior<RuffianEntity> {

    public static final TagKey<Block> RUFFIAN_STORAGE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_storage")
    );

    private int currentStep = 0; // 0: Fetch Damaged Tool, 1: Anvil Repair Loop, 2: Deposit Tool
    private BlockPos chestPos;
    private BlockPos anvilPos;
    private int repairCooldown = 0;

    public RuffianRepairBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    private void setActiveItem(RuffianEntity ruffian, ItemStack stack) {
        ruffian.getInventory().setItem(0, stack);
        ruffian.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    private ItemStack getActiveItem(RuffianEntity ruffian) {
        return ruffian.getInventory().getItem(0);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, RuffianEntity ruffian) {
        boolean isValidState = !ruffian.isTamed() || ruffian.getAiState() == 3;
        if (!isValidState || ruffian.isNapping() || ruffian.isReading() || ruffian.isCrouchingAnxious()) {
            return false;
        }

        this.chestPos = findStorageBlock(ruffian);
        this.anvilPos = findAnvilBlock(ruffian);

        return this.chestPos != null && this.anvilPos != null;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        // Force the behavior to stay active as long as the Ruffian is holding an item during step 2!
        if (this.currentStep == 2 && !getActiveItem(ruffian).isEmpty()) {
            return true;
        }
        return this.currentStep < 3 && checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void start(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.currentStep = 0;
        this.repairCooldown = 0;
        ruffian.setWorking(true);
        navigateToStepTarget(ruffian);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.anvilPos;
        if (target == null) return;

        double distSq = ruffian.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);

        // Step 0: Grab damaged tool from chest
        if (this.currentStep == 0) {
            if (distSq <= 6.0D) {
                if (grabDamagedToolFromChest(ruffian, this.chestPos)) {
                    advanceStep(ruffian);
                } else {
                    stop(level, ruffian, gameTime);
                }
            }
            return;
        }

        // Step 1: Repair loop at the Anvil
        if (this.currentStep == 1) {
            if (distSq <= 6.0D) {
                if (this.repairCooldown > 0) {
                    this.repairCooldown--;
                    return;
                }

                ItemStack held = getActiveItem(ruffian);

                if (held.isEmpty() || !held.isDamaged()) {
                    advanceStep(ruffian);
                    return;
                }

                if (tryConsumeMaterialAndRepair(level, ruffian, this.chestPos, this.anvilPos)) {
                    this.repairCooldown = 10;
                } else {
                    advanceStep(ruffian); // No materials left; deposit whatever progress was made
                }
            }
            return;
        }

        // Step 2: Deposit tool back into chest
        if (this.currentStep == 2) {
            if (distSq <= 6.0D) {
                // Persistent retry: keep trying every tick until chest is free!
                if (depositItemToChest(ruffian, this.chestPos)) {
                    stop(level, ruffian, gameTime);
                } else {
                    // Refresh look target to face chest while waiting for player to close it
                    ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new net.minecraft.world.entity.ai.behavior.BlockPosTracker(this.chestPos));
                }
            } else {
                navigateToStepTarget(ruffian);
            }
        }
    }

    private void advanceStep(RuffianEntity ruffian) {
        this.currentStep++;
        if (this.currentStep < 3) {
            navigateToStepTarget(ruffian);
        }
    }

    private void navigateToStepTarget(RuffianEntity ruffian) {
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.anvilPos;
        if (target != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1.0F, 1));
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        // Safety check: If behavior stops while still holding an active item, attempt deposit or drop item
        ItemStack held = getActiveItem(ruffian);
        if (!held.isEmpty()) {
            if (this.chestPos == null || !depositItemToChest(ruffian, this.chestPos)) {
                // If chest is still occupied or missing when interrupted, drop item at feet so tool isn't lost!
                ruffian.spawnAtLocation(held.copy());
                setActiveItem(ruffian, ItemStack.EMPTY);
            }
        }

        ruffian.setWorking(false);
        this.chestPos = null;
        this.anvilPos = null;
        this.currentStep = 0;
        this.repairCooldown = 0;
    }

    private BlockPos findStorageBlock(RuffianEntity ruffian) {
        BlockPos origin = ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (ruffian.level().getBlockState(mutable).is(RUFFIAN_STORAGE)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findAnvilBlock(RuffianEntity ruffian) {
        BlockPos origin = ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (ruffian.level().getBlockState(mutable).is(BlockTags.ANVIL)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private boolean grabDamagedToolFromChest(RuffianEntity ruffian, BlockPos pos) {
        BlockEntity be = ruffian.level().getBlockEntity(pos);
        if (be instanceof Container container) {
            int toolSlot = WorkstationRecipeUtil.findDamagedToolSlot(container);
            if (toolSlot != -1) {
                ItemStack toolStack = container.getItem(toolSlot);
                int matSlot = WorkstationRecipeUtil.findRepairMaterialSlot(container, toolStack);

                if (matSlot != -1) {
                    ItemStack extractedTool = container.removeItem(toolSlot, 1);
                    setActiveItem(ruffian, extractedTool);
                    container.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryConsumeMaterialAndRepair(ServerLevel level, RuffianEntity ruffian, BlockPos chestPos, BlockPos anvilPos) {
        BlockEntity be = level.getBlockEntity(chestPos);
        ItemStack held = getActiveItem(ruffian);

        if (be instanceof Container container && !held.isEmpty() && held.isDamaged()) {
            int matSlot = WorkstationRecipeUtil.findRepairMaterialSlot(container, held);
            if (matSlot != -1) {
                container.removeItem(matSlot, 1);
                container.setChanged();

                int restoreAmount = Math.max(1, held.getMaxDamage() / 4);
                held.setDamageValue(Math.max(0, held.getDamageValue() - restoreAmount));

                level.playSound(null, anvilPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.1F);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, anvilPos.getX() + 0.5D, anvilPos.getY() + 1.0D, anvilPos.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.0D);
                return true;
            }
        }
        return false;
    }

    private boolean depositItemToChest(RuffianEntity ruffian, BlockPos pos) {
        BlockEntity be = ruffian.level().getBlockEntity(pos);
        ItemStack held = getActiveItem(ruffian);

        if (be instanceof Container container && !held.isEmpty()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotStack = container.getItem(i);

                if (slotStack.isEmpty()) {
                    container.setItem(i, held.copy());
                    setActiveItem(ruffian, ItemStack.EMPTY);
                    container.setChanged();
                    return true;
                } else if (ItemStack.isSameItemSameComponents(slotStack, held) && slotStack.getCount() + held.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(held.getCount());
                    setActiveItem(ruffian, ItemStack.EMPTY);
                    container.setChanged();
                    return true;
                }
            }
        }
        return false;
    }
}