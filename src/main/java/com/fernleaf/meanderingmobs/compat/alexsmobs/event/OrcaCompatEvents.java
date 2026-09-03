package com.fernleaf.meanderingmobs.compat.alexsmobs.event;

import com.fernleaf.meanderingmobs.compat.alexsmobs.goal.orca.*;
import com.fernleaf.meanderingmobs.compat.redomesticate.RedomesticateCompat;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

// DO NOT USE @EventBusSubscriber HERE!
public class OrcaCompatEvents {

    public static void register() {
        NeoForge.EVENT_BUS.register(OrcaCompatEvents.class);
    }

    @SubscribeEvent
    public static void onOrcaInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (event.getTarget() instanceof EntityOrca orca) {
            ItemStack stack = event.getItemStack();
            Player player = event.getEntity();
            Level level = orca.level();

            boolean isTamed = orca.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
            boolean isAnchovyCan = stack.is(MeanderingMobsItemRegistry.ANCHOVY_CAN.get());

            // --- TAMING LOGIC ---
            if (!isTamed && isAnchovyCan) {
                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    if (!player.getAbilities().instabuild) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    }

                    if (level.random.nextInt(3) == 0) {
                        orca.setTame(true, true);
                        orca.setOwnerUUID(player.getUUID());
                        orca.setPersistenceRequired();

                        orca.setData(MeanderingMobsAttachmentRegistry.IS_TAMED.get(), true);
                        orca.setData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get(), Optional.of(player.getUUID()));
                        orca.setData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get(), 2);

                        serverLevel.sendParticles(ParticleTypes.HEART,
                                orca.getX(), orca.getY() + 1.0D, orca.getZ(),
                                7, 0.4D, 0.4D, 0.4D, 0.1D);
                    } else {
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                orca.getX(), orca.getY() + 1.0D, orca.getZ(),
                                5, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
                return;
            }

            // --- HEALING & COMMAND LOGIC ---
            if (isTamed && orca.getOwnerUUID() != null && orca.getOwnerUUID().equals(player.getUUID())) {

                if (isAnchovyCan && orca.getHealth() < orca.getMaxHealth()) {
                    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        orca.heal(8.0F);
                        if (!player.getAbilities().instabuild) {
                            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        }
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                orca.getX(), orca.getY() + 1.0D, orca.getZ(),
                                3, 0.1D, 0.1D, 0.1D, 0.02D);
                    }
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                    event.setCanceled(true);
                    return;
                }

                if (stack.isEmpty() && !player.isShiftKeyDown()) {
                    if (!level.isClientSide()) {
                        player.startRiding(orca);
                    }
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                    event.setCanceled(true);
                    return;
                }

                if (!level.isClientSide()) {
                    int currentState = orca.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
                    int nextState = (currentState + 1) % 3;
                    orca.setData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get(), nextState);

                    String langKey = switch (nextState) {
                        case 1 -> "message.orca.sit";
                        case 2 -> "message.orca.follow";
                        default -> "message.orca.wander";
                    };
                    player.displayClientMessage(Component.translatable(langKey), true);
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onOrcaJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof EntityOrca orca && !event.getLevel().isClientSide()) {
            orca.moveControl = new OrcaMoveControl(orca);

            orca.goalSelector.addGoal(1, new OrcaTameableStateGoal(orca));
            orca.goalSelector.addGoal(2, new OrcaOwnerHurtByTargetGoal(orca));
            orca.goalSelector.addGoal(3, new OrcaOwnerHurtTargetGoal(orca));
            if (RedomesticateCompat.isLoaded()) {
                orca.goalSelector.addGoal(4, new OrcaFindPetBedGoal(orca));
            }
        }
    }
}