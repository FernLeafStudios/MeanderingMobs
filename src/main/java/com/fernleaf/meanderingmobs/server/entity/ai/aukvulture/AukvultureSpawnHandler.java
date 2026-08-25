package com.fernleaf.meanderingmobs.server.entity.ai.aukvulture;

import com.fernleaf.fernframe.umweltlite.goals.api.engine.EmotionAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.PersonalityAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.UmweltAPI;
import com.fernleaf.fernframe.umweltlite.goals.engine.EmotionalMap;
import com.fernleaf.meanderingmobs.server.entity.tameable.AukvultureEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AukvultureSpawnHandler {

    private static final double FLOCK_CHECK_RADIUS = 32.0D;

    public static void evaluateFlockStatus(AukvultureEntity auk) {
        if (auk.level().isClientSide()) return;

        AABB searchBox = auk.getBoundingBox().inflate(FLOCK_CHECK_RADIUS);
        List<AukvultureEntity> nearbyAuks = auk.level().getEntitiesOfClass(
                AukvultureEntity.class,
                searchBox,
                e -> e != auk && e.isAlive() && !e.isTamed()
        );

        if (!nearbyAuks.isEmpty()) {
            auk.setLoneWanderer(false);

            UmweltAPI.getEngine(auk).ifPresent(engine -> {
                PersonalityAPI.overrideTrait(engine, "empathy", 0.8f);
                EmotionalMap flockBond = new EmotionalMap(0.8f, 0.1f, 0.9f);

                for (AukvultureEntity mate : nearbyAuks) {
                    if (mate.isLoneWanderer()) {
                        mate.setLoneWanderer(false);
                        UmweltAPI.getEngine(mate).ifPresent(mEngine ->
                                PersonalityAPI.overrideTrait(mEngine, "empathy", 0.8f)
                        );
                    }

                    EmotionAPI.setSocialAttachment(engine, mate.getUUID(), flockBond);
                    UmweltAPI.getEngine(mate).ifPresent(mateEngine ->
                            EmotionAPI.setSocialAttachment(mateEngine, auk.getUUID(), flockBond)
                    );
                }
            });
        } else {
            auk.setLoneWanderer(true);

            UmweltAPI.getEngine(auk).ifPresent(engine -> {
                PersonalityAPI.overrideTrait(engine, "empathy", 0.3f);
                PersonalityAPI.overrideTrait(engine, "bravery", 0.7f);
            });
        }
    }
}