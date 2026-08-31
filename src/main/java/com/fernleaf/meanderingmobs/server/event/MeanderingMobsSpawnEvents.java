package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.compat.redomesticate.RedomesticateCompat;
import com.fernleaf.meanderingmobs.compat.redomesticate.goal.DolphinFindPetBedGoal;
import com.fernleaf.meanderingmobs.compat.redomesticate.goal.FeatherOnAStickGoal;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.ai.dolphin.DolphinOwnerHurtByTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.dolphin.DolphinOwnerHurtTargetGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.dolphin.DolphinTameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.hostile.HollowRuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.RallyCrystalEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulHoundEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsSpawnEvents {

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // --- Custom Entity Registrations ---
        event.register(
                MeanderingMobsEntityRegistry.WHISP.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.AUKVULTURE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                AukvultureEntity::checkAukvultureSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.PARROT_FISH.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.ANCHOVY.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.TEGU.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TeguEntity::checkTeguSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.PORCUPINE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PorcupineEntity::checkPorcupineSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.RALLY_CRYSTAL.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                RallyCrystalEntity::checkRallyCrystalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.SOUL_HOUND.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SoulHoundEntity::checkSoulHoundSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.OKAPI.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.WOLVERINE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                WolverineEntity::checkWolverineSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                MeanderingMobsEntityRegistry.HOLLOW_RUFFIAN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HollowRuffianEntity::checkHollowRuffianSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // --- Custom Vanilla Entity Spawn Overrides ---
        event.register(
                EntityType.ALLAY,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MeanderingMobsSpawnEvents::checkAllayGrassSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // --- ALLAY TO WHISP SWAP (STRUCTURES ONLY) ---
        if (event.getEntity() instanceof Allay allay && !event.getLevel().isClientSide()) {
            ServerLevel level = (ServerLevel) event.getLevel();
            BlockPos pos = allay.blockPosition();

            boolean isInStructure = level.structureManager().getStructureWithPieceAt(pos, s -> true).isValid();

            if (isInStructure && level.getRandom().nextFloat() < 0.3F) {
                WhispEntity whisp = MeanderingMobsEntityRegistry.WHISP.get().create(level);
                if (whisp != null) {
                    whisp.moveTo(allay.getX(), allay.getY(), allay.getZ(), allay.getYRot(), allay.getXRot());
                    whisp.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);

                    level.addFreshEntity(whisp);
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // --- DOLPHIN LOGIC ---
        if (event.getEntity() instanceof Dolphin dolphin && !event.getLevel().isClientSide()) {
            if (dolphin.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                Objects.requireNonNull(dolphin.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(3.0D);
            }

            dolphin.goalSelector.addGoal(1, new DolphinTameableStateGoal(dolphin));
            dolphin.goalSelector.addGoal(2, new DolphinOwnerHurtByTargetGoal(dolphin));
            dolphin.goalSelector.addGoal(3, new DolphinOwnerHurtTargetGoal(dolphin));
            dolphin.goalSelector.addGoal(4, new MeleeAttackGoal(dolphin, 1.2D, true));
            if (RedomesticateCompat.isLoaded()) {
                dolphin.goalSelector.addGoal(5, new DolphinFindPetBedGoal(dolphin));
                dolphin.goalSelector.addGoal(6, new FeatherOnAStickGoal(dolphin));
            }

            dolphin.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    dolphin,
                    LivingEntity.class,
                    10,
                    true,
                    false,
                    target -> {
                        boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
                        int state = dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());

                        return isTamed && state != 1 && target.getType().is(MeanderingMobsTagRegistry.EntityTypes.DOLPHIN_HATES);
                    }
            ));
        }
    }

    @SubscribeEvent
    public static void onDolphinRespawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Dolphin dolphin) || event.getLevel().isClientSide()) return;

        boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        if (!isTamed) return;

        // If the custom name was saved as default "Dolphin", remove it so floating text disappears
        if (dolphin.hasCustomName() && "Dolphin".equalsIgnoreCase(Objects.requireNonNull(dolphin.getCustomName()).getString())) {
            dolphin.setCustomName(null);
            dolphin.setCustomNameVisible(false);
        }

        // Send dawn chat notification
        long time = event.getLevel().dayTime() % 24000L;
        if (time >= 0 && time <= 5) {
            Optional<UUID> ownerUUID = dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get());
            if (ownerUUID.isPresent() && event.getLevel() instanceof ServerLevel level) {
                Player owner = level.getServer().getPlayerList().getPlayer(ownerUUID.get());
                if (owner != null) {
                    String name = dolphin.hasCustomName() ? Objects.requireNonNull(dolphin.getCustomName()).getString() : dolphin.getName().getString();
                    owner.displayClientMessage(Component.literal(name + " has respawned at its bed"), false);
                }
            }
        }
    }

    public static boolean checkAllayGrassSpawnRules(
            EntityType<?> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        BlockState stateBelow = level.getBlockState(pos.below());
        return (stateBelow.is(Blocks.GRASS_BLOCK) || stateBelow.is(Blocks.MOSS_BLOCK) || stateBelow.is(Blocks.DARK_OAK_LEAVES))
                && level.getBlockState(pos).isAir();
    }
}