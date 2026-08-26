package com.fernleaf.meanderingmobs.server.entity.ai.tegu;

import com.fernleaf.meanderingmobs.server.entity.tameable.TeguEntity;
import com.fernleaf.meanderingmobs.server.entity.ai.AbstractBlockInteractionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class TeguStealFromChestGoal extends AbstractBlockInteractionGoal<TeguEntity> {

    public static final TagKey<Block> TEGU_STEALS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("meanderingmobs", "tegu_steals"));

    public TeguStealFromChestGoal(TeguEntity tegu) {
        super(tegu, 1.2D, 6.25D);
    }

    @Override
    protected boolean canInteract() {
        return this.entity.getMouthItem().isEmpty() && this.entity.getAiState() == 0;
    }

    @Override
    protected BlockPos findTargetBlock() {
        BlockPos teguPos = this.entity.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    mutable.set(teguPos.getX() + x, teguPos.getY() + y, teguPos.getZ() + z);
                    if (this.entity.level().getBlockState(mutable).is(TEGU_STEALS)) {
                        BlockEntity be = this.entity.level().getBlockEntity(mutable);
                        if (be instanceof Container container && !container.isEmpty()) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onReachedBlock(BlockPos pos) {
        BlockEntity blockEntity = this.entity.level().getBlockEntity(pos);

        if (blockEntity instanceof Container container) {
            List<Integer> validSlots = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    validSlots.add(i);
                }
            }

            if (!validSlots.isEmpty()) {
                int randomSlot = validSlots.get(this.entity.getRandom().nextInt(validSlots.size()));
                ItemStack stolenStack = container.removeItem(randomSlot, 1);

                if (!stolenStack.isEmpty()) {
                    this.entity.setMouthItem(stolenStack);
                    container.setChanged();
                }
            }
        }

        setCooldown(100 + this.entity.getRandom().nextInt(100));
        stop();
    }
}