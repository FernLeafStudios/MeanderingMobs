package com.fernleaf.meanderingmobs.server.block.entity;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockEntityRegistry;
import com.fernleaf.meanderingmobs.server.menu.QueueboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueueboxBlockEntity extends BlockEntity implements MenuProvider {
    private boolean wasPowered = false;

    private final SimpleContainer inventory = new SimpleContainer(27) {
        @Override
        public void setChanged() {
            super.setChanged();
            QueueboxBlockEntity.this.setChanged();

            if (QueueboxBlockEntity.this.isPlaying) {
                ItemStack currentDisc = getItem(QueueboxBlockEntity.this.currentSlot);
                if (currentDisc.isEmpty() || !currentDisc.has(DataComponents.JUKEBOX_PLAYABLE)) {
                    QueueboxBlockEntity.this.stopPlayback();
                    if (QueueboxBlockEntity.this.level != null && QueueboxBlockEntity.this.level.hasNeighborSignal(QueueboxBlockEntity.this.worldPosition)) {
                        QueueboxBlockEntity.this.playNextTrack();
                    }
                }
            }
        }
    };

    private int currentSlot = 0;
    private boolean isShuffle = false;
    private int trackTicksRemaining = 0;
    public boolean isPlaying = false;

    public QueueboxBlockEntity(BlockPos pos, BlockState state) {
        super(MeanderingMobsBlockEntityRegistry.QUEUEBOX.get(), pos, state);
    }

    public Container getInventory() {
        return this.inventory;
    }

    public boolean isShuffle() {
        return this.isShuffle;
    }

    public void toggleShuffle() {
        this.isShuffle = !this.isShuffle;
        this.setChanged();
    }

    public void onRedstoneUpdate(boolean hasPower) {
        if (this.level == null) return;

        if (hasPower && !this.wasPowered) {
            if (this.isPlaying) {
                this.stopPlayback();
                this.playNextTrack();
            } else {
                this.playNextTrack();
            }
        }

        else if (!hasPower && this.wasPowered) {
            if (!this.level.hasNeighborSignal(this.worldPosition)) {
                this.stopPlayback();
            }
        }

        this.wasPowered = hasPower;
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide()) return;

        boolean currentlyPowered = this.level.hasNeighborSignal(this.worldPosition);

        if (this.isPlaying) {
            if (!currentlyPowered) {
                stopPlayback();
                return;
            }

            this.trackTicksRemaining--;
            if (this.trackTicksRemaining <= 0) {
                playNextTrack();
            }
        }
    }

    public void playNextTrack() {
        if (this.level == null) return;

        List<Integer> filledSlots = getFilledSlots();
        if (filledSlots.isEmpty()) {
            stopPlayback();
            return;
        }

        if (this.isShuffle) {
            this.currentSlot = filledSlots.get(this.level.random.nextInt(filledSlots.size()));
        } else {
            int nextIndex = -1;
            for (int slot : filledSlots) {
                if (slot > this.currentSlot) {
                    nextIndex = slot;
                    break;
                }
            }
            this.currentSlot = (nextIndex != -1) ? nextIndex : filledSlots.getFirst();
        }

        ItemStack disc = this.inventory.getItem(this.currentSlot);
        JukeboxPlayable component = disc.get(DataComponents.JUKEBOX_PLAYABLE);

        if (component != null) {
            Optional<Holder<JukeboxSong>> songHolder = component.song().unwrap(this.level.registryAccess());

            if (songHolder.isPresent()) {
                Holder<JukeboxSong> holder = songHolder.get();
                this.trackTicksRemaining = holder.value().lengthInTicks();

                var songRegistry = this.level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG);

                // Get the raw integer ID corresponding to the registry key holder
                holder.unwrapKey().ifPresent(key -> {
                    int songId = songRegistry.getId(songRegistry.get(key));
                    // Event 1010 plays jukebox audio client-side
                    this.level.levelEvent(null, 1010, this.worldPosition, songId);
                });

                this.isPlaying = true;
                this.setChanged();
                return;
            }
        }
        stopPlayback();
    }

    public void stopPlayback() {
        if (this.level != null && this.isPlaying) {
            this.level.levelEvent(1011, this.worldPosition, 0);
        }
        this.isPlaying = false;
        this.trackTicksRemaining = 0;
        this.setChanged();
    }

    private List<Integer> getFilledSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (!this.inventory.getItem(i).isEmpty()) {
                slots.add(i);
            }
        }
        return slots;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Queuebox");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new QueueboxMenu(containerId, playerInventory, this.inventory);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.inventory.createTag(registries));
        tag.putInt("CurrentSlot", this.currentSlot);
        tag.putBoolean("Shuffle", this.isShuffle);
        tag.putInt("TrackTicks", this.trackTicksRemaining);
        tag.putBoolean("IsPlaying", this.isPlaying);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.inventory.fromTag(tag.getList("Inventory", 10), registries);
        this.currentSlot = tag.getInt("CurrentSlot");
        this.isShuffle = tag.getBoolean("Shuffle");
        this.trackTicksRemaining = tag.getInt("TrackTicks");
        this.isPlaying = tag.getBoolean("IsPlaying");
    }
}