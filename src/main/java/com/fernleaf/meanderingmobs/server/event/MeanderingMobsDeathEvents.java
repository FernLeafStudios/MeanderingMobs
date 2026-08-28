package com.fernleaf.meanderingmobs.server.event;

import com.evandev.redomesticate.data.ModWorldData;
import com.evandev.redomesticate.data.request.RespawnRequest;
import com.evandev.redomesticate.util.TameableUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.compat.redomesticate.RedomesticateCompat;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsDeathEvents {

    @SubscribeEvent
    public static void onDolphinDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Dolphin dolphin) || dolphin.level().isClientSide()) return;

        boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        if (!isTamed) return;

        Optional<UUID> ownerUUID = dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
        if (ownerUUID.isEmpty()) return;

        ServerLevel level = (ServerLevel) dolphin.level();
        Player owner = level.getServer().getPlayerList().getPlayer(ownerUUID.get());

        // 1. Send death notification
        if (owner != null) {
            Component deathMessage = event.getSource().getLocalizedDeathMessage(dolphin);
            owner.sendSystemMessage(deathMessage);
        }

        // 2. Queue bed respawn request
        if (RedomesticateCompat.isLoaded()) {
            BlockPos bedPos = TameableUtils.getPetBedPos(dolphin);

            if (bedPos != null) {
                ModWorldData data = ModWorldData.get(level);
                if (data != null) {
                    CompoundTag entityData = new CompoundTag();
                    dolphin.saveWithoutId(entityData);

                    String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(dolphin.getType()).toString();
                    entityData.putString("id", entityTypeId);

                    String nameTag = dolphin.hasCustomName() ? Objects.requireNonNull(dolphin.getCustomName()).getString() : dolphin.getName().getString();

                    // CRITICAL FIX: Match Redomesticate's ResourceKey string format
                    String dimension = level.dimension().toString();
                    long deathTime = level.getGameTime();

                    RespawnRequest request = new RespawnRequest(
                            entityTypeId,
                            dimension,
                            entityData,
                            bedPos,
                            deathTime,
                            nameTag
                    );

                    data.addRespawnRequest(request);
                }
            }
        }
    }
}