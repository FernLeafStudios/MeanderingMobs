package com.fernleaf.meanderingmobs.server.entity.ai.ruffian;

import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.util.RuffianStationBehavior;
import com.fernleaf.meanderingmobs.util.BlockPosUtil;
import com.fernleaf.meanderingmobs.util.WorkstationRecipeUtil;
import com.fernleaf.meanderingmobs.server.entity.tameable.RuffianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RuffianRepairBehavior extends RuffianStationBehavior {

    private int repairCooldown = 0;

    public RuffianRepairBehavior() {
        super(6.0D);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian) {
        if (!isRuffianAvailable(ruffian) || !locateStorage(level, ruffian, 6, 2)) {
            return false;
        }

        this.stationPos = BlockPosUtil.findBlockInRadius(level, ruffian.blockPosition(), BlockTags.ANVIL, 6, 2);
        return this.stationPos != null;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.repairCooldown = 0;
        super.start(level, ruffian, gameTime);
    }

    @Override
    protected void tickFetchStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            if (grabDamagedToolFromChest(ruffian, this.chestPos)) {
                advanceStep(ruffian);
            } else {
                stop(level, ruffian, gameTime);
            }
        }
    }

    @Override
    protected void tickProcessStep(ServerLevel level, RuffianEntity ruffian, long gameTime, double distSq) {
        if (distSq <= this.interactionRadiusSq) {
            if (this.repairCooldown > 0) {
                this.repairCooldown--;
                return;
            }

            ItemStack held = getActiveItem(ruffian);

            if (held.isEmpty() || !held.isDamaged()) {
                advanceStep(ruffian);
                return;
            }

            if (tryConsumeMaterialAndRepair(level, ruffian, this.chestPos, this.stationPos)) {
                this.repairCooldown = 10;
            } else {
                advanceStep(ruffian);
            }
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull RuffianEntity ruffian, long gameTime) {
        this.repairCooldown = 0;
        super.stop(level, ruffian, gameTime);
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
}