package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.TeguEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsSpawnEvents {

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
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
    }
}