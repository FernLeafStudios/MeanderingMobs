package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.fernframe.umweltlite.goals.api.engine.EmotionAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.PersonalityAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.UmweltAPI;
import com.fernleaf.fernframe.umweltlite.goals.engine.EmotionalMap;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AukvultureSpawnHandler {

    private static final double FLOCK_CHECK_RADIUS = 16.0D;

    public static void initializeAukvulture(AukvultureEntity auk, ServerLevelAccessor level, MobSpawnType spawnType) {
        if (level.isClientSide()) return;

        // Query nearby active Aukvultures
        AABB searchBox = auk.getBoundingBox().inflate(FLOCK_CHECK_RADIUS);
        List<AukvultureEntity> nearbyAuks = level.getEntitiesOfClass(
                AukvultureEntity.class,
                searchBox,
                // Updated e.isTame() to e.isTamed()
                e -> e != auk && e.isAlive() && !e.isTamed()
        );

        boolean isFlockMember = !nearbyAuks.isEmpty();

        if (isFlockMember) {
            auk.setLoneWanderer(false);

            UmweltAPI.getEngine(auk).ifPresent(engine -> {
                PersonalityAPI.overrideTrait(engine, "empathy", 0.8f);
                EmotionalMap flockBond = new EmotionalMap(0.8f, 0.1f, 0.9f);

                for (AukvultureEntity mate : nearbyAuks) {
                    EmotionAPI.setSocialAttachment(engine, mate.getUUID(), flockBond);

                    UmweltAPI.getEngine(mate).ifPresent(mateEngine ->
                            EmotionAPI.setSocialAttachment(mateEngine, auk.getUUID(), flockBond)
                    );
                }
            });
        } else {
            // Explicitly force Lone Wanderer state
            auk.setLoneWanderer(true);

            UmweltAPI.getEngine(auk).ifPresent(engine -> {
                PersonalityAPI.overrideTrait(engine, "empathy", 0.3f);
                PersonalityAPI.overrideTrait(engine, "bravery", 0.7f);
            });
        }
    }
}