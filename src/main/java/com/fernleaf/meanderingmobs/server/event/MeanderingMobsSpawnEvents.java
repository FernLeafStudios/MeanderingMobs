package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.GuttertankPattern;
import com.fernleaf.meanderingmobs.server.block.RuffianPattern;
import com.fernleaf.meanderingmobs.server.data.VariantSpawnManager;
import com.fernleaf.meanderingmobs.server.entity.ai.allay.WhispOrbitGoal;
import com.fernleaf.meanderingmobs.server.entity.hostile.HollowRuffianEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.RallyCrystalEntity;
import com.fernleaf.meanderingmobs.server.entity.hostile.SoulHoundEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.TeguEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.WolverineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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
}