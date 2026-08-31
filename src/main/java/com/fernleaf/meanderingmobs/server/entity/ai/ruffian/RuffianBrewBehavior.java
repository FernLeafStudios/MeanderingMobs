package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianBrewBehavior extends RuffianStationBehavior {

    private boolean carryingBlazePowder = false;
    private boolean carryingWaterBottle = false;

    public RuffianBrewBehavior() {
        super(6.0D);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        if (!isRuffianAvailable(ruffian) || !locateStorage(level, ruffian, 6, 2)) {
            return false;
        }
        this.stationPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), Blocks.BREWING_STAND, 6, 2);
        if (this.stationPos == null) {
            return false;
        }

        return hasBrewingWork(ruffian, this.chestPos, this.stationPos);
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.carryingBlazePowder = false;
        this.carryingWaterBottle = false;
        super.start(level, ruffian, gameTime);
    }

    @Override
    protected void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            if (grabBrewingMaterialsFromChest(level, ruffian, this.chestPos) || checkCompletedPotions(ruffian, this.stationPos)) {
                advanceStep(ruffian);
            } else {
                stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            BrewingStandBlockEntity stand = getBrewingStand(ruffian, this.stationPos);

            if (extractCompletedPotion(ruffian, this.stationPos)) {
                this.currentStep = 2;
                navigateToStepTarget(ruffian);
                return;
            }

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

            if (!getActiveItem(ruffian).isEmpty()) {
                insertIntoBrewingStand(ruffian, this.stationPos);
                advanceStep(ruffian);
            }
        }
    }

    @Override
    protected boolean shouldRepeatFetchCycle(RuffianEntity ruffian) {
        return hasBrewingWork(ruffian, this.chestPos, this.stationPos);
    }

    private boolean hasBrewingWork(RuffianEntity ruffian, BlockPos cPos, BlockPos sPos) {
        if (cPos == null || sPos == null) {
            return false;
        }

        BrewingStandBlockEntity stand = getBrewingStand(ruffian, sPos);
        if (stand == null || stand.brewTime > 0) {
            return false;
        }

        if (checkCompletedPotions(ruffian, sPos)) {
            return true;
        }

        BlockEntity be = ruffian.level().getBlockEntity(cPos);
        if (be instanceof Container container) {
            boolean needsFuel = stand.getItem(4).isEmpty() && WorkstationRecipeUtil.findSlotMatching(container, stack -> stack.is(Items.BLAZE_POWDER)) != -1;
            boolean needsBottle = (stand.getItem(0).isEmpty() || stand.getItem(1).isEmpty() || stand.getItem(2).isEmpty()) &&
                    WorkstationRecipeUtil.findSlotMatching(container, stack -> stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) != -1;
            boolean needsIngredient = stand.getItem(3).isEmpty() && WorkstationRecipeUtil.findSlotMatching(container, stack -> isValidIngredientForStand((ServerLevel) ruffian.level(), stand, stack)) != -1;

            return needsFuel || needsBottle || needsIngredient;
        }

        return false;
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.carryingBlazePowder = false;
        this.carryingWaterBottle = false;
        super.stop(level, ruffian, gameTime);
    }

    private boolean grabBrewingMaterialsFromChest(ServerLevel level, RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, this.stationPos);
        if (stand == null || stand.brewTime > 0) {
            return false;
        }

        if (stand.getItem(4).isEmpty()) {
            ItemStack blazePowder = WorkstationRecipeUtil.tryExtractFromContainer(level, pos, stack -> stack.is(Items.BLAZE_POWDER), 8);
            if (!blazePowder.isEmpty()) {
                setActiveItem(ruffian, blazePowder);
                this.carryingBlazePowder = true;
                return true;
            }
        }

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

    private void insertIntoBrewingStand(RuffianEntity ruffian, BlockPos pos) {
        BrewingStandBlockEntity stand = getBrewingStand(ruffian, pos);
        ItemStack held = getActiveItem(ruffian);

        if (stand != null && !held.isEmpty()) {
            if (this.carryingBlazePowder) {
                insertOrMerge(ruffian, stand, 4, held);
                return;
            }

            if (this.carryingWaterBottle) {
                for (int i = 0; i < 3; i++) {
                    if (stand.getItem(i).isEmpty()) {
                        stand.setItem(i, held.copy());
                        setActiveItem(ruffian, ItemStack.EMPTY);
                        stand.setChanged();
                        return;
                    }
                }
            }

            if (stand.canPlaceItem(3, held)) {
                insertOrMerge(ruffian, stand, 3, held);
            }
        }
    }

    private void insertOrMerge(RuffianEntity ruffian, BrewingStandBlockEntity stand, int slot, ItemStack held) {
        ItemStack slotStack = stand.getItem(slot);
        if (slotStack.isEmpty()) {
            stand.setItem(slot, held.copy());
            held.setCount(0);
            setActiveItem(ruffian, held.isEmpty() ? ItemStack.EMPTY : held);
            stand.setChanged();
        } else if (ItemStack.isSameItemSameComponents(slotStack, held) && slotStack.getCount() + held.getCount() <= slotStack.getMaxStackSize()) {
            slotStack.grow(held.getCount());
            held.setCount(0);
            setActiveItem(ruffian, ItemStack.EMPTY);
            stand.setChanged();
        }
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

    private boolean isValidIngredientForStand(ServerLevel level, BrewingStandBlockEntity stand, ItemStack ingredient) {
        if (ingredient.isEmpty() || !stand.canPlaceItem(3, ingredient)) {
            return false;
        }

        boolean hasBottles = !stand.getItem(0).isEmpty() || !stand.getItem(1).isEmpty() || !stand.getItem(2).isEmpty();
        if (!hasBottles) {
            return false;
        }

        return level.potionBrewing().isIngredient(ingredient);
    }
}