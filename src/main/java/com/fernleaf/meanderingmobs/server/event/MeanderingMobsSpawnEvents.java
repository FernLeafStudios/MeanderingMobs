package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.entity.*;
import com.fernleaf.meanderingmobs.server.entity.ai.allay.WhispOrbitGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.ServerLevelAccessor;
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
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Allay allay) {
            allay.goalSelector.addGoal(3, new WhispOrbitGoal(allay, 2.5D, 0.05D));
        }
    }
}