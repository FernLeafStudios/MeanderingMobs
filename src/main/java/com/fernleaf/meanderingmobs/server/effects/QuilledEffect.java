package com.fernleaf.meanderingmobs.server.effects;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class QuilledEffect extends MobEffect {

    public QuilledEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A3B52);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Ticks every 200 ticks (Exactly 10-second intervals)
        return duration > 0 && duration % 200 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance effectInstance = entity.getEffect(MeanderingMobsItemRegistry.QUILLED);

        if (effectInstance != null) {
            float finalDamage = getFinalDamage(effectInstance);

            // Using standard generic/cactus/thorns variations
            // To fix death messaging completely, ensure your lang JSON maps "death.attack.quilled"
            DamageSource quilledDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.CACTUS)
            );

            entity.hurt(quilledDamage, finalDamage);
        }
        return true;
    }

    private static float getFinalDamage(MobEffectInstance effectInstance) {
        int durationTicks = effectInstance.getDuration();

        // Assuming standard application window (e.g., starting at 600 ticks / 30 seconds)
        // The longer it remains on the entity, the higher the interval damage index scales
        int ticksElapsed = Math.max(1, 600 - durationTicks);
        float intervalsElapsed = ticksElapsed / 200.0F;

        // Slower exponential progression:
        // Interval 1: 1.0F (0.5 heart)
        // Interval 2: ~1.5F
        // Interval 3: ~2.6F -> Max Capped at 5.0F (2.5 hearts)
        float calculatedDamage = 1.0F + (float) Math.pow(intervalsElapsed * 0.65, 2);
        return Math.min(5.0F, calculatedDamage);
    }
}