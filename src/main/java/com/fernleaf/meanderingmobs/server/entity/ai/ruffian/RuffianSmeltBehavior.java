package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.WorkstationRecipeUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianSmeltBehavior extends Behavior<RuffianEntity> {

    public static final TagKey<Block> RUFFIAN_STORAGE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_storage")
    );
    public static final TagKey<Block> RUFFIAN_WORKSTATION = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_workstation")
    );

    private int currentStep = 0; // 0: Chest Take, 1: Station Insert/Collect, 2: Chest Deposit Output
    private BlockPos chestPos;
    private BlockPos stationPos;
    private boolean carryingFuel = false;

    public RuffianSmeltBehavior() {
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
        this.stationPos = findWorkstationBlock(ruffian);

        return this.chestPos != null && this.stationPos != null;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        return this.currentStep < 3 && checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void start(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.currentStep = 0;
        ruffian.setWorking(true);
        navigateToStepTarget(ruffian);
    }

    @SuppressWarnings("collapsed")
    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.stationPos;
        if (target == null) return;

        double distSq = ruffian.distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);

        // Step 0: Grab raw ingredients OR fuel from chest
        if (this.currentStep == 0 && distSq <= 4.0D) {
            if (grabFromChest(ruffian, this.chestPos)) {
                advanceStep(ruffian);
            } else {
                if (checkFurnaceOutput(ruffian, this.stationPos)) {
                    advanceStep(ruffian);
                } else {
                    stop(level, ruffian, gameTime);
                }
            }
            return;
        }

        // Step 1: At Workstation - Extract output first, or insert fuel/input
        if (this.currentStep == 1 && distSq <= 4.0D) {
            if (extractResultFromFurnace(ruffian, this.stationPos)) {
                this.currentStep = 2;
                navigateToStepTarget(ruffian);
                return;
            }

            if (!getActiveItem(ruffian).isEmpty()) {
                if (insertIntoFurnace(ruffian, this.stationPos)) {
                    stop(level, ruffian, gameTime);
                } else {
                    stop(level, ruffian, gameTime);
                }
            }
            return;
        }

        // Step 2: Deposit cooked output back into the chest
        if (this.currentStep == 2 && distSq <= 4.0D) {
            if (depositItemToChest(ruffian, this.chestPos)) {
                stop(level, ruffian, gameTime);
            } else {
                stop(level, ruffian, gameTime);
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
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.stationPos;
        if (target != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1.0F, 1));
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        ruffian.setWorking(false);
        this.chestPos = null;
        this.stationPos = null;
        this.currentStep = 0;
        this.carryingFuel = false;
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

    private BlockPos findWorkstationBlock(RuffianEntity ruffian) {
        BlockPos origin = ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (ruffian.level().getBlockState(mutable).is(RUFFIAN_WORKSTATION)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private boolean grabFromChest(RuffianEntity ruffian, BlockPos pos) {
        BlockEntity be = ruffian.level().getBlockEntity(pos);
        if (be instanceof Container container) {
            AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, this.stationPos);
            if (furnace != null && furnace.getItem(1).isEmpty()) {
                int fuelSlot = findFuelSlot(container);
                if (fuelSlot != -1) {
                    ItemStack stack = container.getItem(fuelSlot);
                    ItemStack taken = stack.split(Math.min(stack.getCount(), 8));
                    setActiveItem(ruffian, taken);
                    this.carryingFuel = true;
                    container.setChanged();
                    return true;
                }
            }

            int slot = WorkstationRecipeUtil.findProcessableSlot(ruffian.level(), container);
            if (slot != -1) {
                ItemStack stack = container.getItem(slot);
                ItemStack taken = stack.split(Math.min(stack.getCount(), 8));
                setActiveItem(ruffian, taken);
                this.carryingFuel = false;
                container.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean checkFurnaceOutput(RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, pos);
        return furnace != null && !furnace.getItem(2).isEmpty();
    }

    private boolean insertIntoFurnace(RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, pos);
        ItemStack held = getActiveItem(ruffian);

        if (furnace != null && !held.isEmpty()) {
            int slot = this.carryingFuel ? 1 : 0;
            ItemStack furnaceSlot = furnace.getItem(slot);

            if (furnaceSlot.isEmpty()) {
                furnace.setItem(slot, held.copy());
                setActiveItem(ruffian, ItemStack.EMPTY);
                furnace.setChanged();
                return true;
            } else if (ItemStack.isSameItemSameComponents(furnaceSlot, held) && furnaceSlot.getCount() + held.getCount() <= furnaceSlot.getMaxStackSize()) {
                furnaceSlot.grow(held.getCount());
                setActiveItem(ruffian, ItemStack.EMPTY);
                furnace.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean extractResultFromFurnace(RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, pos);
        if (furnace != null) {
            ItemStack resultStack = furnace.getItem(2);
            if (!resultStack.isEmpty()) {
                setActiveItem(ruffian, resultStack.copy());
                furnace.setItem(2, ItemStack.EMPTY);
                furnace.setChanged();
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

    private int findFuelSlot(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (AbstractFurnaceBlockEntity.isFuel(stack)) {
                return i;
            }
        }
        return -1;
    }

    private AbstractFurnaceBlockEntity getFurnace(RuffianEntity ruffian, BlockPos pos) {
        if (pos != null && ruffian.level().getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) {
            return furnace;
        }
        return null;
    }
}