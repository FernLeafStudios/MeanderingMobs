package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.*;
import com.fernleaf.meanderingmobs.server.entity.ai.allay.WhispOrbitGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

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


        // --- Custom Vanilla Entity Spawn Overrides ---

        // Allay spawn rules
        event.register(
                EntityType.ALLAY,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MeanderingMobsSpawnEvents::checkAllayGrassSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
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

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            Entity entity = event.getEntity();

            // new Vanilla Behavior
            if (entity instanceof Allay allay) {
                allay.goalSelector.addGoal(3, new WhispOrbitGoal(allay, 2.5D, 0.05D));
            }

            // Generic handling for ANY mob defined in variant JSONs
            if (!event.loadedFromDisk()) {
                Holder<Biome> biome = event.getLevel().getBiome(entity.blockPosition());
                int selectedVariant = VariantSpawnManager.getVariantForSpawn(entity, biome);

                // If a valid non-default variant was picked, apply it directly via NBT
                if (selectedVariant != 0) {
                    CompoundTag tag = new CompoundTag();
                    entity.saveWithoutId(tag);

                    // Only apply if the entity doesn't already have a custom variant set
                    if (!tag.contains("Variant") || tag.getInt("Variant") == 0) {
                        tag.putInt("Variant", selectedVariant);

                        // Directly reload the tag to trigger readAdditionalSaveData
                        entity.load(tag);
                    }
                }
            }
        }
    }
}