package com.fernleaf.meanderingmobs.server.entity.ai.okapi;

import com.fernleaf.meanderingmobs.server.entity.tameable.OkapiEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;

public class OkapiBrowseGoal extends AbstractBlockInteractionGoal<OkapiEntity> {

    private int browseTimer = 0;

    public OkapiBrowseGoal(OkapiEntity entity, double speedModifier) {
        super(entity, speedModifier, 2.2D);
    }

    @Override
    public boolean canUse() {
        if (this.entity.isVehicle() || (this.entity.isTamed())) {
            return false;
        }
        return super.canUse();
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos entityPos = this.entity.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -4; x <= 4; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -4; z <= 4; z++) {
                    mutablePos.set(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z);
                    if (this.entity.level().getBlockState(mutablePos).is(BlockTags.LEAVES)) {
                        return mutablePos.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        this.entity.getNavigation().stop();
        this.entity.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 30.0F, 30.0F);

        if (this.browseTimer++ > 40) { // Munch for 2 seconds
            this.setCooldown(150); // Cooldown before browsing again
            this.stop();
        }
    }

    @Override
    public void start() {
        this.browseTimer = 0;
        super.start();
    }

    @Override
    public void stop() {
        this.browseTimer = 0;
        super.stop();
    }
}