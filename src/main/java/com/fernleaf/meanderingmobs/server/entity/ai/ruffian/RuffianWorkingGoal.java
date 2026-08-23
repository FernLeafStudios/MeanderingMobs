package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.WorkstationRecipeUtil;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class RuffianWorkingGoal extends Goal {

    public static final TagKey<Block> RUFFIAN_STORAGE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_storage")
    );
    public static final TagKey<Block> RUFFIAN_WORKSTATION = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_workstation")
    );

    private final RuffianEntity ruffian;
    private final double speedModifier;
    private BlockPos chestPos;
    private BlockPos stationPos;
    private int step = 0; // 0: Chest, 1: Workstation, 2: Work/Furnace, 3: Return Chest
    private int workTicks = 0;
    private int cooldown = 0;
    private int repathCooldown = 0;

    public RuffianWorkingGoal(RuffianEntity ruffian) {
        this.ruffian = ruffian;
        this.speedModifier = 1.0D;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean canInteract() {
        int aiState = this.ruffian.getAiState();
        // Allow working if untamed (0) or explicitly set to WORK state (3)
        boolean isValidState = !this.ruffian.isTamed() || aiState == 3;

        return isValidState
                && !this.ruffian.isNapping()
                && !this.ruffian.isReading()
                && !this.ruffian.isCrouchingAnxious();
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        if (!canInteract()) return false;

        this.chestPos = findStorageBlock();
        this.stationPos = findWorkstationBlock();

        return this.chestPos != null && this.stationPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canInteract() && this.step <= 3 && this.chestPos != null && this.stationPos != null;
    }

    @Override
    public void start() {
        this.step = this.ruffian.getMainHandItem().isEmpty() ? 0 : 1;
        this.ruffian.setWorking(true);
        this.repathCooldown = 0;
    }

    @Override
    public void tick() {
        if (this.repathCooldown > 0) {
            this.repathCooldown--;
        }

        // STEP 0: Move to Chest & Grab Item
        if (this.step == 0) {
            if (!this.ruffian.getMainHandItem().isEmpty()) {
                this.step = 1;
                return;
            }

            if (isNear(this.chestPos)) {
                if (grabIngredientFromChest(this.chestPos)) {
                    this.step = 1;
                } else {
                    stop();
                }
            } else {
                moveTo(this.chestPos);
            }
        }

        // STEP 1: Move to Furnace & Deposit Item
        else if (this.step == 1) {
            if (isNear(this.stationPos)) {
                if (insertItemIntoFurnace(this.stationPos)) {
                    this.step = 2;
                    this.workTicks = 80;
                } else {
                    stop();
                }
            } else {
                moveTo(this.stationPos);
            }
        }

        // STEP 2: Process Recipe & Wait
        else if (this.step == 2) {
            this.ruffian.getLookControl().setLookAt(
                    this.stationPos.getX() + 0.5D,
                    this.stationPos.getY() + 0.5D,
                    this.stationPos.getZ() + 0.5D,
                    30.0F, 30.0F
            );

            this.workTicks--;
            if (this.workTicks <= 0) {
                extractResultFromFurnace(this.stationPos);
                this.step = 3;
            }
        }

        // STEP 3: Return Result to Storage
        else if (this.step == 3) {
            if (isNear(this.chestPos)) {
                depositItemToChest(this.chestPos);
                this.cooldown = 100;
                stop();
            } else {
                moveTo(this.chestPos);
            }
        }
    }

    private void moveTo(BlockPos pos) {
        this.ruffian.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 30.0F, 30.0F);
        if (this.repathCooldown <= 0 || this.ruffian.getNavigation().isDone()) {
            this.ruffian.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, this.speedModifier);
            this.repathCooldown = 10;
        }
    }

    private boolean isNear(BlockPos pos) {
        // Increased reach check to 3 blocks squared (9.0D) to avoid pathfinding clipping issues
        return this.ruffian.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 9.0D;
    }

    private BlockPos findStorageBlock() {
        BlockPos origin = this.ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (this.ruffian.level().getBlockState(mutable).is(RUFFIAN_STORAGE)) {
                        BlockEntity be = this.ruffian.level().getBlockEntity(mutable);
                        if (be instanceof Container container) {
                            if (!this.ruffian.getMainHandItem().isEmpty() || WorkstationRecipeUtil.findProcessableSlot(this.ruffian.level(), container) != -1) {
                                return mutable.immutable();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findWorkstationBlock() {
        BlockPos origin = this.ruffian.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (this.ruffian.level().getBlockState(mutable).is(RUFFIAN_WORKSTATION)) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    private boolean grabIngredientFromChest(BlockPos pos) {
        BlockEntity be = this.ruffian.level().getBlockEntity(pos);
        if (be instanceof Container container) {
            int slot = WorkstationRecipeUtil.findProcessableSlot(this.ruffian.level(), container);
            if (slot != -1) {
                ItemStack stack = container.getItem(slot);
                ItemStack singleUnit = stack.split(1);
                this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, singleUnit);
                container.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean insertItemIntoFurnace(BlockPos pos) {
        BlockEntity be = this.ruffian.level().getBlockEntity(pos);
        ItemStack held = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);

        if (held.isEmpty()) return false;

        if (be instanceof Container container) {
            if (!WorkstationRecipeUtil.isProcessable(this.ruffian.level(), held)) return false;

            ItemStack slot0 = container.getItem(0);
            if (slot0.isEmpty()) {
                container.setItem(0, held.copy());
                this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                container.setChanged();
                setFurnaceLit(pos, true);
                return true;
            } else if (ItemStack.isSameItemSameComponents(slot0, held) && slot0.getCount() < slot0.getMaxStackSize()) {
                slot0.grow(1);
                this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                container.setChanged();
                setFurnaceLit(pos, true);
                return true;
            }
        }
        return false;
    }

    private void extractResultFromFurnace(BlockPos pos) {
        BlockEntity be = this.ruffian.level().getBlockEntity(pos);
        if (be instanceof Container container) {
            ItemStack input = container.getItem(0);

            if (!input.isEmpty()) {
                ItemStack result = WorkstationRecipeUtil.processItem(this.ruffian.level(), input);
                container.setItem(0, ItemStack.EMPTY);

                if (!result.isEmpty()) {
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, result);
                } else {
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, input);
                }
            } else {
                ItemStack outputSlot = container.getItem(2);
                if (!outputSlot.isEmpty()) {
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, outputSlot.copy());
                    container.setItem(2, ItemStack.EMPTY);
                }
            }
            container.setChanged();
        }
        setFurnaceLit(pos, false);
    }

    private void setFurnaceLit(BlockPos pos, boolean lit) {
        BlockState state = this.ruffian.level().getBlockState(pos);
        if (state.hasProperty(FurnaceBlock.LIT)) {
            this.ruffian.level().setBlock(pos, state.setValue(FurnaceBlock.LIT, lit), 3);
        }
    }

    private void depositItemToChest(BlockPos pos) {
        BlockEntity be = this.ruffian.level().getBlockEntity(pos);
        ItemStack held = this.ruffian.getItemInHand(InteractionHand.MAIN_HAND);
        if (be instanceof Container container && !held.isEmpty()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotStack = container.getItem(i);
                if (slotStack.isEmpty()) {
                    container.setItem(i, held.copy());
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    container.setChanged();
                    return;
                } else if (ItemStack.isSameItemSameComponents(slotStack, held) && slotStack.getCount() < slotStack.getMaxStackSize()) {
                    slotStack.grow(1);
                    this.ruffian.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    container.setChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.ruffian.setWorking(false);
        this.ruffian.getNavigation().stop();
        this.step = 0;
        this.chestPos = null;
        this.stationPos = null;
    }
}