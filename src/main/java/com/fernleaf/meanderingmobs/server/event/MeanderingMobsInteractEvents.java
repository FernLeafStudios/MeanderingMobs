package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsInteractEvents {

    @SubscribeEvent
    public static void onDolphinInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (event.getTarget() instanceof Dolphin dolphin) {
            ItemStack stack = event.getItemStack();
            Player player = event.getEntity();
            Level level = event.getLevel();

            boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get()); //
            boolean isAnchovyCan = stack.is(MeanderingMobsItemRegistry.ANCHOVY_CAN.get());

            // --- TAMING LOGIC ---
            if (!isTamed && isAnchovyCan) {
                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    if (!player.getAbilities().instabuild) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND); //
                    }

                    if (level.random.nextInt(3) == 0) { //
                        dolphin.setData(MeanderingMobsAttachmentRegistry.IS_TAMED.get(), true); //
                        dolphin.setData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get(), Optional.of(player.getUUID())); //
                        dolphin.setData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get(), 2); // Default to Follow
                        dolphin.setPersistenceRequired();

                        serverLevel.sendParticles(ParticleTypes.HEART,
                                dolphin.getX(), dolphin.getY() + 0.5D, dolphin.getZ(),
                                7, 0.2D, 0.2D, 0.2D, 0.1D); //
                    } else {
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                dolphin.getX(), dolphin.getY() + 0.5D, dolphin.getZ(),
                                5, 0.1D, 0.1D, 0.1D, 0.05D); //
                    }
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide())); //
                event.setCanceled(true); //
                return;
            }

            // --- HEALING & COMMAND LOGIC ---
            if (isTamed) {
                Optional<UUID> ownerUUID = dolphin.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()); //
                if (ownerUUID.isPresent() && ownerUUID.get().equals(player.getUUID())) {

                    // HEALING LOGIC
                    if (isAnchovyCan && dolphin.getHealth() < dolphin.getMaxHealth()) {
                        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                            dolphin.heal(4.0F); // Heals 2 hearts per anchovy eaten

                            if (!player.getAbilities().instabuild) {
                                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                            }

                            serverLevel.sendParticles(ParticleTypes.SMOKE,
                                    dolphin.getX(), dolphin.getY() + 0.3D, dolphin.getZ(),
                                    3, 0.1D, 0.1D, 0.1D, 0.02D);
                        }
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        return;
                    }

                    // COMMAND CYCLING LOGIC
                    if (!level.isClientSide()) {
                        int currentState = dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get()); //
                        int nextState = (currentState + 1) % 3; // Cycles: 0 (Wander) -> 1 (Sit) -> 2 (Follow)
                        dolphin.setData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get(), nextState); //

                        String langKey = switch (nextState) {
                            case 1 -> "message.dolphin.sit";
                            case 2 -> "message.dolphin.follow";
                            default -> "message.dolphin.wander";
                        };
                        player.displayClientMessage(Component.translatable(langKey), true);
                    }
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide())); //
                    event.setCanceled(true); //
                }
            }
        }
    }
}