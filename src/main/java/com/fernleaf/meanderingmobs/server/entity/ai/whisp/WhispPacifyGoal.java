package com.fernleaf.meanderingmobs.server.entity.ai.whisp;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEffectsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.WhispEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class WhispPacifyGoal extends Goal {

    private final WhispEntity whisp;
    private int cooldown = 0;

    public WhispPacifyGoal(WhispEntity whisp) {
        this.whisp = whisp;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.whisp.isTamed() && this.whisp.getAiState() != 1 && !this.whisp.isTagging();
    }

    @Override
    public void tick() {
        if (--this.cooldown <= 0) {
            this.cooldown = 20;

            AABB auraArea = this.whisp.getBoundingBox().inflate(8.0D);
            List<Mob> nearbyMobs = this.whisp.level().getEntitiesOfClass(
                    Mob.class,
                    auraArea,
                    entity -> entity.isAlive() && (
                            entity.getType().is(MeanderingMobsTagRegistry.EntityTypes.WHISP_INFLICT_PACIFISM)
                                    || entity.getTarget() != null // Also affects any active aggressive mob nearby
                    )
            );

            for (Mob mob : nearbyMobs) {
                // 600 ticks = 30 seconds duration
                mob.addEffect(new MobEffectInstance(
                        MeanderingMobsEffectsRegistry.WHIMSICAL, 600, 0, false, true
                ));
            }
        }
    }
}