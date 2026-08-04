package com.fernleaf.meanderingmobs.server.events;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.entity.PorcupineEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class GameplayEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Intercept when a porcupine joins the world container
        if (event.getEntity() instanceof PorcupineEntity porcupine && !event.getLevel().isClientSide()) {

            // Read the permanently saved attachment string
            String savedColor = porcupine.getData(MeanderingMobsEntityRegistry.PORCUPINE_COLOR.get());

            if (!savedColor.equals("none")) {
                // If it already possesses a persistent color, sync it to the tracking system instantly
                porcupine.setSyncColor(savedColor);
            } else {
                // Baseline safety catch for unique edge cases (like structure processors)
                porcupine.determineAndSetVariant();
            }
        }
    }

    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        // Check if an Arrow struck a living entity
        if (event.getProjectile() instanceof Arrow arrow && event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity victim) {

                // Verify the arrow shot was a Quill Arrow item type
                if (arrow.getPickupItemStackOrigin().is(MeanderingMobsItemRegistry.QUILL_ARROW.get())) {

                    // FIXED: Pass the DeferredHolder directly. No .get(), no manual cast!
                    victim.addEffect(new MobEffectInstance(MeanderingMobsItemRegistry.QUILLED, 300, 0));
                }
            }
        }
    }
}