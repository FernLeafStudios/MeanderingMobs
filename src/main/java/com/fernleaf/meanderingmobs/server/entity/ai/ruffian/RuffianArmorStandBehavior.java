package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuffianArmorStandBehavior extends RuffianStationBehavior {

    private ArmorStand targetStand;
    private int workTicks = 0;

    public RuffianArmorStandBehavior() {
        super(4.0D);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        if (!getActiveItem(ruffian).isEmpty() || !isRuffianAvailable(ruffian) || !locateStorage(level, ruffian, 8, 3)) {
            return false;
        }

        List<ArmorStand> stands = level.getEntitiesOfClass(ArmorStand.class, ruffian.getBoundingBox().inflate(8.0D));
        BlockEntity be = level.getBlockEntity(this.chestPos);

        if (be instanceof Container container) {
            for (ArmorStand stand : stands) {
                if (WorkstationRecipeUtil.findArmorForStand(container, stand) != -1) {
                    this.targetStand = stand;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        if (this.targetStand == null || !this.targetStand.isAlive()) {
            return false;
        }
        if (!getActiveItem(ruffian).isEmpty()) {
            return true;
        }
        return super.canStillUse(level, ruffian, gameTime);
    }

    @Override
    protected void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (this.targetStand == null || !this.targetStand.isAlive()) {
            stop(level, ruffian, gameTime);
            return;
        }

        if (distSq <= this.interactionRadiusSq) {
            BlockEntity be = level.getBlockEntity(this.chestPos);
            if (be instanceof Container container) {
                int slot = WorkstationRecipeUtil.findArmorForStand(container, this.targetStand);
                if (slot != -1) {
                    ItemStack armor = container.removeItem(slot, 1);
                    setActiveItem(ruffian, armor);
                    container.setChanged();

                    this.stationPos = this.targetStand.blockPosition();
                    advanceStep(ruffian);
                    return;
                }
            }
            stop(level, ruffian, gameTime);
        }
    }

    @Override
    protected void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (this.targetStand == null || !this.targetStand.isAlive() || getActiveItem(ruffian).isEmpty()) {
            stop(level, ruffian, gameTime);
            return;
        }

        this.stationPos = this.targetStand.blockPosition();
        double actualDistSq = ruffian.distanceToSqr(this.targetStand);

        ruffian.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.targetStand, true));

        if (actualDistSq <= this.interactionRadiusSq) {
            this.workTicks++;
            if (this.workTicks >= 20) {
                ItemStack heldArmor = getActiveItem(ruffian);
                EquipmentSlot slot = this.targetStand.getEquipmentSlotForItem(heldArmor);

                this.targetStand.setItemSlot(slot, heldArmor.copy());
                setActiveItem(ruffian, ItemStack.EMPTY);
                this.workTicks = 0;

                advanceStep(ruffian);
            }
        }
    }

    @Override
    protected boolean shouldRepeatFetchCycle(RuffianEntity ruffian) {
        if (this.targetStand == null || !this.targetStand.isAlive() || this.chestPos == null) {
            return false;
        }
        BlockEntity be = ruffian.level().getBlockEntity(this.chestPos);
        return be instanceof Container container && WorkstationRecipeUtil.findArmorForStand(container, this.targetStand) != -1;
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        super.stop(level, ruffian, gameTime);
        this.targetStand = null;
        this.workTicks = 0;
    }
}