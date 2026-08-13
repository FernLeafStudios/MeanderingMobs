package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.fernframe.umweltlite.goals.api.engine.EmotionAPI;
import com.fernleaf.fernframe.umweltlite.goals.api.engine.UmweltAPI;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsTagRegistry;
import com.fernleaf.meanderingmobs.server.entity.AukvultureEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class AukvultureTameEvents {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof AukvultureEntity auk)) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        // 1. Taming Logic
        if (!auk.isTame() && stack.is(MeanderingMobsTagRegistry.Items.AUKVULTURE_TAME_FOOD)) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                if (auk.isLoneWanderer()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    if (serverLevel.random.nextInt(3) == 0) {
                        auk.tame(player);
                        UmweltAPI.getEngine(auk).ifPresent(engine -> {
                            EmotionAPI.setValence(engine, 0.9f);
                            EmotionAPI.setArousal(engine, 0.2f);
                        });

                        serverLevel.sendParticles(ParticleTypes.HEART, auk.getX(), auk.getY() + 0.5, auk.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                    } else {
                        serverLevel.sendParticles(ParticleTypes.SMOKE, auk.getX(), auk.getY() + 0.5, auk.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                    }
                } else {
                    // Non-lone-wanderers refuse taming
                    serverLevel.sendParticles(ParticleTypes.SMOKE, auk.getX(), auk.getY() + 0.5, auk.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // 2. State-Switching Logic (Wander, Sit, Follow)
        if (auk.isTame() && auk.isOwnedBy(player) && player.isCrouching()) {
            int nextState = (auk.getAiState() + 1) % 3;
            auk.setAiState(nextState);

            String translationKey = switch (nextState) {
                case 1 -> "message.meanderingmobs.aukvulture.state.follow";
                case 2 -> "message.meanderingmobs.aukvulture.state.wander";
                default -> "message.meanderingmobs.aukvulture.state.sit";
            };

            player.displayClientMessage(Component.translatable(translationKey), true);

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}