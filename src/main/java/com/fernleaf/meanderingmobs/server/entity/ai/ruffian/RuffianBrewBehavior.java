package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.server.entity.ai.util.WorkstationRecipeUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianBrewBehavior extends Behavior<RuffianEntity> {

    public static final TagKey<Block> RUFFIAN_STORAGE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_storage")
    );

    private int currentStep = 0; // 0: Fetch from Chest, 1: Process at Brewing Stand, 2: Deposit to Chest
    private BlockPos chestPos;
    private BlockPos standPos;
    private boolean carryingBlazePowder = false;
    private boolean carryingWaterBottle = false;

    public RuffianBrewBehavior() {
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
        if (!isValidState || ruffian.isNapping() || ruffian.isCrouchingAnxious()) {
            return false;
        }

        this.chestPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), RUFFIAN_STORAGE, 6, 2);
        this.standPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), Blocks.BREWING_STAND, 6, 2);

        return this.chestPos != null && this.standPos != null;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.currentStep == 2 && !getActiveItem(ruffian).isEmpty()) {
            return true;
        }
        return this.currentStep < 3 && checkExtraStartConditions(level, ruffian);
    }

    @Override
    protected void start(@NotNull ServerLevel level, RuffianEntity ruffian, long gameTime) {
        this.currentStep = 0;
        this.carryingBlazePowder = false;
        this.carryingWaterBottle = false;
        ruffian.setWorking(true);
        navigateToStepTarget(ruffian);
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.standPos;
        if (target == null) return;

        double distSq = ruffian.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);

        // Step 0: Fetch fuel, bottles, or valid ingredients from chest
        if (this.currentStep == 0) {
            if (distSq <= 6.0D) {
                if (grabBrewingMaterialsFromChest(level, ruffian, this.chestPos)) {
                    advanceStep(ruffian);
                } else if (checkCompletedPotions(ruffian, this.standPos)) {
                    advanceStep(ruffian);
                } else {
                    stop(level, ruffian, gameTime);
                }
            }
            return;
        }

        // Step 1: Interacting with Brewing Stand (Inserting or Ejecting)
        if (this.currentStep == 1) {
            if (distSq <= 6.0D) {
                BrewingStandBlockEntity stand = getBrewingStand(ruffian, this.standPos);

                // If stand finished brewing, extract finished potions
                if (extractCompletedPotion(ruffian, this.standPos)) {
                    this.currentStep = 2;
                    navigateToStepTarget(ruffian);
                    return;
                }

                // If an invalid ingredient is stuck in Slot 3, eject it back to storage
                if (stand != null && stand.brewTime <= 0) {
                    ItemStack currentIngredient = stand.getItem(3);
                    if (!currentIngredient.isEmpty() && !isValidIngredientForStand(level, stand, currentIngredient)) {
                        setActiveItem(ruffian, currentIngredient.copy());
                        stand.setItem(3, ItemStack.EMPTY);
                        stand.setChanged();
                        this.currentStep = 2;
                        navigateToStepTarget(ruffian);
                        return;
                    }
                }

                // Insert held item into stand
                if (!getActiveItem(ruffian).isEmpty()) {
                    insertIntoBrewingStand(ruffian, this.standPos);
                    stop(level, ruffian, gameTime);
                }
            }
            return;
        }

        // Step 2: Deposit items into storage chest
        if (this.currentStep == 2) {
            if (distSq <= 6.0D) {
                ItemStack held = getActiveItem(ruffian);
                if (WorkstationRecipeUtil.tryDepositToContainer(level, this.chestPos, held)) {
                    setActiveItem(ruffian, ItemStack.EMPTY);
                    stop(level, ruffian, gameTime);
                } else {
                    ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.chestPos));
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
        BlockPos target = (this.currentStep == 0 || this.currentStep == 2) ? this.chestPos : this.standPos;
        if (target != null) {
            ruffian.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1.0F, 1));
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        ItemStack held = getActiveItem(ruffian);
        if (!held.isEmpty()) {
            if (this.chestPos == null || !WorkstationRecipeUtil.tryDepositToContainer(level, this.chestPos, held)) {
                ruffian.spawnAtLocation(held.copy());
                setActiveItem(ruffian, ItemStack.EMPTY);
            } else {
                setActiveItem(ruffian, ItemStack.EMPTY);
            }
        }

        ruffian.setWorking(false);
        this.chestPos = null;
        this.standPos = null;
        this.currentStep = 0;
        this.carryingBlazePowder = false;
        this.carryingWaterBottle = false;
    }

    private boolean grabBrewingMaterialsFromChest(ServerLevel level, RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, this.standPos);
        if (stand == null || stand.brewTime > 0) return false;

        // 1. Fuel Check (Slot 4)
        if (stand.getItem(4).isEmpty()) {
            ItemStack blazePowder = WorkstationRecipeUtil.tryExtractFromContainer(level, pos, stack -> stack.is(Items.BLAZE_POWDER), 8);
            if (!blazePowder.isEmpty()) {
                setActiveItem(ruffian, blazePowder);
                this.carryingBlazePowder = true;
                return true;
            }
        }

        // 2. Water Bottle Check (Slots 0, 1, 2)
        boolean hasEmptyBottleSlot = stand.getItem(0).isEmpty() || stand.getItem(1).isEmpty() || stand.getItem(2).isEmpty();
        if (hasEmptyBottleSlot) {
            ItemStack waterBottle = WorkstationRecipeUtil.tryExtractFromContainer(level, pos, stack -> {
                if (stack.is(Items.POTION)) {
                    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    return contents.is(Potions.WATER);
                }
                return false;
            }, 1);

            if (!waterBottle.isEmpty()) {
                setActiveItem(ruffian, waterBottle);
                this.carryingWaterBottle = true;
                return true;
            }
        }

        // 3. Ingredient Check (Slot 3) - Only grab if it forms a valid recipe with current bottles!
        if (stand.getItem(3).isEmpty()) {
            ItemStack validIngredient = WorkstationRecipeUtil.tryExtractFromContainer(level, pos,
                    stack -> isValidIngredientForStand(level, stand, stack), 1
            );

            if (!validIngredient.isEmpty()) {
                setActiveItem(ruffian, validIngredient);
                return true;
            }
        }

        return false;
    }

    private boolean checkCompletedPotions(RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, pos);
        if (stand != null && stand.brewTime <= 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack bottle = stand.getItem(i);
                if (!bottle.isEmpty() && bottle.is(Items.POTION)) {
                    PotionContents contents = bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (!contents.is(Potions.WATER) && !contents.is(Potions.AWKWARD)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean insertIntoBrewingStand(RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, pos);
        ItemStack held = getActiveItem(ruffian);

        if (stand != null && !held.isEmpty()) {
            if (this.carryingBlazePowder) {
                return insertOrMerge(stand, 4, held);
            }

            if (this.carryingWaterBottle) {
                for (int i = 0; i < 3; i++) {
                    if (stand.getItem(i).isEmpty()) {
                        stand.setItem(i, held.copy());
                        setActiveItem(ruffian, ItemStack.EMPTY);
                        stand.setChanged();
                        return true;
                    }
                }
            }

            if (stand.canPlaceItem(3, held)) {
                return insertOrMerge(stand, 3, held);
            }
        }
        return false;
    }

    private boolean insertOrMerge(BrewingStandBlockEntity stand, int slot, ItemStack held) {
        ItemStack slotStack = stand.getItem(slot);
        if (slotStack.isEmpty()) {
            stand.setItem(slot, held.copy());
            held.setCount(0);
            stand.setChanged();
            return true;
        } else if (ItemStack.isSameItemSameComponents(slotStack, held) && slotStack.getCount() + held.getCount() <= slotStack.getMaxStackSize()) {
            slotStack.grow(held.getCount());
            held.setCount(0);
            stand.setChanged();
            return true;
        }
        return false;
    }

    private boolean extractCompletedPotion(RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, pos);
        if (stand != null && stand.brewTime <= 0) {
            for (int i = 0; i < 3; i++) {
                ItemStack bottle = stand.getItem(i);
                if (!bottle.isEmpty() && bottle.is(Items.POTION)) {
                    PotionContents contents = bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (!contents.is(Potions.WATER) && !contents.is(Potions.AWKWARD)) {
                        setActiveItem(ruffian, bottle.copy());
                        stand.setItem(i, ItemStack.EMPTY);
                        stand.setChanged();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BrewingStandBlockEntity getBrewingStand(RuffianEntity ruffian, BlockPos pos) {
        if (pos != null && ruffian.level().getBlockEntity(pos) instanceof BrewingStandBlockEntity stand) {
            return stand;
        }
        return null;
    }

    /**
     * Checks if the ingredient can validly brew with the bottles currently sitting in the stand.
     */
    private boolean isValidIngredientForStand(ServerLevel level, BrewingStandBlockEntity stand, ItemStack ingredient) {
        if (ingredient.isEmpty() || !stand.canPlaceItem(3, ingredient)) {
            return false;
        }

        boolean hasBottles = !stand.getItem(0).isEmpty() || !stand.getItem(1).isEmpty() || !stand.getItem(2).isEmpty();
        if (!hasBottles) return false;

        return level.potionBrewing().isIngredient(ingredient);
    }
}