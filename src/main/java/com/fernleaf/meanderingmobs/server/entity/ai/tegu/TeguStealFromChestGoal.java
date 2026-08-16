package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TeguStealFromChestGoal extends Goal {

    public static final TagKey<Block> TEGU_STEALS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "tegu_steals"));

    private final TeguEntity tegu;
    private BlockPos targetChestPos;
    private int cooldown = 0;

    public TeguStealFromChestGoal(TeguEntity tegu) {
        this.tegu = tegu;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        // Tegu only steals when wandering (State 0) and its mouth is empty
        if (!this.tegu.getMouthItem().isEmpty() || this.tegu.getAiState() != 0) {
            return false;
        }

        this.targetChestPos = findNearestChest();
        return this.targetChestPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetChestPos != null
                && this.tegu.getMouthItem().isEmpty()
                && this.tegu.getAiState() == 0;
    }

    @Override
    public void start() {
        if (this.targetChestPos != null) {
            this.tegu.getNavigation().moveTo(
                    this.targetChestPos.getX() + 0.5D,
                    this.targetChestPos.getY(),
                    this.targetChestPos.getZ() + 0.5D,
                    1.2D
            );
        }
    }

    @Override
    public void stop() {
        this.targetChestPos = null;
        this.cooldown = 100 + this.tegu.getRandom().nextInt(100); // 5-10 second re-scan delay
    }

    @Override
    public void tick() {
        if (this.targetChestPos == null) return;

        this.tegu.getLookControl().setLookAt(
                this.targetChestPos.getX() + 0.5D,
                this.targetChestPos.getY() + 0.5D,
                this.targetChestPos.getZ() + 0.5D,
                30.0F,
                30.0F
        );

        double distSqr = this.tegu.distanceToSqr(Vec3.atCenterOf(this.targetChestPos));

        // Attempt steal when within 2.5 blocks of container center
        if (distSqr <= 6.25D) {
            stealFromContainer(this.targetChestPos);
            this.stop();
        } else if (this.tegu.getNavigation().isDone()) {
            // Re-path if navigation stalled before reaching block
            this.tegu.getNavigation().moveTo(
                    this.targetChestPos.getX() + 0.5D,
                    this.targetChestPos.getY(),
                    this.targetChestPos.getZ() + 0.5D,
                    1.2D
            );
        }
    }

    private void stealFromContainer(BlockPos pos) {
        BlockEntity blockEntity = this.tegu.level().getBlockEntity(pos);

        if (blockEntity instanceof Container container) {
            List<Integer> validSlots = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    validSlots.add(i);
                }
            }

            if (!validSlots.isEmpty()) {
                int randomSlot = validSlots.get(this.tegu.getRandom().nextInt(validSlots.size()));
                ItemStack stolenStack = container.removeItem(randomSlot, 1);

                if (!stolenStack.isEmpty()) {
                    this.tegu.setMouthItem(stolenStack);
                    container.setChanged();
                }
            }
        }
    }

    private BlockPos findNearestChest() {
        BlockPos teguPos = this.tegu.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(teguPos.getX() + x, teguPos.getY() + y, teguPos.getZ() + z);
                    if (this.tegu.level().getBlockState(mutable).is(TEGU_STEALS)) {
                        BlockEntity be = this.tegu.level().getBlockEntity(mutable);
                        if (be instanceof Container container && !container.isEmpty()) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}