package com.fernleaf.meanderingmobs.server.block;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CarvedStrippedSpruceLogBlockEntity extends BlockEntity {
    private RuneType runeType = RuneType.DEERFOX;

    public CarvedStrippedSpruceLogBlockEntity(BlockPos pos, BlockState state) {
        super(MeanderingMobsBlockEntityRegistry.CARVED_STRIPPED_SPRUCE_LOG_ENTITY.get(), pos, state);
    }

    public void setRuneType(RuneType type) {
        this.runeType = type;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public RuneType getRuneType() {
        return this.runeType;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CarvedStrippedSpruceLogBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.getRuneType() == RuneType.DEERFOX) {
            if (level.getGameTime() % 100 == 0 && level.getMoonPhase() == 0 && level.isNight()) {
                BlockPos blockAbove = pos.above();

                if (level.getBlockState(blockAbove).is(Blocks.LODESTONE)) {
                    // Deerfox lunar beacon logic goes here
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RuneType", this.runeType.getId());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("RuneType")) {
            this.runeType = RuneType.byId(tag.getInt("RuneType"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}