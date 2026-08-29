package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianSmeltBehavior extends RuffianStationBehavior {

    public static final TagKey<Block> RUFFIAN_WORKSTATION = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "ruffian_workstation")
    );

    private boolean carryingFuel = false;

    public RuffianSmeltBehavior() {
        super(4.0D);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        if (!isRuffianAvailable(ruffian) || !locateStorage(level, ruffian, 6, 2)) {
            return false;
        }

        this.stationPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), RUFFIAN_WORKSTATION, 6, 2);
        return this.stationPos != null;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.carryingFuel = false;
        super.start(level, ruffian, gameTime);
    }

    @Override
    protected void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            if (grabFromChest(level, ruffian, this.chestPos)) {
                advanceStep(ruffian);
            } else if (checkFurnaceOutput(ruffian, this.stationPos)) {
                advanceStep(ruffian);
            } else {
                stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            if (extractResultFromFurnace(ruffian, this.stationPos)) {
                this.currentStep = 2;
                navigateToStepTarget(ruffian);
                return;
            }

            if (!getActiveItem(ruffian).isEmpty()) {
                insertIntoFurnace(ruffian, this.stationPos);
                stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.carryingFuel = false;
        super.stop(level, ruffian, gameTime);
    }

    private boolean grabFromChest(ServerLevel level, RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, this.stationPos);

        if (furnace != null && furnace.getItem(1).isEmpty()) {
            ItemStack fuel = WorkstationRecipeUtil.tryExtractFromContainer(level, pos, AbstractFurnaceBlockEntity::isFuel, 8);
            if (!fuel.isEmpty()) {
                setActiveItem(ruffian, fuel);
                this.carryingFuel = true;
                return true;
            }
        }

        ItemStack processable = WorkstationRecipeUtil.tryExtractFromContainer(level, pos, stack -> WorkstationRecipeUtil.isProcessable(level, stack), 8);
        if (!processable.isEmpty()) {
            setActiveItem(ruffian, processable);
            this.carryingFuel = false;
            return true;
        }

        return false;
    }

    private boolean checkFurnaceOutput(RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, pos);
        return furnace != null && !furnace.getItem(2).isEmpty();
    }

    private void insertIntoFurnace(RuffianEntity ruffian, BlockPos pos) {
        AbstractFurnaceBlockEntity furnace = getFurnace(ruffian, pos);
        ItemStack held = getActiveItem(ruffian);

        if (furnace != null && !held.isEmpty()) {
            int slot = this.carryingFuel ? 1 : 0;
            ItemStack furnaceSlot = furnace.getItem(slot);

            if (furnaceSlot.isEmpty()) {
                furnace.setItem(slot, held.copy());
                setActiveItem(ruffian, ItemStack.EMPTY);
                furnace.setChanged();
            } else if (ItemStack.isSameItemSameComponents(furnaceSlot, held) && furnaceSlot.getCount() + held.getCount() <= furnaceSlot.getMaxStackSize()) {
                furnaceSlot.grow(held.getCount());
                setActiveItem(ruffian, ItemStack.EMPTY);
                furnace.setChanged();
            }
        }
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

    private AbstractFurnaceBlockEntity getFurnace(RuffianEntity ruffian, BlockPos pos) {
        if (pos != null && ruffian.level().getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) {
            return furnace;
        }
        return null;
    }
}